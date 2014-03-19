package election_algorithm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receives message from other servers
 *
 * @author
 *
 */
public class MessageReceiverThread extends Thread {

  private int poolSize = 8;
  private Election objElection;
  private MulticastSocket listener;
  private int multicastListnerPort;
  DatagramPacket packet;

  public MessageReceiverThread(Election srcObjElection, int srcPort) {
    objElection = srcObjElection;
    multicastListnerPort = srcPort;
  }

  public void setListener() {
    try {
      listener = new MulticastSocket(multicastListnerPort);
      listener.setSoTimeout(5000);
    } catch (IOException ex) {
      Logger.getLogger(MessageReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void run() {
    ByteArrayInputStream bis = null;
    ObjectInputStream ois = null;
    try {
      if (listener == null) {
        listener = new MulticastSocket(multicastListnerPort);
      }
      listener.joinGroup(InetAddress.getByName(objElection.getMulticastGroupAddress()));
      byte buffer[] = new byte[1024];
      packet = new DatagramPacket(buffer, buffer.length);
      bis = new ByteArrayInputStream(buffer);
      ois = new ObjectInputStream(bis);
      ExecutorService objThreadPool = Executors.newFixedThreadPool(poolSize);
      do {
           listener.receive(packet);  
           Message objMessage = (Message) ois.readObject();

        //Ignore messages sent by itself.
        if (objMessage.getId() != objElection.getId()) {
             System.out.println("Processing message: " + objMessage.getType() + " from " + objMessage.getLocalHostAddress()+objMessage.getId());
          objThreadPool.execute(new MessageHandlerThread(objElection, objMessage));
        }
      } while (true);
    } catch (SocketTimeoutException ste) {
      System.err.println("caught " + ste.getMessage());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        if (ois != null) {
          ois.close();
        }
        if (bis != null) {
          bis.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
