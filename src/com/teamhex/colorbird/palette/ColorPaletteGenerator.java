package com.teamhex.colorbird.palette;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * public class <b>ColorPaletteGenerator</b>
 * <br><br><br>
 * <b>Class Overview</b>
 * <br><br>
 * {@code ColorPaletteGenerator} contains static methods and classes for deriving representative 
 * colors from an array of colors.
 * <br><br>
 * Representative colors are generated using the standard hard k-means clustering algorithm, such
 * that each point belongs to the single closest cluster based on Euclidean distance. Clusters are
 * then sorted by Z-ordering to obtain a more aesthetically pleasing palette. Because k-means 
 * clustering only produces a local minimum, there is no guarantee that the same clusters will be
 * returned for any two identical inputs. However, because the means are initially seeded using 
 * the k-means++ algorithm, the results should be very similar.
 * <br><br>
 * Individual colors are stored in ARGB format as {@code int} values, such that bits 0-7 indicate
 * the blue value, bits 8-15 represent the green value, bits 16-23 represent the red value, and
 * bits 24-31 represent the alpha value. This representation is compatible with both 
 * {@link android.graphics.Color} and {@link java.awt.Color}.
 */
public class ColorPaletteGenerator 
{
	/**
	 * public static class <b>ColorSet</b>
	 * <br><br><br>
	 * <b>Class Overview</b>
	 * <br><br>
	 * {@code ColorSet} represents a set of one or more colors that defines a cluster.
	 * <br><br>
	 * Because only the mean value of a cluster is used for the k-means clustering algorithm,
	 * {@code ColorSet} does not store the individual colors that it contains. Rather, it sums up
	 * the individual ARGB values and divides by the number of colors in the set to obtain the 
	 * mean value for the cluster.
	 */
	public static class ColorSet
	{
		public ColorSet() { clear(); }
		public ColorSet(int i) { set(i); }
		private ColorSet(double a, double r, double g, double b, int n) { _A = a; _R = r; _G = g; _B = b; _n = n; }
		
		public void set(int i) 
		{ 
			clear(); 
			add(i); 
		}
		
		public void add(int i) 
		{ 
			_A += (i >> 24); 
			_R += ((i >> 16) & 0xFF); 
			_G += ((i >> 8) & 0xFF); 
			_B += (i & 0xFF); 
			_n++;
		}
		
		public ColorSet mean() 
		{ 
			return new ColorSet(_A/_n, _R/_n, _G/_n, _B/_n, _n); 
		}
		
		public void clear() 
		{ 
			_A = 0; _R = 0; _G = 0; _B = 0; _n = 0; 
		}
		
		public int toInt() 
		{ 
			return (((int)_A & 0xFF) << 24) | 
				   (((int)_R & 0xFF) << 16) | 
				   (((int)_G & 0xFF) << 8)  | 
				    ((int)_B & 0xFF); 
		}
		
		public static double dist(ColorSet v1, ColorSet v2)
		{
			return (v1._A - v2._A) * (v1._A - v2._A) +
				   (v1._R - v2._R) * (v1._R - v2._R) +
				   (v1._G - v2._G) * (v1._G - v2._G) +
				   (v1._B - v2._B) * (v1._B - v2._B);
		}
		
		public static double dist(int v1, ColorSet v2)
		{
			return ((v1 >> 24) - v2._A) * ((v1 >> 24) - v2._A) +
				   (((v1 >> 16) & 0xFF) - v2._R) * (((v1 >> 16) & 0xFF) - v2._R) +
				   (((v1 >> 8) & 0xFF) - v2._G) * (((v1 >> 8) & 0xFF) - v2._G) +
				   ((v1 & 0xFF) - v2._B) * ((v1 & 0xFF) - v2._B);
		}
		
		private double _A, _R, _G, _B;
		private int _n;
	};
	
