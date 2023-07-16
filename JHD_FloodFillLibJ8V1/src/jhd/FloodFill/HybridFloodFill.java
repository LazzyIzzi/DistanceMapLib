package jhd.FloodFill;
//Copyright (c) John H Dunsmuir 2022
//MIT-License

import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**Class for simulation of non-wetting imbibition into random porous media with both resolved and unresolved porosity
 * @author John H Dunsmuir
 *
 */
public class HybridFloodFill extends Offsets
{
	/** Constructor for HybridFloodFill*/
	public HybridFloodFill() {};

	/**Experimental data structure passed to Flood_Fill*/
	public static class FloodParams
	{	/**  float Flood pixels between floodMin and floodMax*/
		public float	floodMin;	// the range of values to flood
		/** float Flood pixels between floodMin and floodMax*/
		public float	floodMax;
		/** float Set flooded pixels to this value*/
		public float	floodVal;	// flood pixels between floodMin and floodMax with floodVal
		/** int the connectivity 6(face), 18(face and edge), 26(face,edge and corner)*/
		public int		neighbors;
		/** show a progress bar*/
		public boolean showStatus;
	}

	/**A data structure returned by characterize and hybridFloodFill*/
	public static class PorosityReport
	{
		/**The number of resolved Pore voxels in the image*/
		public int resolvedPoreVoxelCount;
		/**The number of resolved voxels in the image*/
		public int resolvedSolidVoxelCount;
		/**The number of unresolved Pore voxels in the image*/
		public int unresolvedVoxelCount;
		/**The number of voxels containing porosity in the image*/
		public int floodableVoxelCount;
		/**The resolved solid volume in the image*/
		public float resolvedSolidVolume;		
		/**The resolved pore volume in the image*/
		public float resolvedPoreVolume;		
		/**The unresolved pore volume in the image*/
		public float unresolvedPoreVolume;		
		/**The resolved volume in the image*/
		public float resolvedPorosity;		
		/**The unresolved volume in the image*/
		public float unresolvedPorosity;		
		/**Resolved + unresolved volumes in the image*/
		public float totalPorosity;

	}

	/**A data structure returned by hybridFloodFill*/
	public static class FloodStats
	{
		/**True if flood reached back slice.*/
		public boolean contact;			
		/**The number of iterations at testFace contact.*/
		public int contactCycles;		
		/**The total number of iterations to completion.*/
		public int totalCycles;
		/**The mean tortousity at the back slice*/
		public float meanTort;
		/**The standard deviation of the  tortousity at the back slice*/
		public float stdDevTort;

	}
	/**Data structure returned by hybridFloodFill*/
	public static class FloodReport
	{		
		/** Post flood porosity  before the flood process*/
		public PorosityReport beforeFlood;
		/** Post flood porosity  after the flood process*/
		public PorosityReport afterFlood;
		/** Statistics associated with the flood process*/
		public FloodStats floodStatistics;
	}

	//***************************************************************************************************

	private void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	//*********************************************************************************************

	/**
	 * @param oImageArr An Object[]  as returned by
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a> 
	 * (must be float data where unresolved porosity 0&lt;phi&lt;1 and resolved porosity  phi=1)
	 * @param width int The image width in pixels
	 * @param height int The image height in pixels
	 * @param depth int The image depth in pixels
	 * @param pixWidth float The width of the pixel in user units;
	 * @param pixHeight float The height of the pixel in user units;
	 * @param pixDepth float The depth of the pixel in user units;
	 * @return A PorosityReport data structure.
	 */
	public PorosityReport	characterize( Object[] oImageArr, int width, int height, int depth,
			double 	pixWidth, double pixHeight, double pixDepth)			
	{
		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","HybridFloodFill.characterize Bad Data");
			return null;
		}
		
		//In a porosity image solid=0, pore=1, and unresolved= 0<val<1
		
		PorosityReport phiRpt = new PorosityReport();


