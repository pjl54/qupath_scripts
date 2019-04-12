/**
 * Script to import binary masks & create annotations, adding them to the current object hierarchy.
 *
 * It is assumed that each mask is stored in a PNG file in a project subdirectory called 'masks'.
 * Each file name should be of the form:
 *   [Short original image name]_[Classification name]_([downsample],[x],[y],[width],[height])-mask.png
 *
 * Note: It's assumed that the classification is a simple name without underscores, i.e. not a 'derived' classification
 * (so 'Tumor' is ok, but 'Tumor: Positive' is not)
 *
 * The x, y, width & height values should be in terms of coordinates for the full-resolution image.
 *
 * By default, the image name stored in the mask filename has to match that of the current image - but this check can be turned off.
 *
 * @author Pete Bankhead
 */


import ij.measure.Calibration
import ij.plugin.filter.ThresholdToSelection
import ij.process.ByteProcessor
import ij.process.ImageProcessor

import qupath.imagej.objects.ROIConverterIJ
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.objects.classes.PathClassFactory

import qupath.lib.roi.*
import qupath.lib.objects.*

import java.awt.Graphics2D
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

// Get the main QuPath data structures
def server = getCurrentImageData().getServer()
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()

String name = server.getShortServerName()

// Create annotations for all the files
def annotations = []
String imgPath = server.getPath()

// If your file has a 5 character image extension, watch out
String result = imgPath[6..server.getPath().length()-5] + '_mask.png'

File fileMask = new File(result)

print(fileMask)
print(result)
annotations << parseAnnotation(fileMask)

// Add annotations to image
hierarchy.addPathObjects(annotations, false)

//selects all annotation areas.  Change this if you want only a subset of your areas split into contiguous area components.
def areaAnnotations = getAnnotationObjects().findAll {it.getROI() instanceof AreaROI}

areaAnnotations.each { selected ->
    def polygons = PathROIToolsAwt.splitAreaToPolygons(selected.getROI())
    def newPolygons = polygons[1].collect {
        updated = it
        for (hole in polygons[0])
            updated = PathROIToolsAwt.combineROIs(updated, hole, PathROIToolsAwt.CombineOp.SUBTRACT)
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
        print(file)
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
    cal.xOrigin = 0
    cal.yOrigin = 0
    def roi = ROIConverterIJ.convertToPathROI(roiIJ, cal, 1, -1, 0, 0)

    // Create & return the object
    return new PathAnnotationObject(roi, null)
}