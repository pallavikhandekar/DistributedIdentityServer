package identity.server;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.UUID;


/**
 * Remote Interface for Identity server application. 
 * @author Pallavi and Jacob
 *
 */
public interface ServerOperations extends java.rmi.Remote {
	
	/**
	 * Creates user login in database.
	 * @param loginName : user name to be created.
	 * @param realName : real name of user.
	 * @param password : password of user.
	 * @param iPAddress : ipAddress of machine from which create request is receieved.
	 * @return UUID created for user login.
	 * @throws RemoteException
	 */
	public UUID createUser(String loginName, String realName, String password, String iPAddress) throws RemoteException;
	
	/**
	 * Looks for user information based on user name from database.
	 * @param loginName : user name to be fetched.
	 * @return UserInfo object with all details of user.
	 * @throws RemoteException
	 */
	public UserInfo lookUpByLoginName(String loginName)throws RemoteException;
	
	/**
	 * Looks up for user information based on UUID from database.
	 * @param uuid : UUID of user to be fetched. 
	 * @return UserInfo object with all details of user.
	 * @throws RemoteException
	 */
	public UserInfo lookUpByUUID(UUID uuid) throws RemoteException;
	
	/**
	 * Modifies old login name to new login name in database if password and new login name are correct.
	 * @param oldLoginName : old login user name to be modified.
	 * @param newLoginName : new login user name to be assigned.
	 * @param password : password for old user name.
	 * @return Response (modification success message) or Error message.
	 * @throws RemoteException
	 */
	public String modifyUser(String oldLoginName, String newLoginName, String password) throws RemoteException;
	
	/**
	 * Deletes user name from database.
	 * @param loginName : login name to be deleted.
	 * @param password : password for login name
	 * @return Response (deletion success message) or Error message.
	 * @throws RemoteException
	 */
	public String deleteUser(String loginName, String password) throws RemoteException;
	
	/**
	 * Fetched all user names in database.
	 * @return List of user names.
	 * @throws RemoteException
	 */
	public LinkedList<String> getLoginNames() throws RemoteException;
	
	/**
	 * Fetched all user uuid's in database.
	 * @return List of user names.
	 * @throws RemoteException
	 *
	 */
	public LinkedList<UUID> getUUIDs() throws RemoteException;
	
	/**
	 * Fetches all user informations from database. 
	 * @return List of UserInfo objects
	 * @throws RemoteException
	 */
	public LinkedList<UserInfo> getAll() throws RemoteException;

}
