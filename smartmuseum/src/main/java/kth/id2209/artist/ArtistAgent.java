/**
 * 
 */
package kth.id2209.artist;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author pradeeppeiris
 *
 */
public class ArtistAgent extends Agent {

	private static final Logger log = Logger.getLogger(ArtistAgent.class.getName());

	private ArtistGui gui;
	
	private List<AID> curators = new ArrayList<AID>();

	protected void setup() {
		log.info("Initialize Artist Agent");
		gui = new ArtistGuiImpl();
		gui.setAgent(this);
		gui.show();
		
		registerCurators();
	}

	protected void takeDown() {

	}

	private void registerCurators() {
		log.info("Register Curators for Auction");
		DFAgentDescription template = new DFAgentDescription(); 
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-curator");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			curators.clear();
			for (int i = 0; i < result.length; ++i) {
				log.info("Add curator: " + result[i].getName());
				curators.add(result[i].getName());
			}
		} catch (FIPAException e) {
			log.severe("Error in Curator Agent search: " + e.getMessage());
		}
	}
	
	public void startAuction(String id, String name, String type, double startPrice, double reservePrice) {
		log.info("Start Auction, Start Price: " + startPrice + " Reserved Price: " + reservePrice );
		DutchAuction aution = new DutchAuction(id, name, type, startPrice, reservePrice, curators);
		addBehaviour(aution);
	}

	private class DutchAuction extends FSMBehaviour {
	
		AuctionItem auctionItem;
		double startPrice;
		double reservePrice;
		List<AID> participents;
		
		private final String STATE_START = "START";
		private final String STATE_OFFER = "OFFER";
		private final String STATE_LISTEN = "LISTEN";
		private final String STATE_CLOSED = "CLOSED";
		
		public static final int TRANS_OFFER_TO_LISTEN = 0;
		public static final int TRANS_OFFER_TO_CLOSE = 1;
		public static final int TRANS_LISTEN_TO_OFFER = 2;
		public static final int TRANS_LISTEN_TO_CLOSE = 3;
		
		private DutchAuction(String id, String name, String type, double startPrice, double reservePrice, List<AID> participents) {
			this.startPrice = startPrice;
			this.reservePrice = reservePrice; 
			this.participents = participents;
			this.auctionItem = new AuctionItem(id, name, type, startPrice);
			
			registerFirstState(new AuctionStart(auctionItem), STATE_START);
			registerState(new PriceOffer(auctionItem), STATE_OFFER);
			registerState(new BidListner(), STATE_LISTEN);
			registerLastState(new CloseAuction(), STATE_CLOSED);

			registerDefaultTransition(STATE_START, STATE_OFFER);
			registerTransition(STATE_OFFER, STATE_LISTEN, TRANS_OFFER_TO_LISTEN);
			registerTransition(STATE_OFFER, STATE_CLOSED, TRANS_OFFER_TO_CLOSE);
			registerTransition(STATE_LISTEN, STATE_OFFER, TRANS_LISTEN_TO_OFFER);
			registerTransition(STATE_LISTEN, STATE_CLOSED, TRANS_LISTEN_TO_CLOSE);
			
			scheduleFirst();
		}

		public double getStartPrice() {
			return startPrice;
		}

		public double getReservePrice() {
			return reservePrice;
		}

		private class AuctionItem {
			double REDUCTION_RATE = 100;
			String id;
			String name;
			String type;
			double currentPrice;
			
			AuctionItem(String id, String name, String type, double currentPrice) {
				this.id = id;
				this.name = name;
				this.type = type;
				this.currentPrice = currentPrice;
			}
			
			public double updateCurrentPrice() {
				return this.currentPrice -= REDUCTION_RATE;
			}
			
			public String toJsonString() {
				JSONObject json = new JSONObject();
				json.put("id",id);
				json.put("name",name);
				json.put("type",type);
				json.put("price",currentPrice);
				
				return json.toJSONString();
			}
		}
		
		private class AuctionStart extends OneShotBehaviour {
			AuctionItem auctionItem;
			
			private AuctionStart(AuctionItem auctionItem) {
				this.auctionItem = auctionItem;
			}
			
			@Override
			public void action() {
				log.info("Acution Start and inform partcipent");
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				for(AID participent : participents) {
					msg.addReceiver(participent);
				}
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
				msg.setContent(auctionItem.toJsonString());
				myAgent.send(msg);
			}
			
		}
		
		private class PriceOffer extends OneShotBehaviour {
			
			AuctionItem auctionItem;
			
			private boolean reservedPriceReached = false;
			
			private PriceOffer(AuctionItem auctionItem) {
				this.auctionItem = auctionItem;
			}
			
			@Override
			public void action() {
				log.info("Propse Price ");
				double currentPrice = auctionItem.updateCurrentPrice();
				if(currentPrice > reservePrice) {
					log.info("Current Price is not reached to reserver price, send propose: " + currentPrice);
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					for(AID participent : participents) {
						msg.addReceiver(participent);
					}
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
					msg.setContent("Current Price: " + auctionItem.currentPrice);
					myAgent.send(msg);
					
				} else {
					log.info("Current Price is reached to reserver price: " + currentPrice);
					reservedPriceReached = true;
				}
				
				
				
			}
			
			@Override
			public int onEnd() {
				if (reservedPriceReached)
					return TRANS_OFFER_TO_CLOSE;
				else
					return TRANS_OFFER_TO_LISTEN;
			}
			
		}
		
		private class BidListner extends Behaviour {
			List<ACLMessage> bids = new ArrayList<ACLMessage>();
			boolean bidAccepted = false;
			
			@Override
			public void action() {
				log.info("Listen to bid ");
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION));
				if (msg != null) {
					log.info("Got response " + msg.getContent());
					if(msg.getContent().equals("Accept")) {
						bidAccepted = true;
					}
					bids.add(msg);
				} else {
					block();
				}
			}

			@Override
			public boolean done() {
				boolean heardFromAll = false;
				if(participents.size() == bids.size()){
					log.info("Received response from all bidders ");
					heardFromAll = true;
				}
				
				return heardFromAll;
			}
			
			@Override
			public int onEnd() {
				int nextTransition = TRANS_LISTEN_TO_OFFER;
				if(bidAccepted) {
					boolean winnerNotUpdated = true;
					for(ACLMessage response : bids) {
						if(winnerNotUpdated && response.getContent().equals("Accept")) {
							log.info("Got first accepted bidder and response as Winner  ");
							ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
							msg.addReceiver(response.getSender());
							msg.setContent("Winner");
							myAgent.send(msg);
							winnerNotUpdated = false;	
						} else {
							ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
							msg.addReceiver(response.getSender());
							msg.setContent("Auction Closed");	
							myAgent.send(msg);
						}
					}
					
					nextTransition = TRANS_LISTEN_TO_CLOSE;
				} 
				
				bids.clear();
				return nextTransition;
			}
			
		}
		
		private class CloseAuction extends OneShotBehaviour {
			@Override
			public void action() {
				log.info("Close Auction ");
				
			}
		}
	}
	
	
}
