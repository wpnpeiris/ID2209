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
import jade.util.leap.Properties;

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
		sd.setName(queenName); 
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
		CheckValue checkValue;
		Properties checkProperties;
		int currentRow;
		
		public static final int TRANS_INIT_TO_WAIT = 0;
		public static final int TRANS_WAIT_TO_MOVE = 1;
		public static final int TRANS_WAIT_TO_CHECK = 2;
		public static final int TRANS_MOVE_TO_VERIFY = 3;
		public static final int TRANS_CHECK_TO_WAIT = 4;
		public static final int TRANS_VERIFY_TO_WAIT = 5;
		public static final int TRANS_VERIFY_TO_MOVE = 6;
		
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
			
//			registerDefaultTransition(STATE_INIT, STATE_WAIT);
			registerTransition(STATE_INIT, STATE_WAIT, TRANS_INIT_TO_WAIT);
			registerTransition(STATE_WAIT, STATE_MOVE, TRANS_WAIT_TO_MOVE);
			registerTransition(STATE_WAIT, STATE_CHECK, TRANS_WAIT_TO_CHECK);
			registerTransition(STATE_MOVE, STATE_VERIFY, TRANS_MOVE_TO_VERIFY);
			registerTransition(STATE_CHECK, STATE_WAIT, TRANS_CHECK_TO_WAIT);
			registerTransition(STATE_VERIFY, STATE_WAIT, TRANS_VERIFY_TO_WAIT);
			registerTransition(STATE_VERIFY, STATE_MOVE, TRANS_VERIFY_TO_MOVE);
		}
		
		private class QueenVerify extends Behaviour {
			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
			private int repliesCount = 0;
			private boolean allSaidYes = true;
			
			@Override
			public void action() {
//				log.info(queenName + "  is waiting for verification");
				ACLMessage msg = receive(mt);
				if(msg != null) {
					log.info("VVVVVVVVVVVVV " + queenName + " Receive verifictaion " + msg.getContent() + " from " + msg.getSender().getLocalName());
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
				return heardFromAll();
			}
			
			public int onEnd() {
				if (allSaidYes) {
					log.info(">>>>>>>>>> All said yes for " + queenName);
					updateChessBoardAgent();
//					informNextQueenToMove();
					
					return TRANS_VERIFY_TO_WAIT;
				} else {
					log.info("<<<<<<<<<<<<<<< All said no for " + queenName);
					return TRANS_VERIFY_TO_MOVE;
				}
			}
			
			private boolean heardFromAll() {
				return (repliesCount == numPeerQueens);
			}
			
			private void updateChessBoardAgent() {
				log.info("Update ChessBoard Agent about current possition");
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
			
			private void informNextQueenToMove() {
				String nextQueen = "Queen" + (queenId + 1);
				log.info("Inform " + nextQueen + " to start moving");
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-queen");
				template.addServices(sd);

				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result != null && result.length > 0) {
						ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
						for (int i = 0; i < result.length; ++i) {
							if (result[i].getName().getLocalName().equals(nextQueen)) {
								log.info("@@@@@@@@@@@@@@@@@@@@ " + result[i].getName().getLocalName());
								msg.addReceiver(result[i].getName());
							}
						}

						msg.setContent(ChessCommand.MOVE);
//						msg.setContent("TEST");
						log.info("");
						log.info("");
						log.info("");
						log.info("");
						
//						XXXXX
						if(nextQueen.equals("Queen2"))
							send(msg);
						
					} else {
						log.severe(nextQueen + " is not registred or not available");
					}
				} catch (FIPAException e) {
					log.severe(e.getMessage());
				}
			}
			
		}
		
		
		
		private class QueenMove extends OneShotBehaviour {
			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			
			@Override
			public void action() {
				ACLMessage tmp = receive(mt);
				log.info("LLLLLLLLLLLLL " + tmp);
				
				
				
				log.info("MMMMMMMMMMMMMMMMMMMM " + queenName + " in Move State");
				currentRow = currentRow + 1;
//				log.info(queenName + " moved to row " + currentRow);
				callValidity();
			}
			
			
			public int onEnd() {
				return TRANS_MOVE_TO_VERIFY;
			}
			
			private void callValidity() {
//				log.info(queenName + " check validity of current position other Queens " );
				DFAgentDescription template = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-queen");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if(result != null && result.length > 0) {
						numPeerQueens = result.length - 1;
						ACLMessage msg = new ACLMessage(ACLMessage.CFP);
						for (int i = 0; i < result.length; ++i) {
							if(!result[i].getName().getLocalName().equals(queenName)) {
								msg.addReceiver(result[i].getName());
							}
						}
						
						msg.setContent(createCheckMessage());
						send(msg);
					}
				} catch (FIPAException e) {
//					log.severe(e.getMessage());
				}
			}
			
			private String createCheckMessage() {
				StringBuilder sb = new StringBuilder();
				sb.append(ChessCommand.CHECK).append(",").append(currentRow).append(",").append(queenId);
				return sb.toString();
			}


			
		}
		
		private class CheckValue {
			int row;
			int col;
		
			private CheckValue(int row, int col) {
				this.row = row;
				this.col = col;
			}
		}
		
		private class QueenWait extends Behaviour {

			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			
			private Integer nextTransistion;
			
			
			@Override
			public void action() {
				log.info("------------------ " + queenName + " in Wait State");
				ACLMessage msg = receive(mt);
				log.info("PPPPPPPPPPPPPP " + queenName);
				if(msg != null) {
					log.info("WWWWWWWWWWWWWWWWWW " + queenName + " Receive Inform " + msg.getContent());
					if(msg.getContent().equals(ChessCommand.MOVE)) {
//						nextTransistion = TRANS_WAIT_TO_MOVE;
						addBehaviour(new QueenMove());
					} else if(msg.getContent().contains(ChessCommand.CHECK)) {
						checkQueen = msg.getSender();
						checkValue = parseCheckMessage(msg.getContent());
//						nextTransistion = TRANS_WAIT_TO_CHECK;
						addBehaviour(new QueenCheck());
						block();
					}
				} else {
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
//				if (nextTransistion != null)
//					return nextTransistion;
//				else
//					return TRANS_WAIT_TO_WAIT;
				return nextTransistion;
			}
			
			private CheckValue parseCheckMessage(String msg) {
				String[] msgValues = msg.split(",");
				return new CheckValue(Integer.valueOf(msgValues[1]), Integer.valueOf(msgValues[2]));
			}
		}
		
		private class QueenCheck extends OneShotBehaviour {
			
//			private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			
			@Override
			public void action() {
//				ACLMessage tmp = receive(mt);
//				if(tmp != null) {
//					log.info("KKKKKKKKKK " + tmp.getSender().getLocalName());
//				}
				
				log.info("CCCCCCCCCCCCCCCCCCC " + queenName + " in Move Check for " + checkQueen.getLocalName());
				ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
				msg.addReceiver(checkQueen);
				
				if(validCheck()) {
//					log.info(checkQueen.getLocalName() + " is valid for " + queenName);
					msg.setContent(CheckVerification.YES);
				} else {
//					log.info(checkQueen.getLocalName() + " is not valid for " + queenName );
					msg.setContent(CheckVerification.NO);
				}
				
//				log.info(queenName + " send verification for " + checkQueen.getLocalName());
				send(msg);
			}
			
//			public int onEnd() {
//				log.info("======================= " + queenName);
//				return TRANS_CHECK_TO_WAIT;
//			}
			
			private boolean validCheck() {
//				boolean valid;
//				if(isPriorQueen()) {
//					valid = true;
//				} else {
//					valid = (checkValue.row == 3);
//				}
				log.info("XXXXXXXXXXXXXXXXX " + queenName + " : " + isPriorQueen());
				return isPriorQueen();
			}
			
			private boolean isPriorQueen() {
				int checkQueenId = Integer.valueOf(checkQueen.getLocalName().substring(5));
				return (checkQueenId < queenId);
			}
		}
		
		private class QueenInit extends OneShotBehaviour {

			@Override
			public void action() {
//				log.info("Queen" + queenId  + " in Init State");
				currentRow = 0;
			} 
			
			public int onEnd() {
				return TRANS_INIT_TO_WAIT;
			}
			
		}
	}

}
