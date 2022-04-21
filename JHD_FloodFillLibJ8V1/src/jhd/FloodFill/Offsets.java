package jhd.FloodFill;
/* static utility methods for getting the array indices or 1d offsets of the voxels surrounding a pixel or voxel&dot;
 * Be sure to avoid wrapping when using offsets.
 *  
 */
/**A class with static methods for working with voxel offsets
 * <ol>
 * <li>Offsets are used to quickly access the indices of voxels touching a central voxel by simply adding the offset to the voxel's 1D, 2D or 3D index.</li>
 * <li>Offsets arrays are in decreasing order of connectivity, in 3D Face touching(0-5), Edge touching (6-17)and finally Corner touching(17-25).</li>
 * <li>The connectivity of a flood is determined by how far down the list one goes.</li>
 * </ol>
 * */


public class Offsets {

	/**Class for 2D and 3D pixel offset and value information
	 * @author John H Dunsmuir DOB LazziIzzi
	 *
	 */
	public static class PointDesc // PointLoc PointParams PixLoc PixXYZV 
	{
		/**Pixel x coordinate*/
		public int x;
		/**Pixel y coordinate*/
		public int y;
		/**Pixel z coordinate*/
		public int z;
		/**Pixel value*/
		public double val;

		/**Default Constructor*/
		public PointDesc() {}
		
		/**1D offset Constructor 
		 * @param offset1D the absolute offset of a voxel index from 0*/	
		public PointDesc(int offset1D) {this.x = offset1D;}
		
		/**1D offset Constructor 
		 * @param offset1D the absolute offset of a voxel index from 0	
		 * @param val A value associated with the pixel/voxel*/
		public PointDesc(int offset1D, double val) {this.x = offset1D; this.val = val;}

		/**2D offset Constructor 
		 * @param iCol AKA Pixel x coordinate
		 * @param jRow AKA Pixel y coordinate*/
		public PointDesc(int iCol, int jRow) { this.x= iCol; this.y= jRow;}
		
		/**2D offset Constructor 
		 * @param iCol AKA Pixel x coordinate
		 * @param jRow AKA Pixel y coordinate
		 * @param val A value associated with the pixel*/
		public PointDesc(int iCol, int jRow, double val) { this.x= iCol; this.y= jRow; this.val = val;}
		 
		/**3D offset Constructor 
		 * @param iCol AKA Pixel x coordinate
		 * @param jRow AKA Pixel y coordinate
		 * @param kSlice AKA Pixel z coordinate*/
		public PointDesc(int iCol, int jRow, int kSlice) { this.x= iCol; this.y= jRow; this.z = kSlice;}
		 
		/**3D offset Constructor 
		 * @param iCol AKA Pixel x coordinate
		 * @param jRow AKA Pixel y coordinate
		 * @param kSlice AKA Pixel z coordinate
		 * @param val A value associated with the voxel*/
		public PointDesc(int iCol, int jRow, int kSlice, double val) { this.x= iCol; this.y= jRow; this.z = kSlice; this.val= val; };		
	}

	//*******************************************************************************************

	/**
	 * @return A list of voxel connection types "Face","Face &amp; Edge","Face, Edge &amp;Corners"
	 */
	public static String[] GetConnectivityChoices()
	{
		String[] conChoices ={"Face","Face & Edge","Face, Edge &Corners"};		
		return conChoices;
	}
	
	//*******************************************************************************************

	/**
	 * @param connectivityChoice A string returned from GetConnectivityChoices
	 * @return The maximum index of the offset array for the requested connectivity e.g. maxIndex = 6 for Face connected voxels, use for(i=0;i&lt;maxIndex;i++)
	 */
	public static int GetConnectivityValue(String connectivityChoice)
	{
		int maxIndex = -1;
		switch(connectivityChoice)
		{
		case "Face":
			maxIndex=6;
			break;
		case "Face & Edge":
			maxIndex=18;
			break;
		case "Face, Edge &Corners":
			maxIndex=26;
			break;
		}
		return maxIndex;
	}
	//*******************************************************************************************
	
	/** Get the absolute 1D offsets of pixels surrounding a 2D central pixel
	 * @param width image width in pixels
	 * @param height image height in pixels
	 * @return An PointDesc array of offsets from a 1D index, face(0-3), face&#38;edge(0-7)
	 */
	public static PointDesc[] get1dOffsets2D(int width, int height)
	{
		PointDesc[] offsets = new PointDesc[8];
		PointDesc[] offsetPts = getIndexOffsets2D();
		for(int i=0;i<8;i++)
		{
			offsets[i] = new PointDesc(offsetPts[i].x + offsetPts[i].y*width);
		}
		return offsets;			
	}

