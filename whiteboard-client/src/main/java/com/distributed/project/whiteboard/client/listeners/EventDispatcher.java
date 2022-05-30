package com.distributed.project.whiteboard.client.listeners;

import java.io.BufferedWriter;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.distributed.project.whiteboard.client.WhiteboardClient;
import com.distributed.project.whiteboard.client.dto.ActionMessageDto;
import com.distributed.project.whiteboard.client.utilities.TypeConversionUtils;

/**
 * This runnable class is used to dispatch the events produced by the current
 * client.
 * 
 * @implNote We have used a seperate thread to dispatch events so as to not
 *           block other I/O inputs from the users.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class EventDispatcher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcher.class);

	protected WhiteboardClient whiteboardClient;
	protected BufferedWriter out;

	/**
	 * This constructor is used to {@link WhiteboardClient} and
	 * {@link BufferedWriter}.
	 * 
	 * @param whiteboardClient
	 * @param out
	 */
	public EventDispatcher(WhiteboardClient whiteboardClient, BufferedWriter out) {
		this.whiteboardClient = whiteboardClient;
		this.out = out;
	}

	/**
	 * Run method of {@link EventDispatcher}, running in an infinite and dispatching
	 * events as soon as they are added to the queue.
	 */
	@Override
	public void run() {
		while (true) {
			dispatchEvents();
		}
	}

	/**
	 * This method is used to fetch the events produced by the current client and
	 * send it to the server.
	 */
	private void dispatchEvents() {
		try {
			// Polling the queue for events
			ActionMessageDto newActionDto = whiteboardClient.getActionList().poll();

			// If polled event is not null
			if (Objects.nonNull(newActionDto)) {
				// Send it to the server
				out.write(TypeConversionUtils.convertObjectToString(newActionDto) + StringUtils.LF);
				out.flush();
			}
		} catch (Exception e) {
			LOGGER.error("Exception in run() method of EventDispatcher", e);
		}
	}
}
