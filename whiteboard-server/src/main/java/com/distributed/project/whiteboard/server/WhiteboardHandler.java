package com.distributed.project.whiteboard.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.server.dto.ActionMessageDto;
import com.distributed.project.whiteboard.server.dto.UserDto;
import com.distributed.project.whiteboard.server.utils.Constants;
import com.distributed.project.whiteboard.server.utils.TypeConversionUtils;

/**
 * This class is used to handle events triggered by the clients. It maintains
 * the client connection and sends direct or broadcast messages.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class WhiteboardHandler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WhiteboardHandler.class);

	private WhiteboardServer whiteboardServer = null;
	private Socket client = null;
	private Long clientUID = null;
	private UserDto userInfo = null;

	/**
	 * This constructor is used to initialize the client information and the
	 * instance of the server.
	 * 
	 * @param whiteboardServer
	 * @param client
	 * @param clientUID
	 */
	public WhiteboardHandler(WhiteboardServer whiteboardServer, Socket client, Long clientUID) {
		this.whiteboardServer = whiteboardServer;
		this.client = client;
		this.clientUID = clientUID;
	}

	@Override
	public void run() {
		LOGGER.info("Client {}, whiteboard handler started", clientUID);

		// Opening the input and output stream with the client
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {

			// Initializing the event string variable
			String eventString = null;

			// Keeping the thread running while client connection available
			while ((eventString = in.readLine()) != null) {
				// Parsing the request from client
				ActionMessageDto request = TypeConversionUtils.convertToCustomClass(eventString,
						ActionMessageDto.class);
				LOGGER.info("Message from Client {} recieved {}", clientUID, request);

				// Broadcast urgent actions like draw, chat, exit and process other actions
				// normally
				if (Constants.URGENT_BROADCAST_ACTIONS.contains(request.getAction())) {
					handleBroadcastMessages(eventString);
				} else if (Constants.ACTION_EXIT.equalsIgnoreCase(request.getAction())) {
					LOGGER.info("Exit event recieved from User {}", request.getUser().getClientUserName());
					handleUserExit(request);
					break;
				} else {
					handleAdminLogic(request, in, out);
				}
			}
			// Closing client socket on exiting while loop
			client.close();
		} catch (IOException e) {
			LOGGER.error("Exception occured in run method of DictionaryUtils for client {}", clientUID, e);
		}
	}

	/**
	 * This method is used to handle admin events like new user permission, new user
	 * accept and reject, user removed and load image.
	 * 
	 * @param request
	 * @param in
	 * @param out
	 */
	private void handleAdminLogic(ActionMessageDto request, BufferedReader in, BufferedWriter out) {
		String action = request.getAction();
		switch (action) {
		case Constants.ACTION_NEW_USER_PERMISSION:
			handleUserAllocation(request, in, out);
			break;
		case Constants.ACTION_NEW_USER_ACCEPT:
			handleUserAccept(request);
			break;
		case Constants.ACTION_NEW_USER_REJECT:
			handleUserReject(request);
			break;
		case Constants.ACTION_USER_KICK:
			handleUserKick(request);
			break;
		case Constants.ACTION_LOAD_IMAGE:
			handleLoadImage(request);
			break;
		default:
			break;
		}
	}

	/**
	 * This method is used to allocate first user as the manager and the following
	 * user's request should be sent to manager for accept/reject. It assigns the
	 * manager UID and populates the clientInfoMap and unverifiedClientMap based on
	 * clients.
	 * 
	 * @param request
	 * @param in
	 * @param out
	 */
	private void handleUserAllocation(ActionMessageDto request, BufferedReader in, BufferedWriter out) {
		// Checking if first user then allocate as manager
		boolean isManager = MapUtils.isEmpty(whiteboardServer.getClientInfoMap());
		if (isManager) {
			LOGGER.info("Manager allocation to user id - {}, username - {}", clientUID,
					request.getUser().getClientUserName());

			// Creating UserDto for manager and assigning to userInfo variable
			UserDto managerUserDto = new UserDto(clientUID, request.getUser().getClientUserName(), isManager, in, out);
			userInfo = managerUserDto;

			// Adding manager to client info map
			whiteboardServer.getClientInfoMap().put(clientUID, managerUserDto);

			// Setting the manager client UID global variable
			whiteboardServer.setManagerClientUID(new AtomicLong(clientUID));

			// Sending the manager assign event to first user
			ActionMessageDto managerAssignEvent = new ActionMessageDto(managerUserDto, Constants.ACTION_ASSIGN_MANAGER);
			managerAssignEvent.setActiveUserList(new ArrayList<>(whiteboardServer.getClientInfoMap().values()));

			// Sending direct message to the manager/first client
			sendMessage(TypeConversionUtils.convertObjectToString(managerAssignEvent), out);
		} else {
			LOGGER.info("Add user permission for user id - {}, username - {}", this.clientUID,
					request.getUser().getClientUserName());

			// Creating UserDto for the requesting client and assinging to userInfo variable
			UserDto clientUser = new UserDto(this.clientUID, request.getUser().getClientUserName(), false, in, out);
			userInfo = clientUser;

			// Adding the client information to unverified client map since manager has to
			// accept the request
			whiteboardServer.getUnverifiedClients().put(clientUID, clientUser);

			// Creating manager permission event
			ActionMessageDto managerPermissionEvent = new ActionMessageDto(clientUser,
					Constants.ACTION_NEW_USER_PERMISSION);
			managerPermissionEvent.setSelectedUser(clientUser);

			// Sending direct message to manager for permission
			sendMessageToManager(TypeConversionUtils.convertObjectToString(managerPermissionEvent));
		}
	}

	/**
	 * This methed is used to handle events when manager accepts a join request. It
	 * allocates user to the clientInfoMap and removes it from unverifiedClientMap.
	 * Then it broadcasts the new user added event to other clients.
	 * 
	 * @param request
	 */
	private void handleUserAccept(ActionMessageDto request) {
		// Fetching the accepted user info from unverifiedClientMap
		UserDto acceptedUser = whiteboardServer.getUnverifiedClients().get(request.getSelectedUser().getClientUID());

		// Adding the user to the client info map
		whiteboardServer.getClientInfoMap().put(acceptedUser.getClientUID(), acceptedUser);

		// Removing the user from unverified client map
		whiteboardServer.getUnverifiedClients().remove(acceptedUser.getClientUID());

		// Creating the new user added event and setting list of active clients
		ActionMessageDto newUserAddedEvent = new ActionMessageDto(userInfo, Constants.ACTION_NEW_USER_ADDED);
		newUserAddedEvent.setSelectedUser(acceptedUser);
		newUserAddedEvent.setActiveUserList(new ArrayList<>(whiteboardServer.getClientInfoMap().values()));

		// Broadcasting the event to all active clients
		handleBroadcastMessages(TypeConversionUtils.convertObjectToString(newUserAddedEvent));
	}

	/**
	 * This method is used to handle event when manager rejects a join request. It
	 * removes the user from unverified client map and sends a reject message
	 * directly to the rejected user.
	 * 
	 * @param request
	 */
	private void handleUserReject(ActionMessageDto request) {
		// Fetching the rejected user info from the unverified client map
		UserDto rejectedUser = whiteboardServer.getUnverifiedClients().get(request.getSelectedUser().getClientUID());

		// Removing the user from the unverified client map
		whiteboardServer.getUnverifiedClients().remove(rejectedUser.getClientUID());

		// Sending reject event directly to the rejected user
		sendMessage(TypeConversionUtils.convertObjectToString(request), rejectedUser.getOut());
	}

	/**
	 * This method is used to handle the kick user event from manager once manager
	 * selects a users and removes it. It updates the client info map and broadcast
	 * the event for other clients to show system messages and update active client
	 * list.
	 * 
	 * @param request
	 */
	private void handleUserKick(ActionMessageDto request) {
		// Fetching the kicked user info
		UserDto kickedUser = request.getSelectedUser();

		// Broadcasting kicked user event for kicked client to exit and other clients to
		// update client list
		handleBroadcastMessages(TypeConversionUtils.convertObjectToString(request));

		// Removing kicked user after broadcasting event
		whiteboardServer.getClientInfoMap().remove(kickedUser.getClientUID());
	}

	/**
	 * This method is used handle exit of manager and normal clients. If manager is
	 * exiting then sending event to all clients to exit. If a user is exiting
	 * themselves and not exiting due to kick event then sending refresh user list
	 * event to all remaining clients to update active client list.
	 * 
	 * @param request
	 */
	private void handleUserExit(ActionMessageDto request) {
		// Manager exiting event
		if (whiteboardServer.getManagerClientUID().equals(request.getUser().getClientUID())) {
			LOGGER.info("Manager Exiting");

			// Remove manager from exit client map
			whiteboardServer.getClientInfoMap().remove(request.getUser().getClientUID());

			// Send force quit event to remaining clients
			ActionMessageDto managerExitEvent = new ActionMessageDto(userInfo, Constants.ACTION_FORCE_QUIT);

			// Broadcasting event to remaning clients
			handleBroadcastMessages(TypeConversionUtils.convertObjectToString(managerExitEvent));
		} else if (whiteboardServer.getClientInfoMap().containsKey(request.getUser().getClientUID())) {
			// Removing user from client info map
			whiteboardServer.getClientInfoMap().remove(request.getUser().getClientUID());

			// Creating refresh user list event to update new client list
			ActionMessageDto refreshUserListEvent = new ActionMessageDto(userInfo, Constants.ACTION_REFRESH_USER_LIST);
			refreshUserListEvent.setActiveUserList(new ArrayList<>(whiteboardServer.getClientInfoMap().values()));
			refreshUserListEvent.setSelectedUser(request.getUser());

			// Sending refresh user list event to remaining clients
			handleBroadcastMessages(TypeConversionUtils.convertObjectToString(refreshUserListEvent));
		}
	}

	/**
	 * This method is used to load image sent by the manager. It is called in 2
	 * cases. One when a new user joins the whiteboard to populate that client's
	 * whiteboard with current image. Second, when a manager loads a new image from
	 * local directory.
	 * 
	 * @param request
	 */
	private void handleLoadImage(ActionMessageDto request) {
		// If selected user is non null, then user joined fresh and only update the
		// whiteboard of that user
		if (Objects.nonNull(request.getSelectedUser())) {
			// Fetching userInfo of new user
			UserDto selectedUser = whiteboardServer.getClientInfoMap().get(request.getSelectedUser().getClientUID());

			// Sending load image event to that specific user
			sendMessage(TypeConversionUtils.convertObjectToString(request), selectedUser.getOut());
		} else {
			// Manager loaded image from local, update all client's whiteboard
			handleBroadcastMessages(TypeConversionUtils.convertObjectToString(request));
		}
	}

	/**
	 * This method is used to send direct event to manager.
	 * 
	 * @param eventString
	 */
	private void sendMessageToManager(String eventString) {
		sendMessage(eventString,
				whiteboardServer.getClientInfoMap().get(whiteboardServer.getManagerClientUID()).getOut());
	}

	/**
	 * This method is used to send message to a specific client.
	 * 
	 * @param eventString
	 * @param out
	 */
	private void sendMessage(String eventString, BufferedWriter out) {
		try {
			out.write(eventString + StringUtils.LF);
			out.flush();
		} catch (Exception e) {
			LOGGER.error("Exception in sendMessage", e);
		}
	}

	/**
	 * This method is used to send an event to all clients except the one triggering
	 * the event to stop duplication.
	 * 
	 * @param eventString
	 */
	private void handleBroadcastMessages(String eventString) {
		// Iterating over the client info map
		for (Map.Entry<Long, UserDto> entry : whiteboardServer.getClientInfoMap().entrySet()) {
			// Checking if the iterated user is not itself
			if (!entry.getKey().equals(clientUID)) {
				try {
					LOGGER.info("Sending event to {}", entry.getKey());
					entry.getValue().getOut().write(eventString + StringUtils.LF);
					entry.getValue().getOut().flush();
				} catch (Exception e) {
					LOGGER.error("Exception while sending request to client {}", entry.getKey(), e);
				}
			}
		}
	}
}
