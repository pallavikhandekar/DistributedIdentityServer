package identity.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Database class of Identity server application.
 * Writes and Reads data to and from database file respectively.
 * 
 * @author Pallavi and Jacob 
 *
 */
public class Database {

  private LinkedList<UserInfo> lstUserData = null;
  private String dbName = null;

  public Database(String srcFileName) throws FileNotFoundException, IOException, ClassNotFoundException {
    loadList(srcFileName);
  }

  /**
   * Creates user login in list.
   * @param loginName : user name to be created.
   * @param realName : real name of user.
   * @param password : password of user.
   * @param iPAddress : ipAddress of machine from which create request is receieved.
   * @return UUID created for user login.
  
   */
  public UUID createUser(String srcLoginName, String srcPassword, String srcRealName, String srcIPAddress) {
    UUID uuid = UUID.randomUUID();
    if (lstUserData == null || lstUserData.isEmpty()) {
      //Adding first object to Database.
      lstUserData = new LinkedList<UserInfo>();
    }
    UserInfo userInfo = new UserInfo(uuid, srcLoginName, srcPassword, srcRealName, srcIPAddress);
    userInfo.setCreateDate(new Date().toString());
    lstUserData.add(userInfo);
    return uuid;
  }

  /**
   * Looks for user information based on user name from list.
   * @param loginName : user name to be fetched.
   * @return UserInfo object with all details of user.
   */
  public UserInfo lookUpByLoginName(String srcLoginName) {
    if (lstUserData == null || lstUserData.isEmpty()) {
      return null;
    } else {
      for (UserInfo ui : lstUserData) {
        if (ui.getLoginName().equals(srcLoginName)) {
          return ui;
        }
      }
    }
    return null;
  }

  /**
   * Looks up for user information based on UUID from list.
   * @param uuid : UUID of user to be fetched. 
   * @return UserInfo object with all details of user.
   */
  public UserInfo lookUpByUUID(UUID srcUUID) {
    if (lstUserData == null || lstUserData.isEmpty()) {
      return null;
    } else {
      for (UserInfo ui : lstUserData) {
        if (ui.getUuid().equals(srcUUID)) {
          return ui;
        }
      }
    }
    return null;
  }

  /**
   * Modifies old login name to new login name in list if password and new login name are correct.
   * @param oldLoginName : old login user name to be modified.
   * @param newLoginName : new login user name to be assigned.
   * @param password : password for old user name.
   * @return Response (modification success message) or Error message.
   */
  public String modifyUser(String oldLoginName, String newLoginName, String password) {
    if (lstUserData == null || lstUserData.isEmpty()) {
      return "Response: Transaction Unsuccessful. Missing account.";
    } else {
      for (UserInfo ui : lstUserData) {
        if (ui.getLoginName().equals(oldLoginName)) {
          //do following code if account found
          if (checkPassword(ui, password)) {
            ui.setLoginName(newLoginName);
            ui.setLastModifiedDate(new Date().toString());
            return "Response: Transaction Successful";
          } else if (ui.getLoginName().equals(oldLoginName) && !checkPassword(ui, password)) {
            return "Error: Incorrect Password";
          }
        }
      }
    }
    return "Error: Returning a String to make Java happy";
  }

  /**
   * Deletes user name from list.
   * @param loginName : login name to be deleted.
   * @param password : password for login name
   * @return Response (deletion success message) or Error message.
   */
  public String deleteUser(String loginName, String password) {
    if (lstUserData == null || lstUserData.isEmpty()) {
      return "Response: Transaction Unsuccessful. Missing account.";
    } else {
      for (UserInfo ui : lstUserData) {
        if (ui.getLoginName().equals(loginName) && checkPassword(ui, password)) {
          lstUserData.remove(ui);
          return "Response: Transaction Successful";
        } else if (ui.getLoginName().equals(loginName) && !checkPassword(ui, password)) {
          return "Error: Incorrect Password";
        }
      }
    }
    return "Error: Returning a String to make Java happy";
  }

  /**
   * Fetched all user names in database.
   * @return List of user names.
   */
  public LinkedList<String> getLoginNames() {
    LinkedList<String> strList = new LinkedList<String>();
    for (UserInfo ui : lstUserData) {
      strList.add(ui.getLoginName());
    }
    return strList;
  }

  /**
   * Fetched all user uuid's in database.
   * @return List of user names.
  
   */
  public LinkedList<UUID> getUUIDs() {
    LinkedList<UUID> uuidList = new LinkedList<UUID>();
    for (UserInfo ui : lstUserData) {
      uuidList.add(ui.getUuid());
    }
    return uuidList;
  }

  /**
   * Fetches all user informations from database. 
   * @return List of UserInfo objects
   */
  public LinkedList<UserInfo> getAll() {
    return lstUserData;
  }

  /**
   * Loads List with data from database file.
   * @param srcFileName  :Database file
   * @throws IOException 
   * @throws ClassNotFoundException
   * @throws FileNotFoundException
   */
  @SuppressWarnings("unchecked")
  public void loadList(String srcFileName) throws IOException, ClassNotFoundException, FileNotFoundException {
    File file = new File(srcFileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    dbName = srcFileName;
    ObjectInputStream ois = null;
    try {
      BufferedInputStream buf = new BufferedInputStream(
              new FileInputStream(file));
      ois = new ObjectInputStream(buf);
      lstUserData = (LinkedList<UserInfo>) ois.readObject();

    } catch (FileNotFoundException fnfe) {
      throw new FileNotFoundException("Database is missing!");
    } catch (EOFException eofe) {
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Writes list data to Database.
   */
  public void writetoDisk() {
    try {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dbName));
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(lstUserData);
      oos.flush();
      oos.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
  public void setLstUserData(LinkedList<UserInfo> lstUserData) {
	this.lstUserData = lstUserData;
}


  public LinkedList<UserInfo> getLstUserData() {
	return lstUserData;
}

/**
   * Validates password to modify or delete user names.
   * @param ui : ui.getPassword is password stored in database
   * @param password : password from client.
   * @return
   */
  private boolean checkPassword(UserInfo ui, String password) {
	    if (ui.getPassword() == null && password == null) {
      return true;
    } else if (ui.getPassword().equals(password)) {
      return true;
    } else {
      return false;
    }
  }
}
