# DistanceMapLib
<p><strong>This project started life as C routines for characterizing random pore network geometry and measuring things like concentration as a function of distance. The port to java was pretty straightforward. An interesting add-on is an MICP simulator that works by first flood-filling a Euclidean distance map of resolved pores and then continues the flood by using voxel gray level as a proxy for porosity. This page gives a basic description of the methods in the DistanceMap library.&nbsp; All methods support asymmetric pixels/voxel sizes and physical dimensions.</strong></p>
<!--See the <a href="https://lazzyizzi.github.io/#DistanceMapLib" target="_blank">DistanceMapLib</a> Web Page for detailed information.-->
  
## ExactEuclideanDistanceMap, 2D and 3D

In a segmented image, ExactEuclideanDistanceMap calculates the shortest distance between each pixel in component &quot;A&quot; to its nearest connected neighbor in component &quot;B&quot; using <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.322.7605&rep=rep1&type=pdf" target="_blank">Danielsson's algorithm</a>. In this implementation the input image must have one component set to zero. The EDM is calculated in either the zero or non-zero component as selected by the user.&nbsp; To improve performance, I have done a few optimizations on Danielsson&rsquo;s original 2D algorithm and extended those to 3D.

**Segmented Pixels**
<br>
![Image of Segmented Pixels](https://github.com/LazzyIzzi/LazzyIzzi.github.io/blob/main/DistanceMapImages/DistanceMapLib/SegCells.png)
<br>
<br>
**2D Euclidean Distance Map, each pixel value replaced by its distance to the nearest surface**
<br>
![Image EDM of Segmented Pixels](https://github.com/LazzyIzzi/LazzyIzzi.github.io/blob/main/DistanceMapImages/DistanceMapLib/SegCellsEDM.png)
<br>
## EuclideanSpheres</strong>, 3D only

Voxels within a Euclidean sphere of radius R are defined on a Cartesian grid as &radic;(x<sup>2</sup>+y<sup>2</sup>+z<sup>2</sup>) &lt; R.&nbsp; Euclidean spheres can be used to restore a 3D shape from its medial surface (thinning the medial surface to a medial axis is a lossy process and is not reversible by sphere drawing). Euclidean spheres are also useful in constructing random porous media.

## GeodesicTransformer</strong>, 2D and 3D

In a segmented image, this GDT calculates the shortest distance between seed pixel locations in component &ldquo;A&rdquo; and all other connected component &ldquo;A&rdquo; pixels in the image using a &ldquo;brushfire&rdquo; algorithm.&nbsp; This propagates the local shortest distance in a pixel&rsquo;s touching neighbors. As such, it is limited to the directions a shortest path can take, e.g. in 2D only left, right, forward, back and 45&deg; turns are allowed. This results in a distance over-estimate of up to 8% in non-cardinal directions that can be problematic when computing distances from point seeds over long distances.&nbsp; This GDT supports seeds at single and multiple points, 2D image edges and 3D volume surfaces. Distance errors are usually small when propagating from edges or surfaces.

**GDT of the 2D image with a point seed at far right of colored region.<br>
The points labeled -1 are not connected.**
<br>
![Image GDT of Segmented Pixels](https://github.com/LazzyIzzi/LazzyIzzi.github.io/blob/main/DistanceMapImages/DistanceMapLib/SegCellsGDT.png)

<h2><strong>Geodesic Path</strong>, 2D and 3D</h2>

This method finds the shortest distance between a selected point or points in a geodesic image and the nearest seed point or surface.

**2D Shortest Path example.<br>
Seed is BLUE Destination is GREEN**
<br>
![Image shortest path in GDT](https://github.com/LazzyIzzi/LazzyIzzi.github.io/blob/main/DistanceMapImages/DistanceMapLib/ShortestPath.png)

## HybridFloodFill, 3D only

It is useful to think of hybrid flooding as a simplified method for simulating non-wetting invasion of a random porous medium. It is analogous to mercury injection capillary pressure (<a href="https://perminc.com/resources/fundamentals-of-fluid-flow-in-porous-media/chapter-2-the-porous-medium/multi-phase-saturated-rock-properties/laboratory-measurement-capillary-pressure/mercury-injection/" target="_blank">MICP</a>) measurements of porous materials. In MICP a porous sample that is not wet by mercury is placed in a mercury filled pressure vessel. Initially, due to the high interfacial surface tension, the mercury does not invade the porous sample.&nbsp; When sufficient external pressure is applied, the mercury surface begins to bulge into the sample&rsquo;s largest exposed pores. As more pressure is applied to overcome the surface tension, the mercury bulges into ever smaller pores. MICP records the volume of mercury injected at each pressure.&nbsp; The derivative of the resulting P-V curve is sometimes interpreted as a pore size distribution.</p>

3D images usually do not have sufficient definition (size and resolution) to capture the range of pore and throat sizes in many random porous materials. To get around this limitation, the hybrid flood fill method must be applied to a pre-processed image where the porosity has been broken into two parts, resolved (all voxels are 1) and unresolved porosity (voxel values are less than 1 and greater than zero). <ins>It is important to use a system of length units where the voxel width, height, and depth are all greater than 1. If the voxel dimensions are less than 1 calculated resolved pore diameters may overlap values fo unresolved pores.</ins>

HybridFloodFill takes the pre-processed image and a flood cutoff length as arguments and simulates a single pressure measurement in MICP by flooding the image from the top slice toward the back slice. First, the resolved pore space is converted to length measurements using an EDM. This is followed by a conventional gray scale flood up to the cutoff value. Cutoff values &gt;1 flood only resolved pores connected to the top slice. The curved flood surface is reconstructed by drawing Euclidean spheres of the current flood radius at each point along the flood surface. The flooded voxels are counted and the flooded volume and the flooded image are returned in the report.

Cutoff values &lt; 1 flood all connected resolved pores and unresolved pores up to the cutoff value. In unresolved pores, the porosity is used as a proxy for length*.  The resolved (fully flooded), unresolved (partially flooded) voxel volumes, are returned in the report. If the flood has reached the back slice (breakthrough) an estimated tortuosity is also reported.

**Shape of invading fluid at breakthrough during simulated MICP in a random porous medium.<br>
The fluid is injected from the bottom.<br>
The medium is rendered transparent to reveal the invading fluid.**
<br>
![Image Flooded Path At Breakthrough](https://github.com/LazzyIzzi/LazzyIzzi.github.io/blob/main/DistanceMapImages/DistanceMapLib/SyntheticRockAtBreakthroughGray.png)

* The hybrid floodfill code was originally written to test this hypothesis. Initial results were encouraging with behavior consistent within mineral types. Other uses have included the study of [mixed porosity carbonates](https://www.researchgate.net/publication/338394414_Nuclear_magnetic_resonance_and_x-ray_microtomography_pore-scale_analysis_of_oil_recovery_in_mixed-porosity_carbonates)
