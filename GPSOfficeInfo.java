//******************************************************************************
//
// File:   GPSOfficeInfo.java
//
//
//******************************************************************************
/**
 * Class GPSOfficeInfo provides information about neighboring offices and their coordinates .
 * 
 * @author Pratik Rasam
 * @version 01-Apr-2013
 */
public class GPSOfficeInfo {
	private double x;
	private double y;
	/** Get the Y-Coordinate for a Package
	 *  @return y 
	 */
	public GPSOfficeInfo(double x, double y) {
		this.x = x;
		this.y = y;
	}
	/** Get the X-Coordinate for a Package
	 *  @return x 
	 */
	public double getX() {
		return this.x;

	}
	/** Get the Y-Coordinate for a Package
	 *  @return y 
	 */
	public double getY() {
		return this.y;
	}
	/**
	 * Calculate distance with a package destination distance
	 * @param Package p package to find distance
	 * @return distance distance of the node
	 */
	public double getDistance(Package p) {
		double pX = p.getX();
		double pY = p.getY();
		double distance = Math.sqrt((Math.pow((this.x - pX), 2))
				+ (Math.pow((this.y - pY), 2)));
		return distance;
	}
}
