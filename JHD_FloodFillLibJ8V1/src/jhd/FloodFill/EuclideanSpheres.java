package jhd.FloodFill;

import java.util.Random;

import javax.swing.JOptionPane;

/**Class for drawing Euclidean spheres scaled in image units */
public class EuclideanSpheres
{
	/**Default constructor*/
	public EuclideanSpheres()

	{
		// TODO Auto-generated constructor stub
	}

	/**Nested class to simplify random sphere draw arguments list*/
	public static class RandomSphereParams
	{
		/**Default Constructor*/
		public RandomSphereParams(){};
		/**Pixel Dimensions in user units*/
		public double pixWidth,pixHeight,pixDepth;
		/**The min and max radii to draw*/
		public double minRadius,maxRadius;
		/**Draw until this volume fraction of spheres is obtained*/
		public double volFrac;
		/**Fill the spheres with this value*/
		public double fillVal;
		/**The user dimension unit*/
		public String unit;	
	}

	/**Nested class to simplify sphere draw arguments list */
	public static class SphereParams
	{
		/**Default Constructor*/
		public SphereParams(){};
		/**Pixel Dimensions in user units*/
		public double pixWidth,pixHeight,pixDepth;
		/**Pixel location in pixel units*/
		public int centerX,centerY,centerZ;
		/**Sphere rasius in user units*/
		public double radius;
		/**Fill the spheres with this value*/
		public double fillVal;
		/**The user dimension unit*/
		public String unit;	
	}

	//***************************************************************************************************

	private void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	//***************************************************************************************************

	/**Draws overlapping spheres of random size at random locations until volFrac is reached.
	 * @param oImageArr An Object[] as returned by <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>
	 * @param imgWidth The image width in pixels
	 * @param imgHeight The image height in pixels
	 * @param imgDepth The image depth (slices) in pixels
	 * @param rsp A RandomSphereParameters data block
	 * @return The number of pixels that have been set to fillVal
	 */
	public int DrawRandomSpheres(Object[] oImageArr,  int imgWidth, int imgHeight, int imgDepth, RandomSphereParams rsp)

	{
		int			imgPixCount, desiredPixCount,failedCount, spherePixCount = 0;
		final int 	MAXFAIL = 10000;

		if(rsp.volFrac>=1.0 || rsp.volFrac<=0.0)
		{
			infoBox("Volume Fraction must be >0 and < 1", "In DrawRandomSpheres");
			return 0;
		}
		
		SphereParams sp = new SphereParams();;
		Random myRandom = new Random();
		long msec = System.currentTimeMillis();
		msec = msec % 10000;
		myRandom.setSeed(msec);
		
		if(rsp.minRadius > rsp.maxRadius)
		{
			double temp = rsp.minRadius;
			rsp.minRadius = rsp.maxRadius;
			rsp.maxRadius=   temp;
		}

		if(rsp.minRadius < 0) rsp.minRadius=0;

		// how many voxels do we need to fill the get the user's volume fraction
		imgPixCount		= imgWidth*imgHeight*imgDepth;
		desiredPixCount = (int) (rsp.volFrac*imgPixCount);

		//Draw spheres until a volume fraction is obtained
		//Set the counters that track the number of sphere draw attempts to 0;
		failedCount=0; // a safety valve
		int pixCnt = 0;
		//int okCnt=0;

		//High volume fractions may result in many failed drawing attempts
		//MAXFAIL prevents runaway loop
		while(spherePixCount < desiredPixCount && failedCount < MAXFAIL)
		{
			// Java Math.random() gives a number between 0 and 1
			//Restricting the sphere origins to the inside the image creates
			//a density bias at the image edges. To avoid this, the sphere origin
			// is allowed to lie up to maxRadius outside of the image
			double pixRadiusX =  rsp.maxRadius/rsp.pixWidth;
			double pixRadiusY =  rsp.maxRadius/rsp.pixHeight;
			double pixRadiusZ =  rsp.maxRadius/rsp.pixDepth;
			
			sp.centerX = (int) (myRandom.nextDouble() * (imgWidth + 2*pixRadiusX)-pixRadiusX);
			sp.centerY = (int) (myRandom.nextDouble() * (imgHeight + 2*pixRadiusY)-pixRadiusY);
			sp.centerZ = (int) (myRandom.nextDouble() * (imgDepth + 2*pixRadiusZ)-pixRadiusZ);

			sp.radius =  myRandom.nextDouble() * (rsp.maxRadius - rsp.minRadius) + rsp.minRadius;
			
//			sp.centerX = (int) (Math.random() * (imgWidth + 2*pixRadiusX)-pixRadiusX);
//			sp.centerY = (int) (Math.random() * (imgHeight + 2*pixRadiusY)-pixRadiusY);
//			sp.centerZ = (int) (Math.random() * (imgDepth + 2*pixRadiusZ)-pixRadiusZ);
//
//			sp.radius =  Math.random() * (rsp.maxRadius - rsp.minRadius) + rsp.minRadius;

			//Copy the other parameters
			sp.fillVal =rsp.fillVal;
			sp.pixWidth = rsp.pixWidth;
			sp.pixHeight = rsp.pixHeight;
			sp.pixDepth = rsp.pixDepth;

			pixCnt = DrawSphere(oImageArr,imgWidth,imgHeight,imgDepth,sp);

			//If part of a sphere was drawn successfully
			if(pixCnt>0)
			{
				spherePixCount += pixCnt;
				//okCnt++;
			}
			else failedCount++;
		}
		//System.out.println("okCnt=" + okCnt);

		if(failedCount>=MAXFAIL)
		{
			infoBox("after " + MAXFAIL + " tries, failed to obtain the requested volume fraction","In DrawRandomSpheres");
		}
		return spherePixCount;
	}

