package com.distributed.project.whiteboard.client.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.StringUtils;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used to create the draw area for the user. It handles the
 * graphics and tools used for drawing. It also sends the events generated to
 * server to be broadcasted to other clients.
 * 
 * @implNote It extends {@link JPanel} to seggregate the code and easily
 *           integrate with the frame.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class DrawArea extends JPanel implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -921803482495204166L;

	private Image image;
	private Graphics2D graphics2d;
	private Point oldPoint = new Point(0, 0);
	private Point currentPoint = new Point(0, 0);
	private String selectedTool = Constants.TOOL_PENCIL;
	private Color selectedColor = Color.BLACK;

	private boolean uiEnabled = false;

	protected CoordinateBar coordinateBar;
	protected WhiteboardClient whiteboardClient;

	/**
	 * This constructor is used to initialize the draw area.
	 * 
	 * @param whiteboardClient
	 * @param coordinateBar
	 */
	public DrawArea(WhiteboardClient whiteboardClient, CoordinateBar coordinateBar) {
		this.coordinateBar = coordinateBar;
		this.whiteboardClient = whiteboardClient;
		initialize();
	}

	/**
	 * This method is used to initialize the draw area.
	 */
	private void initialize() {
		// Initializing the draw area
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBounds(81, 70, 437, 324);
		setBackground(Color.white);

		// Marking double buffered as false
		setDoubleBuffered(false);

		// Adding mouse listeners
		addMouseListener(this);
		addMouseMotionListener(this);

		setEnabled(false);
	}

	/**
	 * Overriding the paintComponent method to handle drawing and other actions on
	 * the draw area jpanel.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		if (image == null) {
			image = createImage(getSize().width, getSize().height);
			graphics2d = (Graphics2D) image.getGraphics();
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2d.setPaint(Color.white);
			graphics2d.fillRect(0, 0, getSize().width, getSize().height);
			graphics2d.setPaint(Color.BLACK);
		}
		// Drawing image according to the graphics
		if (uiEnabled) {
			g.drawImage(image, 0, 0, null);
		}
	}

	/**
	 * This method is called whenever a user selects and drags the mouse.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// Populating the first point
		currentPoint = e.getPoint();

		// Setting the coordinates in the coordinate bar
		coordinateBar.setCoordinates(e.getPoint());

		// Drawing line on drag for free hand drawing(pencil or eraser)
		if (StringUtils.isNotBlank(selectedTool) && Constants.FREE_HAND_TOOLS.contains(selectedTool)) {
			graphics2d.drawLine(oldPoint.x, oldPoint.y, currentPoint.x, currentPoint.y);
			repaint();

			// Sending event to other clients
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, currentPoint, null, selectedColor, null, null));

			// Marking current point as old point
			oldPoint = currentPoint;
		}
	}

	/**
	 * This method is called whenever user clicks on the draw area.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		oldPoint = e.getPoint();
		coordinateBar.setCoordinates(e.getPoint());

		switch (selectedTool) {
		case Constants.TOOL_ERASER:
			// If eraser is selected change color and stroke
			graphics2d.setColor(Color.WHITE);
			graphics2d.setStroke(new BasicStroke(20));
			break;
		case Constants.TOOL_TEXT:
			// Inserting text as soon as user clicks
			insertText();
			break;
		default:
			break;
		}
	}

	/**
	 * This method is used whenever a user releases the mouse from drag.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		switch (selectedTool) {
		case Constants.TOOL_LINE:
			currentPoint = e.getPoint();
			createLine(oldPoint, currentPoint, null);
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, currentPoint, null, selectedColor, null, null));
			break;
		case Constants.TOOL_RECTANGLE:
			currentPoint = e.getPoint();
			createRectangle(oldPoint, currentPoint, null);
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, currentPoint, null, selectedColor, null, null));
			break;
		case Constants.TOOL_CIRCLE:
			currentPoint = e.getPoint();
			createCircle(oldPoint, currentPoint, null);
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, currentPoint, null, selectedColor, null, null));
			break;
		case Constants.TOOL_TRIANGLE:
			createTriangle(oldPoint, currentPoint, e.getPoint(), null);
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, currentPoint, e.getPoint(), selectedColor, null, null));
			break;
		case Constants.TOOL_ERASER:
			setSelectedColor(selectedColor);
			graphics2d.setStroke(new BasicStroke(1));
			break;
		default:
			break;
		}
	}

	/**
	 * This method is used to create lines between the given points in the
	 * arguments.
	 * 
	 * @param firstPoint
	 * @param secondPoint
	 * @param secondaryColor
	 */
	public void createLine(Point firstPoint, Point secondPoint, Color secondaryColor) {
		setSecondaryColor(secondaryColor);

		graphics2d.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);
		repaint();

		graphics2d.setColor(selectedColor);
	}

	/**
	 * This method is used to create a rectangle from start point to end point given
	 * in the arguments.
	 * 
	 * @param firstPoint
	 * @param secondPoint
	 * @param secondaryColor
	 */
	public void createRectangle(Point firstPoint, Point secondPoint, Color secondaryColor) {
		setSecondaryColor(secondaryColor);

		int topLeftx = Math.min(firstPoint.x, secondPoint.x);
		int topLefty = Math.min(firstPoint.y, secondPoint.y);
		int width = Math.abs(secondPoint.x - firstPoint.x);
		int height = Math.abs(secondPoint.y - firstPoint.y);

		graphics2d.drawRect(topLeftx, topLefty, width, height);
		repaint();

		graphics2d.setColor(selectedColor);
	}

	/**
	 * This method is used to create a circle with the given points.
	 * 
	 * @param firstPoint
	 * @param secondPoint
	 * @param secondaryColor
	 */
	public void createCircle(Point firstPoint, Point secondPoint, Color secondaryColor) {
		setSecondaryColor(secondaryColor);

		int widthC = Math.abs(secondPoint.x - firstPoint.x);
		int heightH = Math.abs(secondPoint.y - firstPoint.y);

		graphics2d.drawOval(firstPoint.x, firstPoint.y, widthC, heightH);
		repaint();

		graphics2d.setColor(selectedColor);
	}

	/**
	 * This method is used to create a triangle from the points given in the
	 * arguments.
	 * 
	 * @param firstPoint
	 * @param secondPoint
	 * @param lastPoint
	 * @param secondaryColor
	 */
	public void createTriangle(Point firstPoint, Point secondPoint, Point lastPoint, Color secondaryColor) {
		setSecondaryColor(secondaryColor);

		Point midPoint;
		if (firstPoint.x > secondPoint.x) {
			midPoint = new Point((secondPoint.x + (Math.abs(firstPoint.x - secondPoint.x) / 2)), lastPoint.y);
		} else {
			midPoint = new Point((secondPoint.x - (Math.abs(firstPoint.x - secondPoint.x) / 2)), lastPoint.y);
		}
		int[] xs = { firstPoint.x, secondPoint.x, midPoint.x };
		int[] ys = { firstPoint.y, secondPoint.y, midPoint.y };

		graphics2d.drawPolygon(xs, ys, 3);
		repaint();

		graphics2d.setColor(selectedColor);
	}

	/**
	 * This method is used to insert text written by other clients on the
	 * whiteboard.
	 * 
	 * @param firstPoint
	 * @param text
	 * @param secondaryColor
	 */
	public void createText(Point firstPoint, String text, Color secondaryColor) {
		setSecondaryColor(secondaryColor);

		graphics2d.drawString(text, firstPoint.x, firstPoint.y);
		repaint();

		graphics2d.setColor(selectedColor);
	}

	/**
	 * This method is used to take text input from the user once user selects text
	 * input and clicks on a point in the whiteboard area.
	 * 
	 */
	private void insertText() {
		String inputText = (String) JOptionPane.showInputDialog(this, "Please input text in the below field",
				"Text Input", JOptionPane.PLAIN_MESSAGE, null, null, "input");
		if (StringUtils.isNotEmpty(inputText)) {
			graphics2d.drawString(inputText, oldPoint.x, oldPoint.y);
			repaint();
			sendEvent(new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_DRAW, selectedTool,
					oldPoint, null, null, selectedColor, inputText, null));
		}
	}

	/**
	 * This method is used to perform eraser action done by other clients on the
	 * whiteboard.
	 * 
	 * @param firstPoint
	 * @param secondPoint
	 */
	public void eraserAction(Point firstPoint, Point secondPoint) {
		graphics2d.setColor(Color.WHITE);
		graphics2d.setStroke(new BasicStroke(20));
		graphics2d.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);

		repaint();

		setSelectedColor(selectedColor);
		graphics2d.setStroke(new BasicStroke(1));
	}

	/**
	 * This method is used to clear the whiteboard.
	 */
	public void clear() {
		graphics2d.setPaint(Color.white);
		graphics2d.fillRect(0, 0, getSize().width, getSize().height);
		setSelectedColor(selectedColor);
		repaint();
	}

	/**
	 * This method is used to load an image on the draw area.
	 * 
	 * @param bufferedImage
	 */
	public void loadImage(BufferedImage bufferedImage) {
		clear();
		graphics2d.drawImage(bufferedImage, 0, 0, null);
	}

	/**
	 * This method is used to add events to the queue which are triggered from the
	 * current user.
	 * 
	 * @param actionMessageDto
	 */
	private void sendEvent(ActionMessageDto actionMessageDto) {
		if (uiEnabled) {
			whiteboardClient.getActionList().add(actionMessageDto);
		}
	}

	/**
	 * This method is used to enable UI after the user has been accepted.
	 */
	public void enableUIAfterVerification() {
		uiEnabled = true;
		setEnabled(true);
	}

	public String getSelectedTool() {
		return selectedTool;
	}

	public void setSelectedTool(String selectedTool) {
		this.selectedTool = selectedTool;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(Color selectedColor) {
		this.selectedColor = selectedColor;
		graphics2d.setColor(selectedColor);
	}

	public void setSecondaryColor(Color secondaryColor) {
		if (Objects.nonNull(secondaryColor)) {
			graphics2d.setColor(secondaryColor);
		}
	}

	/**
	 * This method is called whenever the users moves the mouse on the draw area. It
	 * captures the coordinates and displays onto the coordinate bar.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		coordinateBar.setCoordinates(e.getPoint());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
