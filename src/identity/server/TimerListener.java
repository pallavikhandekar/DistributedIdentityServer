/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package identity.server;

import election_algorithm.Election;
import election_algorithm.Message;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.Timer;

/**
 * Performs task when a Timer delay of 2min is over
 *
 * @author Pallavi K
 *
 */
class TimerListner implements ActionListener {

	Election objElection;
	int rmiregistry;
	Message objMessage;

	public TimerListner(Election srcObjElection, int srcRmiRegistryPort) {
		objElection = srcObjElection;
		rmiregistry = srcRmiRegistryPort;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		System.out.println("Holding election");
		try {
			objMessage = new Message(rmiregistry, Election.MessageType.ELECTION, InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException ex) {

			ex.printStackTrace();
		}
		objElection.isCoordinater = true;
		objElection.getCheckPointTimer().restart();
		objElection.setObjMessage(objMessage);
		objElection.SendMessage(5190);


		Timer t = (Timer) e.getSource();
		t.restart();

	}
}
