//******************************************************************************
//
// File:   GPSOfficeRef.java
//
//
//******************************************************************************
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import java.rmi.RemoteException;
import java.rmi.Remote;


/**
 * GPSOfficeRef interface specifies the Java Remote interface for the distributed
 * GPSOffice object in the Gepgraphic Package System.
 */
public interface GPSOfficeRef extends Remote{
    public void send(Package p) throws RemoteException;  

    /**
	 * Return Office XCoordinate
	 * @param x X-Coordinate
	 */
    public double getX() throws RemoteException;
    /**
	 * Get Neighbouring list from network
	 *  This function fetches the neighboring offices and stores the minimum distance neighbors for a particular
	 *  office
	 */
    public void getNearNeighbours() throws RemoteException;
    /**
	 * Return Office XCoordinate
	 * @param y -Coordinate
	 */
    public double getY() throws RemoteException;
    /**
	 * Return the tracking number for a particular package
	 * @param Package p Package for fetching tracking id
	 */
    public long getTrackingNumber(Package p)throws RemoteException;
    /**
	 * The Node on initialization reports to the appropriate listener
	 * @param RemoteEventListener<PackageEvent> event Remote listener for the event
	 * 
	 */
    public Lease addListener(RemoteEventListener<PackageEvent> event) throws RemoteException;
}
