package com.distributed.project.whiteboard.client.utilities;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

/**
 * This class is used to store action constants, for maintaining similarity
 * between client and server.
 * 
 * @author Abhijeet - 1278218
 *
 */
public final class Constants {

	private Constants() {
		throw new IllegalStateException("Constants class cannot be instantiated");
	}

	// FILE CONSTANTS
	public static final String FILE_NEW = "NEW";
	public static final String FILE_OPEN = "OPEN";
	public static final String FILE_SAVE = "SAVE";
	public static final String FILE_SAVE_AS = "SAVE AS";

	// FONT CONSTANTS
	public static final String FONT_LUCIDA_GRANDE = "Lucida Grande";

	// BACKGROUND COLOR CONSTANTS
	public static final Color MANAGER_UI_COLOR = new Color(240, 128, 128);
	public static final Color GUEST_UI_COLOR = new Color(176, 224, 230);

	// TOOL ACTION CONSTANTS
	public static final String TOOL_PENCIL = "PENCIL";
	public static final String TOOL_ERASER = "ERASER";
	public static final String TOOL_LINE = "LINE";
	public static final String TOOL_CIRCLE = "CIRCLE";
	public static final String TOOL_RECTANGLE = "RECTANGLE";
	public static final String TOOL_TRIANGLE = "TRIANGLE";
	public static final String TOOL_TEXT = "TEXT";
	public static final String TOOL_COLOR = "COLOR";

	public static final ImmutableList<String> FREE_HAND_TOOLS = ImmutableList.of(TOOL_PENCIL, TOOL_ERASER);

	// ACTION CONSTANTS
	public static final String ACTION_DRAW = "DRAW";
	public static final String ACTION_CHAT = "CHAT";
	public static final String ACTION_SYSTEM_CHAT = "SYSTEM_CHAT";
	public static final String ACTION_NEW_USER_PERMISSION = "NEW_USER_PERMISSION";
	public static final String ACTION_NEW_USER_ACCEPT = "NEW_USER_ACCEPT";
	public static final String ACTION_NEW_USER_REJECT = "NEW_USER_REJECT";
	public static final String ACTION_USER_KICK = "USER_KICK";
	public static final String ACTION_ASSIGN_MANAGER = "ASSIGN_MANAGER";
	public static final String ACTION_NEW_USER_ADDED = "NEW_USER_ADDED";
	public static final String ACTION_EXIT = "EXIT";
	public static final String ACTION_REFRESH_USER_LIST = "REFRESH_USER_LIST";
	public static final String ACTION_LOAD_IMAGE = "LOAD_IMAGE";
	public static final String ACTION_CLEAR = "CLEAR";
	public static final String ACTION_FORCE_QUIT = "FORCE_QUIT";

	// REPLACEABLES
	public static final String USER = "<<USER>>";

	// SYSTEM MESSAGES
	public static final String MSG_LOAD_IMAGE = "Manager has loaded a new image on the whiteboard" + StringUtils.LF;
	public static final String MSG_LOAD_IMAGE_ERROR = "Unable to load image. Please check the file and try again!!"
			+ StringUtils.LF;
	public static final String MSG_KICK_USER = USER + " has been removed by the manager" + StringUtils.LF;
	public static final String MSG_USER_ADDED = USER + " has joined the whiteboard" + StringUtils.LF;
	public static final String MSG_USER_EXIT = USER + " has exited the whiteboard" + StringUtils.LF;
	public static final String MSG_CLEAR = "Manager has cleared the whiteboard" + StringUtils.LF;
	public static final String MSG_FILE_SAVED = "Draw area saved to file" + StringUtils.LF;
	public static final String MSG_FILE_SAVED_ERROR = "Unable to save draw area to file. Please try again!!"
			+ StringUtils.LF;
}
