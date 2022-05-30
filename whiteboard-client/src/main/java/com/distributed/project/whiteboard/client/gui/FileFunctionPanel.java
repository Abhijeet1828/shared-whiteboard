package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used for creating the file function panel. It is used for file
 * operation such as NEW, SAVE and SAVE AS. It also displays the type of user
 * connected.
 * 
 * @implNote The file functions can only be operated by the manager, for other
 *           users the buttons are disabled
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class FileFunctionPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -3593958423007522564L;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileFunctionPanel.class);

	private WhiteboardClient whiteboardClient;
	private DrawArea drawArea;
	private ChatBoxPanel chatBoxPanel;

	private JButton newFileButton;
	private JButton openFileButton;
	private JButton saveFileButton;
	private JButton saveAsFileButton;
	private JLabel userTypeLabel;

	// For storing the current selected file path
	private String currentSelectedFilePath;

	/**
	 * This constructor is used to initialize the client variable and the file
	 * function panel UI.
	 * 
	 * @param whiteboardClient
	 * @param drawArea
	 * @param chatBoxPanel
	 */
	public FileFunctionPanel(WhiteboardClient whiteboardClient, DrawArea drawArea, ChatBoxPanel chatBoxPanel) {
		this.whiteboardClient = whiteboardClient;
		this.drawArea = drawArea;
		this.chatBoxPanel = chatBoxPanel;
		initialize();
	}

	/**
	 * This method is used to initilize the file function panel UI with the file
	 * function buttons.
	 */
	private void initialize() {
		// Initializing the panel
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(new Color(220, 220, 220));
		setBounds(0, 0, 620, 30);
		setLayout(null);

		// Creating the new file button
		newFileButton = new JButton(Constants.FILE_NEW, FontIcon.of(BoxiconsRegular.FILE_BLANK, 20));
		newFileButton.setBounds(0, 0, 79, 29);
		newFileButton.setActionCommand(Constants.FILE_NEW);
		newFileButton.addActionListener(this);
		newFileButton.setEnabled(false);

		// Adding the new file button to file function panel
		add(newFileButton);

		// Creating the open file button
		openFileButton = new JButton(Constants.FILE_OPEN, FontIcon.of(BoxiconsRegular.FILE_FIND, 20));
		openFileButton.setBounds(91, 0, 79, 29);
		openFileButton.setActionCommand(Constants.FILE_OPEN);
		openFileButton.addActionListener(this);
		openFileButton.setEnabled(false);

		// Adding the open file button to file function panel
		add(openFileButton);

		// Creating the save file button
		saveFileButton = new JButton(Constants.FILE_SAVE, FontIcon.of(BoxiconsRegular.SAVE, 20));
		saveFileButton.setBounds(182, 0, 79, 29);
		saveFileButton.setActionCommand(Constants.FILE_SAVE);
		saveFileButton.addActionListener(this);
		saveFileButton.setEnabled(false);

		// Adding the save file button to file function panel
		add(saveFileButton);

		// Creating the save as file button
		saveAsFileButton = new JButton(Constants.FILE_SAVE_AS, FontIcon.of(BoxiconsRegular.EXPORT, 20));
		saveAsFileButton.setBounds(273, 0, 84, 29);
		saveAsFileButton.setActionCommand(Constants.FILE_SAVE_AS);
		saveAsFileButton.addActionListener(this);
		saveAsFileButton.setEnabled(false);

		// Adding the save as file button to the file function panel
		add(saveAsFileButton);

		// Creating the user type label
		userTypeLabel = new JLabel("MANAGER");
		userTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userTypeLabel.setBounds(501, 5, 98, 16);
		userTypeLabel.setVisible(false);

		// Adding the user type label in the file function panel
		add(userTypeLabel);
	}

	/**
	 * This method is called whenever user clicks on any of the file functions. It
	 * then executes code related to the button clicked.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		switch (actionCommand) {
		case Constants.FILE_NEW:
			newFile();
			break;
		case Constants.FILE_OPEN:
			loadFile();
			break;
		case Constants.FILE_SAVE:
			saveToCurrentFile();
			break;
		case Constants.FILE_SAVE_AS:
			saveFileAsImage();
			break;
		default:
			LOGGER.error("No such action command - {}", actionCommand);
			break;
		}
	}

	/**
	 * This method is called when the user clicks on the load file button. It
	 * displays the file selection UI. Once user selects a PNG file to load. We
	 * convert the image and display it on the draw area.
	 * 
	 */
	private void loadFile() {
		try {
			// Initializing the file chooser
			JFileChooser fileChooser = new JFileChooser();

			// Allowing only files to be selected
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// Adding only PNG image selection filter
			fileChooser.setFileFilter(new ImageSelectionFilter());

			// Displaying the file selection UI
			int selectedOption = fileChooser.showOpenDialog(drawArea);

			// If user clicks on cancel then do nothing
			if (selectedOption != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// Get the selected file
			File openFile = fileChooser.getSelectedFile();

			// Store the current selected file to for furthur save operations
			currentSelectedFilePath = openFile.getAbsolutePath();

			// Read and load image on the draw area
			drawArea.loadImage(ImageIO.read(openFile));

			// Creating load image event for other users to load the same image as manager
			ActionMessageDto loadFileEvent = new ActionMessageDto(whiteboardClient.getUserInfo(),
					Constants.ACTION_LOAD_IMAGE);

			// Converting the selected file to array of bytes
			byte[] bytes = Files.readAllBytes(openFile.toPath());

			// Encoding the array of bytes to send in load image event
			loadFileEvent.setDrawboardImage(new String(Base64.getEncoder().encode(bytes)));

			// Adding the load image event to the queue
			whiteboardClient.getActionList().add(loadFileEvent);

			// Adding the load image message to the chat box panel
			chatBoxPanel.append(Constants.MSG_LOAD_IMAGE, Color.RED);
		} catch (IOException e) {
			LOGGER.error("Exception in loadFile()", e);

			// In exception restore the current selected file path
			currentSelectedFilePath = StringUtils.EMPTY;

			// Add error message on chat box of manager to know image load failed
			chatBoxPanel.append(Constants.MSG_LOAD_IMAGE_ERROR, Color.RED);
		}
	}

	/**
	 * This method is called when the manager clicks on the save button. If a file
	 * had been saved earlier or loaded from the local then it saves it to that file
	 * or else displays the save as option.
	 * 
	 */
	private void saveToCurrentFile() {
		try {
			// If no file is previously selected, then display save as option
			if (StringUtils.isBlank(currentSelectedFilePath)) {
				saveFileAsImage();
			} else {
				// Getting the current selected file
				File saveFile = new File(currentSelectedFilePath);

				// Converting draw area to file
				convertDrawAreaToFile(saveFile);

				// Adding save files message onto the chat box panel
				chatBoxPanel.append(Constants.MSG_FILE_SAVED, Color.RED);
			}
		} catch (IOException e) {
			LOGGER.error("Exception in saveToCurrentFile()", e);

			// Add error message on chat box of manager to know save file failed
			chatBoxPanel.append(Constants.MSG_FILE_SAVED_ERROR, Color.RED);
		}
	}

	/**
	 * This method is used to save a new file with the given name. It is called when
	 * manager clicks on the save as option. it saves the current draw area as a png
	 * file in the folder selected by the manager.
	 */
	private void saveFileAsImage() {
		try {
			// Initializing the file chooser
			JFileChooser fileChooser = new JFileChooser();

			// Allowing only files to be selected
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// Displaying the file save UI
			int selectedOption = fileChooser.showSaveDialog(drawArea);

			// If user clicks on cancel then do nothing
			if (selectedOption != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// Get the selected file
			File saveFile = fileChooser.getSelectedFile();

			// If the file does not end with png extension then adding it at the end
			if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png")) {
				saveFile = new File(saveFile.getAbsolutePath() + ".png");
			}

			// Storing the current selected file for further save operations
			currentSelectedFilePath = saveFile.getAbsolutePath();

			// Converting draw area to file
			convertDrawAreaToFile(saveFile);

			// Adding save files message onto the chat box panel
			chatBoxPanel.append(Constants.MSG_FILE_SAVED, Color.RED);
		} catch (IOException e) {
			LOGGER.error("Exception in saveFileAsImage()", e);

			// In exception restore the current selected file path
			currentSelectedFilePath = StringUtils.EMPTY;

			// Add error message on chat box of manager to know image save failed
			chatBoxPanel.append(Constants.MSG_FILE_SAVED_ERROR, Color.RED);
		}
	}

	/**
	 * This method is used to create a new file which essentialy means clearing the
	 * whiteboard and removing the current selected file.
	 * 
	 */
	private void newFile() {
		// Clearing the draw area
		drawArea.clear();

		// Creating the clear event for other clients to clear as well
		ActionMessageDto clearEvent = new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_CLEAR);

		// Adding event to the queue
		whiteboardClient.getActionList().add(clearEvent);

		// Adding draw area cleared message in chat box
		chatBoxPanel.append(Constants.MSG_CLEAR, Color.RED);

		// Marking the current selected file as empty
		currentSelectedFilePath = StringUtils.EMPTY;
	}

	/**
	 * This method is used to enable the UI according to the type of user. If
	 * manager then it enables the file functions. If it is a normal user then it
	 * changes the background color and display guest on the file panel.
	 */
	public void enableUIAfterVerification() {
		// If the current user is the manager
		if (whiteboardClient.getIsManager()) {
			// Set the manager background color
			setBackground(Constants.MANAGER_UI_COLOR);

			// Enable file functions
			newFileButton.setEnabled(true);
			openFileButton.setEnabled(true);
			saveFileButton.setEnabled(true);
			saveAsFileButton.setEnabled(true);
		} else {
			// Setting the user label to guest if not manager
			userTypeLabel.setText("GUEST");

			// Setting the normal user background for differentiation
			setBackground(Constants.GUEST_UI_COLOR);
		}
		// Enabling the user type label
		userTypeLabel.setVisible(true);
	}

	/**
	 * This method is used to print the draw area onto the given PNG file.
	 * 
	 * @param selectedFile
	 * @throws IOException
	 */
	private void convertDrawAreaToFile(File selectedFile) throws IOException {
		// Creating buffered image
		BufferedImage bufferedImage = new BufferedImage(drawArea.getSize().width, drawArea.getSize().height,
				BufferedImage.TYPE_INT_RGB);

		// Creating graphics for buffered image
		Graphics2D graphics2d = bufferedImage.createGraphics();

		// Printing draw area onto the buffered image
		drawArea.print(graphics2d);

		// Writing the image onto the file
		ImageIO.write(bufferedImage, "png", selectedFile);
	}
}
