/**
 * Script to import binary masks & create annotations, adding them to the current object hierarchy.
 */
import qupath.imagej.tools.IJTools
import qupath.lib.regions.ImagePlane
import qupath.lib.objects.PathObjects;

import ij.measure.Calibration
import ij.plugin.filter.ThresholdToSelection
import ij.process.ByteProcessor
import ij.process.ImageProcessor

import qupath.lib.objects.PathAnnotationObject
import qupath.lib.objects.classes.PathClassFactory

import qupath.lib.roi.*
import qupath.lib.objects.*

import java.awt.Graphics2D
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

// Only need to change these if your mask files aren't in the image directory or aren't name <imageName>_mask.png
// Need to use \\ instead of \ in Windows filepaths
String customMaskDir = 'D:\\featuredImages'
String customSuffix = '_mask.png'

// Get the main QuPath data structures
def server = getCurrentImageData().getServer()
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf("/") + 1;
int ind2 = path2.lastIndexOf(".") - 1;
name = path2[ind1..ind2]

// Create annotations for all the files
def annotations = []
String imgPath = server.getPath()
print(imgPath)
// If your file has a 5 character image extension, watch out

// Replaces everything after . with customSuffix
String maskFilename = name + customSuffix
print(name)

File fileMask = new File(maskFilename)
if(!fileMask.exists()) {
print(maskFilename + ' does not exist')
	maskFilename = customMaskDir + File.separator + maskFilename
	//maskFilename = maskFilename.replaceFirst('[\\.].*$',customSuffix)
	fileMask = new File(maskFilename)
}
print(maskFilename)
if(!fileMask.exists()) {
	print(maskFilename + ' does not exist')
	return
	}
else {
		print('Loading mask file ' + fileMask)
	}

annotations << parseAnnotation(fileMask)

// Add annotations to image
addObject(annotations[0])
//hierarchy.addPathObjects(annotations, false)

//selects all annotation areas.  Change this if you want only a subset of your areas split into contiguous area components.
def areaAnnotations = getAnnotationObjects().findAll {it.getROI() instanceof AreaROI}

areaAnnotations.each { selected ->
    def polygons = RoiTools.splitAreaToPolygons(selected.getROI())
    def newPolygons = polygons[1].collect {
        updated = it
        for (hole in polygons[0])
            updated = RoiTools.combineROIs(updated, hole, PathROIToolsAwt.CombineOp.SUBTRACT)
    return updated
    }

// Remove original annotation, add new ones
    annotations = newPolygons.collect {new PathAnnotationObject(it)}
    resetSelection()
    removeObject(selected, true)
    addObjects(annotations)

}


/**
 * Create a new annotation from a binary image, parsing the classification & region from the file name.
 *
 * Note: this code doesn't bother with error checking or handling potential issues with formatting/blank images.
 * If something is not quite right, it is quite likely to throw an exception.
 *
 * @param file File containing the PNG image mask.  The image name must be formatted as above.
 * @return The PathAnnotationObject created based on the mask & file name contents.
 */
def parseAnnotation(File file) {
    // Read the image
        BufferedImage src = ImageIO.read(file)
    BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), 10);
    Graphics2D g2d= img.createGraphics();
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();    


    
    // To create the ROI, travel into ImageJ
    def bp = new ByteProcessor(img)
    bp.setThreshold(1, Double.MAX_VALUE, ImageProcessor.NO_LUT_UPDATE)
    def roiIJ = new ThresholdToSelection().convert(bp)

    // Convert ImageJ ROI to a QuPath ROI
    // This assumes we have a single 2D image (no z-stack, time series)
    // Currently, we need to create an ImageJ Calibration object to store the origin
    // (this might be simplified in a later version)
    def cal = new Calibration()
    cal.xOrigin = 0.0
    cal.yOrigin = 0.0
    
    int z = 0
int t = 0
def plane = ImagePlane.getPlane(z, t)
    
    def roi = IJTools.convertToROI(roiIJ, cal, 1.toDouble(), plane)
    //def roi = convertToPolygonOrAreaROI(roiIJ, 0.toDouble(), 0.toDouble(), 1.toDouble(), -1, 0, 0)
    // Create & return the object
    print(roi)
    return PathObjects.createAnnotationObject(roi)
    //return new PathAnnotationObject(roi)
}