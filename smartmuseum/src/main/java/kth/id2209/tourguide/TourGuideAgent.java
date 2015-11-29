/**
 * 
 */
package kth.id2209.tourguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author pradeeppeiris
 *
 */
public class TourGuideAgent extends Agent {
	
	private static final Logger log = Logger.getLogger(TourGuideAgent.class.getName());
	
	
	private List<AID> curatorAgents = new ArrayList<AID>();
	
	private Map<String, List<String>> artifactGenre = new HashMap<String, List<String>>();
	
	protected void setup() {
		log.info("Initailize TourGuide Agent");
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-tourguide"); 
		sd.setName(getLocalName()+"-Publish-tourguide"); 
		dfd.addServices(sd);
		
		try {
			log.info("Register Tourguide Agent");
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			log.severe("Error in TourGuide Agent register: " + e.getMessage());
		}
		
		log.info("Add TickerBehavior which search for Curator Agents");
		addBehaviour(new TickerBehaviour(this, 10000) {

			@Override
			protected void onTick() {
				log.info("Search for CuratorAgent");
				DFAgentDescription template = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-curator");
				template.addServices(sd);
				
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					curatorAgents.clear();
					artifactGenre.clear();
					for (int i = 0; i < result.length; ++i) {

						log.info("CuratorAgent " + result[i].getName() + " is added to Curator Catalog");
						curatorAgents.add(result[i].getName());
						
						log.info("CuratorAgent " + result[i].getName() + " is called to get its artifacts");
						myAgent.addBehaviour(new OneShotBehaviour() {
							@Override
							public void action() {
								ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
								for(AID curator : curatorAgents) {
									cfp.addReceiver(curator);
								}

								cfp.setConversationId("curator-artifact");
								cfp.setReplyWith("cfp" + System.currentTimeMillis());
								myAgent.send(cfp);
							}
						});
					}
				} catch (FIPAException e) {
					log.severe("Error in Curator Agent search: " + e.getMessage());
				}	
			}
			
		});
		
		log.info("Add CuratorManager which handle Curator reply");
		addBehaviour(new CuratorManager());
		
		log.info("Add RecommendationManager which handle Profiler requests");
		addBehaviour(new RecommendationManager());
		
	}
	
	private class CuratorManager extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String reply = msg.getContent();
				log.info("Get CuratorReply " + msg.getContent());
				updateCatalogGenre(reply);
			} else {
				block();
			}
			
		}
		
		private void updateCatalogGenre(String artifacts) {
			log.info("Parse CuratorReply " + artifacts);
			try {
				JSONParser jsonParser = new JSONParser();
				JSONArray jsonArray = (JSONArray)jsonParser.parse(artifacts);
				for(Object obj :  jsonArray) {
					JSONObject jsonLineItem = (JSONObject) obj;
					String genre = (String) jsonLineItem.get("genre");
					if(artifactGenre.containsKey(genre)) {
						artifactGenre.get(genre).add(jsonLineItem.toJSONString());
					} else {
						List<String> items = new ArrayList<String>();
						items.add(jsonLineItem.toJSONString());
						artifactGenre.put(genre, items);
					}
				}
			} catch (ParseException e) {
				log.severe("Error in pasing CuratorReply " + e.getMessage());
			}
		}
		
	}
	
	private class RecommendationManager extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String profileIntrest = msg.getContent();
				log.info("Request received: " + profileIntrest);
				String recommendation = getRecommendation(profileIntrest);
				log.info("Tour recommendation: " + recommendation);
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(recommendation);
				myAgent.send(reply);
			} else {
				block();
			}
		}
		
		private String getRecommendation(String profileIntrest) {
			String[] intresets = profileIntrest.split(",");
			JSONArray list = new JSONArray();
			for(String s : intresets) {
				if(s != null && s.length() > 0) {
					if(artifactGenre.containsKey(s)) {
						JSONParser jsonParser = new JSONParser();
						List<String> artifactListStr = artifactGenre.get(s);
						for(String artifactStr :  artifactListStr) {
							try {
								JSONObject artifact = (JSONObject)jsonParser.parse(artifactStr);
								list.add(artifact);
							} catch (ParseException e) {
								log.severe("Error in artfactStr parsing " + e.getMessage());
							}
							
						}
					}
				}
			}

			return list.toJSONString();
		}
		
	}

	
	
}
