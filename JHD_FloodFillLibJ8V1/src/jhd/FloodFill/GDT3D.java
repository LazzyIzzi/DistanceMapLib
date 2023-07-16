package jhd.FloodFill;
//Copyright (c) John H Dunsmuir 2022
//MIT-License


import java.awt.Dimension;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


/**Methods for 2D and 3D Geodesic transform and tortousity
 * @author John H Dunsmuir
 *
 */
public class GDT3D extends Offsets{

	/**Default Constructor*/
	public GDT3D(){}
		
	//*********************************************************************************************
	//PORE and SOLID tags are assigned values larger than an expected image dimension.
	//TOLERANCE determines if colliding flood front distance values are sufficiently close to avoid re-flooding
	final float	open = 99998,solid= 99999,tolerance =0.001f; //original 
//	final float	open = 99998,solid= 99999,tolerance =0.01f;
	final String[] seedChoices3D = {"LeftSlice","RightSlice","TopSlice","BottomSlice","FrontSlice","BackSlice","Point(s)"};
	final String[] seedChoices2D = {"LeftEdge","RightEdge","TopEdge","BottomEdge","Point(s)"};
	final String[] mapChoices = {"Map 0","Map !0"};
	
	//double whDiag,wdDiag,hdDiag,whdDiag;
	double whDiag,whdDiag;
	PointDesc[] offsets3D;
	PointDesc[] offsets2D;
	int refloodCnt=0,totCnt=0;		

	//*********************************************************************************************
	/**Used to build Dialogs
	 * @return A map choices: "Map 0","Map !0"
	 */
	public String[] getMapChoices()
	{
		return mapChoices;
	}
	
	//***************************************************************************************************

	private void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	//*********************************************************************************************
	//*********************************************************************************************
	//3D Public methods
	//*********************************************************************************************
	//*********************************************************************************************

	//*********************************************************************************************	
	/**Used to build Dialogs
	 * @return A list of choices for the start of the GDT: "LeftSlice","RightSlice","TopSlice","BottomSlice","FrontSlice","BackSlice","Point(s)"
	 */
	public String[] getSeedChoices3D()
	{
		return seedChoices3D;
	}

