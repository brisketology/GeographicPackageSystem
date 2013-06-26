//******************************************************************************
//
// File:   Headquarters.java
//
//
//******************************************************************************
import java.util.*;
import edu.rit.ds.registry.RegistryProxy;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 * Class Headquarters monitors over all the packages getting transferred via the network
 * <P>
 * Usage:java Customer <host> <port> <name> <X> <Y>
 * <host> = Registry Server's host
 * <port> = Registry Server's port
 * @author  Pratik Rasam
 * @version 01-Apr-2013
 */
public class Headquarters {

private static String host;
private static int port;
private static RegistryProxy registry;
private static RegistryEventListener registryListener;
private static RegistryEventFilter registryFilter;
private static RemoteEventListener<PackageEvent> nodeListener;	

	/**
	 * Main program
	 * Program prints out notifications from all the GPSOffices in the network.
	 */
	public static void main (String[] args) throws Exception
	   {
		if(args.length!=2)usage();
	    String host = args[0];
	    int port = Integer.parseInt(args[1]);

           registry = new RegistryProxy (host, port);
	     
	       registryListener = new RegistryEventListener(){

			@Override
			public void report(long seqnum, RegistryEvent event)
					throws RemoteException {
				// TODO Auto-generated method stub
				 getPacketInfo (event.objectName());
			}
	    	   
	       };
	       UnicastRemoteObject.exportObject(registryListener,0);
	       nodeListener = new RemoteEventListener<PackageEvent>(){
	    	   public void report(long seqnum,PackageEvent event){
	    		   System.out.println(event.Message);
	    	   }
	       };
	       UnicastRemoteObject.exportObject(nodeListener,0);
	       registryFilter = new RegistryEventFilter().reportType("GPSOffice").reportBound();
	       registry.addEventListener(registryListener,registryFilter);
	       for(String office:registry.list("GPSOffice"))
	       {
	    	   getPacketInfo(office);
	       }
	       
	   }
	/**
	 * Node listener listens to the object specified by the parmeters of this function
	 * @param objectName
	 */
	private static void getPacketInfo
    (String objectName)
    {
    try
       {
       GPSOfficeRef office = (GPSOfficeRef) registry.lookup (objectName);
       office.addListener (nodeListener);
       }
    catch (NotBoundException exc)
       {
       }
    catch (RemoteException exc)
       {
       }
    }
	/**
	 * Print error message and exit.
	 */
	private static void usage()
	    {
		System.out.println("Usage:java Headquarters <host> <port>");
		System.exit(1);
	    }
}
