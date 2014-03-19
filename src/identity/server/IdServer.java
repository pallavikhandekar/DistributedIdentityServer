package identity.server;

import election_algorithm.Election;
import election_algorithm.Election.MessageType;
import election_algorithm.Message;
import election_algorithm.MessageReceiverThread;
import java.io.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.UUID;
import javax.crypto.*;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.swing.Timer;

/**
 * Server Class for Identity Server application.
 *
 *
 * @author Pallavi and Jacob
 */
public class IdServer implements ServerOperations {

  /**
   * 
   *
   */
  private Database db = null;
  private static int rmiregistry = 5195;
  public static boolean isVerbose = false;
  private static final String DATABASE_FILE = "./Database";
  private static final String SERVER_NAME = "IdServer";
  private static final String MULT_CROUP_ADDR = "230.230.230.1";
  private static final String CAMMANDLINE_PARAMETER_ERROR = "java IdServer [--port <port#> --verbose | --verbose --port <port#> | --port <port#> | --verbose]";
  private static boolean privateServerCall = false;
  private static Key key = null;
  private final static String KEY_FILE_NAME = "./resources/key";
  private static Timer timerToCheckCoordinator;
  private Election objElection=null;

  /**
   * Creates a Database object for file referenced by DATABASE_FILE
   *
   * @throws FileNotFoundException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public IdServer(String s) throws FileNotFoundException, ClassNotFoundException, IOException {
    super();
    db = new Database(DATABASE_FILE);
    objElection = new Election(MULT_CROUP_ADDR, rmiregistry, 5190, 5191, InetAddress.getLocalHost().getHostAddress(),db);
  }

  public void bind(String name, int registryPort) {
    try {
      RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
      RMIServerSocketFactory rmiServerSocketFactory = new SslRMIServerSocketFactory();
      ServerOperations server = (ServerOperations) UnicastRemoteObject.exportObject(this, 0, rmiClientSocketFactory, rmiServerSocketFactory);
      String bindUrl = "//localhost:"+registryPort+"/"+name;
      Naming.rebind(bindUrl, server);
      System.out.println(name + " bound in registry");
    } catch (Exception e) {
      System.out.println("Exception occurred: " + e);
      System.exit(1);
    }
  }

  /**
   * Creates Registry and binds server to Registry.
   *
   * @param args
   */
  public static void main(String[] args) {
    parseCommandlineParameters(args);

    System.out.println("Setting System Properties:");

    System.setProperty("javax.net.ssl.keyStore", "./resources/Server_Keystore");
    // Warning: change to match your password! Also the password should be
    // stored encrypted in a file outside the program.
    System.setProperty("javax.net.ssl.keyStorePassword", "P@ssw0rd");
    System.setProperty("java.security.policy", "./resources/mysecurity.policy");
    System.setProperty("java.rmi.server.codebase", "file:" + System.getProperty("user.dir") + "/");
    try {
      System.err.println("Hello. I am a server. May name is " + SERVER_NAME + rmiregistry);
      IdServer objIdServer = new IdServer(SERVER_NAME + rmiregistry);
      objIdServer.bind(SERVER_NAME + rmiregistry, rmiregistry);
      System.out.println(SERVER_NAME + rmiregistry + " is listening for a client...");
      //adding listener to handle shutdowns and other interruptions by user.
      Runtime.getRuntime().addShutdownHook(new ShutdownListener(objIdServer));

      //NEW CODE
      MessageReceiverThread objMessageReceiverThread1 = new MessageReceiverThread(objIdServer.objElection,objIdServer.objElection.getMulticastServerPort());
      MessageReceiverThread objMessageReceiverThread2 = new MessageReceiverThread(objIdServer.objElection, objIdServer.objElection.getMulticastClientPort());
      objMessageReceiverThread1.start();
      objMessageReceiverThread2.start();
      Thread.sleep(2000);
      
      Message objMessage;
      
      objIdServer.objElection.setNumberOfServers(1);
      objMessage = new Message(rmiregistry, MessageType.NEWCOMER, InetAddress.getLocalHost().getHostAddress(),objIdServer);
      objIdServer.objElection.setObjMessage(objMessage);
      objIdServer.objElection.SendMessage(5190);
      
    //Whenever server logs in it hold an election. It assumes that it is coordinator
      objIdServer.objElection.setCurrentCoordinatorHostAddress(InetAddress.getLocalHost().getHostAddress());
      objIdServer.objElection.setCurrentCoordinatorPort(rmiregistry);
      objIdServer.objElection.isCoordinater = true;
      objIdServer.objElection.getCheckPointTimer().start();
      objMessage = new Message(rmiregistry, MessageType.ELECTION, InetAddress.getLocalHost().getHostAddress());
      objIdServer.objElection.setObjMessage(objMessage);
      objIdServer.objElection.SendMessage(5190);
      timerToCheckCoordinator = new Timer(50000, new TimerListner(objIdServer.objElection, rmiregistry));
      timerToCheckCoordinator.start();
    } catch (Exception e) {
      System.out.println("Exception occurred: " + e);
    }
  }

