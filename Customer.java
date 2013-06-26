//******************************************************************************
//
// File:   Customer.java
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
 * Class Customer is a main Client program which sends a package to a specific
 * GPS office. The Customer program is aware of the location of the package
 * whenever it is beamed from one office to another.
 * <P>
 * Usage:java Customer <host> <port> <name> <X> <Y> <host> = Registry Server's
 * host <port> = Registry Server's port <name> = Name of the GPS Office <X> = X
 * co-ordinate of the destination. <Y> = Y co-ordinate of the destination
 * 
 * @author Pratik Rasam
 * @version 01-Apr-2013
 */
public class Customer {

	// Prevent Construction
	private static String host;
	private static int port;
	private long trackingNumber;

	private Package myPackage;

	private static RegistryProxy registry;
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;
	private static RemoteEventListener<PackageEvent> nodeListener;

	private Customer() {
		// this.trackingNumber = tracking;
	}

	// Main Program
	/**
	 * Main program
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 5)
			usage();
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String name = args[2];
		double X = Double.parseDouble(args[3]);
		double Y = Double.parseDouble(args[4]);
		// Connecting to Registry
		long trackingNumber = 0;

		final Customer thisCustomer = new Customer();
		Package p = new Package(X, Y);
		registry = new RegistryProxy(host, port);

		registryListener = new RegistryEventListener() {

			@Override
			public void report(long seqnum, RegistryEvent event)
					throws RemoteException {
				// TODO Auto-generated method stub
				getPacketInfo(event.objectName());
			}

		};
		UnicastRemoteObject.exportObject(registryListener, 0);
		nodeListener = new RemoteEventListener<PackageEvent>() {
			public void report(long seqnum, PackageEvent event) {
				if (thisCustomer.myPackage.trackingNumber == event.trackingNumber) {
					System.out.println(event.Message);
					
					if (event.status.equals("Delivered")) {
						System.exit(0);
						
					}
					else if (event.status.equals("Lost"))
						System.exit(0);
				}
			}
		};
		UnicastRemoteObject.exportObject(nodeListener, 0);
		registryFilter = new RegistryEventFilter().reportType("GPSOffice")
				.reportBound();
		registry.addEventListener(registryListener, registryFilter);
		for (String officeObjects : registry.list("GPSOffice")) {
			getPacketInfo(officeObjects);
		}

		GPSOfficeRef office = (GPSOfficeRef) registry.lookup(name);
		long tracker = office.getTrackingNumber(p);
		thisCustomer.myPackage = new Package(X, Y);
		thisCustomer.myPackage.setTrackingNumber(tracker);
		thisCustomer.myPackage.isSet = true;
		office.send(thisCustomer.myPackage);

	}

	private static void getPacketInfo(String objectName) {
		try {
			GPSOfficeRef office = (GPSOfficeRef) registry.lookup(objectName);
			office.addListener(nodeListener);
		} catch (NotBoundException exc) {
		} catch (RemoteException exc) {
		}
	}

	/**
	 * Print error message and exit.
	 */
	private static void usage() {
		System.out.println("Usage:java Customer <host> <port> <name> <X> <Y>");
		System.exit(1);
	}
}
