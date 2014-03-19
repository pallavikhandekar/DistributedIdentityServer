package election_algorithm;

import election_algorithm.Election.MessageType;

public class MessageHandlerThread extends Thread {

	private Election objElection;
	private Message objMessage;
	private static final String VER_SEPERATOR = ".";
	Message m = null;

	public MessageHandlerThread(Election srcObjElection, Message srcObjMessage) {
		objElection = srcObjElection;
		objMessage = srcObjMessage;
	}
	
	public MessageHandlerThread(Election srcObjElection, Message srcObjMessage, int port) {
		objElection = srcObjElection;
		objMessage = srcObjMessage;
	}

	public void run() {
		processMessage();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void processMessage() {
		//Depending upon type of Message set parameters.
		switch (objMessage.getType()) {
		
			case NEWCOMER://Received from Server
				objElection.setNumberOfServers(objElection.getNumberOfServers() +1);
				break;
				
				
			case ELECTION: //Received from Server
				if (objMessage.getId() < objElection.getId()) {
					System.out.println("M bigger");
					m = new Message(objElection.getId(), MessageType.OK, objElection.getLocalHostAddress());
					objElection.setObjMessage(m);
					objElection.SendMessage(objElection.getMulticastServerPort());
				} else {
					if(objElection.isCoordinater)
					{
						System.out.println("I am smaller but I was coordinator");
						objElection.getCheckPointTimer().stop();
    					//Send List of updated users to Servers on multicast.
						m = new Message(objElection.getId(), MessageType.COPY_LIST_OF_SERVERS, objElection.getLocalHostAddress());
						objElection.setVersion(objElection.getVersion() + 1);
						m.setVersion(objElection.getId() + VER_SEPERATOR + objElection.getVersion()); 
						m.setListOfUpdatedUsers(objElection.getDb().getLstUserData());
						objElection.setObjMessage(m);
						objElection.SendMessage(objElection.getMulticastServerPort());

						while(!objElection.isPollOnCopyConfirmation())
						{
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						objElection.setPollOnCopyConfirmation(false);
						objElection.getDb().writetoDisk();
						m = new Message(objElection.getId(), MessageType.COMMIT, objElection.getLocalHostAddress());
						m.setVersion(objElection.getId() + VER_SEPERATOR + objElection.getVersion()); 
						objElection.setObjMessage(m);
						objElection.SendMessage(objElection.getMulticastServerPort());
					}
					else
					{
						System.out.println("M smaller");
					}
				}
				break;
				
				
			case I_AM_COORDINATOR: //Received from Server
				objElection.setCurrentCoordinatorHostAddress(objMessage.getLocalHostAddress()); 
				objElection.setCurrentCoordinatorPort(objMessage.getId());
				objElection.isCoordinater = false;
				if(objElection.getMulticastServerPort() == 0) //is client
				{
					objElection.setCoordinatorReceived(true);
				}
				break;
				
				
			case OK: //Received from Server
				objElection.isCoordinater = false;
				objElection.getCheckPointTimer().stop();
				break;
				
				
			case WHO_IS_COORDINATOR: //received from client
				System.out.println(objElection.isCoordinater);
				if (objElection.isCoordinater) {
					System.out.println("M co-ordinator");
					Message m = new Message(objElection.getId(), MessageType.I_AM_COORDINATOR, objElection.getLocalHostAddress());
					objElection.setObjMessage(m);
					objElection.SendMessage(objElection.getMulticastClientPort());
				}
				break;
				
			case DONE:
				//Only the server who sent the message should keep track of DONE responses.
				if(objElection.isCoordinater && (objMessage.getVersion().substring(0, 3).equals(objElection.getId())))
				{
					objElection.setDoneMessageCount(objElection.getDoneMessageCount() + 1);
					if(objElection.getDoneMessageCount() == (objElection.getNumberOfServers() -1))
					{
						objElection.setPollOnCopyConfirmation(true);
						objElection.setDoneMessageCount(0);
					}
				}
				break;
				
			case COPY_LIST_OF_SERVERS:
				objElection.getDb().setLstUserData(objMessage.getListOfUpdatedUsers());
				m = new Message(objElection.getId(), MessageType.DONE, objElection.getLocalHostAddress());
				m.setVersion(objMessage.getVersion());
				objElection.setObjMessage(m);
				objElection.SendMessage(objElection.getMulticastServerPort());
				
			case COMMIT:
				objElection.getDb().writetoDisk();
				
			default:
				break;
		}
	}
}
