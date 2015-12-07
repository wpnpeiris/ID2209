/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * @author pradeeppeiris
 *
 */
public class ChessBoardAgent extends Agent {
	
	private static final Logger log = Logger.getLogger(ChessBoardAgent.class.getName());
	
	private ChessBoardGui gui;
	
	private static final int DEFAULT_SIZE = 4;
	
	private final int SLEEP_TIME = 200;
	
	private final String START_QUEEN = "Queen1";
	
	protected void setup() {
		log.info("Initialize ChessBoard Agent");
		Object[] args = getArguments();
		int nQueens;
		if (args != null && args.length > 0) {
			nQueens = Integer.valueOf((String)args[0]);
		} else {
			nQueens = DEFAULT_SIZE;
		}
		
		register();
		displayUI(nQueens);
		createQueenAgents(nQueens);
		
		addBehaviour(new ChessBoardBehavior());
		start();
	}
	
	private void register() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-chessboard"); 
		sd.setName("Chessboard");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			log.severe(e.getMessage());
		}
	}
	
	private void displayUI(int nQueens) {
		log.info("Display ChessBoard GUI with dimension " + nQueens);
		gui = new ChessBoardGuiImpl();
		gui.setAgent(this);
		gui.show(nQueens);
	}
	
	private void createQueenAgents(int nQueens) {
		log.info("Create number of " + nQueens + " Queen Agents");
		ContainerController containerController = getContainerController();
		try {
			for (int i = 1; i <= nQueens; i++) {
				containerController.createNewAgent("Queen" + i, XQueen.class.getName(), new Object[]{i, nQueens}).start();
			}
		} catch (StaleProxyException e) {
			log.severe(e.getMessage());
		}
	}
	
	private void start() {
		log.info("Start game by informing " + START_QUEEN + " to start moving");
		addBehaviour(new WakerBehaviour(this, SLEEP_TIME) {
			protected void onWake() {
				proposeMove(START_QUEEN);
			}
		});
	}
	
	private void proposeMove(String queenName) {
		DFAgentDescription template = new DFAgentDescription(); 
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Publish-queen");
		template.addServices(sd);
		
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			if(result != null && result.length > 0) {
				ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
				for (int i = 0; i < result.length; ++i) {
					if(result[i].getName().getLocalName().equals(queenName)) {
						msg.addReceiver(result[i].getName());
					}
				}
			
				msg.setContent(ChessCommand.MOVE);
				send(msg);
				
			} else {
				log.severe("Queen is not registred or not available");
			}
			
		} catch (FIPAException e) {
			log.severe(e.getMessage());
		}
	}
	
	private class ChessBoardBehavior extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		
		public void action() {
			final ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				log.info("Receive ChessBoard update request: " + msg.getContent());
				String[] parsedMsg = msg.getContent().split(",");
				boolean update = Boolean.valueOf(parsedMsg[0]);
				int row = Integer.valueOf(parsedMsg[1]);
				int col = Integer.valueOf(parsedMsg[2]);
				gui.update(row, col, update);
				
				if(update) {
					final String nextQueenToStart = "Queen" + (col + 1);
					addBehaviour(new WakerBehaviour(myAgent, SLEEP_TIME) {
						protected void onWake() {
							proposeMove(nextQueenToStart);
						}
					});
				}
				
				
			}
		}
		
	}
	
	
	
}
