package com.distributed.project.whiteboard.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.gui.ChatBoxPanel;
import com.distributed.project.whiteboard.client.gui.CoordinateBar;
import com.distributed.project.whiteboard.client.gui.DrawArea;
import com.distributed.project.whiteboard.client.gui.FileFunctionPanel;
import com.distributed.project.whiteboard.client.gui.HeaderPanel;
import com.distributed.project.whiteboard.client.gui.ToolPanel;
import com.distributed.project.whiteboard.client.gui.UserPanel;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used for setting up the whiteboard UI. It also handles other
 * functions related to UI.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class WhiteBoardUI extends JFrame {

	private static final long serialVersionUID = -6156713632601513816L;

	private static final Logger LOGGER = LoggerFactory.getLogger(WhiteBoardUI.class);

	private static final String TEMP_FILE_PATH = "/Users/abhijeet/Downloads/" + "test-";
	private static final String IMAGE_EXT = ".png";

	private WhiteboardClient whiteboardClient;
	private DrawArea drawArea;
	private ChatBoxPanel chatBoxPanel;
	private ToolPanel toolPanel;
	private FileFunctionPanel fileFunctionPanel;
	private UserPanel userPanel;
	private CoordinateBar coordinateBar;
	private HeaderPanel headerPanel;

	/**
	 * This constructor populates the the whiteboard client and intializes the UI.
	 * 
	 * @param whiteboardClient
	 */
	public WhiteBoardUI(WhiteboardClient whiteboardClient) {
		this.whiteboardClient = whiteboardClient;
		initialize();
	}

	/**
	 * This method is used to initialize the UI for whiteboard.
	 */
	private void initialize() {
		// Initializing the frame
		setTitle("Shared Whiteboard");
		setBounds(100, 100, 620, 550);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		// Taking username before starting the shared client
		userLogin();

		// Initializing the chat box panel
		chatBoxPanel = new ChatBoxPanel(whiteboardClient);
		getContentPane().add(chatBoxPanel);

		// Initialiazing the coordinate bar
		coordinateBar = new CoordinateBar(whiteboardClient);
		getContentPane().add(coordinateBar);

		// Initializing the draw area
		drawArea = new DrawArea(whiteboardClient, coordinateBar);
		getContentPane().add(drawArea);

		// Initializing the tool panel
		toolPanel = new ToolPanel(whiteboardClient, drawArea);
		getContentPane().add(toolPanel);

		// Initializing the header panel
		headerPanel = new HeaderPanel(whiteboardClient);
		getContentPane().add(headerPanel);

		// Initializing the file function panel
		fileFunctionPanel = new FileFunctionPanel(whiteboardClient, drawArea, chatBoxPanel);
		getContentPane().add(fileFunctionPanel);

		// Initializing the user panel
		userPanel = new UserPanel(whiteboardClient, chatBoxPanel);
		getContentPane().add(userPanel);
	}

	/**
	 * This method is used to take the user input for user login before starting the
	 * UI.
	 * 
	 */
	private void userLogin() {
		// Showing dialog box for input of user name
		String inputText = (String) JOptionPane.showInputDialog(this, "Please input your username",
				"Shared Whiteboard Login", JOptionPane.INFORMATION_MESSAGE, FontIcon.of(BoxiconsRegular.USER, 40), null,
				"username");
		// If username entered is not empty
		if (StringUtils.isNotEmpty(inputText)) {
			// Setting the user name for current user
			whiteboardClient.setClientUserName(inputText.trim(), true);
		} else {
			// Invalid entry or clicked on cancel
			LOGGER.error("Invalid username or user clicked on cancel -- Exiting");
			System.exit(0);
		}
	}

	/**
	 * This method is used to when a manager receives a join request. It displays a
	 * join request pop-up to the manager and based on the decision sends events
	 * back to the server.
	 * 
	 * @param request
	 */
	public void userPermission(ActionMessageDto request) {
		// Join request pop up
		int result = JOptionPane.showConfirmDialog(this,
				request.getSelectedUser().getClientUserName() + StringUtils.SPACE
						+ "is requesting to join the whiteboard",
				"Whiteboard Join Request", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		// If manager clicks on yes
		if (result == JOptionPane.YES_OPTION) {
			// Send user accepted event to server
			ActionMessageDto userAccepted = new ActionMessageDto(whiteboardClient.getUserInfo(),
					Constants.ACTION_NEW_USER_ACCEPT);
			userAccepted.setSelectedUser(request.getSelectedUser());

			// Adding the accept user event to the queue
			whiteboardClient.getActionList().add(userAccepted);

			// Sending load image event to the new user for showing the same screen
			ActionMessageDto loadImageEvent = new ActionMessageDto(whiteboardClient.getUserInfo(),
					Constants.ACTION_LOAD_IMAGE);
			loadImageEvent.setSelectedUser(request.getSelectedUser());
			loadImageEvent.setDrawboardImage(convertDrawAreaToStringBytes());

			// Adding the load image event to the queue
			whiteboardClient.getActionList().add(loadImageEvent);

			// Adding user to the active clients list
			userPanel.addUserToList(request.getSelectedUser());

			// Adding the system message to chat since manager wont recieve the user added
			// event themselves
			chatBoxPanel.append(
					Constants.MSG_USER_ADDED.replace(Constants.USER, request.getSelectedUser().getClientUserName())
							+ StringUtils.LF,
					Color.RED);
		} else {
			// Creating the user rejected event for the selected user
			ActionMessageDto userRejected = new ActionMessageDto(whiteboardClient.getUserInfo(),
					Constants.ACTION_NEW_USER_REJECT);
			userRejected.setSelectedUser(request.getSelectedUser());

			// Adding the event to the queue
			whiteboardClient.getActionList().add(userRejected);
		}
	}

	/**
	 * This method is used to display success messages on the screen of the user
	 * with the specified text in arguments.
	 * 
	 * @param message
	 */
	public void showSuccessMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * This method is used to display error messages on the screen of the user with
	 * the specified text in the arguments. It shuts downs the system if it is a
	 * critical error.
	 * 
	 * @param message
	 * @param isCritical
	 */
	public void showErrorMessage(String message, boolean isCritical) {
		JOptionPane.showMessageDialog(this, message, "Alert", JOptionPane.WARNING_MESSAGE);

		// Shutting down the application for critical errors
		if (isCritical) {
			this.dispose();
			System.exit(1);
		}
	}

	/**
	 * This method is used to enable the UI for the users once they are accepted. It
	 * enables different features for managers and normal clients.
	 */
	public void enableUserUI() {
		fileFunctionPanel.enableUIAfterVerification();
		userPanel.enableUIAfterVerification();
		chatBoxPanel.enableUIAfterVerification();
		drawArea.enableUIAfterVerification();
		toolPanel.enableUIAfterVerification();
		coordinateBar.enableUIAfterVerification();
		headerPanel.enableUIAfterVerification();
	}

	/**
	 * This method is used to convert the current user's draw area to string of
	 * bytes to send across the network.
	 * 
	 * @return
	 */
	private String convertDrawAreaToStringBytes() {
		try {
			// Creating a temp file
			File drawFile = new File(TEMP_FILE_PATH + whiteboardClient.getUserInfo().getClientUID() + IMAGE_EXT);

			// Creating a buffered image of the current draw area
			BufferedImage bufferedImage = new BufferedImage(drawArea.getSize().width, drawArea.getSize().height,
					BufferedImage.TYPE_INT_RGB);

			// Creating graphics for the buffered image
			Graphics2D graphics2d = bufferedImage.createGraphics();

			// Printing the draw area onto the buffered image
			drawArea.print(graphics2d);

			// Writing the buffered image onto the file in PNG format
			ImageIO.write(bufferedImage, "png", drawFile);

			// Converting file to array of bytes
			byte[] bytes = Files.readAllBytes(drawFile.toPath());

			// Deleting the temp file
			Files.deleteIfExists(Paths.get(drawFile.getAbsolutePath()));

			// Returning encoded format of the array of bytes
			return new String(Base64.getEncoder().encode(bytes));
		} catch (Exception e) {
			LOGGER.error("Exception in convertDrawAreaToFile", e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * This method is used to convert the draw area string of bytes image to file
	 * and then display it to the current user's draw area.
	 * 
	 * @param actionMessageDto
	 */
	public void loadImageFromServer(ActionMessageDto actionMessageDto) {
		// Creating a temp file
		File file = new File(TEMP_FILE_PATH + whiteboardClient.getUserInfo().getClientUID() + IMAGE_EXT);

		// Starting the file output stream reader
		try (FileOutputStream fout = new FileOutputStream(file)) {
			// Decoding the string and converting it back to array of bytes
			byte[] bytes = Base64.getDecoder().decode(actionMessageDto.getDrawboardImage());

			// Writing the bytes onto the file
			fout.write(bytes);
			fout.flush();

			// Printing the current image on the current user's draw area
			drawArea.loadImage(ImageIO.read(file));

			// Deleting the temp file
			Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
		} catch (Exception e) {
			LOGGER.error("Exception in loadImageFromServer", e);
		}
	}

	/**
	 * This method is used to fetch the instance of {@link DrawArea}
	 * 
	 * @return
	 */
	public DrawArea getDrawArea() {
		return drawArea;
	}

	/**
	 * This method is used to fetch the instance of {@link ChatBoxPanel}
	 * 
	 * @return
	 */
	public ChatBoxPanel getChatBoxPanel() {
		return chatBoxPanel;
	}

	/**
	 * This method is used to fetch the instance of {@link UserPanel}
	 * 
	 * @return
	 */
	public UserPanel getUserPanel() {
		return userPanel;
	}

	/**
	 * This method is used to fetch the instance of {@link ToolPanel}
	 * 
	 * @return
	 */
	public ToolPanel getToolPanel() {
		return toolPanel;
	}
}
