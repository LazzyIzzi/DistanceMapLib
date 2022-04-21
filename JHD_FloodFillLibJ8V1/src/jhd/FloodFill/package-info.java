/**<p>Contains methods for computing distance transforms in 2D and 3D images. Anisotropic image pixel/voxel dimensions are supported.</p>
<ol>
	<li>Gray scale flooding.
	<li>Computing the exact 2D and 3D Euclidean distance map (EDM) using Danielsson's algorithm.
	<br><small><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Danielsson P.E. Euclidean distance mapping. Computer Graphics Image Processing, 14, 1980, pp. 227-248.</i></small></li>
	<li>Drawing Euclidean spheres.</li> 
	<li>Computing the 2D and 3D geodesic distance transform (GDT) by serial propagation.</li>
	<li>Finding the shortest path to a source point in a GDT mapped image
	<li>Simulation of non-wetting invasion of random porous media using EDM and sphere drawing for resolved porosity, gray level flood for unresolved porosity and GDT for tortuosity of flooded path.</li>
</ol>
<p>All calculations are done in-place, on the supplied image data.</p>
*/
package jhd.FloodFill;