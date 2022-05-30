package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used to display the title and the name of the user joining the
 * whiteboard.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class HeaderPanel extends JPanel {

	private static final long serialVersionUID = -6897146541100297989L;

	private WhiteboardClient whiteboardClient;
	private JLabel headingLabel;

	/**
	 * This constructor initialized the client variable and the header UI.
	 * 
	 * @param whiteboardClient
	 */
	public HeaderPanel(WhiteboardClient whiteboardClient) {
		this.whiteboardClient = whiteboardClient;
		initialize();
	}

	/**
	 * This method is used to initialize the header UI.
	 */
	private void initialize() {
		// Initializing the header panel
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(new Color(220, 220, 220));
		setBounds(0, 29, 620, 42);
		setLayout(null);

		// Creating the header label
		headingLabel = new JLabel("Shared Whiteboard");
		headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headingLabel.setBounds(6, 6, 608, 30);
		headingLabel.setFont(new Font(Constants.FONT_LUCIDA_GRANDE, Font.BOLD, 17));

		// Adding the header label to the header panel
		add(headingLabel);
	}

	/**
	 * This method is used to set the background according to the user type. It also
	 * changes the header label by adding the user name in front of it.
	 * 
	 */
	public void enableUIAfterVerification() {
		setBackground(whiteboardClient.getIsManager() ? Constants.MANAGER_UI_COLOR : Constants.GUEST_UI_COLOR);

		// Adding the current user name in header once accepted
		headingLabel.setText("Shared Whiteboard - " + whiteboardClient.getUserInfo().getClientUserName());
	}

}
