package com.distributed.project.whiteboard.client.listeners;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.WhiteBoardUI;
import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.gui.ChatBoxPanel;
import com.distributed.project.whiteboard.client.gui.DrawArea;
import com.distributed.project.whiteboard.client.gui.ToolPanel;
import com.distributed.project.whiteboard.client.gui.UserPanel;
import com.distributed.project.whiteboard.client.utilities.Constants;
import com.distributed.project.whiteboard.client.utilities.TypeConversionUtils;

/**
 * This runnable class is used to listen to the events produced by other clients
 * and execute them according to the actions.
 * 
 * @implNote We are using a separate thread to listen to events, so that we dont
 *           miss urgent events and stop execution of other I/O operations done
 *           by the user.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class EventListener implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

	private WhiteboardClient whiteboardClient;
	private WhiteBoardUI whiteBoardUI;
	private BufferedReader in;
	private DrawArea drawArea;
	private ChatBoxPanel chatBoxPanel;
	private UserPanel userPanel;
	private ToolPanel toolPanel;
	private ExecutorService executorService;

	/**
	 * This constructor is used to initialize the instances of classes and variables
	 * being used in this runnable class.
	 * 
	 * @param whiteboardClient
	 * @param whiteBoardUI
	 * @param bufferedReader
	 * @param executorService
	 */
	public EventListener(WhiteboardClient whiteboardClient, WhiteBoardUI whiteBoardUI, BufferedReader bufferedReader,
			ExecutorService executorService) {
		this.whiteboardClient = whiteboardClient;
		this.whiteBoardUI = whiteBoardUI;
		this.in = bufferedReader;
		this.drawArea = whiteBoardUI.getDrawArea();
		this.chatBoxPanel = whiteBoardUI.getChatBoxPanel();
		this.userPanel = whiteBoardUI.getUserPanel();
		this.toolPanel = whiteBoardUI.getToolPanel();
		this.executorService = executorService;
	}

	/**
	 * The run method runs in an infinite loop to keep listening to the incoming
	 * events.
	 */
	@Override
	public void run() {
		while (true) {
			listenEvents();
		}
	}

	/**
	 * This method listens to the input stream and acts accordingly if it recevies
	 * any events from the server.
	 */
	private void listenEvents() {
		try {
			// Reading the input stream
			String eventString = in.readLine();

			// If the event received is not blank
			if (StringUtils.isNotBlank(eventString)) {

				// Convert the event to the action message dto
				ActionMessageDto actionMessageDto = TypeConversionUtils.convertToCustomClass(eventString,
						ActionMessageDto.class);

				// If the event is free hand draw / or eraser then immediately work on it
				if (actionMessageDto.getAction().equalsIgnoreCase(Constants.ACTION_DRAW)
						&& actionMessageDto.getTool().equalsIgnoreCase(Constants.TOOL_PENCIL)) {
					// Setting the current editor
					toolPanel.setEditor(actionMessageDto.getUser().getClientUserName());

					// Draw free hand drawing based on coordinates
					drawArea.createLine(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint(),
							actionMessageDto.getColor());
				} else if (actionMessageDto.getAction().equalsIgnoreCase(Constants.ACTION_DRAW)
						&& actionMessageDto.getTool().equalsIgnoreCase(Constants.TOOL_ERASER)) {
					// Setting the current editor
					toolPanel.setEditor(actionMessageDto.getUser().getClientUserName());

					// Erase the drawing based on coordinates
					drawArea.eraserAction(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint());
				} else {
					// If not immediate events then handle seperately
					eventSeggregrator(actionMessageDto);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Exception in run() method of DrawEventListener", e);
		}
	}

	/**
	 * This method is used to process the non-immediate events received by the
	 * client and perform actions accordingly.
	 * 
	 * @param actionMessageDto
	 */
	private void eventSeggregrator(ActionMessageDto actionMessageDto) {
		String action = actionMessageDto.getAction();
		switch (action) {
		case Constants.ACTION_DRAW:
			drawAction(actionMessageDto);
			break;
		case Constants.ACTION_CHAT:
			chatBoxPanel.recieveMessage(actionMessageDto);
			break;
		case Constants.ACTION_ASSIGN_MANAGER:
			handleManagerAssign(actionMessageDto);
			break;
		case Constants.ACTION_NEW_USER_PERMISSION:
			// Running user permission event in seperate thread due to user input block,
			// other events should work normally in background
			executorService.execute(() -> handleUserAddPermission(actionMessageDto));
			break;
		case Constants.ACTION_NEW_USER_ADDED:
			// Running user added event in seperate thread since for new user blocking
			// dialog is shown, and other events should work in background
			executorService.execute(() -> handleUserAdded(actionMessageDto));
			break;
		case Constants.ACTION_NEW_USER_REJECT:
			handleUserRejected(actionMessageDto);
			break;
		case Constants.ACTION_USER_KICK:
			handleKickUserEvent(actionMessageDto);
			break;
		case Constants.ACTION_REFRESH_USER_LIST:
			handleRefreshUserList(actionMessageDto);
			break;
		case Constants.ACTION_LOAD_IMAGE:
			handleLoadImageEvent(actionMessageDto);
			break;
		case Constants.ACTION_CLEAR:
			// Setting the current editor
			toolPanel.setEditor(actionMessageDto.getUser().getClientUserName());
			// Appending the system message in chat box panel
			chatBoxPanel.append(Constants.MSG_CLEAR, Color.RED);
			// Clearing the draw area
			drawArea.clear();
			break;
		case Constants.ACTION_FORCE_QUIT:
			handleForceQuit();
			break;
		default:
			LOGGER.error("No such action defined -- {}", action);
			break;
		}
	}

	/**
	 * This method is used to work on draw actions other than free hand draw and
	 * eraser actions. It seggregrates the tool used by the other clients and
	 * accordingly makes them.
	 * 
	 * @param actionMessageDto
	 */
	private void drawAction(ActionMessageDto actionMessageDto) {
		// Setting the current editor
		toolPanel.setEditor(actionMessageDto.getUser().getClientUserName());
		String tool = actionMessageDto.getTool();
		switch (tool) {
		case Constants.TOOL_LINE:
			// Creating a line using selected color and coordinates
			drawArea.createLine(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint(),
					actionMessageDto.getColor());
			break;
		case Constants.TOOL_RECTANGLE:
			// Creating a rectangle using selected color and coordinates
			drawArea.createRectangle(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint(),
					actionMessageDto.getColor());
			break;
		case Constants.TOOL_CIRCLE:
			// Creating a circle using selected color and coordinates
			drawArea.createCircle(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint(),
					actionMessageDto.getColor());
			break;
		case Constants.TOOL_TRIANGLE:
			// Creating a triangle using selected color and coordinates
			drawArea.createTriangle(actionMessageDto.getStartPoint(), actionMessageDto.getEndPoint(),
					actionMessageDto.getDragPoint(), actionMessageDto.getColor());
			break;
		case Constants.TOOL_TEXT:
			// Drawing text on board using selected color and coordinates
			drawArea.createText(actionMessageDto.getStartPoint(), actionMessageDto.getDrawText(),
					actionMessageDto.getColor());
			break;
		default:
			LOGGER.error("No such tool defined -- {}", tool);
			break;
		}
	}

	/**
	 * This method is invoked when client receives the ASSIGN_MANAGER event. It
	 * assigns the current user as the manager and enables UI according to the
	 * manager.
	 * 
	 * @param actionMessageDto
	 */
	private void handleManagerAssign(ActionMessageDto actionMessageDto) {
		LOGGER.info("Assigning current user {} as manager", actionMessageDto.getUser());

		// Setting the isManager global variable
		whiteboardClient.setIsManager(new AtomicBoolean(true));

		// Setting the userInfo global variable
		whiteboardClient.setUserInfo(actionMessageDto.getUser());

		// Enabling the manager UI
		whiteBoardUI.enableUserUI();

		// Refershing the list of active users
		userPanel.refreshUserList(actionMessageDto.getActiveUserList());
	}

	/**
	 * This method is invoked when manager receives the NEW_USER_PERMISSION event
	 * which is a join request by the new client trying to join the whiteboard.
	 * 
	 * @implNote Runs on separate thread due to blocking user input I/O
	 * 
	 * @param actionMessageDto
	 */
	private void handleUserAddPermission(ActionMessageDto actionMessageDto) {
		LOGGER.info("Manager received join request from user {}", actionMessageDto.getSelectedUser());

		// Invokes the join request pop-up to manager
		whiteBoardUI.userPermission(actionMessageDto);
	}

	/**
	 * This method is invoked when user recieves the NEW_USER_ADDED event. If the
	 * user added is the current user then we activate the client UI, or else we
	 * update the client list and add system message in the chat box panel.
	 * 
	 * @implNote Runs on separate thread due to blocking user input I/O
	 * 
	 * @param request
	 */
	private void handleUserAdded(ActionMessageDto request) {
		LOGGER.info("New user added - {}", request.getSelectedUser());

		// If added user is the current user
		if (request.getSelectedUser().getClientUserName()
				.equalsIgnoreCase(whiteboardClient.getUserInfo().getClientUserName())) {
			// Setting the userInfo global variable
			whiteboardClient.setUserInfo(request.getSelectedUser());

			// Enabling the normal user UI
			whiteBoardUI.enableUserUI();

			// Showing success message to current user
			whiteBoardUI.showSuccessMessage("Manager has accepted your request to join the whiteboard");
		}

		// Refreshing the active user list
		userPanel.refreshUserList(request.getActiveUserList());

		// Adding user added system message in the chat box panel
		chatBoxPanel
				.append(Constants.MSG_USER_ADDED.replace(Constants.USER, request.getSelectedUser().getClientUserName())
						+ StringUtils.LF, Color.RED);
	}

	/**
	 * This method is invoked when user receives the REFRESH_USER_LIST event. This
	 * event is triggered when a user exits the whiteboard. It is used to refresh
	 * the active client list.
	 * 
	 * @param request
	 */
	private void handleRefreshUserList(ActionMessageDto request) {
		LOGGER.info("User exited - {}", request.getSelectedUser());

		// Refereshing the active user list
		userPanel.refreshUserList(request.getActiveUserList());

		// Adding the user exit system message in the chat box panel
		chatBoxPanel.append(
				Constants.MSG_USER_EXIT.replace(Constants.USER, request.getSelectedUser().getClientUserName()),
				Color.RED);
	}

	/**
	 * This method is invoked when user receives the NEW_USER_REJECT event. This
	 * event is triggered when the manager rejects the join request of the user.
	 * 
	 * @param request
	 */
	private void handleUserRejected(ActionMessageDto request) {
		LOGGER.info("Current user rejected by the manager");

		// Creating the user exit event, to remove connection from the server end
		ActionMessageDto userExitEvent = new ActionMessageDto(request.getSelectedUser(), Constants.ACTION_EXIT);

		// Adding the event to the queue
		whiteboardClient.getActionList().add(userExitEvent);

		// Showing error message on the user's screen
		whiteBoardUI.showErrorMessage("Manager has rejected your request to join the whiteboard", true);
	}

	/**
	 * This method is invoked when user recives the USER_KICK event. If the kicked
	 * user is the current user then the current user sends the exit event and exits
	 * the application. If the kicked user is not the current user then we remove
	 * the user from active client list and display system message in the chat box
	 * panel.
	 * 
	 * @param request
	 */
	private void handleKickUserEvent(ActionMessageDto request) {
		LOGGER.info("User kicked by manager - {}", request.getSelectedUser());

		// If kicked user is the current user
		if (request.getSelectedUser().getClientUserName()
				.equalsIgnoreCase(whiteboardClient.getUserInfo().getClientUserName())) {
			// Creating the user exit event, to remove connection from the server end
			ActionMessageDto userExitEvent = new ActionMessageDto(request.getSelectedUser(), Constants.ACTION_EXIT);

			// Adding event to the queue
			whiteboardClient.getActionList().add(userExitEvent);

			// Showing error message on the user's screen
			whiteBoardUI.showErrorMessage("Manager has removed you from the whiteboard" + StringUtils.LF, true);
		} else {
			// If not current user, then remove user from the active client list
			userPanel.removeUserFromList(request.getSelectedUser());

			// Adding system message to chat box panel
			chatBoxPanel.append(
					Constants.MSG_KICK_USER.replace(Constants.USER, request.getSelectedUser().getClientUserName()),
					Color.RED);
		}
	}

	/**
	 * This method is invoked when user receives the LOAD_IMAGE event. It is
	 * triggered in 2 scenarios. One where the user has newly joined the system and
	 * server sends the current draw area to synchronize the client. Second where
	 * the manager loads an image from the local onto the draw area and then this
	 * event synchronizes the image on all clients.
	 * 
	 * @param request
	 */
	private void handleLoadImageEvent(ActionMessageDto request) {
		LOGGER.info("Manager {} loaded a new image", request.getUser());

		// Setting the current editor
		toolPanel.setEditor(request.getUser().getClientUserName());

		// Loading the image sent by manager
		whiteBoardUI.loadImageFromServer(request);

		// If it is not meant for a particular user then display system message
		if (Objects.isNull(request.getSelectedUser())) {
			LOGGER.info("Manager loaded a new image on whiteboard");
			chatBoxPanel.append(Constants.MSG_LOAD_IMAGE, Color.RED);
		}
	}

	/**
	 * This method is invoked when the user receives the FORCE_QUIT event. It is
	 * triggered when manager exits the whiteboard and instructs other clients to
	 * close the current session.
	 */
	private void handleForceQuit() {
		LOGGER.info("Manager has exited the whiteboard");

		// Sending exit message to the server to close connections
		ActionMessageDto userForceQuit = new ActionMessageDto(whiteboardClient.getUserInfo(), Constants.ACTION_EXIT);

		// Adding the event to the queue
		whiteboardClient.getActionList().add(userForceQuit);

		// Showing error message to the client
		whiteBoardUI.showErrorMessage("Manager has closed the current session", true);
	}

}
