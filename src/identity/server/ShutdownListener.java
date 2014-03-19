package identity.server;

/**Listen for CTRL+c, wite the database (serialized object), pause for an arbitrary time and end.
 * @author Jacob Bennett & Pallavi Khandekar
 * 
 */
public class ShutdownListener extends Thread {

	private IdServer objIdServer = null;
	
	/**
	 * Pass in the server object so we can use its functions to save the database
	 * @param srcIdServer the server object
	 */
	public ShutdownListener(IdServer srcIdServer) {
	objIdServer = srcIdServer;
	}
	
	/**
	 * When the thread is started, the 'run' function begins.
	 * distinguish verbose command line parameter's boolean
	 * if true, print the act of writing the DB to disk.
	 */
	 public void run() {
		
		try {
			if(IdServer.isVerbose)
			{
				System.out.println("Writing data to Disk...");
			}
			objIdServer.getDatabase().writetoDisk();
			if(IdServer.isVerbose)
			{
				System.out.println("Finished writing data to Disk...");
			}
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		
	 }
	
}
