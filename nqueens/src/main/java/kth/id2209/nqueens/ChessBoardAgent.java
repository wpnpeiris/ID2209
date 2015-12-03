/**
 * 
 */
package kth.id2209.nqueens;

import java.util.logging.Logger;

import jade.core.Agent;
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
	
	protected void setup() {
		log.info("Initialize Artist Agent");
		Object[] args = getArguments();
		int nQueens;
		if (args != null && args.length > 0) {
			nQueens = Integer.valueOf((String)args[0]);
		} else {
			nQueens = DEFAULT_SIZE;
		}
		
		gui = new ChessBoardGuiImpl();
		gui.setAgent(this);
		gui.show(nQueens);
		
		log.info("Create number of " + nQueens + " Queen Agents");
		ContainerController containerController = getContainerController();
		try {
			for (int i = 1; i <= nQueens; i++) {
				containerController.createNewAgent("Queen" + i, Queen.class.getName(), new Object[]{i}).start();
			}
		} catch (StaleProxyException e) {
			log.severe(e.getMessage());
		}
//		gui.update(2, 1, false);
	}
}