		// find floodable voxels in front slice and initialize the flood list
		int unresVoxCnt=0,resPoreVoxCnt=0,resSolidVoxCnt=0;
		float voxVal;
		double unresVol=0;
		float voxelVolume = (float)(pixWidth*pixHeight*pixDepth);
		float imageVolume = voxelVolume*width*height*depth;
		for(int k=0;k<depth;k++)
		{
			float[] slice = (float[])oImageArr[k];
			for(int i=0;i<width;i++)
			{
				for(int j=0;j<height;j++)
				{
					voxVal = slice[i+j*width];
					if(voxVal == 0) resSolidVoxCnt++;
					else if(voxVal>=1) resPoreVoxCnt++;
					else if(voxVal <1.0 && voxVal>0)
					{
						unresVol += voxVal*voxelVolume;
						unresVoxCnt++;
					}
				}
			}
		}

		phiRpt.resolvedSolidVoxelCount =resSolidVoxCnt;
		phiRpt.resolvedPoreVoxelCount =resPoreVoxCnt;
		phiRpt.unresolvedVoxelCount =unresVoxCnt;
		phiRpt.floodableVoxelCount = resPoreVoxCnt + unresVoxCnt;

		phiRpt.resolvedPoreVolume = resPoreVoxCnt*voxelVolume;
		phiRpt.resolvedSolidVolume = resSolidVoxCnt*voxelVolume;
		phiRpt.unresolvedPoreVolume = (float)unresVol;		

