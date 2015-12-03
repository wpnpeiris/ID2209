/**
 * 
 */
package kth.id2209.nqueens;

import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pradeeppeiris
 *
 */
public class ChessBoardGuiImpl implements ChessBoardGui {

	private static final int APP_WINDOW_HEIGHT = 350;
	private static final int APP_WINDOW_WIDTH = 500;

	private ChessBoardAgent agent;
	
	private Map<String, Button> labelMap = new HashMap<String, Button>();
	
	@Override
	public void setAgent(ChessBoardAgent agent) {
		this.agent = agent;

	}

	@Override
	public void show(int n) {
		Frame mainFrame = new Frame("N Queens");
		
		mainFrame.setLayout(new GridLayout(0, n));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		Font font = new Font("Arial", Font.PLAIN, 30);
		for(int i = 1; i <= n; i++) {
			for(int j = 1; j <= n; j++) {
				Button btn = new Button("Q");
				btn.setFont(font);
				btn.setEnabled(false);
				labelMap.put(getPositionKey(i,j), btn);
				mainFrame.add(btn);
			}
		}
		mainFrame.setSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
		mainFrame.setVisible(true);
	}

	@Override
	public void update(int row, int col, boolean on) {
		Button btn = labelMap.get(getPositionKey(row, col));
		if(on) {
			btn.setLabel("Q");
		} else {
			btn.setLabel("");
		}
		btn.repaint();
	}

	private String getPositionKey(int row, int  col) {
		StringBuilder sb = new StringBuilder();
		sb.append(row).append(",").append(col);
		return sb.toString();
	}
}
