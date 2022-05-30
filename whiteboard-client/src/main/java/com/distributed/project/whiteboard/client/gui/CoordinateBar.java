package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.StringUtils;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used for creating a coordinate bar at the bottom of the
 * whiteboard which shows the coordinates of the pointer. It also has the exit
 * button which can be used to gracefully shutdown the system.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class CoordinateBar extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8023430725933782885L;

	private WhiteboardClient whiteboardClient;
	private JLabel coordinateLabel;

	/**
	 * This constructor is used to intialize the client and the coordinate bar UI.
	 * 
	 * @param whiteboardClient
	 */
	public CoordinateBar(WhiteboardClient whiteboardClient) {
		this.whiteboardClient = whiteboardClient;
		initialize();
	}

	/**
	 * This method is used to initialize the coordinate bar panel UI.
	 * 
	 */
	private void initialize() {
		// Initializing the coordinate bar panel
		setBackground(new Color(211, 211, 211));
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBounds(0, 490, 620, 32);
		setLayout(null);

		// Initializing the exit button
		JButton exitButton = new JButton("EXIT");
		exitButton.setBounds(497, 0, 117, 29);
		exitButton.addActionListener(this);

		// Adding the exit button to the coordinate bar
		add(exitButton);

		// Initializing the coordinate label
		coordinateLabel = new JLabel("0 X 0");
		coordinateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		coordinateLabel.setBounds(6, 5, 74, 21);

		// Adding the coordinate label to the coordinate bar
		add(coordinateLabel);
	}

	/**
	 * This method is called whenever the user moves the mouse in the draw area to
	 * capture the coordinates and display in the coordinate bar.
	 * 
	 * @param coordinates
	 */
	public void setCoordinates(Point coordinates) {
		coordinateLabel
				.setText(String.valueOf(coordinates.x) + StringUtils.SPACE + "X" + StringUtils.SPACE + coordinates.y);
	}

	/**
	 * This method is called when a manager joins or when a user gets accepted. It
	 * changes the color of the UI according to the role allocated.
	 */
	public void enableUIAfterVerification() {
		setBackground(whiteboardClient.getIsManager() ? Constants.MANAGER_UI_COLOR : Constants.GUEST_UI_COLOR);
	}

	/**
	 * This method is called when the user clicks on the exit button in the
	 * coordinate bar. It closes the application for the user and sends exit event
	 * to the server, for it to close the connection and inform other users.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Creating the exit event
		ActionMessageDto exitUserEvent = new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_EXIT);

		// Adding the exit event to the queue
		whiteboardClient.getActionList().add(exitUserEvent);

		// Showing the exit message pop-up to the user
		JOptionPane.showMessageDialog(this, "Thankyou for using Shared Whiteboard", "Alert",
				JOptionPane.INFORMATION_MESSAGE);

		// Exiting the application
		System.exit(1);
	}

}
