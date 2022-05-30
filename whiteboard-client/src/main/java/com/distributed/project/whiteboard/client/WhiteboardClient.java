package com.distributed.project.whiteboard.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.dto.UserDto;
import com.distributed.project.whiteboard.client.listeners.EventDispatcher;
import com.distributed.project.whiteboard.client.listeners.EventListener;
import com.distributed.project.whiteboard.client.utilities.Constants;

/**
 * This class is used to initialize the whiteboard client and connections to the
 * server. It also initiates the threads for event listener and event
 * dispatcher.
 * 
 * @see {@link EventListener}
 * @see {@link EventDispatcher}
 * 
 * @author Abhijeet - 1278218
 *
 */
public class WhiteboardClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WhiteboardClient.class);

	// Queue used for storing and executing actions done by the current client
	private Queue<ActionMessageDto> actionList = new ConcurrentLinkedQueue<>();

	// Thread pool used to executing listener threads
	private ExecutorService executorService = new ThreadPoolExecutor(5, 10, 100, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());

	// Atomic boolean for maintaining if the current client is manager
	private AtomicBoolean isManager = new AtomicBoolean(false);

	// Synchronized user info, for maintaining identity
	private UserDto userInfo;

	private WhiteBoardUI whiteBoardUI;
	protected Socket socket;
	protected BufferedReader in;
	protected BufferedWriter out;

	/**
	 * This method is the entry point for application startup.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Initializing the client
		WhiteboardClient whiteboardClient = new WhiteboardClient();

		// Checking if the startup arguemnts are correct
		if (Objects.isNull(args) || args.length < 2 || StringUtils.isBlank(args[0])
				|| !StringUtils.isNumeric(args[1])) {
			LOGGER.error("Port number or hostname mentioned in wrong format");
			JOptionPane.showMessageDialog(null, "Startup arguments incorrect - Correct Use: *.jar hostname port");
			System.exit(0);
		}
		// Initializing the whiteboard UI
		whiteboardClient.whiteBoardUI = new WhiteBoardUI(whiteboardClient);

		// Running the whitebord UI in a seperate thread
		SwingUtilities.invokeLater(() -> whiteboardClient.whiteBoardUI.setVisible(true));

		// Starting up client connection to server
		whiteboardClient.clientConfigurationsAndConnection(args[0], Integer.parseInt(args[1]));
	}

	/**
	 * This method is used to start connections with the server and initialize the
	 * listener threads.
	 * 
	 * @param serverAddress
	 * @param port
	 */
	private void clientConfigurationsAndConnection(String serverAddress, int port) {
		try {
			// Opening socket with dictionary server
			socket = new Socket(serverAddress, port);

			// Input connection - messages from server
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

			// Output connection - messages to server
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			// Running thread for event dispatcher
			EventDispatcher eventDispatcher = new EventDispatcher(this, out);
			executorService.execute(eventDispatcher);

			// Running thread for event listener
			EventListener eventListener = new EventListener(this, whiteBoardUI, in, executorService);
			executorService.execute(eventListener);
		} catch (IOException e) {
			LOGGER.error("IOException in clientConfigurations", e);
		}
	}

	/**
	 * This method is used to set the client user name input and create the user
	 * info for the user permission event to assign manager or get permission from
	 * manager.
	 * 
	 * @param clientUserName
	 * @param isUserLogin
	 */
	public void setClientUserName(String clientUserName, boolean isUserLogin) {
		LOGGER.info("Setting the clientUserName {} for current user", clientUserName);

		// Creating user info
		UserDto userDto = new UserDto();
		userDto.setClientUserName(clientUserName);

		// Setting the global variable for user info
		setUserInfo(userDto);

		// If it is coming from user login then send user permission event
		if (isUserLogin) {
			// Creating the user permission event
			ActionMessageDto newUserPermissionEvent = new ActionMessageDto(getUserInfo(),
					Constants.ACTION_NEW_USER_PERMISSION);

			// Adding the event to the action list for dispatching
			actionList.add(newUserPermissionEvent);
		}
	}

	/**
	 * This method is used to fetch the action list to add events.
	 * 
	 * @implNote It is not marked as synchronized since we are using
	 *           {@link ConcurrentLinkedQueue}
	 * @return
	 */
	public Queue<ActionMessageDto> getActionList() {
		return actionList;
	}

	/**
	 * This method is used to fetch if the current client is the manager.
	 * 
	 * @implNote It is not marked as synchronized since the variable is
	 *           {@link AtomicBoolean}
	 * 
	 * @return
	 */
	public boolean getIsManager() {
		return isManager.get();
	}

	/**
	 * This method is used to set the value of isManager field.
	 * 
	 * @implNote It is not marked as synchronized since the variable is
	 *           {@link AtomicBoolean}.
	 * 
	 * @param isManager
	 */
	public void setIsManager(AtomicBoolean isManager) {
		this.isManager = isManager;
	}

	/**
	 * This method is used to fetch the user info set as the global variable.
	 * 
	 * @return
	 */
	public synchronized UserDto getUserInfo() {
		return userInfo;
	}

	/**
	 * This method is used to set the user info in the global variable.
	 * 
	 * @param userInfo
	 */
	public synchronized void setUserInfo(UserDto userInfo) {
		this.userInfo = userInfo;
	}
}
