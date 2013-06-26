//******************************************************************************
//
// File:   PackageEvent.java
//
//
//******************************************************************************
import edu.rit.ds.RemoteEvent;
/**
 * Class PackageEvent provides information about the event generated.
 * 
 * @author Pratik Rasam
 * @version 01-Apr-2013
 */
public class PackageEvent extends RemoteEvent {
	public String officeName;
	public long trackingNumber;
	public String status;
	public String Message;
	/**
	 * Constructor for PackageEvent
	 * @param Message Message to display
	 * @param trackingnumber Package trackingnumber
	 * @param officeName Name of GPSOffice
	 * @param status States if package arrived, departed, delivered or failed
	 * @param p Package for which event responds
	 */
	public PackageEvent(String Message, long trackingnumber, String officeName,
			String status,Package p) {
		this.Message = Message;
		this.officeName = officeName;
		this.trackingNumber = p.getTrackingNumber();
		if (status == "Departed")
			this.status = "Departed";
		else if (status == "Arrived")
			this.status = "Arrived";
		else if (status == "Delivered")
			this.status = "Delivered";
		else if (status == "Lost")
			this.status = "Lost";
	}
}
