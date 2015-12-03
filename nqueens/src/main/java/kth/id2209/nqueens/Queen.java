/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
		addBehaviour(new QueenBehaviour(queenId, 0));
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

	private class QueenBehaviour extends Behaviour {
		int step = 0;
		int ownColumn;
		int currentRow;
		
		private QueenBehaviour(int ownColumn, int currentRow) {
			this.ownColumn = ownColumn;
			this.currentRow = currentRow;
		}

		@Override
		public void action() {
			switch (step) {
			case 0:
				log.info("Step 0. Start with first queen");
				if(queenId == 1) {
					log.info("Fisrt queen, initiate move");
					
				} else {
					log.info("Not first queen, wait for command");
					
				}
				
				
				step = 1;
				break;
			case 1:
				log.info("Step 1. XXXXX");
				step = 2;
				break;
			}
		}

		@Override
		public boolean done() {
			return step == 2;
		}
	}
}
