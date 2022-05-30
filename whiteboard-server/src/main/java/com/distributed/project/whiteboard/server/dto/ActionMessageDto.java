package com.distributed.project.whiteboard.server.dto;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * This class is used as a DTO, to transfer data between server and client.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class ActionMessageDto implements Serializable {

	private static final long serialVersionUID = -3860692784004659771L;

	private UserDto user;

	private String action;

	private String tool;

	private Point startPoint;

	private Point endPoint;

	private Point dragPoint;

	private Color color;

	private String drawText;

	private String chatMessage;

	private UserDto selectedUser;

	private List<UserDto> activeUserList;

	private String drawboardImage;

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}

	public Point getDragPoint() {
		return dragPoint;
	}

	public void setDragPoint(Point dragPoint) {
		this.dragPoint = dragPoint;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getDrawText() {
		return drawText;
	}

	public void setDrawText(String drawText) {
		this.drawText = drawText;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}

	public UserDto getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserDto selectedUser) {
		this.selectedUser = selectedUser;
	}

	public List<UserDto> getActiveUserList() {
		return activeUserList;
	}

	public void setActiveUserList(List<UserDto> activeUserList) {
		this.activeUserList = activeUserList;
	}

	public String getDrawboardImage() {
		return drawboardImage;
	}

	public void setDrawboardImage(String drawboardImage) {
		this.drawboardImage = drawboardImage;
	}

	public ActionMessageDto(UserDto user, String action, String tool, Point startPoint, Point endPoint, Point dragPoint,
			Color color, String drawText, String chatMessage) {
		super();
		this.user = user;
		this.action = action;
		this.tool = tool;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.dragPoint = dragPoint;
		this.color = color;
		this.drawText = drawText;
		this.chatMessage = chatMessage;
	}

	public ActionMessageDto(UserDto user, String action) {
		super();
		this.user = user;
		this.action = action;
	}
	
	public ActionMessageDto() {
		super();
	}

	@Override
	public String toString() {
		return "ActionMessageDto [user=" + user + ", action=" + action + ", tool=" + tool + ", startPoint=" + startPoint
				+ ", endPoint=" + endPoint + ", dragPoint=" + dragPoint + ", color="
				+ (Objects.nonNull(color) ? String.valueOf(color.getRGB()) : "null") + ", drawText=" + drawText
				+ ", chatMessage=" + chatMessage + ", selectedUser=" + selectedUser + ", activeUserList="
				+ activeUserList + ", drawboardImage=" + drawboardImage + "]";
	}

}