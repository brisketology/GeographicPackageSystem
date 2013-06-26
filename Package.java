//******************************************************************************
//
// File:   Package.java
//
//
//******************************************************************************
import edu.rit.ds.registry.RegistryProxy;
import java.io.Serializable;
/**
 * Class Package provides information about package as well as configuring properties 
 * of the package object
 * 
 * @author Pratik Rasam
 * @version 01-Apr-2013
 */
public class Package implements Serializable{

	public long trackingNumber;
	public  boolean isSet;
	private double xCordinate;
	private  double yCordinate;
	/**
	 * Constructor for Package
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	Package(double x,double y)
	{
		isSet=false;
		xCordinate=x;
		yCordinate=y;
	}
	double getX()
	{
		return this.xCordinate;
	}
	double getY()
	{
		return this.yCordinate;
	}
	void setTrackingNumber(long number)
	{
		isSet=true;
		this.trackingNumber= number;
		
	}
	long getTrackingNumber()
	{
		return this.trackingNumber;
	}
	public boolean equals(Object ob)
	{
		if((ob instanceof Package)&&
					(this.trackingNumber==((Package)ob).trackingNumber))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
