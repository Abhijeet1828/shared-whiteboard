package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.utilities.Constants;
import com.distributed.project.whiteboard.client.utilities.AESUtils;

/**
 * This class is used to initiate the chat box panel UI. It creates a message
 * area to show history of messages and allows user to type text messages.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @implNote All the messages sent and received by the user except system
 *           messages are encrypted using AES-CBC encryption. {@link AESUtils}
 * 
 * @author Abhijeet - 1278218
 *
 */
public class ChatBoxPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8211214429134291268L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatBoxPanel.class);

	// Adding tab space to display receiving text messages on the right side
	private static final String RECEIVE_MESSAGE_TAB_SPACE = "\t" + "\t" + "\t";

	private WhiteboardClient whiteboardClient;
	private JTextPane messageArea;
	private JTextField typeMessageField;
	private JScrollBar scrollBar;
	private JButton sendButton;

	/**
	 * This constructor is used to initialize the chat box panel UI and the client
	 * variable.
	 * 
	 * @param whiteboardClient
	 */
	public ChatBoxPanel(WhiteboardClient whiteboardClient) {
		this.whiteboardClient = whiteboardClient;
		initialize();
	}

	/**
	 * This method is used to initialize the various components inside the chat box
	 * panel.
	 */
	private void initialize() {
		// Initializing the chat box panel
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(new Color(211, 211, 211));
		setBounds(0, 394, 620, 97);
		setLayout(null);

		// Creating the send button with properties
		sendButton = new JButton("SEND");
		sendButton.setBounds(482, 23, 117, 56);
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);

		// Adding send button to chat box panel
		add(sendButton);

		// Creating message area with properties
		messageArea = new JTextPane();
		messageArea.setEditable(false);

		// Creating scroll pane and adding message area to it
		JScrollPane scrollPane = new JScrollPane(messageArea);
		scrollPane.setBounds(16, 6, 454, 61);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Adding scroll pane to the chat box panel
		add(scrollPane);

		// Populate scroll bar to use for later
		scrollBar = scrollPane.getVerticalScrollBar();

		// Creating type message field with properties
		typeMessageField = new JTextField();
		typeMessageField.setBounds(16, 65, 454, 26);
		typeMessageField.setColumns(10);
		typeMessageField.setEnabled(false);

		// Adding type message to the chat box panel
		add(typeMessageField);
	}

	/**
	 * This method is called whenever user clicks on the send button. It encrypts
	 * the text and sends it to the server for it to be broadcasted.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Fetching the typed text
		String typedText = typeMessageField.getText();

		// Encrypting the typed text with client's UID
		String encryptedText = AESUtils.encryptString(typedText,
				whiteboardClient.getUserInfo().getClientUID().toString());

		// If typed text is not blank and encryption was successful
		if (StringUtils.isNotBlank(encryptedText)) {

			// Append the tyed text to current user's chat box
			append(whiteboardClient.getUserInfo().getClientUserName() + ": " + typedText + StringUtils.LF,
					new Color(135, 206, 235));

			// Setting the scrollbar to recent message
			scrollBar.setValue(scrollBar.getMaximum());

			// Creating and adding chat message event to the queue
			whiteboardClient.getActionList().add(new ActionMessageDto(whiteboardClient.getUserInfo(),
					Constants.ACTION_CHAT, null, null, null, null, null, null, encryptedText));

			// Clearing the type message field for next user input
			typeMessageField.setText(StringUtils.EMPTY);
		}
	}

	/**
	 * This method is called when a message is received from other clients. It
	 * displays the sent message in the text pane.
	 * 
	 * @param actionMessageDto
	 */
	public void recieveMessage(ActionMessageDto actionMessageDto) {
		// Decrypting the text with sender's clientUID
		String decryptedText = AESUtils.decryptString(actionMessageDto.getChatMessage(),
				actionMessageDto.getUser().getClientUID().toString());

		// If decryption is successful
		if (StringUtils.isNotBlank(decryptedText)) {
			// Appending the message to the chat box
			append(RECEIVE_MESSAGE_TAB_SPACE + actionMessageDto.getUser().getClientUserName() + ": " + decryptedText
					+ StringUtils.LF, new Color(60, 179, 113));

			// Setting the scroll bar to recent message
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}

	/**
	 * This method is used to append text messages to chat box message area. It
	 * appends the text with the provided color.
	 * 
	 * @param inputText
	 * @param color
	 */
	public void append(String inputText, Color color) {
		// Creating style attributes for the message
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet attribute = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

		int len = messageArea.getDocument().getLength();

		messageArea.setCaretPosition(len);
		messageArea.setCharacterAttributes(attribute, false);
		messageArea.replaceSelection(inputText);
		try {
			// Inserting string to the text pane
			messageArea.getDocument().insertString(len, inputText, attribute);
		} catch (BadLocationException e) {
			LOGGER.error("Exception in ChatBoxPanel in inserting message", e);
		}
	}

	/**
	 * This method is used to enable the chat box panel UI once user has been
	 * accepted. It also changes the color of the UI based on the user type.
	 */
	public void enableUIAfterVerification() {
		sendButton.setEnabled(true);
		typeMessageField.setEnabled(true);

		setBackground(whiteboardClient.getIsManager() ? Constants.MANAGER_UI_COLOR : Constants.GUEST_UI_COLOR);
	}

}
