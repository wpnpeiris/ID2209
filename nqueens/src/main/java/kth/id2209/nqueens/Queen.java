/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.AID;
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
	private String queenName;
	private int numPeerQueens;
	
	protected void setup(){
		log.info("Initialize Queen Agent");
		Object[] args = getArguments();
		if(args != null && args.length > 0) {
			queenId = (Integer)args[0];
			queenName = "Queen" + queenId;
		} else {
			log.severe("Queen initiated without an id");
			return;
		}
		
		register();
		addBehaviour(new QueensStateBehaviour());
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
		AID checkQueen;
		int currentRow;
		
		public static final int TRANS_WAIT_TO_WAIT = 0;
		public static final int TRANS_WAIT_TO_MOVE = 1;
		public static final int TRANS_WAIT_TO_CHECK = 2;
		public static final int TRANS_MOVE_TO_VERIFY = 3;
		public static final int TRANS_CHECK_TO_WAIT = 4;
		public static final int TRANS_VERIFY_TO_WAIT = 5;
		
		private final String STATE_INIT = "INIT";
		private final String STATE_WAIT = "WAIT";
		private final String STATE_MOVE = "MOVE";
		private final String STATE_CHECK = "CHECK";
		private final String STATE_VERIFY = "VERIFY";
		
		private QueensStateBehaviour() {
//			this.column = column;
			
			registerFirstState(new QueenInit(), STATE_INIT);
			registerState(new QueenWait(), STATE_WAIT);
			registerState(new QueenMove(), STATE_MOVE);
			registerState(new QueenCheck(), STATE_CHECK);
			registerState(new QueenVerify(), STATE_VERIFY);
			
			registerDefaultTransition(STATE_INIT, STATE_WAIT);
			registerTransition(STATE_WAIT, STATE_WAIT, TRANS_WAIT_TO_WAIT);
			registerTransition(STATE_WAIT, STATE_MOVE, TRANS_WAIT_TO_MOVE);
			registerTransition(STATE_WAIT, STATE_CHECK, TRANS_WAIT_TO_CHECK);
			registerTransition(STATE_MOVE, STATE_VERIFY, TRANS_MOVE_TO_VERIFY);
			registerTransition(STATE_CHECK, STATE_WAIT, TRANS_CHECK_TO_WAIT);
			registerTransition(STATE_VERIFY, STATE_WAIT, TRANS_VERIFY_TO_WAIT);
		}
		
		private class QueenVerify extends Behaviour {
			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			private int repliesCount = 0;
			private boolean allSaidYes = true;
			
			@Override
			public void action() {
				log.info(queenName + " is waiting for verification");
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					log.info(queenName + " Receive verifictaion " + msg.getContent() + " from " + msg.getSender().getLocalName());
					repliesCount++;
					
					if(msg.getContent().equals(CheckVerification.NO)){
						allSaidYes = false;
					}
					
					if(heardFromAll() && allSaidYes) {
						log.info(queenName + " heard YES from all peer queens ");
						
					}
					
				} else {
					block();
				}
			}

			@Override
			public boolean done() {
				if(heardFromAll()) {
					if(allSaidYes) {
						updateChessBoardAgent();
					}
					
					return true;
				} else {
					return false;
				}
			}
			
			public int onEnd() {
				if (allSaidYes)
					return TRANS_VERIFY_TO_WAIT;
				else
					return TRANS_VERIFY_TO_WAIT;
			}
			
			private boolean heardFromAll() {
				return (repliesCount == numPeerQueens);
			}
			
			private void updateChessBoardAgent() {
				log.info("XXXX Update ChessBoard Agent about current possition");
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-chessboard");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result != null && result.length > 0) {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(result[0].getName());
						msg.setContent(currentRow + "," + queenId);
						send(msg);
					}
				} catch (FIPAException e) {
					log.severe(e.getMessage());
				}
			}
			
		}
		
		private class QueenCheck extends OneShotBehaviour {

			@Override
			public void action() {
				log.info(queenName + " in Move Check for " + checkQueen.getLocalName());
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(checkQueen);
				
				if(valid()) {
					log.info(checkQueen.getLocalName() + " is valid for " + queenName);
					msg.setContent(CheckVerification.YES);
				} else {
					log.info(checkQueen.getLocalName() + " is not valid for " + queenName );
					msg.setContent(CheckVerification.NO);
				}
				
				log.info(queenName + " send verification for " + checkQueen.getLocalName());
				myAgent.send(msg);
			}

			public int onEnd() {
				return TRANS_CHECK_TO_WAIT;
			}
			
			private boolean valid() {
				return isPriorQueen();
			}
			
			private boolean isPriorQueen() {
				int checkQueenId = Integer.valueOf(checkQueen.getLocalName().substring(5));
				return (checkQueenId < queenId);
			}
			
			
		}
		
		private class QueenMove extends OneShotBehaviour {

			@Override
			public void action() {
				log.info(queenName + " in Move State");
				currentRow = currentRow + 1;
				log.info(queenName + " moved to row " + currentRow);
				checkValidity();
			}
			
			public int onEnd() {
				return TRANS_MOVE_TO_VERIFY;
			}
			
			private void checkValidity() {
				log.info(queenName + " check validity of current position other Queens " );
				DFAgentDescription template = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-queen");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if(result != null && result.length > 0) {
						numPeerQueens = result.length - 1;
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						for (int i = 0; i < result.length; ++i) {
							if(!result[i].getName().getLocalName().equals(queenName)) {
								msg.addReceiver(result[i].getName());
							}
						}
						
						msg.setContent(ChessCommand.CHECK);
						myAgent.send(msg);
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
				log.info(queenName + " in Wait State");
				ACLMessage msg = myAgent.receive(mt);
				
				if(msg != null) {
					log.info(queenName + " Receive Inform " + msg.getContent());
					if(msg.getContent().equals(ChessCommand.MOVE)) {
						nextTransistion = TRANS_WAIT_TO_MOVE;
					} else if(msg.getContent().equals(ChessCommand.CHECK)) {
						checkQueen = msg.getSender();
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
