package com.distributed.project.whiteboard.client.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used for creating the tool bar. It is used to display all the
 * drawing tools available to the user. It also displays the current editor.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class ToolPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -4957807155106421633L;

	private WhiteboardClient whiteboardClient;
	private DrawArea drawArea;
	private JButton pencil;
	private JButton eraser;
	private JButton line;
	private JButton circle;
	private JButton rectangle;
	private JButton triangle;
	private JButton text;
	private JButton colorChooser;
	private JTextField editorTextField;

	/**
	 * This constructor is used to initialize the client and the tool panel UI.
	 * 
	 * @param whiteboardClient
	 * @param drawArea
	 */
	public ToolPanel(WhiteboardClient whiteboardClient, DrawArea drawArea) {
		this.whiteboardClient = whiteboardClient;
		this.drawArea = drawArea;
		initialize();
	}

	/**
	 * This method is used to initialize the tool panel UI and add the different
	 * tools used in drawing.
	 * 
	 */
	private void initialize() {
		// Initializing the tool panel
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(new Color(211, 211, 211));
		setBounds(0, 70, 82, 324);
		setLayout(null);

		// Creating the pencil tool button
		pencil = new JButton(FontIcon.of(BoxiconsRegular.PENCIL, 20));
		pencil.setBounds(6, 6, 29, 29);
		pencil.setActionCommand(Constants.TOOL_PENCIL);
		pencil.setToolTipText("For free hand drawing");
		pencil.addActionListener(this);
		pencil.setEnabled(false);

		// Adding the pencil tool button to tool panel
		add(pencil);

		// Creating the eraser tool button
		eraser = new JButton(FontIcon.of(BoxiconsRegular.ERASER, 20));
		eraser.setBounds(47, 6, 29, 29);
		eraser.setActionCommand(Constants.TOOL_ERASER);
		eraser.setToolTipText("For erasing the drawing");
		eraser.addActionListener(this);
		eraser.setEnabled(false);

		// Adding the eraser tool button to tool panel
		add(eraser);

		// Creating the line tool button
		line = new JButton(FontIcon.of(BoxiconsRegular.MOVE_VERTICAL, 20));
		line.setBounds(6, 47, 29, 29);
		line.setActionCommand(Constants.TOOL_LINE);
		line.setToolTipText("For drawing a line");
		line.addActionListener(this);
		line.setEnabled(false);

		// Adding the line tool button to tool panel
		add(line);

		// Creating the circle tool button
		circle = new JButton(FontIcon.of(BoxiconsRegular.CIRCLE, 20));
		circle.setBounds(47, 47, 29, 29);
		circle.setActionCommand(Constants.TOOL_CIRCLE);
		circle.setToolTipText("For drawing a circle");
		circle.addActionListener(this);
		circle.setEnabled(false);

		// Adding the circle tool button to tool panel
		add(circle);

		// Creating the rectangle tool button
		rectangle = new JButton(FontIcon.of(BoxiconsRegular.RECTANGLE, 20));
		rectangle.setBounds(6, 88, 29, 29);
		rectangle.setActionCommand(Constants.TOOL_RECTANGLE);
		rectangle.setToolTipText("For drawing a rectangle");
		rectangle.addActionListener(this);
		rectangle.setEnabled(false);

		// Adding the rectangle tool button to tool panel
		add(rectangle);

		// Creating the triangle tool button
		triangle = new JButton(FontIcon.of(BoxiconsRegular.SHAPE_TRIANGLE, 20));
		triangle.setBounds(47, 88, 29, 29);
		triangle.setActionCommand(Constants.TOOL_TRIANGLE);
		triangle.setToolTipText("For drawing a triangle");
		triangle.addActionListener(this);
		triangle.setEnabled(false);

		// Adding the triangle tool button to tool panel
		add(triangle);

		// Creating the text tool button
		text = new JButton(FontIcon.of(BoxiconsRegular.TEXT, 20));
		text.setBounds(6, 129, 70, 29);
		text.setActionCommand(Constants.TOOL_TEXT);
		text.setToolTipText("For inserting a text");
		text.addActionListener(this);
		text.setEnabled(false);

		// Adding the text tool button to tool panel
		add(text);

		// Creating the colorChooser tool button
		colorChooser = new JButton(FontIcon.of(BoxiconsRegular.PALETTE, 20));
		colorChooser.setBounds(6, 170, 70, 29);
		colorChooser.setActionCommand(Constants.TOOL_COLOR);
		colorChooser.setToolTipText("For choosing color");
		colorChooser.addActionListener(this);
		colorChooser.setEnabled(false);

		// Adding the colorChooser tool button to tool panel
		add(colorChooser);

		// Creating the current label
		JLabel currentLabel = new JLabel("Current");
		currentLabel.setHorizontalAlignment(SwingConstants.CENTER);
		currentLabel.setBounds(0, 211, 82, 16);
		add(currentLabel);

		// Creating the editor label
		JLabel editorLabel = new JLabel("Editor");
		editorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		editorLabel.setBounds(0, 228, 82, 16);
		add(editorLabel);

		// Creating the current editor text field
		editorTextField = new JTextField();
		editorTextField.setBounds(0, 253, 82, 26);
		editorTextField.setColumns(10);
		editorTextField.setEditable(false);

		// Adding the current editor text to tool panel
		add(editorTextField);
	}

	/**
	 * This method is called when a user clicks on any of the tool buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		switch (actionCommand) {
		case Constants.TOOL_PENCIL:
		case Constants.TOOL_ERASER:
		case Constants.TOOL_RECTANGLE:
		case Constants.TOOL_LINE:
		case Constants.TOOL_CIRCLE:
		case Constants.TOOL_TRIANGLE:
		case Constants.TOOL_TEXT:
			drawArea.setSelectedTool(actionCommand);
			break;
		case Constants.TOOL_COLOR:
			chooseColor();
			break;
		default:
			break;
		}
	}

	/**
	 * This method is called when the user clicks on the choose color option. It
	 * display the color chooser dialog in which user can select from a variety of
	 * colors.
	 * 
	 * @implNote We use {@link JColorChooser} for displaying color choosing panel.
	 * 
	 */
	private void chooseColor() {
		// Displaying the cholor chooser dialog
		Color color = JColorChooser.showDialog(drawArea, "Please choose a color", Color.BLACK);
		if (color != null) {
			// If user selects color then assign color in the draw area
			drawArea.setSelectedColor(color);
		}
	}

	/**
	 * THis method is used to enable the tool for users once they have been
	 * accepted. It also changes the color of the panel based on the type of user.
	 */
	public void enableUIAfterVerification() {
		pencil.setEnabled(true);
		eraser.setEnabled(true);
		line.setEnabled(true);
		circle.setEnabled(true);
		rectangle.setEnabled(true);
		triangle.setEnabled(true);
		text.setEnabled(true);
		colorChooser.setEnabled(true);

		setBackground(whiteboardClient.getIsManager() ? Constants.MANAGER_UI_COLOR : Constants.GUEST_UI_COLOR);
	}

	public void setEditor(String userName) {
		editorTextField.setText(userName);
	}

	public void clearEditor() {
		editorTextField.setText(StringUtils.EMPTY);
	}
}
