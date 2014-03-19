package election_algorithm;

import identity.server.CheckPointTimer;
import identity.server.Database;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.Timer;


/**
 * This class is used by Server to set its id and to Multicast message to all other servers and receive message from all other servers.
 *
 * The Server/Process will to hold and election will multicast message to rest servers that it wants to lead an election. If it receives 'OK' from all other servers then it will be elected as
 * Coordinator.
 *
 * @author
 *
 */
public class Election {

	private String multicastGroupAddress;
	private Message objMessage;
	private String currentCoordinatorHostAddress;
	private int currentCoordinatorPort;
	private int multicastServerPort;
	private int multicastClientPort;
	private int id;
	
	private String localHostAddress;
	public boolean isCoordinater;
	private boolean coordinatorReceived = false;
	private int numberOfServers;
	private Database db = null;
	private int version = 0;
	private boolean pollOnCopyConfirmation; 
	private int doneMessageCount = 0; 
	private Timer checkPointTimer = null;
	
	
	public enum MessageType {
		NEWCOMER, //SERVER-SERVER on joining multi cast
		ELECTION, //SERVER-SERVER when holding an election
		OK, //SERVER-SERVER Higher id server is running and hence current server cannot be coordinator.
		I_AM_COORDINATOR, //SERVER_SERVER and SERVER-CLIENT Message type sent to all servers indicating who is Coordinator
		WHO_IS_COORDINATOR, //CLIENT-SERVER : Sent by Client to all servers.
		COPY_LIST_OF_SERVERS, // SERVER-SERVER
		DONE,
		COMMIT
	}

	public Election(String srcMulticastGroupAddress, int srcId, int srcServerPort, int srcClientPort, String srcLocalHostAddress, Database srcDb) {
		multicastGroupAddress = srcMulticastGroupAddress;
		id = srcId;
		multicastServerPort = srcServerPort;
		multicastClientPort = srcClientPort;
		localHostAddress = srcLocalHostAddress;
		db = srcDb;
		checkPointTimer = new Timer(120000,new CheckPointTimer(this)); 
	}


	public String getMulticastGroupAddress() {
		return multicastGroupAddress;
	}

	public void setMulticastGroupAddress(String multicastGroupAddress) {
		this.multicastGroupAddress = multicastGroupAddress;
	}

	public Message getObjMessage() {
		return objMessage;
	}

	public void setObjMessage(Message objMessage) {
		this.objMessage = objMessage;
	}

	public int getMulticastServerPort() {
		return multicastServerPort;
	}

	public void setMulticastServerPort(int serverPort) {
		this.multicastServerPort = serverPort;
	}

	public int getMulticastClientPort() {
		return multicastClientPort;
	}

	public void setMulticastClientPort(int clientPort) {
		this.multicastClientPort = clientPort;
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
	
	public String getCurrentCoordinatorHostAddress() {
		return currentCoordinatorHostAddress;
	}

	public void setCurrentCoordinatorHostAddress(
			String currentCoordinatorHostAddress) {
		this.currentCoordinatorHostAddress = currentCoordinatorHostAddress;
	}

	public int getCurrentCoordinatorPort() {
		return currentCoordinatorPort;
	}

	public void setCurrentCoordinatorPort(int currentCoordinatorPort) {
		this.currentCoordinatorPort = currentCoordinatorPort;
	}

	public boolean isCoordinatorReceived() {
		return coordinatorReceived;
	}

	public void setCoordinatorReceived(boolean coordinatorReceived) {
		this.coordinatorReceived = coordinatorReceived;
	}

	public int getNumberOfServers() {
		return numberOfServers;
	}

	public void setNumberOfServers(int numberOfServers) {
		this.numberOfServers = numberOfServers;
	}
	
	public Database getDb() {
		return db;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}	
	
	public boolean isPollOnCopyConfirmation() {
		return pollOnCopyConfirmation;
	}

	public void setPollOnCopyConfirmation(boolean pollOnCopyConfirmation) {
		this.pollOnCopyConfirmation = pollOnCopyConfirmation;
	}
	
	public int getDoneMessageCount() {
		return doneMessageCount;
	}

	public void setDoneMessageCount(int doneMessageCount) {
		this.doneMessageCount = doneMessageCount;
	}
	
	public Timer getCheckPointTimer() {
		return checkPointTimer;
	}

	
	//Send Message to other Servers
	public void SendMessage(int srcSendPort) {
		try {
			System.out.println("Sending message " + getObjMessage().getType());
			MulticastSocket ms = new MulticastSocket(srcSendPort);
			ms.joinGroup(InetAddress.getByName(this.multicastGroupAddress));

			byte buffer[] = getBytes(getObjMessage());

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(getMulticastGroupAddress()), srcSendPort);
			ms.send(packet);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public byte[] getBytes(Message m) {
		byte buffer[] = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(m);
			oos.flush();
			bos.flush();
			buffer = bos.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return buffer;
	}
}
