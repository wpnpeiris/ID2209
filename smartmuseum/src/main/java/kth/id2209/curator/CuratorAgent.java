/**
 * 
 */
package kth.id2209.curator;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Properties;

/**
 * @author pradeeppeiris
 *
 */
public class CuratorAgent extends Agent {
	private final String DEFAULT_CONTAINER = "default";
	
	private static final Logger log = Logger.getLogger(CuratorAgent.class.getName());
	
	private Map<String, Artifact> artifacts = new HashMap<String, Artifact> ();
	
	private transient CuratorGui gui;
	
	private String containerName;
	
	private void initGui() {
		log.info("Initailize Curator Agent's GUI");
		gui = new CuratorGuiImpl();
		gui.setAgent(this);
		gui.show();
	}
	
	private void setContainerName() {
		log.info("Set container name");
		Properties prop = getBootProperties();
		String contName = prop.getProperty("container-name");
		if(contName != null) {
			containerName = contName;
		} else {
			containerName = DEFAULT_CONTAINER;
		}
	}
	
	private void registerCurator() {
		log.info("Register Curator Agent in DF");
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-curator"); 
		sd.setName(getLocalName()+"-Publish-curator"); 
		sd.setOwnership(containerName);
		dfd.addServices(sd);
		
		try {
			log.info("Register Profiler Agent");
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			log.severe("Error in Profile Agent register: " + e.getMessage());
		}
	}
	
	private void addBehaviours() {
		log.info("Add Behaviours of Curator Agent");
		addBehaviour(new ArtifactRequest());
		addBehaviour(new AuctionItemListener());
		addBehaviour(new AuctionPriceListener());
		addBehaviour(new AuctionResultListener());
	}
	
	protected void setup() {
		log.info("Initailize Curator Agent");
		initGui();  
		setContainerName();
		registerCurator();
		addBehaviours();
	}
	
	protected void beforeClone() {
		log.info(getLocalName() + " is cloning");
	}

	protected void afterClone() {
		log.info(getLocalName() + " is cloned");
		afterMove();
	}
		
	protected void afterMove() {
		log.info(getLocalName() + " is arrived to this location.");
		initGui();
		registerCurator();
		addBehaviours();
	}
	
	public void updateArtifacts(String id, String name, String creator, 
									String dateCreate, String placeCreate, String genre) {
		addBehaviour(new ArtifactManager(id, name, creator, dateCreate, placeCreate, genre, getLocalName()));
	}
	
	public void cloneCurator(String containerName, String curatorName) {
		log.info("Clone Curator with name: " + curatorName + " in container: " + containerName);
		ContainerID destination = new ContainerID();
		destination.setName(containerName);
		doClone(destination, curatorName);
	}
	
	private class AuctionResultListener extends CyclicBehaviour {

		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				log.info("Receive Auction result: " + msg.getContent());
				gui.updateAutionStatus(msg.getContent());
			}
		}
		
	}
	
	private class AuctionPriceListener extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		
		@Override
		public void action() {
			final ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				log.info("Receive Auction price: " + msg.getContent());
				gui.updateAutionPrice(msg.getContent(), new OfferCallback() {

					@Override
					public void accept() {
						log.info("Accept Offer " + msg.getContent());
						ACLMessage reply = msg.createReply();
						reply.setContent("Accept");
						myAgent.send(reply);
						
					}

					@Override
					public void reject() {
						log.info("Reject Offer" +  msg.getContent());
						ACLMessage reply = msg.createReply();
						reply.setContent("Reject");
						myAgent.send(reply);
						
					}
					
				});
			}
		}
		
	}
	
	private class AuctionItemListener extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				log.info("Receive Action Rrequest of item: " + msg.getContent());
				gui.updateAutionItem(getItemDetail(msg.getContent()));
			}
		}
		
		private String getItemDetail(String jsonString) {
			StringBuilder sb = new StringBuilder();
			
			try {
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject)jsonParser.parse(jsonString);
				
				sb.append("Aution Item: ")
					.append(jsonObject.get("id")).append(" ")
					.append(jsonObject.get("name")).append(" (")
					.append(jsonObject.get("type")).append(")");
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			return sb.toString();
		}
		
	}
	
	private class ArtifactRequest extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				log.info("Receive Artifact Rrequest");
				
				ACLMessage reply = msg.createReply();
				reply.setContent(createArtifactsList());
				myAgent.send(reply);
			} else {
				block();
			}
		}
		
		private String createArtifactsList() {
			JSONArray list = new JSONArray();
			for(Artifact a : artifacts.values()) {
				list.add(a.getAttributes());
			}
			return list.toJSONString();
		}
		
	}
	
	private class ArtifactManager extends OneShotBehaviour {

		private Artifact artifact;
		
		private ArtifactManager(String id, String name, String creator, 
				String dateCreate, String placeCreate, String genre, String holder) {
			this.artifact = new Artifact(id, name, creator, dateCreate, placeCreate, genre, holder);
		}
		
		@Override
		public void action() {
			log.info("Add new Artifact");
			artifacts.put(artifact.id, artifact);
		}
		
	}
	
	private class Artifact {
		private String id;
		private JSONObject attributes;
		
		private Artifact(String id, String name, String creator, 
				String dateCreate, String placeCreate, String genre, String holder) {
			this.id = id;
			
			this.attributes = new JSONObject();
			this.attributes.put("id",id);
			this.attributes.put("name",name);
			this.attributes.put("creator",creator);
			this.attributes.put("dateCreate",dateCreate);
			this.attributes.put("placeCreate",placeCreate);
			this.attributes.put("genre",genre);
			this.attributes.put("holder",holder);
		}

		public JSONObject getAttributes() {
			return this.attributes;
		}
		
	}
	
}
