/**
 * 
 */
package kth.id2209.curator;

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
public class CuratorGuiImpl implements CuratorGui {
	private static final int APP_WINDOW_HEIGHT = 650;
	private static final int APP_WINDOW_WIDTH = 500;
	
	private CuratorAgent agent;
	
	private Frame mainFrame;
	
	private Label autionItem;
	
	private Label autionPrice;
	
	private Label autionStatus;
	
	private OfferCallback offerCallback;
	
	@Override
	public void setAgent(CuratorAgent agent) {
		this.agent = agent;
	}

	@Override
	public void show() {
		launch();
	}

	@Override
	public void addArtifact() {
		// TODO Auto-generated method stub
		
	}
	
	private void launch() {
		mainFrame = new Frame("Curator-" + agent.getLocalName());
		mainFrame.setSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
		mainFrame.setLayout(new GridLayout(0, 1, 15, 15));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		Panel idPanel = new Panel();
		idPanel.setLayout(new GridLayout(1,2));
		Label idLbl = new Label("  Id");
		idLbl.setSize(150,50);
		idPanel.add(idLbl);
		final TextField id = new TextField();
		id.setSize(350,50);
		idPanel.add(id);
		mainFrame.add(idPanel);
	      
		Panel namePanel = new Panel();
		namePanel.setLayout(new GridLayout(1,2));
		Label nameLbl = new Label("  Name");
		namePanel.add(nameLbl);
		final TextField name = new TextField();
		namePanel.add(name);
		mainFrame.add(namePanel);
		
		Panel creatorPanel = new Panel();
		creatorPanel.setLayout(new GridLayout(1,2));
		Label creatorLbl = new Label("  Creator");
		creatorPanel.add(creatorLbl);
		final TextField creator = new TextField();
		creatorPanel.add(creator);
		mainFrame.add(creatorPanel);
		
		Panel creDatePanel = new Panel();
		creDatePanel.setLayout(new GridLayout(1,2));
		Label credateLbl = new Label("  Date of Create");
		creDatePanel.add(credateLbl);
		final TextField dateCreate = new TextField();
		creDatePanel.add(dateCreate);
		mainFrame.add(creDatePanel);
		
		Panel creplacePanel = new Panel();
		creplacePanel.setLayout(new GridLayout(1,2));
		Label creplaceLbl = new Label("  Place of Create");
		creplacePanel.add(creplaceLbl);
		final TextField placeCreate = new TextField();
		creplacePanel.add(placeCreate);
		mainFrame.add(creplacePanel);
		
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
		Button button = new Button("Add Artifact");
		btnPanel.add(button);
		mainFrame.add(btnPanel);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.updateArtifacts(id.getText(), name.getText(), creator.getText(), 
						dateCreate.getText(), placeCreate.getText(), genre.getSelectedItem());
			}
			
		});
		
		Panel auctionPanel1 = new Panel();
		auctionPanel1.setLayout(new GridLayout(1,1));
		autionItem = new Label();
		auctionPanel1.add(autionItem);
		mainFrame.add(auctionPanel1);
		
		Panel auctionPanel2 = new Panel();
		auctionPanel2.setLayout(new GridLayout(1,2));
		autionPrice = new Label();
		auctionPanel2.add(autionPrice);
		
		autionStatus = new Label();
		auctionPanel2.add(autionStatus);
		mainFrame.add(auctionPanel2);
		
		Panel auctionPanel3 = new Panel();
		auctionPanel3.setLayout(new GridLayout(1,2));
		Button accept = new Button("Accept Offer");
		auctionPanel3.add(accept);
		mainFrame.add(auctionPanel3);
		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				offerCallback.accept();
			}
			
		});
		
		Button reject = new Button("Reject Offer");
		auctionPanel3.add(reject);
		mainFrame.add(auctionPanel3);
		reject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				offerCallback.reject();
			}
			
		});
		
		
		
		mainFrame.setVisible(true);
	}

	public void updateAutionItem(String itemDetail) {
		autionItem.setText(itemDetail);
		autionItem.repaint();
		
		autionStatus.setText("");
		autionStatus.repaint();
	}
	
	public void updateAutionPrice(String price, OfferCallback callback) {
		offerCallback = callback;
		autionPrice.setText(price);
		autionPrice.repaint();
	}
	
	public void updateAutionStatus(String status) {
		autionStatus.setText(status);
		autionStatus.repaint();
		
	}
	
}
