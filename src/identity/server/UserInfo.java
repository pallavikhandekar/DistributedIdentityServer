package identity.server;

import java.io.Serializable;
import java.util.UUID;

public class UserInfo implements Serializable {

	/**
	 * Unique id for user.
	 */
	private UUID uuid;
	/**
	 * User name.
	 */
	private String loginName;
	/**
	 * Password of user login.
	 */
	private String password;
	/**
	 * Real name of User.
	 */
	private String realName;
	/**
	 * IP address of Client machine which created user.
	 */
	private String IPAddress;
	/**
	 * Create date of user name.
	 */
	private String createDate;
	/**
	 * Modified date of user.
	 */
	private String lastModifiedDate;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserInfo(UUID srcUUID, String srcLoginName, String srcPassword, String srcRealName, String srcIPAddress) {
		super();
		this.uuid = srcUUID;
		this.loginName = srcLoginName;
		this.password = srcPassword;
		this.realName = srcRealName;
		this.IPAddress = srcIPAddress;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
  @Override
	public String toString()
	{
		 return ("UUID: " + getUuid() + " Login Name: " + getLoginName() + " Real Name: " + getRealName() + " IPAddress: " + getIPAddress() + " Created Date: " + getCreateDate() + " LastModified Date: " 
		+ getLastModifiedDate() + "\n");
	}

}
