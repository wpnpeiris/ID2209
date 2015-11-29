/**
 * 
 */
package kth.id2209.profiler;

import java.awt.Button;
import java.awt.Color;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author pradeeppeiris
 *
 */
public class ProfilerGuiImpl implements ProfilerGui {
	private static final int APP_WINDOW_HEIGHT = 380;
	private static final int APP_WINDOW_WIDTH = 600;
	
	private ProfilerAgent agent;
	
	private Frame mainFrame = new Frame();
	private Panel recommendedTour = new Panel();
	
	@Override
	public void setAgent(ProfilerAgent agent) {
		this.agent = agent;
	}

	@Override
	public void show() {
		launch();
	}

	@Override
	public void updateTourSuggestions(String content) {
		JSONParser jsonParser = new JSONParser();
		try {
			JSONArray jsonArray = (JSONArray)jsonParser.parse(content);
			recommendedTour.removeAll();
			for(Object obj :  jsonArray) {
				JSONObject item = (JSONObject) obj;
				
				Label lbl1 = new Label(item.get("holder").toString());
				lbl1.setBackground(Color.WHITE);
				recommendedTour.add(lbl1);
				
				Label lbl2 = new Label(item.get("name").toString());
				lbl2.setBackground(Color.WHITE);
				recommendedTour.add(lbl2);
				
				Label lbl3 = new Label(item.get("creator").toString());
				lbl3.setBackground(Color.WHITE);
				recommendedTour.add(lbl3);
				
				Label lbl4 = new Label(item.get("dateCreate").toString());
				lbl4.setBackground(Color.WHITE);
				recommendedTour.add(lbl4);
				
				Label lbl5 = new Label(item.get("placeCreate").toString());
				lbl5.setBackground(Color.WHITE);
				recommendedTour.add(lbl5);
			}
			recommendedTour.repaint();
			mainFrame.resize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT + 20);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	private void launch() {
		mainFrame.setTitle("Profiler-" + agent.getLocalName());
		mainFrame.setSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
		mainFrame.setLayout(new GridLayout(0, 1, 15, 15));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		
		Panel agePanel = new Panel();
		agePanel.setLayout(new GridLayout(1,2));
		agePanel.add(new Label("  Age"));
		final TextField age = new TextField();
		agePanel.add(age);
		mainFrame.add(agePanel);
		
		Panel occPanel = new Panel();
		occPanel.setLayout(new GridLayout(1,2));
		occPanel.add(new Label("  Occupation"));
		final TextField occupation = new TextField();
		occPanel.add(occupation);
		mainFrame.add(occPanel);
		
		Panel genderPanel = new Panel();
		genderPanel.setLayout(new GridLayout(1,2));
		genderPanel.add(new Label("  Gender"));
		final TextField gender = new TextField();
		genderPanel.add(gender);
		mainFrame.add(genderPanel);
		
		Panel intrestPanel = new Panel();
		intrestPanel.setLayout(new GridLayout(1,2));
		intrestPanel.add(new Label("  Intrest"));
		final List intrest = new List(4, true);
		intrest.add("Portrait Paintings");
		intrest.add("Landscape Paintings");
		intrest.add("Photography");
		intrest.add("Fantacy");
		intrestPanel.add(intrest);
		mainFrame.add(intrestPanel);
		 
		Panel btnPanel = new Panel();
		btnPanel.setLayout(new GridLayout(1,2));
		Button profileUpdate = new Button("Update Profile");
		btnPanel.add(profileUpdate);
		
		profileUpdate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.updateProfile(Integer.valueOf(age.getText()), occupation.getText(), gender.getText(), intrest.getSelectedItems());
			}
			
		});
		
		Button tourRequest = new Button("Tour Request");
		btnPanel.add(tourRequest);
		tourRequest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agent.tourRequest();
			}
		});
		mainFrame.add(btnPanel);
		
		recommendedTour.setLayout(new GridLayout(0,5));
		
		Panel recommendedTourHeader = new Panel();
		recommendedTourHeader.setLayout(new GridLayout(0,5));
		Label l1 = new Label("Venue", Label.CENTER);
		Label l2 = new Label("Artifact Name", Label.CENTER);
		Label l3 = new Label("Artifact Creator", Label.CENTER);
		Label l4 = new Label("Artifact Date", Label.CENTER);
		Label l5 = new Label("Artifact Place", Label.CENTER);
		recommendedTourHeader.add(l1);
		recommendedTourHeader.add(l2);
		recommendedTourHeader.add(l3);
		recommendedTourHeader.add(l4);
		recommendedTourHeader.add(l5);
		
		mainFrame.add(recommendedTourHeader);
		mainFrame.add(recommendedTour);
		
		mainFrame.setVisible(true);
	}

}
