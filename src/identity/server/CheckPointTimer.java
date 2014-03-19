package identity.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import election_algorithm.Election;
import election_algorithm.Message;
import election_algorithm.Election.MessageType;

public class CheckPointTimer implements ActionListener {

	private Election objElection = null;
	Message m = null;
	private static final String VER_SEPERATOR = ".";

	public CheckPointTimer(Election srcObjElection) {
		objElection = srcObjElection;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (IdServer.isVerbose) {
			System.out.println("Writing data to Disk...");
		}
		m = new Message(objElection.getId(), MessageType.COPY_LIST_OF_SERVERS,
				objElection.getLocalHostAddress());
		objElection.setVersion(objElection.getVersion() + 1);
		m.setVersion(objElection.getId() + VER_SEPERATOR + objElection.getVersion());
		m.setListOfUpdatedUsers(objElection.getDb().getLstUserData());
		objElection.setObjMessage(m);
		objElection.SendMessage(objElection.getMulticastServerPort());

		while (!objElection.isPollOnCopyConfirmation()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		objElection.setPollOnCopyConfirmation(false);
		objElection.getDb().writetoDisk();
		m = new Message(objElection.getId(), MessageType.COMMIT,
				objElection.getLocalHostAddress());
		m.setVersion(objElection.getId() + VER_SEPERATOR
				+ objElection.getVersion());
		objElection.setObjMessage(m);
		objElection.SendMessage(objElection.getMulticastServerPort());
		if (IdServer.isVerbose) {
			System.out.println("Finished writing data to Disk...");
		}
		Timer t = (Timer) arg0.getSource();
		t.restart();
	}
}