	//*******************************************************************************************	
	
	/** Get the absolute 1D offsets and center-to-center distances of pixels surrounding a 2D central pixel, pixel units must be the same
	 * @param width image width in pixels
	 * @param height image height in pixels
	 * @param pixWidth pixel width in physical units
	 * @param pixHeight pixel height in physical units
	 * @return An PointDesc array of 1D offsets and physical distances
	 */
	public static PointDesc[] get1dOffsets2D(int width, int height, double pixWidth, double pixHeight)
	{
		PointDesc[] offsetPts = getIndexOffsets2D(pixWidth,pixHeight);
		PointDesc[] offsets = new PointDesc[8];
		for(int i=0;i<8;i++)
		{
			offsets[i] = new PointDesc(offsetPts[i].x + offsetPts[i].y*width, offsetPts[i].val);
		}
		return offsets;			
	}

	//*******************************************************************************************
	
	/** Get the absolute 1D offsets of voxels surrounding a 3D central voxel
	 * @param width image width in pixels
	 * @param height image height in pixels
	 * @param depth depth width in pixels
	 * @return An PointDesc array of offsets from a 1D index,  3D face(0-5) 3D face&#38;edge(0-17), 3D face,edge,corners(0-25) 
	 */
	public static PointDesc[] get1dOffsets3D(int width, int height,int depth)
	{
		PointDesc[] offsets = new PointDesc[26];
		PointDesc[] offsetPts = getIndexOffsets3D();
		int sliceSize = width*height;
		for(int i=0;i<26;i++)
		{
			offsets[i] = new PointDesc(offsetPts[i].x + offsetPts[i].y*width +offsetPts[i].z*sliceSize);
		}
		return offsets;			
	}

	//*******************************************************************************************
	
	/** Get the absolute 1D offsets and center-to-center distances of voxels surrounding a central voxel, voxel units must be the same
	 * @param width image width in pixels
	 * @param height image height in pixels
	 * @param depth image width in pixels
	 * @param pixWidth pixel width in physical units
	 * @param pixHeight pixel height in physical units
	 * @param pixDepth pixel depth in physical units
	 * @return An PointDesc array of offsets and physical distances, face(0-5), face&#38;edge(0-17), face,edge,corners(0-25) 
	 */
	public static PointDesc[] get1dOffsets3D(int width, int height,int depth, double pixWidth, double pixHeight, double pixDepth)
	{
		PointDesc[] offsetPts = getIndexOffsets3D(pixWidth,pixHeight,pixDepth);
		int sliceSize = width*height;
		PointDesc[] offsets = new PointDesc[26];
		for(int i=0;i<26;i++)
		{
			offsets[i] = new PointDesc(offsetPts[i].x + offsetPts[i].y*width +offsetPts[i].z*sliceSize, offsetPts[i].val);
		}
		return offsets;			
	}

	//*******************************************************************************************
	
	/**Get the index offsets of pixels surrounding a 2D central pixel, eg offset(-1,0) is to the left, if the central pixel is (27,32) the one to the left is(26,32)
	 * @return a list of i,j 2D offsets, face(0-3), face&#38;edge(0-7)
	 */
	public static PointDesc[] getIndexOffsets2D()
	{
		PointDesc[] offsetPts = new PointDesc[8];

		// left = -1;		up = -1;		fwd = -1
		// right = +1		down = +1		back = +1
		//2D face neighbors
		offsetPts[0] = new PointDesc( 0, -1);//up
		offsetPts[1] = new PointDesc( 0,  1);//down
		offsetPts[2] = new PointDesc(-1,  0);//left
		offsetPts[3] = new PointDesc( 1,  0);//right
		//2d corner neighbors
		offsetPts[4] = new PointDesc(-1, -1);//left, up
		offsetPts[5] = new PointDesc( 1, -1);//right, up
		offsetPts[6] = new PointDesc(-1,  1);//left, down
		offsetPts[7] = new PointDesc( 1,  1);//right, down

		return offsetPts;

	}

	//*******************************************************************************************
	
