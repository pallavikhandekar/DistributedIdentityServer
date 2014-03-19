package election_algorithm;

import election_algorithm.Election.MessageType;
import identity.server.IdServer;
import identity.server.UserInfo;

import java.io.Serializable;
import java.util.LinkedList;


public class Message implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private MessageType type;
	private int id;
	private String localHostAddress;
	private LinkedList<UserInfo> listOfUpdatedUsers;
	private String version;


	public Message(int srcId, MessageType srcMessageType, String srcLocalHostAddress) {
		id = srcId;
		type = srcMessageType;
		localHostAddress = srcLocalHostAddress;
	}

	public Message(int srcId, MessageType srcMessageType, String srcLocalHostAddress, IdServer srcObjIdServer) {
		id = srcId;
		type = srcMessageType;
		localHostAddress = srcLocalHostAddress;
		
	}
	
	
	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocalHostAddress() {
		return localHostAddress;
	}

	public void setLocalHostAddress(String localHostAddress) {
		this.localHostAddress = localHostAddress;
	}
	

	public LinkedList<UserInfo> getListOfUpdatedUsers() {
		return listOfUpdatedUsers;
	}

	public void setListOfUpdatedUsers(LinkedList<UserInfo> listOfUpdatedUsers) {
		this.listOfUpdatedUsers = listOfUpdatedUsers;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
