package util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Interpolator implements Cloneable {

	private TreeMap<Double, Double> sortMap = new TreeMap<Double,Double>();

	/**
	 * Construct a <code>LinearInterpolator</code> with no points.
	 */
	public Interpolator() {
	}
	
	public Interpolator(double ... points) {
		if (points.length%2 != 0) {
			throw new IllegalArgumentException("Requires even number of parameters");
		}
		for (int i=0; i<points.length; i+=2) {
			addPoint(points[i], points[i+1]);
		}
	}
	
	/**
	 * Construct a <code>LinearInterpolator</code> with the given points.
	 * 
	 * @param x		the x-coordinates of the points.
	 * @param y		the y-coordinates of the points.
	 * @throws IllegalArgumentException		if the lengths of <code>x</code> and <code>y</code>
	 * 										are not equal.
	 * @see #addPoints(double[], double[])
	 */
	public Interpolator(double[] x, double[] y) {
		addPoints(x,y);
	}
	
	
	/**
	 * Add the point to the linear interpolation.
	 * 
	 * @param x		the x-coordinate of the point.
	 * @param y		the y-coordinate of the point.
	 */
	public void addPoint(double x, double y) {
		sortMap.put(x, y);
	}
	
	/**
	 * Add the points to the linear interpolation.
	 * 
	 * @param x		the x-coordinates of the points.
	 * @param y		the y-coordinates of the points.
	 * @throws IllegalArgumentException		if the lengths of <code>x</code> and <code>y</code>
	 * 										are not equal.
	 */
	public void addPoints(double[] x, double[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Array lengths do not match, x="+x.length +
					" y="+y.length);
		}
		for (int i=0; i < x.length; i++) {
			sortMap.put(x[i],y[i]);
		}
	}
	
	
	public void removePoint(double x) {
		sortMap.remove(x);
	}
	
	public void clear() {
		sortMap.clear();
	}
	
	
	public double getValue(double x) {
		Map.Entry<Double,Double> e1, e2;
		double x1, x2;
		double y1, y2;
		
		if (Double.isNaN(x))
			return Double.NaN;
		
		e1 = sortMap.floorEntry(x);
		
		if (e1 == null) {
			// x smaller than any value in the set
			e1 = sortMap.firstEntry();
			if (e1 == null) {
				return Double.NaN;
			}
			e2 = sortMap.higherEntry(e1.getKey());
			if (e2 == null) {
				// only one value in the set
				return e1.getValue();
			}
		} else {
			
			e2 = sortMap.higherEntry(e1.getKey());
			if (e2 == null) {
				// x larger than any value in the set
				e2 = e1;
				e1 = sortMap.lowerEntry(e2.getKey());
				if (e1 == null) {
					// only one value in the set
					return e2.getValue();
				}
			}
		}
		
		x1 = e1.getKey();
		x2 = e2.getKey();
		y1 = e1.getValue();
		y2 = e2.getValue();
		
		return (x - x1)/(x2-x1) * (y2-y1) + y1;
	}
	
	
	public double[] getXPoints() {
		double[] x = new double[sortMap.size()];
		Iterator<Double> iter = sortMap.keySet().iterator();
		for (int i=0; iter.hasNext(); i++) {
			x[i] = iter.next();
		}
		return x;
	}
	

	public double[] getYPoints() {
		double[] y = new double[sortMap.size()];
		Iterator<Double> iter = sortMap.values().iterator();
		for (int i=0; iter.hasNext(); i++) {
			y[i] = iter.next();
		}
		return y;
	}
	
	
	public Iterator<Map.Entry<Double, Double>> iterator() {
		return sortMap.entrySet().iterator();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Interpolator clone() {
		try {
			Interpolator other = (Interpolator)super.clone();
			other.sortMap = (TreeMap<Double,Double>)this.sortMap.clone();
			return other;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("CloneNotSupportedException?!",e);
		}
	}

	
	public static void main(String[] args) {
		Interpolator interpolator = new Interpolator(
				new double[] {1, 1.5, 2, 4, 5},
				new double[] {0, 1,   0, 2, 2.5}
		);
		
		for (double x=0; x < 6; x+=0.1) {
			System.out.printf("%.1f:  %.2f\n", x, interpolator.getValue(x));
		}
	}
	
}
