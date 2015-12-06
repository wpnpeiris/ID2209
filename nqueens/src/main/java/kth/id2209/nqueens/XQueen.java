/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
public class XQueen extends Agent {
	private static final Logger log = Logger.getLogger(XQueen.class.getName());
	private int queenId;
	private int currentRow;
	private String queenName;
	private int numQueens;
	
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
	
	protected void setup(){
		log.info("Initialize Queen Agent");
		Object[] args = getArguments();
		if(args != null && args.length > 0) {
			queenId = (Integer)args[0];
			queenName = "Queen" + queenId;
			numQueens = (Integer)args[1];
		} else {
			log.severe("Queen initiated without an id");
			return;
		}
		
		register();
		addBehaviour(new QueensStateBehaviour());
	}
	
	
	private class QueensStateBehaviour extends Behaviour {
		private final int INIT = 0;
		private final int WAIT = 1;
		private final int CHECK_REPLY = 2;
		
		private MessageTemplate mt;
		private int state = 0;
		int currentRow;
		
		int repliesCount = 0;
		boolean allSaidYes = true;
		@Override
		public void action() {
//			log.info("[" +queenName + "] QueensStateBehaviour state: " + state);
			switch (state) {
			case INIT:
//				log.info("[" +queenName + "] QueensStateBehaviour process state init" + state);
				myAgent.addBehaviour(new QueenInit());
				mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				state = WAIT;
				break;
			
			case WAIT:
				log.info("[" +queenName + "] QueensStateBehaviour process state wait");
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					log.info("[" +queenName + "] Receive Inform " + msg.getContent());
					if(msg.getContent().equals(ChessCommand.MOVE)) {
						
						moveNextAndPropose();

						mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
						state = CHECK_REPLY;
						
					} else if(msg.getContent().contains(ChessCommand.CHECK)) {
						log.info("[" + queenName + "] Receive Inform " + msg.getContent());
						CheckValue checkValue = parseCheckMessage(msg.getContent());
						log.info("[" + queenName + "] " + validCheck(checkValue));
						ACLMessage reply = msg.createReply();
						if(validCheck(checkValue)) {
							reply.setContent(CheckVerification.YES);
						} else {
							reply.setContent(CheckVerification.NO);
						}
						reply.setPerformative(ACLMessage.INFORM);
						myAgent.send(reply);
					}
					
				} else {
					block();
				}
				
				break;
			
			case CHECK_REPLY:
				log.info("[" +queenName + "] QueensStateBehaviour process state check reply");
				ACLMessage checkReply = myAgent.receive(mt);
				if(checkReply != null) {
					log.info("[" +queenName + "]  Receive Check Reply " + checkReply.getContent() + " from [" + checkReply.getSender().getLocalName() + "]");
					repliesCount++;
					if(checkReply.getContent().equals(CheckVerification.NO)){
						allSaidYes = false;
					}
					
					boolean heardFromAll = ((numQueens - 1) == repliesCount);
					if(heardFromAll) {
						if(allSaidYes) {
							log.info("[" +queenName + "] heard YES from all peer queens ");
							updateChessBoardAgent();
							
							mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
							state = WAIT;
							
							allSaidYes = true;
							repliesCount = 0;
						} else {
							log.info("[" +queenName + "] heard NO from one peer queens ");
							allSaidYes = true;
							repliesCount = 0;
							moveNextAndPropose();
						}
					}
				} else {
					block();
				}
				
				break;
				
			}
			
		}
		
		private void moveNextAndPropose() {
			currentRow = currentRow + 1;
			
			if(currentRow > numQueens) {
				log.info("XXXXXXXXXXXXXXXXXXXXXX ");
				
			} else {

				DFAgentDescription template = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Publish-queen");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if(result != null && result.length > 0) {
						ACLMessage inform = new ACLMessage(ACLMessage.PROPOSE);
						for (int i = 0; i < result.length; ++i) {
							if(!result[i].getName().getLocalName().equals(queenName)) {
								log.info("[" +queenName + "] " + result[i].getName().getLocalName());
								inform.addReceiver(result[i].getName());
							}
						}
						
						inform.setContent(createCheckMessage());
						myAgent.send(inform);
						
					}
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void updateChessBoardAgent() {
			log.info("[" +queenName + "] Update ChessBoard Agent about current possition");
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
		
		private String createCheckMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(ChessCommand.CHECK).append(",").append(currentRow).append(",").append(queenId);
			return sb.toString();
		}
		
		private CheckValue parseCheckMessage(String msg) {
			String[] msgValues = msg.split(",");
			return new CheckValue(Integer.valueOf(msgValues[1]), Integer.valueOf(msgValues[2]));
		}
		
		private boolean validCheck(CheckValue checkValue) {
			boolean valid;
			if(isPriorQueen(checkValue.col)) {
				valid = true;
			} else {
				valid = notInSameRow(checkValue.row) && notInSameDiagonal(checkValue);
			}
			
			return valid;
		}
		
		private boolean notInSameRow(int checkRow) {
			return (checkRow != currentRow);
		}
		
		private boolean notInSameDiagonal(CheckValue checkValue) {
			return (Math.abs(checkValue.row - currentRow) != Math.abs(checkValue.col - queenId));
		}
		
		private boolean isPriorQueen(int checkCol) {
			return (checkCol < queenId);
		}
		
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	private class QueenInit extends OneShotBehaviour {

		@Override
		public void action() {
			log.info("[" +queenName + "] QueenInit init , set current row to " + currentRow);
			currentRow = 0;
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

}