	/**
	 * private static {@link Comparator}<{@link Integer}> <b>ColorComparator</b>
	 * <br><br><br>
	 * Compares two objects based on the natural ordering of their three-dimensional Z-order 
	 * curves.
	 */
	private static Comparator<Integer> ColorComparator = new Comparator<Integer>()
	{
		public int dilate3(int i)
		{
			return (i&0x01)|((i&0x02)<<2)|((i&0x04)<<4)|((i&0x08)<<6)|((i&0x10)<<8)|((i&0x20)<<10)|((i&0x40)<<12)|((i&0x80)<<14);
		}
		public int zOrder(int i)
		{
			int r = ((i >> 16) & 0xFF);
			int g = ((i >> 8) & 0xFF);
			int b = i & 0xFF;
			return dilate3(r) + (dilate3(g) << 1) + (dilate3(b) << 2);
		}
		
	    @Override
	    public int compare(Integer i1, Integer i2) 
	    {
	    	int z1 = zOrder(i1);
	    	int z2 = zOrder(i2);
	    	
	        return z1 - z2;
	    }
	};
	
	/**
	 * public static {@code int[]} <b>colorAlgorithm</b>({@code int[] pixels}, {@code int k})
	 * <br><br><br>
	 * Computes a color palette of size {@code k} using k-means clustering.
	 * <br><br>
	 * @param pixels - an array of color values over which to generate the palette.
	 * @param k - the number of colors to be included in the palette.
	 * @return <li>An {@code int[]} array with {@code k} elements that represents the colors of 
	 * the palette. Because k-means clustering returns a local (and not a global) minimum, there 
	 * is no guarantee that the same color palette will be returned for any two identical inputs.
	 * <li>If it is not possible to generate a palette with {@code k} elements, {@code null} is
	 * returned instead.
	 */
	public static int[] colorAlgorithm(int[] pixels, int k)
	{
		int n = pixels.length;
		if ((k < n) && (k > 0))
		{
			Random rand = new Random();
			
			ColorSet[] means = new ColorSet[k];
			for (int i = 0; i < k; i++)
			{
				means[i] = new ColorSet();
			}
			
			
			//k-means++ initialization
			double[] distances = new double[n];
			
			means[0].set(pixels[rand.nextInt(n)]);
			for (int i = 1; i < k; i++)
			{
				double sum = 0;
				for (int j = 0; j < n; j++)
				{
					double mindist = ColorSet.dist(pixels[j], means[0]);
					for (int a = 1; a < i; a++)
					{
						double dist = ColorSet.dist(pixels[j], means[a]);
						if (dist < mindist)
						{
							mindist = dist;
						}
					}
					distances[j] = mindist;
					sum += mindist;
				}
				double p = rand.nextDouble() * sum;
				sum = 0;
				for (int j = 0; j < n; j++)
				{
					sum += distances[j];
					if (sum > p)
					{
						means[i].set(pixels[j]);
						break;
					}
				}
			}
			
			ColorSet[] sets = new ColorSet[k];
			for (int i = 0; i < k; i++)
			{
				sets[i] = new ColorSet();
			}
			
			double prevdist = 0;
			double maxdist = 0;
			do
			{
				for (int i = 0; i < n; i++)
				{
					int min = 0;
					for (int j = 1; j < k; j++)
					{
						if (ColorSet.dist(pixels[i], means[j]) <= ColorSet.dist(pixels[i], means[min]))
						{
							min = j;
						}
					}
					sets[min].add(pixels[i]);
				}
				
				prevdist = maxdist;
				maxdist = 0;
				for (int i = 0; i < k; i++)
				{
					ColorSet mean = sets[i].mean();
					double meandist = ColorSet.dist(means[i], mean);
					if (meandist > maxdist)
					{
						maxdist = meandist;
					}
					means[i] = mean;
					sets[i].clear();
				}
				if (maxdist == 0)
				{
					break;
				}
			}
			while ((maxdist < prevdist) || (prevdist == 0));
			
			Integer[] colors = new Integer[k];
			for (int i = 0; i < k; i++)
			{
				colors[i] = means[i].toInt();
			}
			Arrays.sort(colors, ColorComparator);
			
			int[] colors2 = new int[k];
			for (int i = 0; i < k; i++)
			{
				colors2[i] = colors[i];
			}
			return colors2;
		}
		
		return null;
	}
}
