package com.distributed.project.whiteboard.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.server.dto.UserDto;

/**
 * This class is used to intialize the UI and the server which waits for clients
 * to connect to the whiteboard.
 * 
 * 
 * @author Abhijeet - 1278218
 *
 */
public class WhiteboardServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(WhiteboardServer.class);

	// To track active clients
	private Map<Long, UserDto> clientInfoMap = new ConcurrentHashMap<>();

	// To track unverified clients
	private Map<Long, UserDto> unverifiedClients = new ConcurrentHashMap<>();

	// Track manager client id
	private AtomicLong managerClientUID;

	public static void main(String[] args) {
		try {
			// Creating an instance of the whiteboard server
			WhiteboardServer whiteboardServer = new WhiteboardServer();

			// Checking if the port number mentioned is correct
			if (Objects.isNull(args) || args.length == 0 || !StringUtils.isNumeric(args[0])) {
				LOGGER.error("Port number incorrect");
				JOptionPane.showMessageDialog(null, "Port number not mentioned or contains characters!!");
				System.exit(0);
			}

			// Handling server socket connections
			whiteboardServer.serverConfigurations(Integer.parseInt(args[0]));
		} catch (Exception e) {
			LOGGER.error("Exception in main method", e);
			JOptionPane.showMessageDialog(null, "Unexpected error while running whiteboard server. Please try again!!");
		}
	}

	/**
	 * This method is used to open a server socket and wait for client connections.
	 * Once client gets connected, then it assigns the client to particular thread
	 * for furthur operations.
	 * 
	 * @param port
	 */
	private void serverConfigurations(int port) {
		// Creating an executor service to assign threads to different clients
		ExecutorService executorService = new ThreadPoolExecutor(5, 10, 100, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());

		// Client socket initialization
		Socket clientSocket = null;

		// To track no of clients
		int noOfClients = 0;

		// Starting the server socket and running in infinite loop
		try (ServerSocket server = new ServerSocket(port)) {
			while (true) {
				LOGGER.info("Waiting for connection on port {}", port);

				// Accepting client requests on port
				clientSocket = server.accept();

				// Increasing no of clients once a client gets connected
				noOfClients++;

				// Creating an instance of whiteboard handler and assigning to the client
				WhiteboardHandler whiteboardHandler = new WhiteboardHandler(this, clientSocket,
						RandomUtils.nextLong(100000, 999999));

				// Executing the thread
				executorService.execute(whiteboardHandler);

				LOGGER.info("Connection estabilished with Client Number {} - Hostname {}, Local Port {}", noOfClients,
						clientSocket.getInetAddress().getHostName(), clientSocket.getLocalPort());
			}
		} catch (Exception e) {
			LOGGER.error("Exception in serverConfiguration", e);
		}
	}

	/**
	 * This method is used to fetch the client info map.
	 * 
	 * @return
	 */
	public Map<Long, UserDto> getClientInfoMap() {
		return clientInfoMap;
	}

	/**
	 * This method is used to fetch the unverified client map.
	 * 
	 * @return
	 */
	public Map<Long, UserDto> getUnverifiedClients() {
		return unverifiedClients;
	}

	/**
	 * This method is used to fetch the manager client UID.
	 * 
	 * @return
	 */
	public Long getManagerClientUID() {
		return managerClientUID.get();
	}

	/**
	 * This method is used to set the manager client UID once the first client gets
	 * connected.
	 * 
	 * @param managerClientUID
	 */
	public void setManagerClientUID(AtomicLong managerClientUID) {
		this.managerClientUID = managerClientUID;
	}
}
