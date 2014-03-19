package identity.client;

import election_algorithm.Election;
import election_algorithm.Election.MessageType;
import election_algorithm.Message;
import election_algorithm.MessageReceiverThread;
import identity.server.ServerOperations;
import identity.server.UserInfo;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Parse the command line input and determine the action taken. Connect to a server and carry out the requested task.
 *
 * @author Jacob Bennett & Pallavi Khandekar
 */
public class IdClient {

  private static String host;
  private static ServerOperations stub;
  private static int registryPort;
  private static String[] CLO; //Commandline Parameters Override
  //Keywords
  private static final String CREATE = "--create";
  private static final String C = "-c";
  private static final String MODIFY = "--modify";
  private static final String M = "-m";
  private static final String DELETE = "--delete";
  private static final String D = "-d";
  private static final String LOOKUP = "--lookup";
  private static final String L = "-l";
  private static final String REVERSE_LOOKUP = "--reverse-lookup";
  private static final String R_L = "-r";
  private static final String GET = "--get";
  private static final String G = "-g";
  private static final String PASSWORD = "--password";
  private static final String P = "-p";
  //Errors
  private static final String CREATE_QUERY = "Query Format Error: --create <loginname> [<real name>] [--password <password>]";
  private static final String MODIFY_QUERY = "Query Format Error: --modify <oldloginname> <newloginname> [--password <password>]";
  private static final String DELETE_QUERY = "Query Format Error: --delete <loginname> [--password <password>]";
  private static final String LOOKUP_QUERY = "Query Format Error: --lookup <loginname>";
  private static final String REVERSE_L_QUERY = "Query Format Error: --reverse-lookup <UUID>";
  private static final String GET_QUERY = "Query Format Error: --get users|uuids|all";
  private static final String VAL_KEYWORD_ERR = "Value should not be Keyword";
  private static final String EXCESSIVE_PARAM = "Query Format Error: Excessive query commandline parameters";
  private static String SERVER_NAME;

  /**
   * MAIN Basic controls Check IP Address format, hard code registry port, connect to server make a server object stub parse the rest of the command line parameters
   *
   * @param args command line parameters
   * @throws RemoteException an RMI specific exception
   * @throws NotBoundException Check the stub for successful completion
   * @throws UnknownHostException obviously, throw if there is no host connection
   * @throws MalformedURLException an unnecessary exception
   */
  synchronized public static void main(String[] args) {
    CLO = args;
    if (CLO.length < 1) {
      System.err.println(CREATE_QUERY + "\n" + MODIFY_QUERY + "\n" + DELETE_QUERY + "\n" + LOOKUP_QUERY + "\n" + REVERSE_L_QUERY + "\n" + GET_QUERY);
      System.exit(1);
    }
    //checkIPFormat(CLO[0]);
    
    try {
     
      Election objElection = new Election("230.230.230.1", 0, 0, 5191, InetAddress.getLocalHost().getHostAddress(),null);
      MessageReceiverThread objMessageReceiverThread1 = new MessageReceiverThread(objElection, objElection.getMulticastClientPort());
      objMessageReceiverThread1.setListener();
      objMessageReceiverThread1.start();
      Thread.sleep(500);
      Message objMessage = new Message(0, MessageType.WHO_IS_COORDINATOR, InetAddress.getLocalHost().getHostAddress());
      objElection.setObjMessage(objMessage);
      objElection.SendMessage(5191);
      while (!objElection.isCoordinatorReceived()) {
        Thread.sleep(2000);
      }
      registryPort = objElection.getCurrentCoordinatorPort();
      host = objElection.getCurrentCoordinatorHostAddress();
      SERVER_NAME = "IdServer"+registryPort;
      System.setProperty("javax.net.ssl.trustStore", "./resources/Client_Truststore");
      System.setProperty("java.security.policy", "./resources/mysecurity.policy");
      stub = (ServerOperations) Naming.lookup("//"+host+":"+registryPort+"/"+SERVER_NAME);
      parseClient(CLO);
      
    } catch (ConnectException e) {
      System.err.println("Server not running");
      System.exit(1);
    } catch (java.net.MalformedURLException mfe) {
		System.err.println("RMI endpoint URL not formed correctly: "+mfe);
		System.exit(2);
	} catch (UnmarshalException ue) {
      System.err.println("Client exception: " + ue);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Client exception: " + e);
      System.exit(1);
    }
  }