		phiRpt.resolvedPorosity = phiRpt.resolvedPoreVolume/imageVolume;
		phiRpt.unresolvedPorosity = phiRpt.unresolvedPoreVolume/imageVolume;
		phiRpt.totalPorosity = phiRpt.resolvedPorosity + phiRpt.unresolvedPorosity;
		return phiRpt;
	}

	//*********************************************************************************************
	
	/**Converts a porosity image  0&lt;phi&lt;1 to a hybrid porosity image with unresolved porosity  0&lt;phi&lt;1 and with resolved porosity converted to a Euclidean distance map with 1&lt;phi&lt;floodMax
	 * @param oImageArr An Object[]  as returned by
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a> 
	 * (must be float data where unresolved porosity 0&lt;phi&lt;1 and resolved porosity  phi=1)
	 * @param width int The image width in pixels
	 * @param height int The image height in pixels
	 * @param depth int The image depth in pixels
	 * @param pixWidth float The width of the pixel in user units;
	 * @param pixHeight float The height of the pixel in user units;
	 * @param pixDepth float The depth of the pixel in user units;
	 * @return The longest floodable pore radius, -1 if error occurs.
	 */
	public double phiMapToHybridMap(Object[] oImageArr, int width, int height, int depth,
			double 	pixWidth, double pixHeight, double pixDepth)
	{
		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","HybridFloodFill.phiMapToHybridMap Bad Data");
			return -1;
		}
		if(pixWidth<1 || pixHeight<1 || pixDepth<1)
		{
			infoBox("All voxel dimensions must be > 1 unit","HybridFloodFill.phiMapToHybridMap Bad Data");
			return -1;
		}
		
		//Input porosity maps consist of three domains
		//1. unresolved porosity 0<x<1
		//2. resolved porosity x>=1
		//3. solid x=0
		
		//Hybrid maps also consist of three domains
		//1. unresolved porosity 0<x<1
		//2. resolved porosity a Euclidean map of resolved porosity x>=1
		//3. solid x=0
		
		//Create a temporary copy of the image data
		Object[] oImgCopy = new Object[depth];
		for(int k=0;k<depth;k++)
		{
			oImgCopy[k]= new float[width*height];
			float[] imgSlice = (float[])oImageArr[k];
			float[] copySlice = (float[])oImgCopy[k];
			for(int i=0; i<width*height;i++)
			{
				copySlice[i] = imgSlice[i];
				if(imgSlice[i] < 1) imgSlice[i]=0;
			}
		}		
		
		//Compute the edm non-zero domain of the modified original image
		ExactEuclideanMap edm = new ExactEuclideanMap();
		edm.edm3D(oImageArr, width, height, depth, (float)pixWidth, (float)pixHeight, (float)pixDepth, "Map !0", true);
		
		//Copy the unresolved porosity from the copy into the original image
		double floodMax=Double.MIN_VALUE;
		double floodMin=Double.MAX_VALUE;
		for(int k=0;k<depth;k++)
		{
			float[] imgSlice = (float[])oImageArr[k];
			float[] copySlice = (float[])oImgCopy[k];
			for(int i=0; i<width*height;i++)
			{
				if(copySlice[i] < 1) imgSlice[i]=copySlice[i];
				if(floodMax < imgSlice[i]) floodMax = imgSlice[i];
				if(floodMin > imgSlice[i] && imgSlice[i]>0) floodMin = imgSlice[i];
			}
		}
		
		return floodMax;
	}
	
	public double phiMapToHybridMapOld(Object[] oImageArr, int width, int height, int depth,
			double 	pixWidth, double pixHeight, double pixDepth)
	{
		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","HybridFloodFill.phiMapToHybridMap Bad Data");
			return -1;
		}
		if(pixWidth<1 || pixHeight<1 || pixDepth<1)
		{
			infoBox("All voxel dimensions must be > 1 unit","HybridFloodFill.phiMapToHybridMap Bad Data");
			return -1;
		}
		
		//make a copy of the input data and set copy porosities >= 1 to 0
		Object[] oEdmArr = new Object[depth];
		for(int k=0;k<depth;k++)
		{
			float[] imgSlice = (float[])oImageArr[k];
			oEdmArr[k] = new float[width*height];
			float[] edmSlice = (float[])oEdmArr[k];

			for(int i=0; i<width*height;i++)
			{

				if(imgSlice[i]>=1) edmSlice[i]=0;
				else edmSlice[i] = imgSlice[i];
			}
		}

		//Compute the edm of the copy;
		ExactEuclideanMap edm = new ExactEuclideanMap();
		edm.edm3D(oEdmArr, width, height, depth, (float)pixWidth, (float)pixHeight, (float)pixDepth, "Map 0", true);
		
		
		//Combine the edm with the unresolved porosities
		//Get the max edm distance
		double floodMax=Double.MIN_VALUE;
		for(int k=0;k<depth;k++)
		{
			float[] imgSlice = (float[])oImageArr[k];
			float[] edmSlice = (float[])oEdmArr[k];

			for(int i=0; i<width*height;i++)
			{						
				if(imgSlice[i]>=1) imgSlice[i] = edmSlice[i];
				if(floodMax < imgSlice[i]) floodMax = imgSlice[i];
			}
		}
		return floodMax;
	}

	//*********************************************************************************************
	/**
	 * @param oImageArr An Object[] (must be float data ) as returned by
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>
	 * (must be float data where unresolved porosity 0&lt;phi&lt;1 and resolved porosity  phi=1)&dot; The method wraps the hybrid flood fill algorithm,
	 *  characterize, EDM, Flood, and GDT. 
	 * @param width int The image width in pixels
	 * @param height int The image height in pixels
	 * @param depth int The image depth in pixels
	 * @param pixWidth float The width of the pixel in user units;
	 * @param pixHeight float The height of the pixel in user units;
	 * @param pixDepth float The depth of the pixel in user units;
	 * @param pixUnit The units of pixel size e.g. microns, millimeters, etc.
	 * @param floodMin float The minimum voxel value to be flooded
	 * @param neighbors int The connectedness face(6), face and edge(18), face, edge and corners(26)
	 * @return A FloodReport data structure. The input image becomes a map of tortuosity of the floodable paths
	 */
	public FloodReport	hybridFloodFill( Object[] oImageArr, int width, int height, int depth,
			double 	pixWidth, double pixHeight, double pixDepth, String pixUnit,			
			double	floodMin,int neighbors)
	{
		return hybridFloodFill( oImageArr, width, height,depth,
				 	pixWidth,  pixHeight,  pixDepth,  pixUnit,			
					floodMin, neighbors, true,true);
	}

	//*********************************************************************************************
	/**
	 * @param oImageArr An Object[] (must be float data ) as returned by
	 * <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>
	 * (must be float data where unresolved porosity 0&lt;phi&lt;1 and resolved porosity  phi=1)&dot; The method toggles the characterize, EDM, and GDTcomponents the hybrid flood fill algorithm
	 * to accelerate bulk processing.
	 * @param width int The image width in pixels
	 * @param height int The image height in pixels
	 * @param depth int The image depth in pixels
	 * @param pixWidth float The width of the pixel in user units;
	 * @param pixHeight float The height of the pixel in user units;
	 * @param pixDepth float The depth of the pixel in user units;
	 * @param pixUnit The units of pixel size e.g. microns, millimeters, etc.
	 * @param floodMin float The minimum voxel value to be flooded
	 * @param neighbors int The connectedness face(6), face and edge(18), face, edge and corners(26)
	 * @param doEDM (true) Converts porosity=1 the exact Euclidean distances, set to false if the EDM has been pre-computed.
	 * @param doGDT (true) Convert the flooded volume to tortuosity
	 * @return A FloodReport data structure. The input image becomes a map of tortuosity of the floodable paths
	 */
	public FloodReport	hybridFloodFill( Object[] oImageArr, int width, int height, int depth,
			double 	pixWidth, double pixHeight, double pixDepth, String pixUnit,			
			double	floodMin,int neighbors, boolean doEDM, boolean doGDT)
	{

		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","HybridFloodFill.hybridFloodFill Bad Data");
			return null;
		}
		if(pixWidth<1 || pixHeight<1 || pixDepth<1)
		{
			infoBox("All voxel dimensions must be > 1 unit","HybridFloodFill.hybridFloodFill Bad Data");
			return null;
		}
		if(neighbors != 6 && neighbors != 18 && neighbors != 26)
		{
			infoBox("neighbor parameter must be 6, 18 or 26","HybridFloodFill.hybridFloodFill Bad Data");
			return null;
		}

		int		floodedNeighbors;
		double	floodVal=99999;
		boolean	contact = false; // change to boolean
		boolean drawSpheres;

		//timers
		//		long floodNanoStart =System.nanoTime();

		// rev 1-9-2016
		// If the fllod min is less than 1 then we are flooding microporosity
		// and there is no need to draw spheres since all of the accessible resolved pores have been flooded
		if(floodMin <=1) drawSpheres = false;
		else drawSpheres = true;

		FloodReport fldRpt = new FloodReport();
		float voxelVolume = (float)(pixWidth*pixHeight*pixDepth);
		float imageVolume = voxelVolume*width*height*depth;
		double floodMax;

		//measure the porosity image initial properties 
		fldRpt.beforeFlood = characterize(oImageArr, width, height,  depth, pixWidth,  pixHeight,  pixDepth);
				
		if(doEDM)
		{
			floodMax = phiMapToHybridMap(oImageArr, width, height, depth, (float)pixWidth, (float)pixHeight, (float)pixDepth);
		}
		else //The EDM was precomputed, get the max radius
		{
			floodMax=Double.MIN_VALUE;
			for(int k=0;k<depth;k++)
			{
				float[] imgSlice = (float[])oImageArr[k];

				for(int i=0; i<width*height;i++)
				{						
					if(floodMax < imgSlice[i]) floodMax = imgSlice[i];
				}
			}			
		}
		//begin the flood process, may want to break out into a separate method
		// floodList is a list of pixels forming the current flood surface
		List<PointDesc> floodList = new ArrayList<PointDesc>();

		// touchList is a list of pixels within floodMin and floodMax touching the flood surface
		List<PointDesc>  touchList = new ArrayList<PointDesc>();

		// tempList is a PointLIst reference used to temporarily
		// store the touchList reference when swapping lists
		List<PointDesc> tempList = null;

		// sphereList is a list of locations on the outer flooded surface of the EDM
		List<PointDesc> sphereList = new ArrayList<PointDesc>();

		// offsetList is a list of offsets to i,j,k that give the location of nearest
		// neighbors ranging from 6 through 18 to 26 connected.
		// to vary the connectivity we simply go deeper into the list.		
		PointDesc[] offsetList = Offsets.getIndexOffsets3D();

		int		contactCycles=0,resPoreCnt=0,unresPoreCnt=0,cycleCnt=0;//,totVoxCnt=0;
		int		i,j,k,ii=0, jj=0, kk=0;;

		// find and measure the floodable voxels in front slice,  and initialize the flood list
		k=0;
		double	unresPoreVol=0;
		float voxVal;
		float[] slice = (float[])oImageArr[k];
		for(i=0;i<width;i++)
		{
			for(j=0;j<height;j++)
			{
				voxVal = slice[i+j*width];
				if( voxVal>= floodMin && voxVal<= floodMax)
				{
					floodList.add(new PointDesc(i,j,k,voxVal));
					if(voxVal>=1)resPoreCnt++;
					else if(voxVal <1.0 && voxVal >0 )
					{
						unresPoreVol += voxVal*voxelVolume;
						unresPoreCnt++;
					}
					slice[i+j*width]=(float)floodVal;
				}	
			}
		}// end front slice flood

		//Set up the progress bar
		JPanel fldPanel = new JPanel();
		JFrame frame = new JFrame("Flood Fill Progress");		
		JProgressBar prgBar = new JProgressBar(0,fldRpt.beforeFlood.floodableVoxelCount);

		frame.setSize(400, 100);
		frame.setLocationRelativeTo(null);

		prgBar.setPreferredSize(new Dimension(350, 30));
		prgBar.setValue(0);
		prgBar.setStringPainted(true);			
		fldPanel.add(prgBar);

		frame.add(fldPanel);
		frame.setVisible(true);
		
		// Begin the "burning" algorithm that advances the flood
		while(floodList.isEmpty()==false)
		{
			prgBar.setValue(resPoreCnt + unresPoreCnt);
			touchList.clear();

			for(PointDesc fl : floodList)
			{
				floodedNeighbors = 0;
				for(i=0;i<neighbors;i++)
				{
					// look around the current floodList pixel,
					// if a touching pixel is between min and max set it
					// to floodVal and add it to the touchList
					ii	= fl.x + offsetList[i].x;
					jj	= fl.y + offsetList[i].y;
					kk	= fl.z + offsetList[i].z;

					if(ii>=0 && jj>=0 && kk>=0 && ii<width && jj<height && kk<depth)
					{
						slice = (float[])oImageArr[kk];
						voxVal = slice[ii+jj*width];

						// 1-9-2016 the voxel is already flooded or is outside the image don't add it to the list
						if(voxVal == floodVal || voxVal ==0) floodedNeighbors+=1;	
						else if( voxVal>= floodMin && voxVal<= floodMax)
						{
							touchList.add(new PointDesc(ii,jj,kk,voxVal));
							if(voxVal>=1) resPoreCnt++;
							else if(voxVal <1.0 && voxVal >0)
							{
								unresPoreVol += voxVal*voxelVolume;
								unresPoreCnt++;
							}

							floodedNeighbors+=1;

							slice[ii+jj*width] =  (float)floodVal;
						}
					}
				} // next i, next neighbor

				if(drawSpheres)
				{
					// if the pixel has non-floodable neighbors it is a border pixel.
					// Add it to the sphereList if it is an unflooded open pore 
					if(floodedNeighbors < neighbors )
					{
						ii	= fl.x;
						jj	= fl.y;
						kk	= fl.z;
						// don't draw spheres if any index = 0
						if(( ii==0) || (jj ==0) || (kk==0)) continue;
						else
						{
							voxVal = (float)fl.val;
							// don't draw spheres of radius 1 or less due to limitation of the EDM 							
							if(voxVal<floodVal && voxVal > 1)
							{
								sphereList.add(new PointDesc(ii,jj,kk,fl.val));
							}
						}
					}
				} // end building sphere list
			} // next j, next floodList voxel

			// the list of newly flooded touchList pixels becomes the new floodList
			// and the old floodList is reused as the touchList
			tempList	= floodList;
			floodList	= touchList;
			touchList	= tempList;

			// We test for breakthrough at  the last Slice
			if(contact == false)
			{
				k = depth-1; // Test for breakthrough at  the last Slice
				//k = depth -1 - (int)floodMin; //offset the test slice by the current sphere radius?
				slice = (float[])oImageArr[k];
				for(i=0;i<width;i++)
				{
					for(j=0;j<height;j++)
					{
						if(slice[i+j*width] == floodVal)
						{
							contact = true;
							contactCycles = cycleCnt;
							break;
						}
					}
				}
			}// end contact test
			cycleCnt++; // loop counter

		}// end when floodListLen = 0

		// draw the spheres that have their origins flooded
		// We must draw the spheres only after the flood is complete.  if the surface is restored at each iteration the
		// values of voxels within the sphere radius will be set to floodVal and no longer be floodable in the next iteration
		// causing the flood to stop.
		if(drawSpheres)
		{
			prgBar.setMaximum(sphereList.size());
			prgBar.setValue(0);
			frame.setTitle("Drawing Flood Surface");
			int progress=0;
			for(PointDesc sl : sphereList)
			{
				prgBar.setValue(progress);
				resPoreCnt += DrawSphere(oImageArr,width,height,depth,pixWidth,pixHeight,pixDepth,pixUnit,sl.x,sl.y,sl.z,sl.val,floodVal);
				progress++;
			}
		}

		//		long sphereNanoEnd = System.nanoTime();
		//		int floodCnt = resVoxCnt + unresVoxCnt;
		//		double floodSec = (double)(floodNanoEnd - floodNanoStart)/1e9;
		//		double sphereSec =(double)(sphereNanoEnd - sphereNanoStart)/1e9;
		//
		//		System.out.println("Flood Count =" + floodCnt);
		//		System.out.println("Flood Time sec =" + floodSec);
		//		System.out.println("Sphere Count =" + sphereList.size());
		//		System.out.println("Sphere Time sec =" + sphereSec);
		//		System.out.println("Total Time sec =" + (floodSec + sphereSec));

		// get the total number of voxels flooded
		int floodedVoxCnt=0;
		for(k=0;k<depth;k++)
		{
			slice = (float[])oImageArr[k];
			for(i=0;i<width;i++)
			{
				for(j=0;j<height;j++)
				{
					if(slice[i+j*width] == floodVal) floodedVoxCnt++;
				}
			}
		}

		fldRpt.afterFlood = new PorosityReport();
		fldRpt.afterFlood.floodableVoxelCount=floodedVoxCnt;
		fldRpt.afterFlood.resolvedPoreVoxelCount = resPoreCnt;
		fldRpt.afterFlood.unresolvedVoxelCount=unresPoreCnt;
		fldRpt.afterFlood.resolvedPoreVolume=resPoreCnt*voxelVolume;
		fldRpt.afterFlood.unresolvedPoreVolume=(float)unresPoreVol;//unresVoxCnt*voxelVolume;
		fldRpt.afterFlood.resolvedPorosity=(float)resPoreCnt*voxelVolume/imageVolume;
		fldRpt.afterFlood.unresolvedPorosity=(float)unresPoreVol/imageVolume;
		fldRpt.afterFlood.totalPorosity=fldRpt.afterFlood.resolvedPorosity+fldRpt.afterFlood.unresolvedPorosity;

		fldRpt.floodStatistics = new FloodStats();		
		fldRpt.floodStatistics.contact = contact;
		fldRpt.floodStatistics.contactCycles = contactCycles;
		fldRpt.floodStatistics.totalCycles=cycleCnt;		

		//Prep data for tortuosity 
		for(i=0;i<depth;i++)
		{
			slice = (float[]) oImageArr[i];
			for(j=0;j<slice.length;j++)
			{
				if(slice[j]!=floodVal)
				{
					slice[j]=0;
				}
			}
		}

		if(doGDT)
		{
			GDT3D gdt = new GDT3D();
			gdt.tort3D(oImageArr, width, height, depth, (float)pixWidth, (float)pixHeight, (float)pixDepth, "Map !0", "FrontSlice", null, null, null);
			//gdt.gdt3D(oImageArr, width, height, depth, (float)pixWidth, (float)pixHeight, (float)pixDepth, "Map !0", "FrontSlice", null, null, null);

			//Get the mean and stdDev of the tortuosities in the back slice
			List<Float> torts = new ArrayList<>();
			slice = (float[]) oImageArr[depth-1];
			double meanTort=0;
			for(j=0;j<slice.length;j++)
			{
				if(slice[j]>0)
				{
					torts.add( slice[j]);
					meanTort+=slice[j];
				}
			}
			meanTort /= torts.size();
			double stdDev=0;
			for(Float tort:torts)
			{
				stdDev += Math.pow((tort-meanTort), 2);
			}
			stdDev = Math.sqrt(stdDev/torts.size());
			fldRpt.floodStatistics.meanTort = (float)meanTort;
			fldRpt.floodStatistics.stdDevTort = (float)stdDev;
		}
		else
		{
			fldRpt.floodStatistics.meanTort = -1;
			fldRpt.floodStatistics.stdDevTort = -1;			
		}

		frame.dispose();
		return fldRpt;
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
	// Special version of draw sphere. Cannot draw if voxel value is < 1
	private int DrawSphere(Object[] oImageArr, int imgWidth, int imgHeight, int imgDepth,
			double pixWidth, double pixHeight, double pixDepth, String pixUnit,
			int x, int y, int z, double radius, double fillVal)

	{
		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","HybridFloodFill.hybridFloodFill Bad Data");
			return 0;
		}
		int i,j,k,r1,top,btm,left,right,front,back,spherePixCount=0;
		double a2,b2,c2,r2,dx,dy,dz,v2,voxVal;

		if(radius<0) return 0;

		//establish limits for sphere sub-volume.
		//The sphere center can lie outside of the volume
		//but only the regions within the volume will be drawn.
		r1 = (int)Math.ceil(radius/pixHeight);
		top = y-r1;
		if(top<0) top=0;
		btm = y+r1;

		if(btm>imgHeight) btm=imgHeight;
		r1 = (int)Math.ceil(radius/pixWidth);
		left = x-r1;
		if(left<0) left=0;
		right = x+r1;

		if(right>imgHeight) right=imgHeight;
		r1 = (int)Math.ceil(radius/pixDepth);
		front = z-r1;
		if(front<0) front=0;
		back = z+r1;
		if(back>imgDepth) back=imgDepth;

		//set up the scaling parameters for the three directions
		a2 = 1/pixWidth;
		a2*=a2;
		b2 = 1/pixHeight;
		b2*=b2;
		c2 = 1/pixDepth;
		c2*=c2;
		r2 = radius*radius;


		float[] image;
		for(k=front;k<back;k++)
		{
			image= (float[]) oImageArr[k];
			for(j=top;j<btm;j++)
			{
				for(i=left;i<right;i++)
				{
					voxVal = image[i +j*imgWidth];

					dx = i - x;
					dy = j - y;
					dz = k - z;
					v2 = (dx*dx)/a2 + (dy*dy)/b2 + (dz*dz)/c2;

					//may want to reorder if tests with most likely occurrence first
					if(v2<r2)// don't draw outside of radius, always Pi/6 =0.5236
					{
						if( voxVal != fillVal)// don't draw in flooded voxels, hard to tell, guess also about 0.5
						{
							if(voxVal>1) // don't draw in unresolved porosity, most unlikely
							{
								image[i +j*imgWidth]=(float)fillVal;
								spherePixCount++;								
							}
						}
					}
				}				
			}			
		}

		return spherePixCount;
	}	
}
