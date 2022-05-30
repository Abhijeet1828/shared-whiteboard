package com.distributed.project.whiteboard.server.utils;

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

	// URGENT BROADCAST MESSAGES
	public static final ImmutableList<String> URGENT_BROADCAST_ACTIONS = ImmutableList.of(ACTION_DRAW, ACTION_CHAT,
			ACTION_SYSTEM_CHAT, ACTION_CLEAR);

}