  /**
   * Use regex to determine correct IP address format
   *
   * @param srcIPAddress first command line parameter args[0]
   */
  private static void checkIPFormat(String srcIPAddress) {
    if (srcIPAddress.equalsIgnoreCase("localhost")) {
      host = "127.0.0.1";
    } else if (srcIPAddress.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")) {
      host = srcIPAddress;
    } else {
      System.err.println("Malformed IP Address Error: FOUND: " + srcIPAddress + ", EXPECTED: xxx.xxx.xxx.xxx\n"
              + "Replacing IP address with \"localhost\"");
      String[] CMDInsert = new String[CLO.length + 1];
      CMDInsert[0] = "localhost";
      for (int i = 0; i < CLO.length; i++) {
        CMDInsert[i + 1] = CLO[i];
      }
      CLO = CMDInsert;
    }
  }

  /**
   * Find out what command is to be run and determine if its parameters are well formed
   *
   * @param args all command line parameters args[0] is not handled here
   * @throws RemoteException thrown if a server function fails to run
   */
  private static void parseClient(String[] args) throws RemoteException {
    String missingPassword = null;
    if(args.length < 2) {//found only query type but no arguments
      if (args[0].toLowerCase().equals(CREATE) || args[0].toLowerCase().equals(C)) {//supports 1 - 4 commandline parameters
        System.err.println(CREATE_QUERY);
      } else if (args[0].toLowerCase().equals(LOOKUP) || args[0].toLowerCase().equals(L)) {
        System.err.println(LOOKUP_QUERY);
      } else if (args[0].toLowerCase().equals(REVERSE_LOOKUP) || args[0].toLowerCase().equals(R_L)) {
        System.err.println(REVERSE_L_QUERY);
      } else if (args[0].toLowerCase().equals(MODIFY) || args[0].toLowerCase().equals(M)) {
        System.err.println(MODIFY_QUERY);
      } else if (args[0].toLowerCase().equals(DELETE) || args[0].toLowerCase().equals(D)) {
        System.err.println(DELETE_QUERY);
      } else if (args[0].toLowerCase().equals(GET) || args[0].toLowerCase().equals(G)) {
        System.err.println(GET_QUERY);
      } else {
        System.err.println("Query Format Error: Unknown query");
      }
      System.exit(1);
    } else if (args.length < 3) {//found 1 queries and 1 commandline parameter
      //************CREATE****************************
      if (args[0].toLowerCase().equals(CREATE) || args[0].toLowerCase().equals(C)) {
        if (checkIfValueIsNotKeyWord(args[1])) {
          clientCreate(args[1], null, missingPassword);
        } else {
          System.err.println(VAL_KEYWORD_ERR);
          System.err.println(CREATE_QUERY);
        }
      } //************LOOKUP****************************
      else if (args[0].toLowerCase().equals(LOOKUP) || args[0].toLowerCase().equals(L)) {
        clientLookup(args[1]);
      } //**********REVERSE_LOOKUP****************
      else if (args[0].toLowerCase().equals(REVERSE_LOOKUP) || args[0].toLowerCase().equals(R_L)) {
        clientReverseLookup(args[1]);
      } //***********MODIFY**********************
      else if (args[0].toLowerCase().equals(MODIFY) || args[0].toLowerCase().equals(M)) {
        System.err.println(MODIFY_QUERY);
        System.exit(1);
      } //***********DELETE*************************
      else if (args[0].toLowerCase().equals(DELETE) || args[0].toLowerCase().equals(D)) {
        clientDelete(args[1], missingPassword);
      } //***********GET*****************************
      else if (args[0].toLowerCase().equals(GET) || args[0].toLowerCase().equals(G)) {
        if (args[1].toLowerCase().equals("users")) {
          clientGetUsers();
        } else if (args[1].toLowerCase().equals("uuids")) {
          clientGetUUID();
        } else if (args[1].toLowerCase().equals("all")) {
          clientGetAll();
        } else {
          System.err.println("Query Format Error: Unknown query");
          System.exit(1);
        }
      } else {
        System.err.println("Query Format Error: Unknown query");
        System.exit(1);
      }
    } else if (args.length < 4) {//found 1 queries and 2 commandline parameters
      //************CREATE****************************
      if (args[0].toLowerCase().equals(CREATE) || args[0].toLowerCase().equals(C)) {
        if (checkIfValueIsNotKeyWord(args[1]) || checkIfValueIsNotKeyWord(args[2])) {
          clientCreate(args[1], args[2], missingPassword);
        } else {
          System.err.println(VAL_KEYWORD_ERR);
          System.err.println(CREATE_QUERY);
          System.exit(1);
        }
      } //************LOOKUP****************************
      else if (args[0].toLowerCase().equals(LOOKUP) || args[0].toLowerCase().equals(L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //**********REVERSE_LOOKUP*********************
      else if (args[0].toLowerCase().equals(REVERSE_LOOKUP) || args[0].toLowerCase().equals(R_L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //***********MODIFY****************************
      else if (args[0].toLowerCase().equals(MODIFY) || args[0].toLowerCase().equals(M)) {
        //oldname and newname should not be keywords
        if (checkIfValueIsNotKeyWord(args[1]) && checkIfValueIsNotKeyWord(args[2])) {
          clientModify(args[1], args[2], missingPassword);
        } else {
          System.err.println(VAL_KEYWORD_ERR);
          System.err.println(MODIFY_QUERY);
          System.exit(1);
        }
      } //***********DELETE*************************
      else if (args[0].toLowerCase().equals(DELETE) || args[0].toLowerCase().equals(D)) {
        //Delete query can never have 4 params
        System.err.println(DELETE_QUERY);
        System.exit(1);
      } //***********GET*****************************
      else if (args[0].toLowerCase().equals(GET) || args[0].toLowerCase().equals(G)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } else {
        System.err.println("Query Format Error: Unknown query");
        System.exit(1);
      }
    } else if (args.length < 5) {//found 1 queries and 3 commandline parameters
      //************CREATE****************************
      if (args[0].toLowerCase().equals(CREATE) || args[0].toLowerCase().equals(C)) {
        if ((args[2].toLowerCase().equals(P)) || (args[2].toLowerCase().equals(PASSWORD))) {
          clientCreate(args[1], null, args[3]);
        } else {
          System.err.println(CREATE_QUERY);
          System.exit(1);
        }
      } //************LOOKUP****************************
      else if (args[0].toLowerCase().equals(LOOKUP) || args[0].toLowerCase().equals(L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //**********REVERSE_LOOKUP******************
      else if (args[0].toLowerCase().equals(REVERSE_LOOKUP) || args[0].toLowerCase().equals(R_L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //***********MODIFY****************************
      else if (args[0].toLowerCase().equals(MODIFY) || args[0].toLowerCase().equals(M)) {
        //Modify query can never have 5 params
        System.err.println(MODIFY_QUERY);
        System.exit(1);
      } //***********DELETE*************************
      else if (args[0].toLowerCase().equals(DELETE) || args[0].toLowerCase().equals(D)) {
        if ((args[2].toLowerCase().equals(P)) || (args[2].toLowerCase().equals(PASSWORD))) {
          clientDelete(args[1], args[3]);
        } else {
          System.err.println("Query Format Error: Unknown query commandline parameters");
          System.exit(1);
        }
      } //***********GET*****************************
      else if (args[0].toLowerCase().equals(GET) || args[0].toLowerCase().equals(G)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } else {
        System.err.println("Query Format Error: Unknown query");
        System.exit(1);
      }
    } else if (args.length < 6) {//found 1 queries and 4 or more commandline parameters

      //************CREATE****************************
      if (args[0].toLowerCase().equals(CREATE) || args[0].toLowerCase().equals(C)) {

        if (checkIfValueIsNotKeyWord(args[2])) {
          if ((args[3].toLowerCase().equals(P)) || (args[3].toLowerCase().equals(PASSWORD))) {

            clientCreate(args[1], args[2], args[4]);
          } else {
            System.err.println(CREATE_QUERY);
            System.exit(1);
          }
        } else {
          System.err.println(VAL_KEYWORD_ERR);
          System.err.println(CREATE_QUERY);
          System.exit(1);
        }
      } //************LOOKUP****************************
      else if (args[0].toLowerCase().equals(LOOKUP) || args[0].toLowerCase().equals(L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //**********REVERSE_LOOKUP
      else if (args[0].toLowerCase().equals(REVERSE_LOOKUP) || args[0].toLowerCase().equals(R_L)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //***********MODIFY****************************
      else if (args[0].toLowerCase().equals(MODIFY) || args[0].toLowerCase().equals(M)) {
        //old name and new name should not be keywords.
        if (checkIfValueIsNotKeyWord(args[1]) || checkIfValueIsNotKeyWord(args[2])) {
          if ((args[3].toLowerCase().equals(P)) || (args[3].toLowerCase().equals(PASSWORD))) {
            clientModify(args[1], args[2], args[4]);
          } else {
            System.err.println("Query Format Error: Unknown query commandline parameters");
            System.exit(1);
          }
        } else {
          System.err.println(VAL_KEYWORD_ERR);
          System.err.println(MODIFY_QUERY);
          System.exit(1);
        }
      } //***********DELETE*************************
      else if (args[0].toLowerCase().equals(DELETE) || args[0].toLowerCase().equals(D)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } //***********GET*****************************
      else if (args[0].toLowerCase().equals(GET) || args[0].toLowerCase().equals(G)) {
        System.err.println(EXCESSIVE_PARAM);
        System.exit(1);
      } else {
        System.err.println("Query Format Error: Unknown query");
        System.exit(1);
      }
    } else {//found 1 queries and 5 or more commandline parameters
      System.err.println("Query Format Error: Excessive query commandline parameters");
      System.exit(1);
    }
  }

  /**
   * attempt to make a new account on a failure, print a message
   *
   * @param srcLogin account login name
   * @param srcRealName account holder's name
   * @param srcPassword account holder's password
   * @throws RemoteException failure to run remote function
   */
  private static void clientCreate(String srcLogin, String srcRealName, String srcPassword) throws RemoteException {
    if (srcRealName == null || srcRealName.isEmpty()) {
      srcRealName = System.getProperty("user.name");
    }
    UUID response = stub.createUser(srcLogin, srcRealName, srcPassword, host);
    if (response == null) {
      System.out.println("User name already exists.");
    } else {
      System.out.println("Response: " + response.toString());
    }
  }

  /**
   * Attempt to locate an existing account using the login name on failure, print a message
   *
   * @param srcLogin
   * @throws RemoteException failure to run remote function
   */
  private static void clientLookup(String srcLogin) throws RemoteException {
    UserInfo response = stub.lookUpByLoginName(srcLogin);
    if (response == null) {
      System.out.println("User not found.");
    } else {
      System.out.println("Response: " + response.toString());
    }
  }

  /**
   * Attempt to locate an existing account using the UUID on failure, print a message
   *
   * @param srcUUID the UUID use to do the search
   * @throws RemoteException failure to run remote function
   */
  private static void clientReverseLookup(String srcUUID) throws RemoteException {
    UUID uuid = (UUID) UUID.fromString(srcUUID);
    UserInfo response = stub.lookUpByUUID(uuid);
    if (response == null) {
      System.out.println("User not found.");
    } else {
      System.out.println("Response: " + response.toString());
    }
  }

  /**
   * Attempt to modify an existing account using the login name to a new login name on failure, print a message
   *
   * @param srcOldLoginName original login name
   * @param srcNewLoginName future login name
   * @param srcPassword current password
   * @throws RemoteException failure to run remote function
   */
  private static void clientModify(String srcOldLoginName, String srcNewLoginName, String srcPassword) throws RemoteException {
    System.out.println(stub.modifyUser(srcOldLoginName, srcNewLoginName, srcPassword));
  }

  /**
   * Attempt to delete an existing account using the login name on failure, print a message
   *
   * @param srcLoginName
   * @param srcPassword
   * @throws RemoteException failure to run remote function
   */
  private static void clientDelete(String srcLoginName, String srcPassword) throws RemoteException {
    System.out.println(stub.deleteUser(srcLoginName, srcPassword));
  }

  /**
   * Return a list of all user login names
   *
   * @throws RemoteException failure to run remote function
   */
  private static void clientGetUsers() throws RemoteException {
    LinkedList<String> response = stub.getLoginNames();
    if (response == null || response.isEmpty()) {
      System.out.println("No data found");
    } else {
      System.out.println("Response: ");
      for (String s : response) {
        System.out.println(s);
      }
    }
  }

  /**
   * returns a list of all UUIDs in use
   *
   * @throws RemoteException failure to run remote function
   */
  private static void clientGetUUID() throws RemoteException {
    LinkedList<UUID> response = stub.getUUIDs();
    if (response == null || response.isEmpty()) {
      System.out.println("No data found");
    } else {
      System.out.println("Response: ");
      for (UUID uuid : response) {
        System.out.println(uuid);
      }
    }
  }

  /**
   * Return a printout of each account and its details
   *
   * @throws RemoteException failure to run remote function
   */
  private static void clientGetAll() throws RemoteException {
    LinkedList<UserInfo> response = stub.getAll();
    if (response == null || response.isEmpty()) {
      System.out.println("No data found");
    } else {
      System.out.println("Response: ");
      for (UserInfo ui : response) {
        System.out.println(ui.toString());
      }
    }
  }

  /**
   * validates the value entered by client is not a keyword.
   *
   * @param value : Value entered by client
   * @return true if value not keyword false if value is keyword
   */
  private static boolean checkIfValueIsNotKeyWord(String value) {
    value = value.toLowerCase();
    if ((!value.equals(CREATE)) || (!value.equals(C)) || (!value.equals(MODIFY)) || (!value.equals(M))
            || (!value.equals(DELETE)) || (!value.equals(D)) || (!value.equals(LOOKUP)) || (!value.equals(L))
            || (!value.equals(REVERSE_LOOKUP)) || (!value.equals(R_L)) || (!value.equals(GET)) || (!value.equals(G))
            || (!value.equals(PASSWORD)) || (!value.equals(P))) {
      return true;
    } else {
      return false;
    }
  }
}