	/**Get the index offsets and distance of pixels surrounding a central pixel, eg offset(-1,-1) is (left,up) , if the central pixel is (27,32) the one to the left,up is(26,31) and the distance is &radic; pixWidth<sup>2</sup> + pixHeight<sup>2</sup>, voxel units must be the same.
	 * @param pixWidth pixel width in physical units
	 * @param pixHeight pixel height in physical units
	 * @return a list of i,j 2D offsets and distances, face(0-3), face&#38;edge(0-7)
	 */
	public static PointDesc[] getIndexOffsets2D(double pixWidth, double pixHeight)
	{
		PointDesc[] offsetPts = new PointDesc[8];
		double whDiag = Math.sqrt(pixWidth*pixWidth + pixHeight*pixHeight);

		// left = -1;		up = -1;		fwd = -1
		// right = +1		down = +1		back = +1
		//2D face neighbors
		offsetPts[0] = new PointDesc( 0, -1, pixHeight);//up
		offsetPts[1] = new PointDesc( 0,  1, pixHeight);//down
		offsetPts[2] = new PointDesc(-1,  0, pixWidth);//left
		offsetPts[3] = new PointDesc( 1,  0, pixWidth);//right
		//2d corner neighbors
		offsetPts[4] = new PointDesc(-1, -1, whDiag);//left, up
		offsetPts[5] = new PointDesc( 1, -1, whDiag);//right, up
		offsetPts[6] = new PointDesc(-1,  1, whDiag);//left, down
		offsetPts[7] = new PointDesc( 1,  1, whDiag);//right, down

		return offsetPts;

	}

	//*******************************************************************************************
	
	/**Get the index offsets of voxels surrounding a central voxel, eg offset(-1,-1,-1) (left,up,forward), if the voxel pixel is (27,32,57) the one to the left,up,forward is(26,31,56)  
	 * @return  A PointDesc array of i,j,k offsets, face(0-5), face&#38;edge(0-17),face,edge,corners(0-25) 
	 */
	public static PointDesc[] getIndexOffsets3D()
	{
		PointDesc[] offsetPts = new PointDesc[26];

		// left = -1;		up = -1;		fwd = -1
		// right = +1		down = +1		back = +1
		//3D face neighbors
		offsetPts[0] = new PointDesc( 0, -1,  0);//up
		offsetPts[1] = new PointDesc( 0,  1,  0);//down
		offsetPts[2] = new PointDesc(-1,  0,  0);//left
		offsetPts[3] = new PointDesc( 1,  0,  0);//right
		offsetPts[4] = new PointDesc( 0,  0, -1);//fwd
		offsetPts[5] = new PointDesc( 0,  0,  1);//back

		// 3D edge touching neighbors
		offsetPts[6] = new PointDesc(-1, -1,  0);//left, up
		offsetPts[7] = new PointDesc( 1, -1,  0);//right, up
		offsetPts[8] = new PointDesc(-1,  1,  0);//left, down
		offsetPts[9] = new PointDesc( 1,  1,  0);//right, down
		offsetPts[10] = new PointDesc( 0, -1, -1);//0, up, fwd
		offsetPts[11] = new PointDesc( 0, -1,  1);//0, up, back
		offsetPts[12] = new PointDesc( 0,  1, -1);//0, down, fwd
		offsetPts[13] = new PointDesc( 0,  1,  1);//0, down, back
		offsetPts[14] = new PointDesc(-1,  0, -1);//left, 0, fwd
		offsetPts[15] = new PointDesc( 1,  0, -1);;//right, 0, fwd
		offsetPts[16] = new PointDesc(-1,  0,  1);//left, 0, back
		offsetPts[17] = new PointDesc( 1,  0,  1);//right, 0, back

		// 3D corner touching neighbors
		offsetPts[18] = new PointDesc(-1, -1, -1);//left up fwd
		offsetPts[19] = new PointDesc( 1, -1, -1);//right up fwd 
		offsetPts[20] = new PointDesc(-1,  1, -1);//left down fwd 
		offsetPts[21] = new PointDesc( 1,  1, -1);//right down fwd 
		offsetPts[22] = new PointDesc(-1, -1,  1);//left up back 
		offsetPts[23] = new PointDesc( 1, -1,  1);;//right up back 
		offsetPts[24] = new PointDesc(-1,  1,  1);//left down back 
		offsetPts[25] = new PointDesc( 1,  1,  1);//right down back

		return offsetPts;

	}

	//*******************************************************************************************
	
