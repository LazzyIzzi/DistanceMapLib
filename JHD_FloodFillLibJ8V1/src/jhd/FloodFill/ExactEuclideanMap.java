package jhd.FloodFill;

import java.awt.Dimension;
//import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


/**Functions for computing the exact 2D and 3D Euclidean distance using Danielsson's algorithm.
 * A port of my original C to Java. It's not pretty Java but it's fast and somewhat readable.*/
public class ExactEuclideanMap 
{
	final String[] mapChoices = {"Map 0","Map !0"};

	//***************************************************************************************************

	/**Default constructor*/
	public ExactEuclideanMap(){}

	//***************************************************************************************************

	private void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	//***************************************************************************************************

	/**In Place 2D exact Euclidean distance map of a 2D image using Danielsson's algorithm
	 * @param image A row major 1D float array containing a 2D image
	 * @param imageWidth The image width in pixels
	 * @param imageHeight The height width in pixels
	 * @param pixWidth The pixel width in user units, set to 1 for classic EDM
	 * @param pixHeight The pixel height  in user units, set to 1 for classic EDM
	 * @param floodChoice A string selecting the region to map; "Map 0" or "Map !0"
	 */
	public void edm2D(float[] image, int imageWidth, int imageHeight, float pixWidth, float pixHeight, String floodChoice)
	{

		int i,j,k,n,size,minIndex;
		int home,offsetUP,offsetDOWN,offsetLEFT,offsetRIGHT;
		int offsetUP_LEFT,offsetUP_RIGHT,offsetDOWN_LEFT,offsetDOWN_RIGHT;
		float min=32000.0f;
		float minVal=0.0f;	

		size = imageWidth *imageHeight;

		float[] vertDist =  new float[9];
		float[] vertData = new float[size];
		float[] horizDist =  new float[9];
		float[] horizData = new float[size];
		float[] myVector = new float[9];

		offsetUP			= - imageWidth;
		offsetDOWN			= imageWidth;
		offsetLEFT			= -1;
		offsetRIGHT			= +1;
		offsetUP_LEFT		= -1 -	imageWidth;
		offsetUP_RIGHT		= +1 -	imageWidth;
		offsetDOWN_LEFT		= -1 +	imageWidth;
		offsetDOWN_RIGHT	= +1 +	imageWidth;

		//Condition data and copy the image into the distance buffers
		switch(floodChoice)
		{
		case "Map !0": 
			for(i=0;i<size;i++)
			{
				if(image[i]!=0.0) image[i] = min; else image[i] = 0.0f;	
				vertData[i] = horizData[i] = image[i];
			}
			break;
		case "Map 0":
			for(i=0;i<size;i++)
			{
				if(image[i]==0) image[i] = min; else image[i] = 0.0f;	
				vertData[i] = horizData[i] = image[i];
			}
			break;
		}

		for (j = 1; j < imageHeight; j++)
		{
			for(i = 1; i < imageWidth; i++)
			{
				home	= i + j*imageWidth;
				if(image[home]>0)
				{						
					vertDist[0]	= vertData[home];			
					vertDist[1]	= vertData[home + offsetUP]+pixHeight;					
					vertDist[2]	= vertData[home + offsetLEFT];
					vertDist[3]	= vertData[home + offsetUP_LEFT]+pixHeight;									

					horizDist[0]	= horizData[home];		
					horizDist[1]	= horizData[home + offsetUP];					
					horizDist[2]	= horizData[home + offsetLEFT]+pixWidth;
					horizDist[3]	= horizData[home + offsetUP_LEFT]+pixWidth;										

					for(n = 0;n < 4; n++)
						myVector[n] = (float) (vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]);

					// pick the smallest one
					minVal	= myVector[0];
					minIndex	= 0;
					for(n = 0;n < 4;n++)
						if(myVector[n] < minVal)
						{
							minVal= myVector[n];
							minIndex = n;
						}
					vertData[home] = vertDist[minIndex];
					horizData[home] = horizDist[minIndex];
				}
			}

			for(i = imageWidth -2; i >= 0; i--)
			{
				home	= i + j*imageWidth;
				if(image[home]>0)
				{						
					vertDist[0]	= vertData[home];		
					vertDist[1]	= vertData[home + offsetRIGHT];
					vertDist[2]	= vertData[home + offsetUP_RIGHT]+pixHeight;	

					horizDist[0]	= horizData[home];		
					horizDist[1]	= horizData[home + offsetRIGHT]+pixWidth;
					horizDist[2]	= horizData[home + offsetUP_RIGHT]+pixWidth;

					for(n = 0;n < 3; n++)
						myVector[n] = (float) (vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]);

					// pick the smallest one
					minVal	= myVector[0];
					minIndex	= 0;
					for(n = 0;n < 3;n++)
						if(myVector[n] < minVal)
						{
							minVal= myVector[n];
							minIndex = n;
						}
					vertData[home] = vertDist[minIndex];
					horizData[home] = horizDist[minIndex];
				}
			}
		}

		// second picture scan					
		for (j = imageHeight -2; j>=0; j--)
		{
			for(i = 1; i < imageWidth; i++)
			{
				home	= i + j*imageWidth;
				if(image[home]>0)
				{						
					vertDist[0]	= vertData[home];			
					vertDist[1]	= vertData[home + offsetDOWN]+pixHeight;	
					vertDist[2]	= vertData[home + offsetDOWN_LEFT]+pixHeight;	
					vertDist[3]	= vertData[home + offsetLEFT];

					horizDist[0]	= horizData[home];			
					horizDist[1]	= horizData[home + offsetDOWN];	
					horizDist[2]	= horizData[home + offsetDOWN_LEFT]+pixWidth;
					horizDist[3]	= horizData[home + offsetLEFT]+pixWidth;
					for(n = 0;n < 4; n++)
						myVector[n] = (float) (vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]);

					// pick the smallest one
					minVal	= myVector[0];
					minIndex	= 0;
					for(n = 0;n < 4; n++)
						if(myVector[n] < minVal)
						{
							minVal= myVector[n];
							minIndex = n;
						}
					vertData[home] = vertDist[minIndex];
					horizData[home] = horizDist[minIndex];
				}
			}
			for(i = imageWidth -2; i >= 0; i--)
			{
				home	= i + j*imageWidth;
				if(image[home]>0)
				{						
					vertDist[0]	= vertData[home];
					vertDist[1]	= vertData[home + offsetDOWN_RIGHT]+pixHeight;	
					vertDist[2]	= vertData[home + offsetRIGHT];

					horizDist[0]	= horizData[home];
					horizDist[1]	= horizData[home + offsetDOWN_RIGHT]+pixWidth;
					horizDist[2]	= horizData[home + offsetRIGHT]+pixWidth;

					for(n = 0;n < 3; n++)
						myVector[n] = (float) (vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]);

					// pick the smallest one
					minVal	= myVector[0];
					minIndex	= 0;
					for(n = 0;n < 3; n++)
						if(myVector[n] < minVal)
						{
							minVal= myVector[n];
							minIndex = n;
						}
					vertData[home] = vertDist[minIndex];
					horizData[home] = horizDist[minIndex];
				}
			}
		}		

		//calculate the actual myVector distances from the indices for non-zero values
		for (k = 0; k < size; k++)
			if( image[k] >0) image[k] = (float) Math.sqrt( (double)vertData[k]* (double)vertData[k]+ (double)horizData[k]* (double)horizData[k]);
	}

	//***************************************************************************************************

	/**In Place 2D exact Euclidean distance map of a 2D image using Danielsson's algorithm
	 * @param oImage An Object (must be instanceof float[]) containing a row major 1D array of a 2D image as returned by ImageJ ImageProcessor ip.getPixels();
	 * @param imageWidth The image width in pixels
	 * @param imageHeight The height width in pixels
	 * @param pixWidth The pixel width in user units, set to 1 for classic EDM
	 * @param pixHeight The pixel height  in user units, set to 1 for classic EDM
	 * @param floodChoice A string selecting the region to map; "Map 0" or "Map !0"
	 */
	public void edm2D(Object oImage, int imageWidth, int imageHeight, float pixWidth, float pixHeight, String floodChoice)
	{
		if(!(oImage instanceof float[]))
		{
			infoBox("Only Objects instanceof float[] are supported", "In ExactEuclideanMap.edm3D");
			return;
		}		
		edm2D((float[]) oImage, imageWidth,  imageHeight,  pixWidth,  pixHeight,  floodChoice);
	}

	//***************************************************************************************************

	/**In place 3D Euclidean distance map of a 3D image using Danielsson's algorithm
	 * @param image A row major 1D float array containing a 3D image
	 * @param imageWidth The image width in pixels
	 * @param imageHeight The height width in pixels
	 * @param imageDepth The depth or number of slices in the 3D image
	 * @param pixWidth The pixel width in user units, set to 1 for classic EDM
	 * @param pixHeight The pixel height  in user units, set to 1 for classic EDM
	 * @param pixDepth The pixel depth in user units, set to 1 for classic EDM
	 * @param floodChoice A string selecting the region to map; "Map 0" or "Map !0"
	 * @param showProgress true = Display a progress bar during execution.
	 */
	public void edm3D(float[] image, int imageWidth, int imageHeight, int imageDepth, float pixWidth, float pixHeight, float pixDepth, String floodChoice, boolean showProgress)
	{
		JPanel fldPanel;
		JFrame frame=null;		
		JProgressBar prgBar=null;

		if(showProgress)
		{	
			//Set up the progress bar
			fldPanel = new JPanel();
			frame = new JFrame("EDM Progress");		
			prgBar = new JProgressBar(0,imageDepth*2);

			frame.setSize(400, 100);
			frame.setLocationRelativeTo(null);

			prgBar.setPreferredSize(new Dimension(350, 30));
			prgBar.setValue(0);
			prgBar.setStringPainted(true);			
			fldPanel.add(prgBar);

			frame.add(fldPanel);
			frame.setVisible(true);
		}

		int i,j,k,n,minIndex;
		float minVal,min;
		int home,sliceSize,stackSize;
		int sliceCount;

		sliceCount	= 0;
		sliceSize	= imageWidth * imageHeight;
		stackSize	= imageWidth * imageHeight * imageDepth;
		min=32000.0f;


		float[] vertData	= new float[stackSize];
		float[] horizData	= new float[stackSize];
		float[] depthData	= new float[stackSize];

		float[] vertDist	= new float[9];
		float[] horizDist	= new float[9];
		float[] depthDist	= new float[9];
		float[] myVector	= new float[9];

		// face touching offsets
		int offsetUP		= - imageWidth;
		int offsetDOWN	= imageWidth;
		int offsetLEFT	= -1;
		int offsetRIGHT	= +1;
		int offsetFWD		= sliceSize;
		int offsetBACK	= -sliceSize;

		// edge touching offsets
		int offsetUP_LEFT		= -1 -imageWidth;
		int offsetUP_RIGHT		= +1 -imageWidth;
		int offsetDOWN_LEFT		= -1 +imageWidth;
		int offsetDOWN_RIGHT	= +1 +imageWidth;
		int offsetBACK_UP		= -imageWidth-sliceSize;
		int offsetBACK_DOWN		= imageWidth - sliceSize;
		int offsetBACK_LEFT		= -1 -sliceSize;
		int offsetBACK_RIGHT	= +1 -sliceSize;
		int offsetFWD_UP		= -imageWidth + sliceSize;
		int offsetFWD_DOWN		= imageWidth + sliceSize;
		int offsetFWD_LEFT		= -1 +sliceSize;
		int offsetFWD_RIGHT		= +1 +sliceSize;

		// the farthest neighbors, corner touching
		int offsetBACK_UP_LEFT		= -1 -imageWidth - sliceSize;	//back upLeft
		int offsetBACK_UP_RIGHT		= +1 -imageWidth - sliceSize;	//back upRight
		int offsetBACK_DOWN_LEFT	= -1 +imageWidth - sliceSize;	//back downLeft
		int offsetBACK_DOWN_RIGHT	=+1 +imageWidth - sliceSize;	//back downRight
		int offsetFWD_UP_LEFT		= -1 -imageWidth + sliceSize;	//fwd upLeft
		int offsetFWD_UP_RIGHT		= +1 -imageWidth + sliceSize;	//fwd upRight
		int offsetFWD_DOWN_LEFT		= -1 +imageWidth + sliceSize;	//fwd downLeft
		int offsetFWD_DOWN_RIGHT	= +1 +imageWidth + sliceSize;	//fwd downRight

		switch(floodChoice)
		{
		case "Map !0": 
			for(i=0;i<stackSize;i++)
			{
				if(image[i]!=0.0) image[i] = min; else image[i] = 0.0f;				
				vertData[i] =  horizData[i] =  depthData[i] =image[i];
			}
			break;
		case "Map 0":
			for(i=0;i<stackSize;i++)
			{
				if(image[i]==0) image[i] = min; else image[i] = 0.0f;	
				vertData[i] =  horizData[i] =  depthData[i] =image[i];
			}
			break;
		}

		//Scan the volume front to back
		for (k = 1; k < imageDepth; k++)// scan forward
		{
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)// scan right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetUP]+pixHeight;
						vertDist[2]	= vertData[home + offsetLEFT];
						vertDist[3]	= vertData[home + offsetUP_LEFT]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK];
						vertDist[5]	= vertData[home + offsetBACK_LEFT];
						vertDist[6]	= vertData[home + offsetBACK_UP]+pixHeight;
						vertDist[7]	= vertData[home + offsetBACK_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetUP];
						horizDist[2]	= horizData[home + offsetLEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetUP_LEFT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK];
						horizDist[5]	= horizData[home + offsetBACK_LEFT]+pixWidth;
						horizDist[6]	= horizData[home + offsetBACK_UP];
						horizDist[7]	= horizData[home + offsetBACK_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetUP];
						depthDist[2]	= depthData[home + offsetLEFT];
						depthDist[3]	= depthData[home + offsetUP_LEFT];
						depthDist[4]	= depthData[home + offsetBACK]+pixDepth;
						depthDist[5]	= depthData[home + offsetBACK_LEFT]+pixDepth;
						depthDist[6]	= depthData[home + offsetBACK_UP]+pixDepth;
						depthDist[7]	= depthData[home + offsetBACK_UP_LEFT]+pixDepth;

						for(n = 0;n < 8;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 8;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}

				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetRIGHT];
						vertDist[2]	= vertData[home + offsetUP_RIGHT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_RIGHT];
						vertDist[4]	= vertData[home + offsetBACK_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetRIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetUP_RIGHT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_RIGHT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetRIGHT];
						depthDist[2]	= depthData[home + offsetUP_RIGHT];
						depthDist[3]	= depthData[home + offsetBACK_RIGHT]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_UP_RIGHT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];


						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}

			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetDOWN_LEFT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_DOWN]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN];
						horizDist[2]	= horizData[home + offsetDOWN_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_DOWN];
						horizDist[4]	= horizData[home + offsetBACK_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN];
						depthDist[2]	= depthData[home + offsetDOWN_LEFT];
						depthDist[3]	= depthData[home + offsetBACK_DOWN]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN_RIGHT]+pixHeight;
						vertDist[2]	= vertData[home + offsetBACK_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetBACK_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN_RIGHT];
						depthDist[2]	= depthData[home + offsetBACK_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		// Scan the volume back to front

		for (k = imageDepth -2; k >= 0 ; k--)// scan back
		{
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)// scan right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD];
						vertDist[2]	= vertData[home + offsetFWD_LEFT];
						vertDist[3]	= vertData[home + offsetFWD_UP]+pixHeight;
						vertDist[4]	= vertData[home + offsetFWD_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD];
						horizDist[2]	= horizData[home + offsetFWD_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetFWD_UP];
						horizDist[4]	= horizData[home + offsetFWD_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_LEFT]+pixDepth;
						depthDist[3]	= depthData[home + offsetFWD_UP]+pixDepth;
						depthDist[4]	= depthData[home + offsetFWD_UP_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// scan left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_RIGHT];
						vertDist[2]	= vertData[home + offsetFWD_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetFWD_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_RIGHT]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_UP_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetFWD_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN];
						horizDist[2]	= horizData[home + offsetFWD_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN]+pixDepth;	
						depthDist[2]	= depthData[home + offsetFWD_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 2;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 2;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}					
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		//Calculate distances
		for(i=0;i<image.length;i++)
			image[i] = (float) Math.sqrt( vertData[i]* vertData[i]+ horizData[i]* horizData[i]+depthData[i]*depthData[i]);

		if(showProgress) frame.dispose();
		//return image;	
	}

	//******************************************************************************************************

	/**In place 3D Euclidean distance map of a 3D image using Danielsson's algorithm
	 * @param oImage An object containing a 1D float array from a  row major 3D image
	 * @param imageWidth The image width in pixels
	 * @param imageHeight The height width in pixels
	 * @param imageDepth The depth or number of slices in the 3D image
	 * @param pixWidth The pixel width in user units, set to 1 for classic EDM
	 * @param pixHeight The pixel height  in user units, set to 1 for classic EDM
	 * @param pixDepth The pixel depth in user units, set to 1 for classic EDM
	 * @param floodChoice A string selecting the region to map; "Map 0" or "Map !0"
	 * @param showProgress true = Display a progress bar during execution.
	 */
	public void edm3D(Object oImage, int imageWidth, int imageHeight, int imageDepth, float pixWidth, float pixHeight, float pixDepth, String floodChoice, boolean showProgress)
	{

		if(!(oImage instanceof float[]))
		{
			infoBox("Only Objects instanceof float[] are supported", "In ExactEuclideanMap.edm3D");
			return;
		}

		JPanel fldPanel;
		JFrame frame=null;		
		JProgressBar prgBar=null;

		float[] image = (float[])oImage;

		if(showProgress)
		{	
			//Set up the progress bar
			fldPanel = new JPanel();
			frame = new JFrame("EDM Progress");		
			prgBar = new JProgressBar(0,imageDepth*2);

			frame.setSize(400, 100);
			frame.setLocationRelativeTo(null);

			prgBar.setPreferredSize(new Dimension(350, 30));
			prgBar.setValue(0);
			prgBar.setStringPainted(true);			
			fldPanel.add(prgBar);

			frame.add(fldPanel);
			frame.setVisible(true);
		}

		int i,j,k,n,minIndex;
		float minVal,min;
		int home,sliceSize,stackSize;
		int sliceCount;

		sliceCount	= 0;
		sliceSize	= imageWidth * imageHeight;
		stackSize	= imageWidth * imageHeight * imageDepth;
		min=32000.0f;


		float[] vertData	= new float[stackSize];
		float[] horizData	= new float[stackSize];
		float[] depthData	= new float[stackSize];

		float[] vertDist	= new float[9];
		float[] horizDist	= new float[9];
		float[] depthDist	= new float[9];
		float[] myVector	= new float[9];

		// face touching offsets
		int offsetUP	= - imageWidth;
		int offsetDOWN	= imageWidth;
		int offsetLEFT	= -1;
		int offsetRIGHT	= +1;
		int offsetFWD	= sliceSize;
		int offsetBACK	= -sliceSize;

		// edge touching offsets
		int offsetUP_LEFT		= -1 -imageWidth;
		int offsetUP_RIGHT		= +1 -imageWidth;
		int offsetDOWN_LEFT		= -1 +imageWidth;
		int offsetDOWN_RIGHT	= +1 +imageWidth;
		int offsetBACK_UP		=    -imageWidth - sliceSize;
		int offsetBACK_DOWN		=     imageWidth - sliceSize;
		int offsetBACK_LEFT		= -1 -sliceSize;
		int offsetBACK_RIGHT	= +1 -sliceSize;
		int offsetFWD_UP		=    -imageWidth + sliceSize;
		int offsetFWD_DOWN		=     imageWidth + sliceSize;
		int offsetFWD_LEFT		= -1 +sliceSize;
		int offsetFWD_RIGHT		= +1 +sliceSize;

		// the farthest neighbors, corner touching
		int offsetBACK_UP_LEFT		= -1 -imageWidth - sliceSize;	//back upLeft
		int offsetBACK_UP_RIGHT		= +1 -imageWidth - sliceSize;	//back upRight
		int offsetBACK_DOWN_LEFT	= -1 +imageWidth - sliceSize;	//back downLeft
		int offsetBACK_DOWN_RIGHT	= +1 +imageWidth - sliceSize;	//back downRight
		int offsetFWD_UP_LEFT		= -1 -imageWidth + sliceSize;	//fwd upLeft
		int offsetFWD_UP_RIGHT		= +1 -imageWidth + sliceSize;	//fwd upRight
		int offsetFWD_DOWN_LEFT		= -1 +imageWidth + sliceSize;	//fwd downLeft
		int offsetFWD_DOWN_RIGHT	= +1 +imageWidth + sliceSize;	//fwd downRight

		switch(floodChoice)
		{
		case "Map !0": 
			for(i=0;i<stackSize;i++)
			{
				if(image[i]!=0.0) image[i] = min; else image[i] = 0.0f;				
				vertData[i] =  horizData[i] =  depthData[i] =image[i];
			}
			break;
		case "Map 0":
			for(i=0;i<stackSize;i++)
			{
				if(image[i]==0) image[i] = min; else image[i] = 0.0f;	
				vertData[i] =  horizData[i] =  depthData[i] =image[i];
			}
			break;
		}

		//Scan the volume front to back
		for (k = 1; k < imageDepth; k++)// scan forward
		{
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)// scan right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetUP]+pixHeight;
						vertDist[2]	= vertData[home + offsetLEFT];
						vertDist[3]	= vertData[home + offsetUP_LEFT]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK];
						vertDist[5]	= vertData[home + offsetBACK_LEFT];
						vertDist[6]	= vertData[home + offsetBACK_UP]+pixHeight;
						vertDist[7]	= vertData[home + offsetBACK_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetUP];
						horizDist[2]	= horizData[home + offsetLEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetUP_LEFT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK];
						horizDist[5]	= horizData[home + offsetBACK_LEFT]+pixWidth;
						horizDist[6]	= horizData[home + offsetBACK_UP];
						horizDist[7]	= horizData[home + offsetBACK_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetUP];
						depthDist[2]	= depthData[home + offsetLEFT];
						depthDist[3]	= depthData[home + offsetUP_LEFT];
						depthDist[4]	= depthData[home + offsetBACK]+pixDepth;
						depthDist[5]	= depthData[home + offsetBACK_LEFT]+pixDepth;
						depthDist[6]	= depthData[home + offsetBACK_UP]+pixDepth;
						depthDist[7]	= depthData[home + offsetBACK_UP_LEFT]+pixDepth;

						for(n = 0;n < 8;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 8;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}

				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetRIGHT];
						vertDist[2]	= vertData[home + offsetUP_RIGHT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_RIGHT];
						vertDist[4]	= vertData[home + offsetBACK_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetRIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetUP_RIGHT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_RIGHT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetRIGHT];
						depthDist[2]	= depthData[home + offsetUP_RIGHT];
						depthDist[3]	= depthData[home + offsetBACK_RIGHT]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_UP_RIGHT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];


						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}

			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetDOWN_LEFT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_DOWN]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN];
						horizDist[2]	= horizData[home + offsetDOWN_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_DOWN];
						horizDist[4]	= horizData[home + offsetBACK_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN];
						depthDist[2]	= depthData[home + offsetDOWN_LEFT];
						depthDist[3]	= depthData[home + offsetBACK_DOWN]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN_RIGHT]+pixHeight;
						vertDist[2]	= vertData[home + offsetBACK_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetBACK_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN_RIGHT];
						depthDist[2]	= depthData[home + offsetBACK_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		// Scan the volume back to front

		for (k = imageDepth -2; k >= 0 ; k--)// scan back
		{
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)// scan right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD];
						vertDist[2]	= vertData[home + offsetFWD_LEFT];
						vertDist[3]	= vertData[home + offsetFWD_UP]+pixHeight;
						vertDist[4]	= vertData[home + offsetFWD_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD];
						horizDist[2]	= horizData[home + offsetFWD_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetFWD_UP];
						horizDist[4]	= horizData[home + offsetFWD_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_LEFT]+pixDepth;
						depthDist[3]	= depthData[home + offsetFWD_UP]+pixDepth;
						depthDist[4]	= depthData[home + offsetFWD_UP_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// scan left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_RIGHT];
						vertDist[2]	= vertData[home + offsetFWD_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetFWD_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_RIGHT]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_UP_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetFWD_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN];
						horizDist[2]	= horizData[home + offsetFWD_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN]+pixDepth;	
						depthDist[2]	= depthData[home + offsetFWD_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					home	= i + j*imageWidth + k*imageWidth*imageHeight;
					if(image[home]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 2;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 2;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}					
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		//Calculate distances
		for(i=0;i<image.length;i++)
			image[i] = (float) Math.sqrt( vertData[i]* vertData[i]+ horizData[i]* horizData[i]+depthData[i]*depthData[i]);

		if(showProgress) frame.dispose();
		//return image;	
	}

	//******************************************************************************************************

	/**In Place 3D exact Euclidean distance of a 3D image using Danielsson's algorithm
	 * @param oImageArr An Object[] (must be float data) as returned by <a href="https://imagej.nih.gov/ij/developer/api/ij/ij/ImageStack.html#getImageArray()">ImageJ ImagePlus imp.getStack().getImageArray();</a>
	 * @param imageWidth The image width in pixels
	 * @param imageHeight The height width in pixels
	 * @param imageDepth The depth or number of slices in the 3D image
	 * @param pixWidth The pixel width in user units, set to 1 for classic EDM
	 * @param pixHeight The pixel height  in user units, set to 1 for classic EDM
	 * @param pixDepth The pixel depth in user units, set to 1 for classic EDM
	 * @param floodChoice A string selecting the region to map; "Map 0" or "Map !0"
	 * @param showProgress true = Display a progress bar during execution.
	 */
	public void edm3D(Object[] oImageArr, int imageWidth, int imageHeight, int imageDepth, float pixWidth, float pixHeight, float pixDepth, String floodChoice, boolean showProgress)
	{
		if(!(oImageArr[0] instanceof float[]))
		{
			infoBox("Object[] data must be such that (data[0] instanceof float[]) = true","ExactEuclideanMap.edm3D Bad Data");
			return;
		}

		JPanel fldPanel;
		JFrame frame=null;		
		JProgressBar prgBar=null;

		if(showProgress)
		{	
			//Set up the progress bar
			fldPanel = new JPanel();
			frame = new JFrame("EDM Progress");		
			prgBar = new JProgressBar(0,imageDepth*2);

			frame.setSize(400, 100);
			frame.setLocationRelativeTo(null);

			prgBar.setPreferredSize(new Dimension(350, 30));
			prgBar.setValue(0);
			prgBar.setStringPainted(true);			
			fldPanel.add(prgBar);

			frame.add(fldPanel);
			frame.setVisible(true);
		}

		int i,j,k,n,minIndex;
		float minVal,min;
		int home,sliceSize,stackSize;
		int sliceCount;

		sliceCount	= 0;
		sliceSize	= imageWidth * imageHeight;
		stackSize	= imageWidth * imageHeight * imageDepth;
		min=32000.0f;

		//Tests showed that individual arrays for the distance data
		//are created significantly faster than a single array of a custom class
		//such as distance[i].vert, distance[i].horiz, distance[i].depth.
		//Once created, the execution times are roughly the same.
		float[] vertData	= new float[stackSize];
		float[] horizData	= new float[stackSize];
		float[] depthData	= new float[stackSize];

		//local voxel distances
		float[] vertDist	= new float[9];
		float[] horizDist	= new float[9];
		float[] depthDist	= new float[9];
		float[] myVector	= new float[9];

		// The offsets are calculated in-line at startup.
		// They are not put in an array to avoid array access time
		// and the regions probed by the algorithm are not necessarily sequential.
		// face touching offsets
		int offsetUP	= - imageWidth;
		int offsetDOWN	= imageWidth;
		int offsetLEFT	= -1;
		int offsetRIGHT	= +1;
		int offsetFWD	= sliceSize;
		int offsetBACK	= -sliceSize;

		// edge touching offsets
		int offsetUP_LEFT		= -1 -imageWidth;
		int offsetUP_RIGHT		= +1 -imageWidth;
		int offsetDOWN_LEFT		= -1 +imageWidth;
		int offsetDOWN_RIGHT	= +1 +imageWidth;
		int offsetBACK_UP		= -imageWidth-sliceSize;
		int offsetBACK_DOWN		= imageWidth - sliceSize;
		int offsetBACK_LEFT		= -1 -sliceSize;
		int offsetBACK_RIGHT	= +1 -sliceSize;
		int offsetFWD_UP		= -imageWidth + sliceSize;
		int offsetFWD_DOWN		= imageWidth + sliceSize;
		int offsetFWD_LEFT		= -1 +sliceSize;
		int offsetFWD_RIGHT		= +1 +sliceSize;

		// the farthest neighbors, corner touching
		int offsetBACK_UP_LEFT		= -1 -imageWidth - sliceSize;	//back upLeft
		int offsetBACK_UP_RIGHT		= +1 -imageWidth - sliceSize;	//back upRight
		int offsetBACK_DOWN_LEFT	= -1 +imageWidth - sliceSize;	//back downLeft
		int offsetBACK_DOWN_RIGHT	= +1 +imageWidth - sliceSize;	//back downRight
		int offsetFWD_UP_LEFT		= -1 -imageWidth + sliceSize;	//fwd upLeft
		int offsetFWD_UP_RIGHT		= +1 -imageWidth + sliceSize;	//fwd upRight
		int offsetFWD_DOWN_LEFT		= -1 +imageWidth + sliceSize;	//fwd downLeft
		int offsetFWD_DOWN_RIGHT	= +1 +imageWidth + sliceSize;	//fwd downRight


		//Condition data and initialize the distance arrays
		switch(floodChoice)
		{
		case "Map !0": 
			for(k=0,n=0;k<imageDepth;k++)
			{
				float[] image = (float[]) oImageArr[k];
				for(i=0;i<image.length;i++)
				{
					if(image[i]!=0.0) image[i] = min;
					else image[i] = 0.0f;
					vertData[n] = horizData[n] = depthData[n] = image[i];
					n++;
				}
			}
			break;
		case "Map 0":
			for(k=0,n=0;k<imageDepth;k++)
			{
				float[] image = (float[]) oImageArr[k];
				for(i=0;i<image.length;i++)
				{
					if(image[i]==0.0) image[i] = min;
					else image[i] = 0.0f;
					vertData[n] = horizData[n] = depthData[n] = image[i];
					n++;
				}
			}
			break;
		}


		//Scan the volume front to back
		for (k = 1; k < imageDepth; k++)// scan forward
		{
			float[] image = (float[]) oImageArr[k];
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)// scan right
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetUP]+pixHeight;
						vertDist[2]	= vertData[home + offsetLEFT];
						vertDist[3]	= vertData[home + offsetUP_LEFT]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK];
						vertDist[5]	= vertData[home + offsetBACK_LEFT];
						vertDist[6]	= vertData[home + offsetBACK_UP]+pixHeight;
						vertDist[7]	= vertData[home + offsetBACK_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetUP];
						horizDist[2]	= horizData[home + offsetLEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetUP_LEFT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK];
						horizDist[5]	= horizData[home + offsetBACK_LEFT]+pixWidth;
						horizDist[6]	= horizData[home + offsetBACK_UP];
						horizDist[7]	= horizData[home + offsetBACK_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetUP];
						depthDist[2]	= depthData[home + offsetLEFT];
						depthDist[3]	= depthData[home + offsetUP_LEFT];
						depthDist[4]	= depthData[home + offsetBACK]+pixDepth;
						depthDist[5]	= depthData[home + offsetBACK_LEFT]+pixDepth;
						depthDist[6]	= depthData[home + offsetBACK_UP]+pixDepth;
						depthDist[7]	= depthData[home + offsetBACK_UP_LEFT]+pixDepth;

						for(n = 0;n < 8;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 8;n++)
						{
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						}

						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}

				for(i = imageWidth -2; i >= 0; i--)// left
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetRIGHT];
						vertDist[2]	= vertData[home + offsetUP_RIGHT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_RIGHT];
						vertDist[4]	= vertData[home + offsetBACK_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetRIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetUP_RIGHT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_RIGHT]+pixWidth;
						horizDist[4]	= horizData[home + offsetBACK_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetRIGHT];
						depthDist[2]	= depthData[home + offsetUP_RIGHT];
						depthDist[3]	= depthData[home + offsetBACK_RIGHT]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_UP_RIGHT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];


						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}

			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetDOWN_LEFT]+pixHeight;
						vertDist[3]	= vertData[home + offsetBACK_DOWN]+pixHeight;
						vertDist[4]	= vertData[home + offsetBACK_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN];
						horizDist[2]	= horizData[home + offsetDOWN_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetBACK_DOWN];
						horizDist[4]	= horizData[home + offsetBACK_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN];
						depthDist[2]	= depthData[home + offsetDOWN_LEFT];
						depthDist[3]	= depthData[home + offsetBACK_DOWN]+pixDepth;
						depthDist[4]	= depthData[home + offsetBACK_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetDOWN_RIGHT]+pixHeight;
						vertDist[2]	= vertData[home + offsetBACK_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetDOWN_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetBACK_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetDOWN_RIGHT];
						depthDist[2]	= depthData[home + offsetBACK_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		// Scan the volume back to front

		for (k = imageDepth -2; k >= 0 ; k--)// scan back
		{
			float[] image = (float[]) oImageArr[k];
			for (j = 1; j < imageHeight; j++)// scan down
			{
				for(i = 1; i < imageWidth; i++)	// scan right
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD];
						vertDist[2]	= vertData[home + offsetFWD_LEFT];
						vertDist[3]	= vertData[home + offsetFWD_UP]+pixHeight;
						vertDist[4]	= vertData[home + offsetFWD_UP_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD];
						horizDist[2]	= horizData[home + offsetFWD_LEFT]+pixWidth;
						horizDist[3]	= horizData[home + offsetFWD_UP];
						horizDist[4]	= horizData[home + offsetFWD_UP_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_LEFT]+pixDepth;
						depthDist[3]	= depthData[home + offsetFWD_UP]+pixDepth;
						depthDist[4]	= depthData[home + offsetFWD_UP_LEFT]+pixDepth;

						for(n = 0;n < 5;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 5;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// scan left
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_RIGHT];
						vertDist[2]	= vertData[home + offsetFWD_UP_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_RIGHT]+pixWidth;
						horizDist[2]	= horizData[home + offsetFWD_UP_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_RIGHT]+pixDepth;
						depthDist[2]	= depthData[home + offsetFWD_UP_RIGHT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}
			// second picture scan					
			for (j = imageHeight -2; j>=0; j--)// up
			{
				for(i = 1; i < imageWidth; i++)// right
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN]+pixHeight;
						vertDist[2]	= vertData[home + offsetFWD_DOWN_LEFT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN];
						horizDist[2]	= horizData[home + offsetFWD_DOWN_LEFT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN]+pixDepth;	
						depthDist[2]	= depthData[home + offsetFWD_DOWN_LEFT]+pixDepth;

						for(n = 0;n < 3;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal		= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 3;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
				for(i = imageWidth -2; i >= 0; i--)// left
				{
					int sliceHome = i + j*imageWidth;
					home = i + j*imageWidth + k*sliceSize;
					if(image[sliceHome]>0)
					{						
						vertDist[0]	= vertData[home];
						vertDist[1]	= vertData[home + offsetFWD_DOWN_RIGHT]+pixHeight;

						horizDist[0]	= horizData[home];
						horizDist[1]	= horizData[home + offsetFWD_DOWN_RIGHT]+pixWidth;

						depthDist[0]	= depthData[home];
						depthDist[1]	= depthData[home + offsetFWD_DOWN_RIGHT]+pixDepth;

						for(n = 0;n < 2;n++)
							myVector[n] = vertDist[n]*vertDist[n]+horizDist[n]*horizDist[n]+depthDist[n]*depthDist[n];

						// pick the smallest one
						minVal	= myVector[0];
						minIndex	= 0;
						for(n = 0;n < 2;n++)
							if(myVector[n] < minVal)
							{
								minVal= myVector[n];
								minIndex = n;
							}
						vertData[home] = vertDist[minIndex];
						horizData[home] = horizDist[minIndex];
						depthData[home] = depthDist[minIndex];
					}
				}
			}					
			sliceCount ++;
			if(showProgress) prgBar.setValue(sliceCount);
		}

		//Calculate distances
		for(k=0,n=0;k<imageDepth;k++)
		{
			float[] image = (float[]) oImageArr[k];
			for(i=0;i<image.length;i++)
			{
				image[i] = (float) Math.sqrt( vertData[n]*vertData[n] + horizData[n]*horizData[n] + depthData[n]*depthData[n]);
				n++;
			}
		}
		if(showProgress) frame.dispose();

	}

	//******************************************************************************************************

	/**Used to make dialogs. User selects  to map 0 or 255 in a binarized image 
	 * @return A list of map choices: "Map 0","Map !0"
	 */
	public String[] getMapChoices()
	{
		return mapChoices;
	}
}
