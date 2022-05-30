package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.dto.UserDto;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used to disply the active user list and the option for a
 * manager to kick out users.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class UserPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 2497542622146445059L;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserPanel.class);

	private WhiteboardClient whiteboardClient;
	private ChatBoxPanel chatBoxPanel;

	private JButton removeButton;
	private DefaultListModel<UserDto> listModel;
	private JList<UserDto> list;

	// Used to maintain active client list
	private Map<Long, UserDto> activeClientMap = new ConcurrentHashMap<>();

	/**
	 * This constructor is used to initialize the client variables and initialize
	 * the UI.
	 * 
	 * @param whiteboardClient
	 * @param chatBoxPanel
	 */
	public UserPanel(WhiteboardClient whiteboardClient, ChatBoxPanel chatBoxPanel) {
		this.whiteboardClient = whiteboardClient;
		this.chatBoxPanel = chatBoxPanel;
		initialize();
	}

	/**
	 * This method is used to initialize the user panel UI with all the components.
	 */
	private void initialize() {
		// Initializing the user panel
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(new Color(211, 211, 211));
		setBounds(518, 70, 102, 324);
		setLayout(null);

		// Creating default list model
		listModel = new DefaultListModel<>();

		// Adding the list model to Jlist
		list = new JList<>(listModel);

		// Adding the list to the scroll pane, so it becomes scrollable
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBounds(0, 65, 102, 199);

		// Adding scroll panel to the user panel
		add(scrollPane);

		// Creating users heading label
		JLabel userLabel = new JLabel("USERS");
		userLabel.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		userLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userLabel.setBounds(0, 18, 102, 33);

		// Adding users label to user panel
		add(userLabel);

		// Creating the remove button with properties
		removeButton = new JButton("REMOVE");
		removeButton.setBounds(10, 276, 75, 33);
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);

		// Adding remove button to user panel
		add(removeButton);
	}

	/**
	 * This method is used to enable the UI according to the type of users. If
	 * manager then remove button is enabled or else changing the background for
	 * normal user.
	 */
	public void enableUIAfterVerification() {
		if (whiteboardClient.getIsManager()) {
			setBackground(Constants.MANAGER_UI_COLOR);
			removeButton.setEnabled(true);
		} else {
			setBackground(Constants.GUEST_UI_COLOR);
		}
	}

	/**
	 * This method is used to refresh the list of users with the list provided in
	 * the arguments.
	 * 
	 * @param clientList
	 */
	public void refreshUserList(List<UserDto> clientList) {
		// Clearing the existing list
		listModel.clear();

		// Adding names to the list
		clientList.forEach(c -> listModel.addElement(c));

		// Re-populating the active client map
		activeClientMap.clear();
		activeClientMap = clientList.stream().collect(Collectors.toMap(UserDto::getClientUID, c -> c));
	}

	/**
	 * This method is used to add user to the active client list
	 * 
	 * @param newUser
	 */
	public void addUserToList(UserDto newUser) {
		listModel.addElement(newUser);
		activeClientMap.put(newUser.getClientUID(), newUser);
	}

	/**
	 * This method is used to remove the user from the list and then refresh the
	 * list.
	 * 
	 * @param selectedUser
	 */
	public void removeUserFromList(UserDto selectedUser) {
		activeClientMap.remove(selectedUser.getClientUID());

		refreshUserList(new ArrayList<>(activeClientMap.values()));
	}

	/**
	 * This method is called when the manager clicks on the remove button. It
	 * removes the user from the list and informs the selected user, they have been
	 * kicked.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Fetch the current selected value in the list
		UserDto selectedUser = list.getSelectedValue();

		// If selected user is non null
		if (Objects.nonNull(selectedUser)) {
			// Not allowing manager to kick themselves out
			if (whiteboardClient.getUserInfo().getClientUID().equals(selectedUser.getClientUID())) {
				LOGGER.error("Manager cannot kick itself");
				JOptionPane.showMessageDialog(this, "Please select a user other than yourself to kick", "Alert",
						JOptionPane.WARNING_MESSAGE);
			} else {
				// Creating the kick user event
				ActionMessageDto kickUserEvent = new ActionMessageDto(whiteboardClient.getUserInfo(),
						Constants.ACTION_USER_KICK);
				kickUserEvent.setSelectedUser(activeClientMap.get(selectedUser.getClientUID()));

				// Adding the kick user event to the queue
				whiteboardClient.getActionList().add(kickUserEvent);

				// Removing user from current active list
				removeUserFromList(selectedUser);

				// Appending kick user message in the current user's chatbox
				chatBoxPanel.append(Constants.MSG_KICK_USER.replace(Constants.USER, selectedUser.getClientUserName()),
						Color.RED);
			}
		} else {
			// If no user is selected then show alert message
			JOptionPane.showMessageDialog(this, "Please select a user to remove", "Alert", JOptionPane.WARNING_MESSAGE);
		}
	}
}
