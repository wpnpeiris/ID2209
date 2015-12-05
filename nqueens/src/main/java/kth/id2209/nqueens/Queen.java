/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
public class Queen extends Agent {
	private static final Logger log = Logger.getLogger(Queen.class.getName());
	private int queenId;
	
	protected void setup(){
		log.info("Initialize Queen Agent");
		Object[] args = getArguments();
		if(args != null && args.length > 0) {
			queenId = (Integer)args[0];
		} else {
			log.severe("Queen initiated without an id");
			return;
		}
		
		register();
		addBehaviour(new QueensStateBehaviour(queenId));
	}
	
	private void register() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-queen"); 
		sd.setName("Queen" + queenId); 
		dfd.addServices(sd);
		
		try {
			log.info("Register Queen Agent " + queenId);
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			log.severe("Error in Queen Agent register: " + e.getMessage());
		}
	}

	private class QueensStateBehaviour extends FSMBehaviour {
		int column;
		int currentRow;
		
		public static final int TRANS_WAIT_TO_WAIT = 0;
		public static final int TRANS_WAIT_TO_MOVE = 1;
		public static final int TRANS_WAIT_TO_CHECK = 2;
		
		private final String STATE_INIT = "INIT";
		private final String STATE_WAIT = "WAIT";
		private final String STATE_MOVE = "MOVE";
		private final String STATE_CHECK = "CHECK";
		
		private QueensStateBehaviour(int column) {
			this.column = column;
			
			registerFirstState(new QueenInit(), STATE_INIT);
			registerState(new QueenWait(), STATE_WAIT);
			registerState(new QueenMove(), STATE_MOVE);
			registerState(new QueenCheck(), STATE_CHECK);
			
			registerDefaultTransition(STATE_INIT, STATE_WAIT);
			registerTransition(STATE_WAIT, STATE_WAIT, TRANS_WAIT_TO_WAIT);
			registerTransition(STATE_WAIT, STATE_MOVE, TRANS_WAIT_TO_MOVE);
			registerTransition(STATE_WAIT, STATE_CHECK, TRANS_WAIT_TO_CHECK);
		}
		
		private class QueenCheck extends Behaviour {

			@Override
			public void action() {
				log.info("Queen" + queenId + " in Move Check");
				block();
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
		}
		
		private class QueenMove extends Behaviour {

			@Override
			public void action() {
				log.info("Queen" + queenId + " in Move State");
				currentRow = currentRow + 1;
				log.info("Queen" + queenId + " moved to row " + currentRow);
//				checkValidityWithOthers();
				
				block();
				
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
			private void checkValidityWithOthers() {
				log.info("Queen" + queenId + " check validity of current position other Queens " );
				DFAgentDescription template = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-queen");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if(result != null && result.length > 0) {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						for (int i = 0; i < result.length; ++i) {
							msg.addReceiver(result[i].getName());
						}
						msg.setContent(currentRow + "," + column);
						msg.setProtocol(ChessCommand.CHECK);
						send(msg);
					}
				} catch (FIPAException e) {
					log.severe(e.getMessage());
				}
			}
			
		}
		
		private class QueenWait extends Behaviour {

			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			
			private Integer nextTransistion;
			
			
			@Override
			public void action() {
				log.info("Queen" + queenId + " in Wait State");
				ACLMessage msg = myAgent.receive(mt);
				
				if(msg != null) {
					log.info("Queen" + queenId + " Receive Inform " + msg.getContent());
					if(msg.getContent().equals(ChessCommand.MOVE)) {
						nextTransistion = TRANS_WAIT_TO_MOVE;
//						move();
					} else if(msg.getContent().equals(ChessCommand.CHECK)) {
						nextTransistion = TRANS_WAIT_TO_CHECK;
					}
				} 
				else {
					block();
				}
			}

			@Override
			public boolean done() {
				if(nextTransistion != null)
					return true;
				else
					return false;
			}
			
			public int onEnd() {
				if (nextTransistion != null)
					return nextTransistion;
				else
					return TRANS_WAIT_TO_WAIT;
			}
			
//			private void move() {
//				log.info("Move Queen" + queenId + " from row: " + currentRow + 
//													" to row: " + (++currentRow));
//				if(validMove()) {
//					updateChessBoardAgent();
//					informNextQueen();
//				}
//			}
			
//			private boolean validMove() {
////				checkValidityWithOthers();
//				return true;
//			}
			
			
			
//			private void informNextQueen() {
//				String nextQueen = "Queen" + (queenId + 1);
//				log.info("Inform " + nextQueen + " to start moving");
//				DFAgentDescription template = new DFAgentDescription(); 
//				ServiceDescription sd = new ServiceDescription();
//				sd.setType("Publish-queen");
//				template.addServices(sd);
//
//				try {
//					DFAgentDescription[] result = DFService.search(myAgent, template);
//					if(result != null && result.length > 0) {
//						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//						for (int i = 0; i < result.length; ++i) {
//							if(result[i].getName().getLocalName().equals(nextQueen)) {
//								msg.addReceiver(result[i].getName());
//							}
//						}
//						
//						msg.setProtocol(ChessCommand.MOVE);
//						send(msg);
//					} else {
//						log.severe(nextQueen + " is not registred or not available");
//					}
//				} catch (FIPAException e) {
//					log.severe(e.getMessage());
//				}
//			}
			
//			private void updateChessBoardAgent() {
//				log.info("Update ChessBoard Agent about current possition");
//				DFAgentDescription template = new DFAgentDescription(); 
//				ServiceDescription sd = new ServiceDescription();
//				sd.setType("Publish-chessboard");
//				template.addServices(sd);
//				try {
//					DFAgentDescription[] result = DFService.search(myAgent, template);
//					if(result != null && result.length > 0) {
//						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//						msg.addReceiver(result[0].getName());
//						msg.setContent(currentRow + "," + column);
//						send(msg);
//					}
//				} catch (FIPAException e) {
//					log.severe(e.getMessage());
//				}
//			}
			
		}
		
		
		private class QueenInit extends OneShotBehaviour {

			@Override
			public void action() {
				log.info("Queen" + queenId  + " in Init State");
				currentRow = 0;
			} 
			
		}
	}

}