	/**Get the index offsets and distance of voxels surrounding a central voxel, eg offset(-1,-1,-1) (left,up,forward), if the voxel pixel is (27,32,57) the one to the left,up,forward is(26,31,56) and the distance is &radic; pixWidth<sup>2</sup> + pixHeight<sup>2</sup> + pixDepth<sup>2</sup> , voxel units must be the same.
	 * @param pixWidth pixel width in physical units
	 * @param pixHeight pixel height in physical units
	 * @param pixDepth pixel depth in physical units
	 * @return An PointDesc array of I,J,K offsets and physical distances from the central voxel, face(0-5), face&#38;edge(0-17) 3D face(0-9), face,edge,corners(0-25) 
	 */
	public static PointDesc[] getIndexOffsets3D(double pixWidth, double pixHeight, double pixDepth)
	{
		double whDiag = Math.sqrt(pixWidth*pixWidth + pixHeight*pixHeight);
		double wdDiag = Math.sqrt(pixWidth*pixWidth + pixDepth*pixDepth);
		double hdDiag = Math.sqrt(pixHeight*pixHeight + pixDepth*pixDepth);
		double whdDiag = Math.sqrt(pixWidth*pixWidth + pixHeight*pixHeight + pixDepth*pixDepth);
		PointDesc[] offsetPts = new PointDesc[26];

		// left = -1;		up = -1;		fwd = -1
		// right = +1		down = +1		back = +1
		//3D face neighbors
		offsetPts[0] = new PointDesc( 0, -1,  0, pixHeight);//up
		offsetPts[1] = new PointDesc( 0,  1,  0, pixHeight);//down
		offsetPts[2] = new PointDesc(-1,  0,  0, pixWidth);//left
		offsetPts[3] = new PointDesc( 1,  0,  0, pixWidth);//right
		offsetPts[4] = new PointDesc( 0,  0, -1, pixDepth);//fwd
		offsetPts[5] = new PointDesc( 0,  0,  1, pixDepth);//back

		// 3D edge neighbors
		offsetPts[6] = new PointDesc(-1, -1,  0, whDiag);//left, up
		offsetPts[7] = new PointDesc( 1, -1,  0, whDiag);//right, up
		offsetPts[8] = new PointDesc(-1,  1,  0, whDiag);//left, down
		offsetPts[9] = new PointDesc( 1,  1,  0, whDiag);//right, down
		offsetPts[10] = new PointDesc( 0, -1, -1, hdDiag);//0, up, fwd
		offsetPts[11] = new PointDesc( 0, -1,  1, hdDiag);//0, up, back
		offsetPts[12] = new PointDesc( 0,  1, -1, hdDiag);//0, down, fwd
		offsetPts[13] = new PointDesc( 0,  1,  1, hdDiag);//0, down, back
		offsetPts[14] = new PointDesc(-1,  0, -1, wdDiag);//left, 0, fwd
		offsetPts[15] = new PointDesc( 1,  0, -1, wdDiag);;//right, 0, fwd
		offsetPts[16] = new PointDesc(-1,  0,  1, wdDiag);//left, 0, back
		offsetPts[17] = new PointDesc( 1,  0,  1, wdDiag);//right, 0, back

		// 3D corner neighbors
		offsetPts[18] = new PointDesc(-1, -1, -1, whdDiag);//left up fwd
		offsetPts[19] = new PointDesc( 1, -1, -1, whdDiag);//right up fwd 
		offsetPts[20] = new PointDesc(-1,  1, -1, whdDiag);//left down fwd 
		offsetPts[21] = new PointDesc( 1,  1, -1, whdDiag);//right down fwd 
		offsetPts[22] = new PointDesc(-1, -1,  1, whdDiag);//left up back 
		offsetPts[23] = new PointDesc( 1, -1,  1, whdDiag);;//right up back 
		offsetPts[24] = new PointDesc(-1,  1,  1, whdDiag);//left down back 
		offsetPts[25] = new PointDesc( 1,  1,  1, whdDiag);//right down back

		return offsetPts;

	}
	
	//*******************************************************************************************

	protected Offsets() {}

	//	/**A slightly different implementation of initOffsets
	//	 * @param pixWidth pixel width in physical units
	//	 * @param pixHeight pixel height in physical units
	//	 * @param pixDepth pixel depth in physical units
	//	 * @return a list of i,j,k offsets and physical distances ,2D face(0-3) 2D face&edge(0-7) 3D face(0-9) 3D face&edge(0-17), 3D face,edge,corners(0-25) 
	//	 */
	//	protected static PointDesc[] getIndexOffsets(double pixWidth, double pixHeight, double pixDepth)
	//	{
	//		//get the offset indices
	//		PointDesc[] offsets3D = initOffsets();
	//		PointDesc[] offsetPts2 = new PointDesc[26];
	//		double x,y,z;
	//		
	//		for(int i=0;i<offsets3D.length;i++)
	//		{
	//			x = offsets3D[i].x*pixWidth; x*=x;
	//			y = offsets3D[i].y*pixHeight; y*=y;
	//			z = offsets3D[i].z*pixDepth; z*=z;
	//			offsetPts2[i] = new PointDesc(offsets3D[i].x,offsets3D[i].y,offsets3D[i].z, Math.sqrt(x+y+z));
	//		}
	//		return offsetPts2;			
	//	}


}
