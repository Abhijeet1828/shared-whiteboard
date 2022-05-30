package com.distributed.project.whiteboard.client.dto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is used as a DTO, for transferring user information to server and
 * other clients.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class UserDto implements Serializable {

	private static final long serialVersionUID = -2065818129828511167L;

	private Long clientUID;

	private String clientUserName;

	private boolean isManager;

	@JsonIgnore
	private BufferedReader in;

	@JsonIgnore
	private BufferedWriter out;

	public Long getClientUID() {
		return clientUID;
	}

	public void setClientUID(Long clientUID) {
		this.clientUID = clientUID;
	}

	public String getClientUserName() {
		return clientUserName;
	}

	public void setClientUserName(String clientUserName) {
		this.clientUserName = clientUserName;
	}

	public boolean isManager() {
		return isManager;
	}

	public void setManager(boolean isManager) {
		this.isManager = isManager;
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

	public BufferedWriter getOut() {
		return out;
	}

	public void setOut(BufferedWriter out) {
		this.out = out;
	}

	public UserDto(Long clientUID, String clientUserName, boolean isManager, BufferedReader in, BufferedWriter out) {
		super();
		this.clientUID = clientUID;
		this.clientUserName = clientUserName;
		this.isManager = isManager;
		this.in = in;
		this.out = out;
	}

	public UserDto() {
		super();
	}

	@Override
	public String toString() {
		return clientUserName + "-" + clientUID;
	}

}