  private static void parseCommandlineParameters(String[] args) {
    if (args.length < 1) {
      isVerbose = false;
    } else if (args.length == 1) {
      if (args[0].equalsIgnoreCase("--verbose")) {
        isVerbose = true;
      } else {
        System.out.println(CAMMANDLINE_PARAMETER_ERROR);
        System.exit(1);
      }
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("--port")) {
        try {
          rmiregistry = Integer.parseInt(args[1]);
        } catch (Exception e) {
          System.out.println(CAMMANDLINE_PARAMETER_ERROR);
          System.exit(1);
        }
      } else {
        System.out.println(CAMMANDLINE_PARAMETER_ERROR);
        System.exit(1);
      }
    } else if (args.length == 3) {
      if (args[0].equalsIgnoreCase("--verbose")) {
        isVerbose = true;
        if (args[1].equalsIgnoreCase("--port")) {
          try {
            rmiregistry = Integer.parseInt(args[2]);
          } catch (Exception e) {
            System.out.println(CAMMANDLINE_PARAMETER_ERROR);
            System.exit(1);
          }
        } else {
          System.out.println(CAMMANDLINE_PARAMETER_ERROR);
          System.exit(1);
        }
      } else if (args[0].equalsIgnoreCase("--port")) {
        try {
          rmiregistry = Integer.parseInt(args[1]);

        } catch (Exception e) {
          System.out.println(CAMMANDLINE_PARAMETER_ERROR);
          System.exit(1);
        }
        if (args[2].equalsIgnoreCase("--verbose")) {
          isVerbose = true;
        } else {
          System.out.println(CAMMANDLINE_PARAMETER_ERROR);
          System.exit(1);
        }
      } else {
        System.out.println(CAMMANDLINE_PARAMETER_ERROR);
        System.exit(1);
      }
    } else {
      System.out.println(CAMMANDLINE_PARAMETER_ERROR);
      System.exit(1);
    }
  }

  @Override
  public UUID createUser(String loginName, String realName, String password, String iPAddress) throws RemoteException {
    UUID retVal = null;
    if (isVerbose) {
      System.out.println("Creating Login for User : " + loginName);
    }
    privateServerCall = true;
    if (lookUpByLoginName(loginName) == null) {
      if (password != null) {
        password = encryptPassword(password);
      }
      UUID uuid = db.createUser(loginName, password, realName, iPAddress);
      //db.writetoDisk();
      retVal = uuid;
    } else {
      if (isVerbose) {
        System.out.println("Login Failure . User " + loginName + " already exists.");
      }
    }
    privateServerCall = false;
    return retVal;
  }

  @Override
  public UserInfo lookUpByLoginName(String loginName) throws RemoteException {
    if (isVerbose && privateServerCall == false) {
      System.out.println("Looking up information of user " + loginName + " from database.");
    }
    UserInfo user = db.lookUpByLoginName(loginName);
    if (isVerbose && user == null && privateServerCall == false) {
      System.out.println("User " + loginName + " does not exist in database .");
    } else if (isVerbose && privateServerCall == false) {
      System.out.println("Sending informartion of " + loginName + " to client");
    }
    return user;
  }

  @Override
  public UserInfo lookUpByUUID(UUID uuid) throws RemoteException {
    if (isVerbose) {
      System.out.println("Looking up information for id " + uuid + " from database.");
    }
    UserInfo user = db.lookUpByUUID(uuid);
    if (isVerbose && user == null) {
      System.out.println("User with id " + uuid + " does not exist in database .");
    } else if (isVerbose) {
      System.out.println("Sending informartion of " + uuid + " to client");
    }
    return user;
  }

  @Override
  public String modifyUser(String oldLoginName, String newLoginName, String password) throws RemoteException {
    String retVal = "";
    if (isVerbose) {
      System.out.println("Modifying user " + oldLoginName + " to " + newLoginName);
    }
    privateServerCall = true;
    if (lookUpByLoginName(oldLoginName) != null && lookUpByLoginName(newLoginName) == null) {

      if (password != null) {
        password = encryptPassword(password);
      }
      String result = db.modifyUser(oldLoginName, newLoginName, password);
     /* if (result.charAt(0) == 'R') {
        db.writetoDisk();
      }*/
      retVal = result;
    } else if (lookUpByLoginName(oldLoginName) != null) {
      if (isVerbose) {
        System.out.println("Failed to modify user " + oldLoginName + " in database.");
      }
      retVal = "ERROR: The new login name already exists!";
    } else {
      if (isVerbose) {
        System.out.println("Failed to modify user " + oldLoginName + " in database.");
      }
      retVal = "ERROR: The old login name does not exist";
    }
    privateServerCall = false;
    return retVal;
  }

  @Override
  public String deleteUser(String loginName, String password) throws RemoteException {
    String retVal = "";
    if (isVerbose) {
      System.out.println("Deleting user " + loginName + " from database.");
    }
    privateServerCall = true;
    if (lookUpByLoginName(loginName) != null) {
      if (password != null) {
        password = encryptPassword(password);
      }
      String result = db.deleteUser(loginName, password);
      /*if (result.charAt(0) == 'R') {
        db.writetoDisk();
      } else {*/
        if (isVerbose) {
          System.out.println("Incorrect Password.Failed deleting user " + loginName + " from database.");
        }
     // }
      retVal = result;
    } else {
      retVal = "ERROR: User does not exist.";
      if (isVerbose) {
        System.out.println("Failed deleting user " + loginName + " from database.");
      }
    }
    privateServerCall = false;
    return retVal;
  }

  @Override
  public LinkedList<String> getLoginNames() throws RemoteException {
    if (isVerbose) {
      System.out.println("Sending login names to client");
    }
    return db.getLoginNames();
  }

  @Override
  public LinkedList<UUID> getUUIDs() throws RemoteException {
    if (isVerbose) {
      System.out.println("Sending uuids to client");
    }
    return db.getUUIDs();
  }

  @Override
  public LinkedList<UserInfo> getAll() throws RemoteException {
    if (isVerbose) {
      System.out.println("Sending information of users to client");
    }
    return db.getAll();
  }

  /**
   * Returns a Database object created when class constructor was formed.
   *
   * @return Database object
   */
  public Database getDatabase() {
    return db;
  }
  
  public Election getElectionObject(){
	  return objElection;
  }

  //**************************Start KEY Generation Logic***********************
  /**
   * Generates Key with Algorithm DES. This key is then used further to encrypt the passwords from client.
   */
  private static void generateKey() {
    try {
      KeyGenerator kg = KeyGenerator.getInstance("DES");
      setKey(kg.generateKey());
      File keyFile = new File(KEY_FILE_NAME);
      keyFile.createNewFile();
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keyFile));
      oos.writeObject(getKey());
      oos.close();
    } catch (NoSuchAlgorithmException nsae) {
      nsae.printStackTrace();
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * retrieve the key from already created key file.
   *
   * @throws FileNotFoundException no key file was found
   */
  private static void loadKey() throws FileNotFoundException {
    File keyFile = new File(KEY_FILE_NAME);
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile));
      setKey((Key) ois.readObject());
      ois.close();
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }

  /**
   * Returns the Encryption key. If key is not created then system will first create key and then use it for further encryptions.
   *
   * @return the key
   */
  private static Key getKey() {
    if (key == null) {
      try {
        loadKey();
      } catch (FileNotFoundException e) {
        generateKey();
      }
    }
    return key;
  }

  /**
   * set the key as a private class variable
   *
   * @param srcKey the key to be saved in the object
   */
  private static void setKey(Key srcKey) {
    key = srcKey;
  }

  /**
   * encrypt the password
   *
   * @param password the password of which we will be encrypting
   * @return encrypted password.
   */
  private static String encryptPassword(String password) {
    String encryptedPassword = null;
    try {
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(Cipher.ENCRYPT_MODE, getKey());
      byte[] encryptedBytePassword = cipher.doFinal(password.getBytes());
      encryptedPassword = new String(encryptedBytePassword);

    } catch (NoSuchAlgorithmException nsae) {
      nsae.printStackTrace();
    } catch (NoSuchPaddingException nspe) {
      nspe.printStackTrace();
    } catch (InvalidKeyException ike) {
      ike.printStackTrace();
    } catch (IllegalBlockSizeException ibse) {
      ibse.printStackTrace();
    } catch (BadPaddingException bpe) {
      bpe.printStackTrace();
    }
    return encryptedPassword;
  }
  //**************************End KEY Generation Logic***********************
}
