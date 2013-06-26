//******************************************************************************
//
// File:   GPSOffice.java
//
//
//******************************************************************************
import java.io.IOException;
import java.rmi.RemoteException;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NoSuchObjectException;
import edu.rit.ds.registry.AlreadyBoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class GPSOffice determines the office class and consists of GPSOffices at 
 * different locations. An incoming package is checked, and forwarded to the nerest
 * office if its closer than its own distance.
 * <P>
 * Usage:java Start GPSOffice <host> <port> <name> <X> <Y> <host> = Registry Server's
 * host <port> = Registry Server's port <name> = Name of the GPS Office <X> = X
 * co-ordinate of the destination. <Y> = Y co-ordinate of the destination
 * 
 * @author Pratik Rasam
 * @version 01-Apr-2013
 */
public class GPSOffice implements GPSOfficeRef {
	private int port;
	private boolean officeFail;
	private String host;


	private String name;
	private double x;
	private double y;

	private RegistryProxy registry;
	private HashMap<String, Double> nearList;
	private HashMap<String, GPSOfficeInfo> nearListInfo;
	private List<String> nbList;
	private ScheduledExecutorService pooler;
	private RemoteEventGenerator<PackageEvent> notifyEvent;

	/**
	 * Construct a new GPSOffice assigning values for host,port,name and <x,y> coordinates
	 * for the GPSOffice
	 * 
	 * @param args  Command line arguments.
	 */
	public GPSOffice(String[] args) throws IOException {
		if (args.length != 5) {
			usage();
		}
		host = args[0];
		port = Integer.parseInt(args[1]);
		name = args[2];
		this.x = Double.parseDouble(args[3]);
		this.y = Double.parseDouble(args[4]);
        officeFail = false;
		// Generating Proxy for Registry Server
		registry = new RegistryProxy(host, port);
		// Export
		UnicastRemoteObject.exportObject(this, 0);
		// Bind to Server
		notifyEvent = new RemoteEventGenerator<PackageEvent>();
		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException exc2) {
			}
			throw new IllegalArgumentException("Office(): <name> = \"" + name
					+ "\" already exists");
		} catch (RemoteException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException exc2) {
			}
			throw exc;
		}

		nearListInfo = new HashMap<String, GPSOfficeInfo>();
		nearList = new HashMap<String, Double>();
		try {
			nbList = registry.list();
			
			Iterator iter = nbList.iterator();

			while (iter.hasNext()) {
				String temp = (String) iter.next();

				GPSOfficeRef refOffice = null;
				try {
					refOffice = (GPSOfficeRef) registry.lookup(temp);
					refOffice.getNearNeighbours();

				} catch (NotBoundException e) {
				
					e.printStackTrace();
				}
			}
			this.getNearNeighbours();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pooler = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Return Office Name
	 * @param name
	 */
	String getName() {
		return name;
	}

	/**
	 * Return Office XCoordinate
	 * @param x
	 */
	public double getX() throws RemoteException {
		return this.x;
	}

	/**
	 * Return Office YCoordinate
	 * @param y
	 */
	public double getY() throws RemoteException {
		return this.y;
	}

	/**
	 * Return the nearest office for forward the Package
	 * @param HashMap<String,Double>nearList
	 * @param Package p
	 * @return minOffice Nearest office from source GPSOffice
	 */
	public String getNearest(HashMap<String, Double> nearList, Package p) {

		double pX = p.getX();
		double pY = p.getY();
		double distance;
		distance = Math.sqrt((Math.pow((this.x - pX), 2))
				+ (Math.pow((this.y - pY), 2)));

		Set<String> keys = nearListInfo.keySet();
        String minOffice = this.getName();
		Double minDistance = distance;
		Iterator keyIter = keys.iterator();
		while (keyIter.hasNext()) {
			String neighbor = (String) keyIter.next();

			if (nearListInfo.get(neighbor).getDistance(p) < minDistance) {

				minOffice = neighbor;
				minDistance = nearListInfo.get(neighbor).getDistance(p);

			}
		}

		return minOffice;
	}

	/**
	 * Get Neighbouring list from network
	 *  This function fetches the neighboring offices and stores the minimum distance neighbors for a particular
	 *  office
	 */
	public void getNearNeighbours() throws RemoteException {
		double distance;
		nbList = registry.list();
		Iterator iter = nbList.iterator();

		while (iter.hasNext()) {
			String temp = (String) iter.next();
			if (nearList.containsKey(temp)) {
				continue;
			}
			GPSOfficeRef refOffice = null;
			try {
				refOffice = (GPSOfficeRef) registry.lookup(temp);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			double nbX = refOffice.getX();
			double nbY = refOffice.getY();
			if (temp.equals(this.name)) {
				continue;
			}

			distance = Math.sqrt((Math.pow((this.x - nbX), 2))
					+ (Math.pow((this.y - nbY), 2)));

			if (nearList.size() < 3 && (distance != 0)) {

				nearList.put(temp, distance);
				nearListInfo.put(temp, new GPSOfficeInfo(nbX, nbY));

			} else if (nearList.size() == 3 && (distance != 0)) {
				Set<String> keys = nearList.keySet();
				String maxNode = temp;
				Double maxDistance = distance;
				Iterator keyIter = keys.iterator();
				while (keyIter.hasNext()) {
					String neighbor = (String) keyIter.next();
					if (nearList.get(neighbor) > maxDistance) {
						maxNode = neighbor;
						maxDistance = nearList.get(neighbor);

					}
				}
				if (nearList.containsKey(maxNode)) {

					nearList.remove(maxNode);
					nearListInfo.remove(maxNode);
					nearList.put(temp, distance);
					nearListInfo.put(temp, new GPSOfficeInfo(nbX, nbY));
				}
			}
		}

	}
	/**
	 * Forwarding function for packing routing
	 * @param Package p The package to be forwarded
	 * 
	 * This function notifies the arrival of packages and their departures to the appropriate Customers listening 
	 * to the office. Every request gets forwarded to a new thread pool. Nearest neighbor is fetched and forwarded 
	 * accordingly
	 */
	public void send(final Package p)
			throws RemoteException {
		final String name = this.getName();
		notifyEvent.reportEvent(new PackageEvent("Package number "
				+ p.getTrackingNumber() + " arrived at " + this.getName(),
				p.getTrackingNumber(), this.getName(), "Arrived",p));
		
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		nbList.clear();
		nbList = registry.list();
		this.getNearNeighbours();
		String currentNearest = this.getNearest(nearList, p);
		final String nearest;
		if(!nbList.contains(currentNearest))
		{
			this.nearList.remove(currentNearest);
			this.nearListInfo.remove(currentNearest);
			
			nearest =  this.getNearest(nearList, p);
		}
		else
		{
			nearest = currentNearest;
		}
		
		if (nearest == this.getName()) {
			notifyEvent.reportEvent(new PackageEvent("Package number "
					+ p.getTrackingNumber() + " delivered from "
					+ this.getName() + " office to " + "(" + p.getX() + ","
					+ p.getY()+")", p.getTrackingNumber(), this.getName(), "Delivered",p));
			return;
			
		}
		else
		{
		notifyEvent.reportEvent(new PackageEvent("Package number " + p.getTrackingNumber()
				+ " departed from " + this.getName(), p.getTrackingNumber(), this.getName(),
				"Departed",p));
		}
	
		pooler.execute(new Runnable() {
			public void run() {
				try {
					GPSOfficeRef refOffice = (GPSOfficeRef) registry
							.lookup(nearest);
					refOffice.send(p);
				} catch (RemoteException e) {
					notifyEvent.reportEvent(new PackageEvent("Package number "
							+ p.getTrackingNumber() + " lost by " + name,p.getTrackingNumber(),
							nearest, "Lost",p));
					officeFail=true;
					nearList.remove(nearest);
					nearListInfo.remove(nearest);
					
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					notifyEvent.reportEvent(new PackageEvent("Package number "
							+ p.getTrackingNumber() + " lost by " + name, p.getTrackingNumber(),
							nearest, "Lost",p));
					officeFail=true;
					nearList.remove(nearest);
					nearListInfo.remove(nearest);
				}
			}
		});
         if(officeFail)
         {
        	 this.getNearNeighbours();
        	 officeFail= false;
         }
	}

	/**
	 * Return the tracking number for a particular package
	 * @param Package p Package for fetching tracking id
	 */
	public long getTrackingNumber(Package p) throws RemoteException {
		p.setTrackingNumber(System.currentTimeMillis());
		return p.getTrackingNumber();

	}
	/**
	 * The Node on initialization reports to the appropriate listener
	 * @param RemoteEventListener<PackageEvent> event Remote listener for the event
	 * 
	 */
	public Lease addListener(RemoteEventListener<PackageEvent> event)
			throws RemoteException {
		return notifyEvent.addListener(event);
	}
	/**
	 * Print error message and exit.
	 */
	private static void usage() {
		System.out.println("Usage:java Start GPSOffice <host> <port> <name> <X> <Y>");
		System.exit(1);
	}
}