	//***************************************************************************************************

	/**Draws a sphere in user pixel units, sphere origins may lie outside the image.
	 * @param oImageArr An Object[] as returned by 
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>	 
	 * @param imgWidth The image width in pixels
	 * @param imgHeight The image height in pixels
	 * @param imgDepth The image depth (slices) in pixels
	 * @param sp A SphereParams data block
	 * @return The number of voxels set to fillVal, does not include overlapped voxels.
	 */
	public int DrawSphere(Object[] oImageArr, int imgWidth, int imgHeight, int imgDepth, SphereParams sp)
	{
		int i,j,k,r1,top,btm,left,right,front,back,spherePixCount=0,pad=3;
		double a2,b2,c2,r2,dx,dy,dz,v2,voxVal;

		//Make local copy of params to avoid dereferencing
		int x0 = sp.centerX;
		int y0 = sp.centerY;
		int z0 = sp.centerZ;
		double radius = sp.radius;
		double fillVal = sp.fillVal;
		double pixWidth = sp.pixWidth;
		double pixHeight=sp.pixHeight;
		double pixDepth=sp.pixDepth;
		//String unit = sp.unit;
		
		if(radius<0) radius=0;

		//establish limits for sphere sub-volume.
		//The sphere center can lie outside of the volume
		//but only the regions within the volume will be drawn.
		r1 = (int)Math.ceil(radius/pixHeight);
		top = y0-r1-pad;
		if(top<0) top=0;
		btm = y0+r1+pad;

		if(btm>imgHeight) btm=imgHeight;
		r1 = (int)Math.ceil(radius/pixWidth);
		left = x0-r1-pad;
		if(left<0) left=0;
		right = x0+r1+pad;

		if(right>imgHeight) right=imgHeight;
		r1 = (int)Math.ceil(radius/pixDepth);
		front = z0-r1-pad;
		if(front<0) front=0;
		back = z0+r1+pad;
		if(back>imgDepth) back=imgDepth;

		//set up the scaling parameters for the three directions
		a2 = 1/pixWidth;
		a2*=a2;
		b2 = 1/pixHeight;
		b2*=b2;
		c2 = 1/pixDepth;
		c2*=c2;
		r2 = radius*radius;

		if(oImageArr[0] instanceof double[])
		{
			double[] image;
			for(k=front;k<back;k++)
			{
				image= (double[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						//drawing restrictions go here,
						//e.g. only draw in voxels of a particular value etc.						
						//My restriction, do not overwrite existing spheres.
						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImageArr[0] instanceof float[])
		{
			float[] image;
			for(k=front;k<back;k++)
			{
				image= (float[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=(float)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImageArr[0] instanceof long[])
		{			
			//if(fillVal>Long.MAX_VALUE) fillVal=Long.MAX_VALUE;
			//if(fillVal<Long.MIN_VALUE) fillVal=Long.MIN_VALUE;
			long[] image;
			for(k=front;k<back;k++)
			{
				image= (long[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=(long)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImageArr[0] instanceof int[])
		{
			//if(fillVal>Integer.MAX_VALUE) fillVal=Integer.MAX_VALUE;
			//if(fillVal<Integer.MIN_VALUE) fillVal=Integer.MIN_VALUE;
			int[] image;
			for(k=front;k<back;k++)
			{
				image= (int[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=(int)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImageArr[0] instanceof short[])
		{
			//if(fillVal>Short.MAX_VALUE) fillVal=Short.MAX_VALUE;
			//if(fillVal<Short.MIN_VALUE) fillVal=Short.MIN_VALUE;
			short[] image;
			for(k=front;k<back;k++)
			{
				image= (short[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=(short)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImageArr[0] instanceof byte[])
		{
			//if(fillVal>Byte.MAX_VALUE) fillVal=Byte.MAX_VALUE;
			//if(fillVal<Byte.MIN_VALUE) fillVal=Byte.MIN_VALUE;
			byte[] image;
			for(k=front;k<back;k++)
			{
				image= (byte[]) oImageArr[k];
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth]=(byte)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		return spherePixCount;
	}	

	//***************************************************************************************************

	/**Draws overlapping spheres of random size at random locations until volFrac is reached.
	 * @param oImage An Object containing a 1D array from a row major 3D image
	 * @param imgWidth The image width in pixels
	 * @param imgHeight The image height in pixels
	 * @param imgDepth The image depth (slices) in pixels
	 * @param rsp A RandomSphereParameters data block
	 * @return The number of pixels that have been set to fillVal
	 */
	public int DrawRandomSpheres(Object oImage,  int imgWidth, int imgHeight, int imgDepth, RandomSphereParams rsp)

	{
		int			imgPixCount, desiredPixCount,failedCount, spherePixCount = 0;
		final int 	MAXFAIL = 10000;

		if(rsp.volFrac>=1.0 || rsp.volFrac<=0.0)
		{
			infoBox("Volume Fraction must be >0 and < 1", "In DrawRandomSpheres");
			return 0;
		}
		
		SphereParams sp = new SphereParams();;
		Random myRandom = new Random();
		long msec = System.currentTimeMillis();
		msec = msec % 10000;
		myRandom.setSeed(msec);
		
		if(rsp.minRadius > rsp.maxRadius)
		{
			double temp = rsp.minRadius;
			rsp.minRadius = rsp.maxRadius;
			rsp.maxRadius=   temp;
		}

		if(rsp.minRadius < 0) rsp.minRadius=0;

		// how many voxels do we need to fill the get the user's volume fraction
		imgPixCount		= imgWidth*imgHeight*imgDepth;
		desiredPixCount = (int) (rsp.volFrac*imgPixCount);

		//Draw spheres until a volume fraction is obtained
		//Set the counters that track the number of sphere draw attempts to 0;
		failedCount=0; // a safety valve
		int pixCnt = 0;

		//High volume fractions may result in many failed drawing attempts
		//MAXFAIL prevents runaway loop
		while(spherePixCount < desiredPixCount && failedCount < MAXFAIL)
		{
			// Java Math.random() gives a number between 0 and 1
			//Restricting the sphere origins to the inside the image creates
			//a density bias at the image edges. To avoid this, the sphere origin
			// is allowed to lie up to maxRadius outside of the image
			
			sp.centerX = (int) (myRandom.nextDouble() * (imgWidth + 2*rsp.maxRadius)-rsp.maxRadius);
			sp.centerY = (int) (myRandom.nextDouble() * (imgHeight + 2*rsp.maxRadius)-rsp.maxRadius);
			sp.centerZ = (int) (myRandom.nextDouble() * (imgDepth + 2*rsp.maxRadius)-rsp.maxRadius);

			sp.radius =  Math.random() * (rsp.maxRadius - rsp.minRadius) + rsp.minRadius;

//			sp.centerX = (int) (Math.random() * (imgWidth + 2*rsp.maxRadius)-rsp.maxRadius);
//			sp.centerY = (int) (Math.random() * (imgHeight + 2*rsp.maxRadius)-rsp.maxRadius);
//			sp.centerZ = (int) (Math.random() * (imgDepth + 2*rsp.maxRadius)-rsp.maxRadius);
//
//			sp.radius =  Math.random() * (rsp.maxRadius - rsp.minRadius) + rsp.minRadius;

			//Copy the other parameters
			sp.fillVal =rsp.fillVal;
			sp.pixWidth = rsp.pixWidth;
			sp.pixHeight = rsp.pixHeight;
			sp.pixDepth = rsp.pixDepth;

			pixCnt = DrawSphere(oImage,imgWidth,imgHeight,imgDepth,sp);

			//If part of a sphere was drawn successfully
			if(pixCnt>0) spherePixCount += pixCnt;			
			else failedCount++;
		}

		if(failedCount>=MAXFAIL)
		{
			infoBox("after " + MAXFAIL + " tries, failed to obtain the requested volume fraction","In DrawRandomSpheres");
		}
		return spherePixCount;
	}

	//***************************************************************************************************

	/**Draws a sphere in user pixel units, sphere origins may lie outside the image.
	 * @param oImage An Object containing a 1D array from a row major 3D image
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>	 
	 * @param imgWidth The image width in pixels
	 * @param imgHeight The image height in pixels
	 * @param imgDepth The image depth (slices) in pixels
	 * @param sp A SphereParams data block
	 * @return The number of non-overlapping sphere voxels.
	 */
	public int DrawSphere(Object oImage, int imgWidth, int imgHeight, int imgDepth, SphereParams sp)
	{
		
		int i,j,k,r1,top,btm,left,right,front,back,spherePixCount=0,pad=0;
		double a2,b2,c2,r2,dx,dy,dz,v2,voxVal;
		
		//Make local copy of params to avoid dereferencing
		int x0=sp.centerX;
		int y0 = sp.centerY;
		int z0 = sp.centerZ;
		double radius = sp.radius;
		double fillVal = sp.fillVal;
		double pixWidth = sp.pixWidth;
		double pixHeight=sp.pixHeight;
		double pixDepth=sp.pixDepth;
		//String unit = sp.unit;
		
		if(radius<0) radius=0;

		//establish limits for sphere sub-volume.
		//The sphere center can lie outside of the volume
		//but only the regions within the volume will be drawn.
		r1 = (int)Math.ceil(radius/pixHeight);
		top = y0-r1-pad;
		if(top<0) top=0;
		btm = y0+r1+pad;

		if(btm>imgHeight) btm=imgHeight;
		r1 = (int)Math.ceil(radius/pixWidth);
		left = x0-r1-pad;
		if(left<0) left=0;
		right = x0+r1+pad;

		if(right>imgHeight) right=imgHeight;
		r1 = (int)Math.ceil(radius/pixDepth);
		front = z0-r1-pad;
		if(front<0) front=0;
		back = z0+r1+pad;
		if(back>imgDepth) back=imgDepth;

		//set up the scaling parameters for the three directions
		a2 = 1/pixWidth;
		a2*=a2;
		b2 = 1/pixHeight;
		b2*=b2;
		c2 = 1/pixDepth;
		c2*=c2;
		r2 = radius*radius;
		
		int sliceSize = imgWidth*imgHeight;

		if(oImage instanceof double[])
		{
			double[] image =(double[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						//drawing restrictions go here,
						//e.g. only draw in voxels of a particular value etc.						
						//My restriction, do not overwrite existing spheres.
						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImage instanceof float[])
		{
			float[] image =(float[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=(float)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImage instanceof long[])
		{			
			//if(fillVal>Long.MAX_VALUE) fillVal=Long.MAX_VALUE;
			//if(fillVal<Long.MIN_VALUE) fillVal=Long.MIN_VALUE;
			long[] image =(long[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=(long)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImage instanceof int[])
		{
			//if(fillVal>Integer.MAX_VALUE) fillVal=Integer.MAX_VALUE;
			//if(fillVal<Integer.MIN_VALUE) fillVal=Integer.MIN_VALUE;
			int[] image =(int[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=(int)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImage instanceof short[])
		{
			//if(fillVal>Short.MAX_VALUE) fillVal=Short.MAX_VALUE;
			//if(fillVal<Short.MIN_VALUE) fillVal=Short.MIN_VALUE;
			short[] image =(short[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=(short)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		else if(oImage instanceof byte[])
		{
			//if(fillVal>Byte.MAX_VALUE) fillVal=Byte.MAX_VALUE;
			//if(fillVal<Byte.MIN_VALUE) fillVal=Byte.MIN_VALUE;
			byte[] image =(byte[]) oImage;
			for(k=front;k<back;k++)
			{
				for(j=top;j<btm;j++)
				{
					for(i=left;i<right;i++)
					{
						voxVal = image[i +j*imgWidth + k*sliceSize];

						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

						if(v2 < r2 && voxVal != fillVal)
						{
							image[i +j*imgWidth + k*sliceSize]=(byte)fillVal;
							spherePixCount++;
						}
					}				
				}			
			}
		}

		return spherePixCount;
	}	
}