	//*********************************************************************************************
	/**3D Geodesic distance by distance propagation
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param pixWidth The pixel width in user units
	 * @param pixHeight The pixel height in user units
	 * @param pixDepth The pixel depth in user units
	 * @param mapChoice A string selecting the region to map; "Map 0" or "Map !0"
	 * @param seedChoice Choices are "LeftEdge","RightEdge","TopEdge","BottomEdge","Point(s)"
	 * @param xPts A zero based array of seedPoint column indices, pass null if not using Point(s) choice
	 * @param yPts A zero based array of seedPoint row indices, pass null if not using Point(s) choice
	 * @param zPts A zero based array of seedPoint slice indices, pass null if not using Point(s) choice
	 */
	public void gdt3D(Object[] data, int width, int height, int depth,
			float pixWidth, float pixHeight, float pixDepth,
			String mapChoice, String seedChoice,
			int[] xPts, int[] yPts, int[] zPts)
	{
		if(!(data[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","gdt3D Bad Data");
			return;
		}
		
		float[] tmpOpen,tmpMin;
		
		int openIndex,minIndex;
		float newVal;


		//offsets3D= initOffsets3D( pixWidth, pixHeight, pixDepth);
		offsets3D= Offsets.getIndexOffsets3D( pixWidth, pixHeight, pixDepth);
//		wdDiag= offsets3D[14].val;
//		hdDiag= offsets3D[10].val;
		whdDiag= offsets3D[18].val;

		//Convert data to PORE and SOLID
		int pixInRange = conditionData3D(data,width,height,depth,mapChoice);
		
		//Set up the progress bar
		JPanel fldPanel = new JPanel();
		JFrame frame = new JFrame("GDT Progress");		
		JProgressBar prgBar = new JProgressBar(0,pixInRange);

		frame.setSize(400, 100);
		frame.setLocationRelativeTo(null);
		
		prgBar.setPreferredSize(new Dimension(350, 30));
		prgBar.setValue(0);
		prgBar.setStringPainted(true);			
		fldPanel.add(prgBar);
		
		frame.add(fldPanel);
		frame.setVisible(true);
		
		//Initialize the seed voxels to a distance if 1
		ArrayList<PointDesc> seedPts = initSeedPoints3D(data,width,height,depth,seedChoice,xPts,yPts,zPts);
		ArrayList<PointDesc> outPts = null;
		ArrayList<PointDesc> openPts;

		//timers
//		long nanoSecStart =System.nanoTime();
		
		while(seedPts.isEmpty()==false)
		{
			//Status stuff
			totCnt += seedPts.size() - refloodCnt;
			prgBar.setValue(totCnt);
			refloodCnt = 0;
			
			ArrayList<PointDesc> setPts= new ArrayList<PointDesc>();
			
			for(PointDesc seedPt : seedPts)
			{
				openPts = findOpenPixels3D(data,width,height,depth,seedPt);
				
				for(PointDesc openPt : openPts)
				{
					outPts = findTouchingPixels3D(data,width,height,depth,openPt.x,openPt.y, openPt.z);
					PointDesc min = getMinPoint3D(data,width,height,depth,outPts);
										
					//The pointers to the open and min slices
					tmpOpen = (float[]) data[openPt.z];
					tmpMin = (float[]) data[min.z];
					
					//The positions of the open and min voxels within the slices
					openIndex =  openPt.x + openPt.y*width;
					minIndex = min.x + min.y*width;
					
					//Add the distance offset to the min value 
					newVal = tmpMin[minIndex] + (float)min.val;
					
					//assign it to the open voxel
					tmpOpen[openIndex]=newVal;
					
					setPts.add(new PointDesc(openPt.x,openPt.y,openPt.z,newVal));
				}
			}
			//testing garbage collection vs pointer swapping showed
			//no difference in memory management or performance.
			//so we stay with gc.
			seedPts=setPts;			
		}
		cleanUpGDT3D(data,width,height,depth);
//		long nanoSecEnd = System.nanoTime();
//		double seconds = (double)(nanoSecEnd - nanoSecStart)/1e9;
//		System.out.println("GDT3D_V6.gdt3D run time =" + seconds + " seconds");

		frame.dispose();
	}
	
	//*********************************************************************************************
	/**3D tortuosity map
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The height width in pixels
	 * @param depth The depth or number of slices in the 3D image
	 * @param pixWidth The pixel width in user units
	 * @param pixHeight The pixel height in user units
	 * @param pixDepth The pixel depth in user units
	 * @param floodChoice Choices are "Map 0" and "Map !0"
	 * @param seedChoice Choices are "FrontSlice","BackSlice","LeftSlice","RightSlice","TopSlice","BottomSlice","Point(s)"
	 * @param xPts An array of seedPoint column indices, pass null if not using Point(s) choice
	 * @param yPts An array of seedPoint row indices
	 * @param zPts An array of seedPoint slice indices
	 */
	public void tort3D(Object[] data, int width, int height, int depth, float pixWidth, float pixHeight, float pixDepth,String floodChoice, String seedChoice, int[] xPts, int[] yPts, int[]zPts)
	{
		int	i,j,k,home;
		float slice;
		float[] tmp;
		
		if(!(data[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","gdt3D Bad Data");
			return;
		}

		//Get initial GDT
		gdt3D(data, width, height, depth,  pixWidth,  pixHeight,  pixDepth,floodChoice, seedChoice, xPts, yPts,zPts);

		switch(seedChoice)
		{
		//divide the gdt by the distance from the the seed slice
		case "FrontSlice":
			//for(k = 0,slice=1;k<depth;k++,slice++)
			for(k=1,slice=1;k<depth;k++,slice++)
			{
				tmp=(float[])data[k];
				for(i=0;i<width;i++)
				{
					for(j=0;j<height;j++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixDepth*(slice);;
					}
				}
			}
			break;
		case "BackSlice":
			for(k=depth-2,slice=1;k>=0;k--,slice++)
			{
				tmp=(float[])data[k];
				for(i=0;i<width;i++)
				{
					for(j=0;j<height;j++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixDepth*(slice);;
					}
				}
			}
			break;
		case "LeftSlice":
			for(k=0;k<depth;k++)
			{
				tmp=(float[])data[k];
				for(i=1,slice=1;i<width;i++,slice++)
				{
					for(j=0;j<height;j++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixWidth*(slice);
					}
				}
			}
			break;
		case "RightSlice":
			for(k=0;k<depth;k++)
			{
				tmp=(float[])data[k];
				for(i=width-2,slice=1;i>=0;i--,slice++)
				{
					for(j=0;j<height;j++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixWidth*(slice);
					}
				}
			}

			break;
		case "TopSlice":
			for(k=0;k<depth;k++)
			{
				tmp=(float[])data[k];
				for(i=0;i<width*height;i++)
				{
					//for(j=0,slice=1;j<height;j++,slice++)
					for(j=1,slice=1;j<height;j++,slice++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixHeight*(slice);
					}
				}
			}
			break;
		case "BottomSlice":
			for(k=0;k<depth;k++)
			{
				tmp=(float[])data[k];
				for(i=0;i<width;i++)
				{
					//for(j=0,slice=height;j<height;j++,slice--)
					for(j=height-2,slice=1;j>=0;j--,slice++)
					{
						home = i+j*width;
						if(tmp[home]>0)
							tmp[home] /= pixHeight*(slice);
					}
				}
			}
			break;
		case "Point(s)":			
			// for source point(s) divide GD by distance from points in an empty image		
			//copy the gdt to a float[][] array and set the data to zero
			// the data.clone does not work here
			float fVal;
			float[][] mtVol = new float[depth][width*height];
			if(floodChoice == "Map 0") fVal = 0; else fVal = 255;
			for(k=0;k<depth;k++)
			{
				for(i=0;i<width*height;i++)
				{
					mtVol[k][i]=fVal;
				}
			}

			
			//Get the point gdt of the empty data
			gdt3D(mtVol, width, height, depth,  pixWidth,  pixHeight,  pixDepth,floodChoice, seedChoice, xPts, yPts,zPts);
			
			//Calculate the tortuosity
			float[] dataRow;
			for(k=0;k<depth;k++)
			{
				dataRow=(float[])data[k];
				for(i=0;i<width*height;i++)
				{
					if(dataRow[i]>0)
						dataRow[i] /= mtVol[k][i];
				}
			}
			break;			
		}
	}
		
	//***********************************************************************************************
	/**Finds the shortest path through a 2D GDT image between each probePoint and the GDT origin.
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param probePts a list of the starting points for the path to the 0 GDT destination
	 * @return An Array of lists of the points along the shortest path between each probe point and the 0 GDT destination
	 */
	public ArrayList<PointDesc>[] getGeoPaths3D(Object[] data,int width,int height,int depth, ArrayList<PointDesc>probePts)
	{
		if(!(data[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","gdt3D Bad Data");
			return null;
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<PointDesc>[] paths = new ArrayList[probePts.size()];
		
		//initGdtOffsets3D();
		for(int i=0;i<probePts.size();i++)
		{
			ArrayList<PointDesc> path = getGeoPath3D(data,width,height,depth,probePts.get(i));
			paths[i]=path;
		}
		return paths;
	}

	//***********************************************************************************************
	/**Finds the shortest path through a 2D GDT image between a probePoint and the GDT origin.
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param probePt an probe point, the starting point for the path to the 0 GDT destination
	 * @return A list of the points along the shortest path between the probe point and the 0 GDT destination
	 */
	public ArrayList<PointDesc> getGeoPath3D(Object[] data, int width, int height,int depth, PointDesc probePt)
	{
		double	val;
		if(!(data[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","gdt3D Bad Data");
			return null;
		}

		ArrayList<PointDesc> path = new ArrayList<PointDesc>();

		//Add the probePt to the top of the list
		val = Array.getFloat(data[probePt.z], probePt.x + probePt.y*width);
		path.add(new PointDesc(probePt.x,probePt.y,probePt.z,val));
		while(val>0)
		{
			PointDesc minPt = getLclMin3D(data,width,height,depth,probePt);
			path.add(new PointDesc(minPt.x,minPt.y,minPt.z,val));
			probePt=minPt;
			val = probePt.val;
		}
		return path;
	}
	
	//*********************************************************************************************
	//3D Private methods
	//*********************************************************************************************
	
	//*********************************************************************************************
	/** 
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param pointList a 3D point in the image
	 * @return the location and value of the 26 connected pixel with the smallest value
	 */
	private PointDesc getLclMin3D(Object[] data, int width, int height, int depth, PointDesc probePt)
	{
		float min=Float.MAX_VALUE;
		PointDesc minPoint=null;

		offsets3D = getIndexOffsets3D();

		for(PointDesc offset : offsets3D)
		{
			int x = probePt.x + offset.x;
			int y = probePt.y + offset.y;
			int z = probePt.z + offset.z;
			if(x>=0 && x<width && y>=0 && y< height && z>=0 && z<depth)
			{
				float val = Array.getFloat(data[z], x + y*width);
				if(val<min && val >= 0)
				{
					min = val;
					minPoint = new PointDesc(x,y,z,val);
				}
			}
		}		
		return minPoint;
	}
	
	//*********************************************************************************************
	/**Finds touching open voxels available for flooding, resets voxels to open if the estimated distance exceeds the maximum possible touching voxel distance.
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param seedPt the location and current value of the seed voxel
	 * @return An ArrayList of floodable voxels touching the seed voxel 
	 */
	private ArrayList<PointDesc> findOpenPixels3D(Object[] data, int width, int height, int depth, PointDesc seedPt)
	{
		int index;
		float[] tmp;
		double maxTolerance =whdDiag *(1 + tolerance);
		ArrayList<PointDesc> touchList = new ArrayList<PointDesc>();
		for(PointDesc offsetPt : offsets3D)
		{
			int i = seedPt.x + offsetPt.x;
			int j = seedPt.y + offsetPt.y;
			int k = seedPt.z + offsetPt.z;
			//Check Bounds, avoid try-catch. Not much slower that bounding the volume with "solid"
			//and it does the whole volume right up to the edges:-)
			if(i<width && i>=0 && j<height && j>=0 && k<depth && k>=0)
			{
				tmp = (float[]) data[k];
				index = i+j*width;
				float pixVal=tmp[index];
				if(pixVal==open)
				{
					touchList.add(new PointDesc(i,j,k,offsetPt.val));
				}
				//else if( pixVal < open && pixVal > seedPt.val + maxTolerance)
				else if( pixVal < open && pixVal >= seedPt.val + maxTolerance)
				{
					//Since the distance cannot increase faster than the diagonal distance, pixels with a greater increase
					//are reassigned to the pore space to be re-flooded.
					tmp[index]=open;
					refloodCnt++;
					//By tolerating small errors speed is significantly increased.
					//This is probably due to the accuracy limits of of 32bit float.  Remember we
					//are doing hundreds of additions of sqrt2 and sqrt3
					//real images are NEVER this accurate *-)
				}
			}
		}
		return touchList;
	}

	//*********************************************************************************************

	/**
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param iCol The column position of the voxel, AKA the x or i position
	 * @param jRow The row position of the voxel, AKA the y or j position
	 * @param kDepth The depth position of the voxel, AKA the z or k position same as the imageJ slice position-1.
	 * @return An ArrayList of the 26 voxels touching the voxel at iCol,jRow,kDepth
	 */
	private ArrayList<PointDesc> findTouchingPixels3D(Object[] data, int width, int height, int depth, int iCol,int jRow, int kDepth)
	{
		ArrayList<PointDesc> touchList = new ArrayList<PointDesc>();
		for(PointDesc offsetPt : offsets3D)
		{
			int i = iCol + offsetPt.x;
			int j = jRow + offsetPt.y;
			int k = kDepth + offsetPt.z;
			if(i<width && i>=0 && j<height && j>=0 && k<depth && k>=0)
			{
				touchList.add(new PointDesc(i,j,k,offsetPt.val));
			}
		}
		return touchList;
	}
	
	//*********************************************************************************************

	/**
	 * @param data Object[] where data[0] instanceof float[], as returned by imageJ imp.getStack()&period;getImageArray()&period; The 
	 * image must be 32-bit. The gdt can be performed on the zero or non-zero image pixels.   
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param depth The image depth in pixels
	 * @param pointList A list of the voxels touching an open voxel
	 * @return The location (and value) of the touching voxel with the lowest value.
	 */
	private PointDesc getMinPoint3D(Object[] data, int width, int height, int depth, ArrayList<PointDesc> pointList)
	{
		float min=Float.MAX_VALUE;
		PointDesc minPoint=null;
		int index;// sliceSize = width*height;
		float[] tmp;
		
		for(PointDesc point : pointList)
		{
			index=point.x + point.y*width;// + point.z*sliceSize;
			tmp=(float[])data[point.z];
			float pixVal = tmp[index];
			if(pixVal<min)
			{
				min = pixVal;
				minPoint = point;
			}
		}		
		return minPoint;
	}
	
	//*********************************************************************************************	


	//*********************************************************************************************	
	// Creates a list of user-selected seed points
	private ArrayList<PointDesc> initSeedPoints3D(Object[] data,int width,int height,int depth, String seedChoice, int[] xPts, int[] yPts, int[] zPts)
	{
		int i,j,k,home;
		float[] tmp;
		ArrayList<PointDesc> seedPL = new ArrayList<PointDesc>();

		final double seedVal = 0;
		
		switch(seedChoice)
		{
		case "FrontSlice":
			k = 0;
			tmp = (float[]) data[k];
			for(i=0;i<width;i++)
			{
				for(j=0;j<height;j++)
				{
					home = i + j*width;
					
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "BackSlice":
			k = depth-1;
			tmp = (float[]) data[k];
			for(i=0;i<width;i++)
			{
				for(j=0;j<height;j++)
				{
					home = i + j*width;
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "LeftSlice":
			i=0;
			for(k=0;k<depth;k++)
			{
				tmp = (float[]) data[k];
				for(j=0;j<height;j++)
				{
					home = i + j*width;
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "RightSlice":
			i=width-1;
			for(k=0;k<depth;k++)
			{
				tmp = (float[]) data[k];
				for(j=0;j<height;j++)
				{
					home = i + j*width;
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "TopSlice":
			j=0;
			for(k=0;k<depth;k++)
			{
				tmp = (float[]) data[k];
				for(i=0;i<width;i++)
				{
					home = i + j*width;
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "BottomSlice":
			j=height-1;
			for(k=0;k<depth;k++)
			{
				tmp = (float[]) data[k];
				for(i=0;i<width;i++)
				{
					home = i + j*width;
					if(tmp[home] == open)
					{
						tmp[home]=(float)seedVal;
						seedPL.add(new PointDesc(i,j,k,seedVal));
					}
				}
			}
			break;
		case "Point(s)":
			for(i = 0; i<yPts.length;i++)
			{
				home = xPts[i] + yPts[i]*width;
				tmp = (float[]) data[zPts[i]];
				
				tmp[home]=(float)seedVal;
				seedPL.add(new PointDesc(xPts[i],yPts[i],zPts[i],seedVal));
			}		
			break;
		}
		return seedPL;
	}
	
	//*********************************************************************************************	
	// Converts the binary data to SOLID and PORE and returns the number of mappable voxels
	private int conditionData3D(Object[] data,int width,int height,int depth, String mapChoice)
	{
		int		i, k, pixInRange=0;
		float[] tmp;
		int sliceSize = width*height;				
		// count potentially floodable voxels and condition the image by setting
		// voxels to be mapped to open and  and all others to solid

		switch(mapChoice)
		{
		case "Map 0":
			for(k=0;k<depth;k++)
			{
				tmp= (float[])data[k];
				for(i=0;i<sliceSize;i++)
				{
					if(tmp[i]==0)
					{
						tmp[i]=open;
						pixInRange++;
					}
					else tmp[i]=solid;
				}
			}
			break;

		case "Map !0":
			for(k=0;k<depth;k++)
			{
				tmp= (float[])data[k];
				for(i=0;i<sliceSize;i++)
				{
					if(tmp[i]!=0)
					{
						tmp[i]=open;
						pixInRange++;
					}
					else tmp[i]=solid;
				}
			}
			break;
		}

		return pixInRange;
	}

	//*********************************************************************************************		
	// Convert SOLID to -2 and inaccessible PORE to -1
	private void cleanUpGDT3D(Object[] data, int width, int height, int depth)
	{
		int sliceSize = width*height;
		float val;
		float[] tmp;
		for(int k=0;k<depth;k++)
		{
			tmp = (float[])data[k];
			for(int i=0;i<sliceSize;i++)
			{
				val = tmp[i];
				if(val==open) tmp[i]=-1;
				else if(val==solid) tmp[i]=-2; 
			}
		}
	}

	//*********************************************************************************************
	//*********************************************************************************************
	//2D Public methods
	//*********************************************************************************************
	//*********************************************************************************************
	
	/**Used to build Dialogs
	 * @return A list of choices for the start of the GDT: "LeftEdge","RightEdge","TopEdge","BottomEdge","Point(s)"
	 */
	public String[] getSeedChoices2D()
	{
		return seedChoices2D;
	}

	//*********************************************************************************************
	/**2D Geodesic distance mapper
	 * @param data Object instanceof float[], A row major 1D representation of floating point 32-bit 3D image
	 * @param width The image width in pixels
	 * @param height The height width in pixels
	 * @param pixWidth The pixel width in user units
	 * @param pixHeight The pixel height in user units
	 * @param mapChoice A string selecting the region to map; "Map 0" or "Map !0"
	 * @param seedChoice Choices are "LeftEdge","RightEdge","TopEdge","BottomEdge","Point(s)"
	 * @param xPts An array of seedPoint column indices, pass null if not using Point(s) choice
	 * @param yPts An array of seedPoint row indices, pass null if not using Point(s) choice
	 */
	public void gdt2D(Object data, int width, int height,
			float pixWidth, float pixHeight,
			String mapChoice, String seedChoice,
			int[] xPts, int[] yPts)
	{
		if(!(data instanceof float[])) return;
		
		float[] fData = (float[])data;

		offsets2D= Offsets.getIndexOffsets2D( pixWidth, pixHeight);
		whDiag= offsets2D[4].val;

		//Convert data to PORE and SOLID
		conditionData2D(fData,width,height,mapChoice);
		//Initialize the seed voxels to a distance if 1
		ArrayList<PointDesc> seedPts = initSeedPoints2D(fData,width,height,seedChoice,xPts,yPts);
		//Create the list of offsets to 8 voxels around the home voxel


		while(seedPts.isEmpty()==false)
		{
			ArrayList<PointDesc> outPts = null;
			ArrayList<PointDesc> openPts;
			ArrayList<PointDesc> setPts= new ArrayList<PointDesc>();
			
			for(PointDesc seedPt : seedPts)
			{
				openPts = findOpenPixels2D(fData,width,height,seedPt);
				for(PointDesc openPt : openPts)
				{
					outPts = findTouchingPixels2D(fData,width,height,openPt.x,openPt.y);
					PointDesc min = getMinPoint2D(fData,width,height,outPts);
					fData[openPt.x + openPt.y*width]= fData[min.x + min.y*width]+(float)min.val;
					//imp.updateAndDraw();
					setPts.add(new PointDesc(openPt.x,openPt.y,(double)fData[openPt.x + openPt.y*width]));
				}
			}
			seedPts.clear();
			seedPts.addAll(setPts);
		}
		cleanUpGDT2D(fData);

	}
	
	//*********************************************************************************************

	/**2D Tortuosity mapper
	 * @param data Object instanceof float[], A row major 1D representation of floating point 32-bit 3D image
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param pixWidth The pixel width width in user units
	 * @param pixHeight The pixel height height in user units
	 * @param floodChoice Choices are "Map 0" and "Map !0"
	 * @param seedChoice Choices are "LeftEdge","RightEdge","TopEdge","BottomEdge","Point(s)"
	 * @param xPts An array of seedPoint column indices, pass null if not using Point(s) choice
	 * @param yPts An array of seedPoint row indices
	 */
	public void tort2D(Object data, int width, int height,float pixWidth, float pixHeight, String floodChoice, String seedChoice, int[] xPts, int[] yPts)

	{
		if(!(data instanceof float[])) return;
		float[] fData = (float[])data;

		int	i,j,home,slice;

		//Get initial GDT
		gdt2D(data, width, height, pixWidth, pixHeight, floodChoice, seedChoice, xPts, yPts);
		float[] tortArr = fData;
		switch(seedChoice)
		{
		case "LeftEdge":
			//ignore the left slice where gdt = 0
			for(i=1,slice=1;i<width;i++,slice++)
			{
				for(j=0;j<height;j++)
				{
					home = i+j*width;
					if(tortArr[home]>0)
						tortArr[home] /= pixWidth*(slice);
				}
			}
			break;
		case "RightEdge":
			//ignore the right slice where gdt = 0
			for(i=width-2,slice=1;i>=0;i--,slice++)
			{
				for(j=0;j<height;j++)
				{
					home = i+j*width;
					if(tortArr[home]>0)
						tortArr[home] /= pixWidth*(slice);
				}
			}
			break;
		case "TopEdge":
			for(i=0;i<width;i++)
			{
				//for(j=0,slice=1;j<height;j++,slice++)
				for(j=1,slice=1;j<height;j++,slice++)
				{
					home = i+j*width;
					if(tortArr[home]>0)
						tortArr[home] /= pixHeight*(slice);
				}
			}
			break;
		case "BottomEdge":
			for(i=0;i<width;i++)
			{
				//for(j=0,slice=height;j<height;j++,slice--)
				for(j=height-2,slice=1;j>=0;j--,slice++)
				{
					home = i+j*width;
					if(tortArr[home]>0)
						tortArr[home] /= pixHeight*(slice);
				}
			}
			break;
		case "Point(s)":
			float[] mtVol = new float[tortArr.length];
			float fVal;
			if(floodChoice == "Map 0") fVal = 0; else fVal = 255;
			for(i = 0;i< mtVol.length;i++ ) mtVol[i]=fVal;
			//float[] mtGdtArr = gdt2D(mtVol, width, height, floodChoice, seedChoice, xPts, yPts);
			gdt2D(mtVol, width, height,  pixWidth,  pixHeight, floodChoice, seedChoice, xPts, yPts);
			//for(i = 0;i< mtVol.length;i++ ) tortArr[i]/=mtGdtArr[i];			
			for(i = 0;i< mtVol.length;i++ )
			{
				if(tortArr[i]>0)
				{
					tortArr[i]/=mtVol[i];			
				}
			}
			break;					
		}
		//cleanUpTort(tortArr);		
	}
	
	//***********************************************************************************************
	/**Finds the shortest path through a 2D GDT image between each probePoint and the GDT origin.
	 * @param data Object instance of float[], a row major 1D representation of a 2D image
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param probePts a list of the starting points for the path to the 0 GDT destination
	 * @return An Array of lists of the points along the shortest path between each probe point and the 0 GDT destination
	 */
	public ArrayList<PointDesc>[] getGeoPaths2D(Object data,int width,int height, ArrayList<PointDesc>probePts)
	{
		if(!(data instanceof float[])) return null;
		
		@SuppressWarnings("unchecked")
		ArrayList<PointDesc>[] paths = new ArrayList[probePts.size()];
		for(int i=0;i<probePts.size();i++)
		{
			ArrayList<PointDesc> path = getGeoPath2D((float[])data,width,height,probePts.get(i));
			paths[i]=path;
		}
		return paths;
	}

	//***********************************************************************************************
	/**Finds the shortest path through a 2D GDT image between a probePoint and the GDT origin.
	 * @param data Object instance of float[], a row major 1D representation of a 2D image
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param probePt the starting point for the path to the 0 GDT destination
	 * @return A list of the points along the shortest path between each probe point and the 0 GDT destination
	 */
	public ArrayList<PointDesc> getGeoPath2D(Object data, int width, int height, PointDesc probePt)
	{
		double	val;

		if(!(data instanceof float[])) return null;
		
		ArrayList<PointDesc> path = new ArrayList<PointDesc>();
		//initGdtOffsets2D();
		offsets2D = getIndexOffsets2D();
		float[] fData = (float[])data;

		//Add the probePt to the top of the list
		val = fData[probePt.x + probePt.y*width];
		path.add(new PointDesc(probePt.x,probePt.y,probePt.z,val));
		while(val>0)
		{
			PointDesc minPt = getLclMin2D(fData,width,height,probePt);
			path.add(new PointDesc(minPt.x,minPt.y,minPt.val));
			probePt=minPt;
			val = probePt.val;
		}
		return path;
	}
	
	//*********************************************************************************************
	//2D Private methods
	//*********************************************************************************************
	
	//*********************************************************************************************
	/** 
	 * @param data  a row major 1D representation of a 2D image
	 * @param width The image width in pixels
	 * @param height The image height in pixels
	 * @param PointDesc an 2D point in the image, the z parameter is returned unchanged.
	 * @return the location and value of the 8 connected pixel with the smallest value
	 */
	private PointDesc getLclMin2D(float[] data, int width, int height, PointDesc probePt)
	{
		float min=Float.MAX_VALUE;
		PointDesc minPoint=null;

		for(PointDesc offset : offsets2D)
		{
			int x = probePt.x + offset.x;
			int y = probePt.y + offset.y;
			if(x>=0 && x<width && y>=0 && y< height)
			{
				float val = data[x + y*width];
				if(val<min && val >= 0)
				{
					min = val;
					minPoint = new PointDesc(x,y,probePt.z,val);
				}
			}
		}		
		return minPoint;
	}
	
	//*********************************************************************************************
	
	private ArrayList<PointDesc> findOpenPixels2D(float[] data, int width, int height, PointDesc seedPt)
	{
		int index;
		ArrayList<PointDesc> touchList = new ArrayList<PointDesc>();
		for(PointDesc offsetPt : offsets2D)
		{
			int i = seedPt.x + offsetPt.x;
			int j = seedPt.y + offsetPt.y;
			if(i<width && i>=0 && j<height && j>=0)
			{
				index = i+j*width;
				float pixVal=data[index];
				if(pixVal==open)
				{
					touchList.add(new PointDesc(i,j,offsetPt.val));
				}
				//else if( pixVal < open && pixVal > seedPt.val + whDiag *(1 + tolerance))
				else if( pixVal < open && pixVal >= seedPt.val + whDiag *(1 + tolerance))
				{
					//Since the distance cannot increase faster than the diagonal distance, pixels with a greater increase
					//are reassigned to the pore space to be re-flooded.
					data[index] = open;
					//By tolerating small errors speed is significantly increased.
					//This is probably due to the accuracy limits of of 32bit float.  Remember we
					//are doing hundreds of additions of sqrt2 and sqrt3
					//real images are NEVER this accurate *-)
				}
			}
		}
		return touchList;
	}

	//*********************************************************************************************

	private ArrayList<PointDesc> findTouchingPixels2D(float[] data, int width, int height, int iCol,int jRow)
	{
		ArrayList<PointDesc> touchList = new ArrayList<PointDesc>();
		for(PointDesc offsetPt : offsets2D)
		{
			int i = iCol + offsetPt.x;
			int j = jRow + offsetPt.y;
			if(i<width && i>=0 && j<height && j>=0)
			{
				touchList.add(new PointDesc(i,j,offsetPt.val));
			}
		}
		return touchList;
	}
	
	//*********************************************************************************************

	private PointDesc getMinPoint2D(float[] data, int width, int height, ArrayList<PointDesc> pointList)
	{
		float min=Float.MAX_VALUE;
		PointDesc minPoint=null;
		
		for(PointDesc point : pointList)
		{			
			if(data[point.x + point.y*width]<min)
			{
				min = data[point.x + point.y*width];
				minPoint = point;
			}
		}		
		return minPoint;
	}
	
	//*********************************************************************************************	
	// Creates a list of user-selected seed points
	private ArrayList<PointDesc> initSeedPoints2D(float[] data,int width,int height,String seedChoice, int[] xPts, int[] yPts)
	{
		int home,i,j;//,k;
		final double seedVal = 0;
		ArrayList<PointDesc> seedPL = new ArrayList<PointDesc>();

		//Get the list of origin points
		switch(seedChoice)
		{
		case "LeftEdge":
			i = 0;
			for(j=0;j<height;j++)
			{
				home	= i + j*width;
				if(data[home] == open)
				{
					data[home] =  (float)seedVal;
					seedPL .add(new PointDesc(i,j,1,seedVal));
				}
			}
			break;
		case "RightEdge":
			i = width-1;
			for(j=0;j<height;j++)
			{
				home	= i + j*width;
				if(data[home] == open)
				{
					data[home] =  (float)seedVal;
					seedPL .add(new PointDesc(i,j,1,seedVal));
				}
			}
			break;
		case "TopEdge":
			j=0;
			for(i=0;i<width;i++)
			{
				home	= i + j*width;
				if(data[home] == open)
				{
					data[home] =  (float)seedVal;
					seedPL .add(new PointDesc(i,j,1,seedVal));
				}
			}
			break;
		case "BottomEdge":
			j=height-1;
			for(i=0;i<width;i++)
			{
				home	= i + j*width;
				if(data[home] == open)
				{
					data[home] =  (float)seedVal;
					seedPL .add(new PointDesc(i,j,1,seedVal));
				}
			}
			break;
		case "Point(s)":
			for(i = 0; i<yPts.length;i++)
			{
				home	= xPts[i] + yPts[i]*width;
				data[home] = (float)seedVal;
				seedPL.add(new PointDesc(xPts[i],yPts[i],1,seedVal));
			}
			break;
		}
		return seedPL;	
	}

	//*********************************************************************************************	
	// Converts the binary data to SOLID and PORE and returns the number of mappable voxels
	private int conditionData2D(float[] data,int width,int height, String mapChoice)
	{
		int		i, pixInRange=0;
		int sliceSize = width*height;				
		// count potentially floodable voxels and condition the image by setting
		// voxels to be mapped to open and  and all others to solid

		switch(mapChoice)
		{
		case "Map 0":
				for(i=0;i<sliceSize;i++)
				{
					if(data[i]==0)
					{
						data[i]=open;
						pixInRange++;
					}
					else data[i]=solid;
				}
			break;

		case "Map !0":
				for(i=0;i<sliceSize;i++)
				{
					if(data[i]!=0)
					{
						data[i]=open;
						pixInRange++;
					}
					else data[i]=solid;
				}
			break;
		}

		return pixInRange;
	}

	//*********************************************************************************************		
	// Convert SOLID to -2 and inaccessible PORE to -1
	private void cleanUpGDT2D(float[] data)
	{
		for(int i=0;i<data.length;i++)
		{
			if(data[i] == open) data[i]=-1;
			else if(data[i] == solid) data[i]=-2;								
		}		
	}

}
