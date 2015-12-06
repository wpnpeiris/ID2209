/**
 * 
 */
package kth.id2209.artist;

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author pradeeppeiris
 *
 */
public class ArtistGuiImpl implements ArtistGui {

	private static final int APP_WINDOW_HEIGHT = 350;
	private static final int APP_WINDOW_WIDTH = 500;
	
	private ArtistAgent agent;

	@Override
	public void setAgent(ArtistAgent agent) {
		this.agent = agent;
	}

	@Override
	public void show() {
		launch();
	}
	
	private void launch() {
		Frame mainFrame = new Frame("Artist Auctioner-" + agent.getLocalName());
		mainFrame.setSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
		mainFrame.setLayout(new GridLayout(0, 1, 15, 15));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		Panel idPanel = new Panel();
		idPanel.setLayout(new GridLayout(1,2));
		Label idLbl = new Label("  Item Id");
		idLbl.setSize(150,50);
		idPanel.add(idLbl);
		final TextField id = new TextField();
		id.setSize(350,50);
		idPanel.add(id);
		mainFrame.add(idPanel);
		
		Panel namePanel = new Panel();
		namePanel.setLayout(new GridLayout(1,2));
		Label nameLbl = new Label("  Item Name");
		nameLbl.setSize(150,50);
		namePanel.add(nameLbl);
		final TextField name = new TextField();
		name.setSize(350,50);
		namePanel.add(name);
		mainFrame.add(namePanel);
		
		Panel startPricePanel = new Panel();
		startPricePanel.setLayout(new GridLayout(1,2));
		Label startPriceLbl = new Label("  Start Price");
		startPricePanel.add(startPriceLbl);
		final TextField startPrice = new TextField();
		startPricePanel.add(startPrice);
		mainFrame.add(startPricePanel);
		
		Panel reservePricePanel = new Panel();
		reservePricePanel.setLayout(new GridLayout(1,2));
		Label reservePriceLbl = new Label("  Reserve Price");
		reservePricePanel.add(reservePriceLbl);
		final TextField reservePrice = new TextField();
		reservePricePanel.add(reservePrice);
		mainFrame.add(reservePricePanel);
		
		Panel genrePanel = new Panel();
		genrePanel.setLayout(new GridLayout(1,2));
		Label genreLbl = new Label("  Genre");
		genrePanel.add(genreLbl);
		final List genre = new List(1, false);
		genre.add("Portrait Paintings");
		genre.add("Landscape Paintings");
		genre.add("Photography");
		genre.add("Fantacy");
		genrePanel.add(genre);
		mainFrame.add(genrePanel);
		
		Panel btnPanel = new Panel();
		btnPanel.setLayout(new GridLayout(1,2));
		Button button = new Button("Start Auction");
		btnPanel.add(button);
		mainFrame.add(btnPanel);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agent.startAuction(id.getText(), name.getText(), genre.getSelectedItem(), Double.valueOf(startPrice.getText()), 
										Double.valueOf(reservePrice.getText()));
			}
			
		});
		
		Panel clonePanel1 = new Panel();
		clonePanel1.setLayout(new GridLayout(1,2));
		Label containerLbt = new Label("  Container Name");
		clonePanel1.add(containerLbt);
		final TextField containerName = new TextField();
		clonePanel1.add(containerName);
		mainFrame.add(clonePanel1);
		
		Panel clonePanel2 = new Panel();
		clonePanel2.setLayout(new GridLayout(1,2));
		Label cloneLbl = new Label("  Clone Name");
		clonePanel2.add(cloneLbl);
		final TextField cloneName = new TextField();
		clonePanel2.add(cloneName);
		mainFrame.add(clonePanel2);
		
		Panel clonePanel3 = new Panel();
		clonePanel3.setLayout(new GridLayout(1,2));
		Button cloneBtn = new Button("Clone");
		clonePanel3.add(cloneBtn);
		Button moveBtn = new Button("Move");
		clonePanel3.add(moveBtn);
		
		mainFrame.add(clonePanel3);
		cloneBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.cloneArtist(containerName.getText(), cloneName.getText());
			}
			
		});
		
		moveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.moveArtist(containerName.getText());
			}
			
		});
		
		mainFrame.setVisible(true);
	}

}
