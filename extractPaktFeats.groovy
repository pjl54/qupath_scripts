
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qupath.lib.algorithms.color.EstimateStainVectors;
import qupath.lib.color.ColorDeconvolutionHelper;
import qupath.lib.color.ColorDeconvolutionStains;
import qupath.lib.color.StainVector;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.ColorToolsFX;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.helpers.DisplayHelpers.DialogButton;
import qupath.lib.gui.helpers.dialogs.ParameterPanelFX;
import qupath.lib.gui.plots.ScatterPlot;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.plugins.parameters.ParameterList;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.RectangleROI;
import qupath.lib.roi.interfaces.ROI;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import qupath.lib.common.ColorTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import qupath.lib.roi.PathROIToolsAwt
import qupath.lib.objects.*
import qupath.lib.roi.*


String xmlDirectory = 'F:/Gupta_Zeiss'
def server = getCurrentImageData().getServer()

String imgPath = server.getPath()

// If your file has a 5 character image extension, watch out
String path = imgPath[6..server.getPath().length()-5] + '.xml'
//String path = xmlDirectory + '/' + server.getShortServerName() + '.xml'
//String result = path.replaceAll( "/","\\\\");
File xmlFile = new File(path)

DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance()
DocumentBuilder dBuilder

dBuilder = dbFactory.newDocumentBuilder()
Document doc = dBuilder.parse(xmlFile)


doc.getDocumentElement().normalize();

NodeList Annotation = doc.getElementsByTagName('Annotations');
NodeList Annotations = Annotation.item(0).getElementsByTagName('Annotation');
NodeList Regions = Annotations.item(0)getElementsByTagName('Regions');
NodeList Region = Regions.item(0)getElementsByTagName('Region');

// Loop through the annotations
//print(Region.getLength())
for (R = 0; R < Region.getLength(); R++) {
NodeList Vertices = Region.item(R).getElementsByTagName('Vertices')
NodeList Vertex = Vertices.item(0).getElementsByTagName('Vertex')

def coordinatesX = new float[Vertex.getLength()]
def coordinatesY = new float[Vertex.getLength()]
for (V = 0; V < Vertex.getLength(); V++) {
coordinatesX[V] = Float.parseFloat(Vertex.item(V).getAttribute('X'))
coordinatesY[V] = Float.parseFloat(Vertex.item(V).getAttribute('Y'))
}

def roi = new PolygonROI(coordinatesX,coordinatesY,-1,0,0)

def pathObject = new PathAnnotationObject(roi)
// Add object to hierarchy
addObject(pathObject)
}

// lock all annotations
getAnnotationObjects().each {it.setLocked(true)}

setImageType('BRIGHTFIELD_H_DAB');
resetSelection()
selectAnnotations();
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def selectedObject = hierarchy.getSelectionModel().getSelectedObjects()
selectedObject = selectedObject[0]
//print(selectedObject[0])
//def selectedObject = getAnnotationObjects()
//def roi = selectedObject?.isAnnotation() ? selectedObject.getROI() : null
//def roi = selectedObject
def selectedROI = selectedObject.getROI()
def region = selectedROI
//def region = selectedROI == null ?
//       RegionRequest.createInstance(server.getPath(),1, 0, 0, server.getWidth(), server.getHeight()) 
//       RegionRequest.createInstance(server.getPath(),1, selectedROI)              

//RegionRequest request = RegionRequest.createInstance(imageData.getServerPath(), 1, region);
//		PathObject pathObject = imageData.getHierarchy().getSelectionModel().getSelectedObject();
def pathObject = getSelectedObjects()
pathObject = pathObject[0]
def roi = pathObject.getROI()

print(pathObject)
int MAX_PIXELS = 4000*4000;

//		ROI roi = pathObject == null ? null : pathObject.getROI();
		double downsample = Math.max(1, Math.sqrt((roi.getBoundsWidth() * roi.getBoundsHeight()) / MAX_PIXELS));
		RegionRequest request = RegionRequest.createInstance(imageData.getServerPath(), downsample, roi);

BufferedImage img = imageData.getServer().readBufferedImage(request);
print(request)
print(img)
print(imageData.getColorDeconvolutionStains())
//print(EstimateStainVectors.estimateStains(img,imageData.getColorDeconvolutionStains(),true))
//EstimateStainVectors.estimateStains(img,imageData.getColorDeconvolutionStains(),true)
imageData.setColorDeconvolutionStains(EstimateStainVectors.estimateStains(img,imageData.getColorDeconvolutionStains(),true));
print(imageData.getColorDeconvolutionStains())
//setColorDeconvolutionStains('{"Name" : "nfkb", "Stain 1" : "Hematoxylin", "Values 1" : "0.64231 0.60862 0.46585 ", "Stain 2" : "DAB", "Values 2" : "0.46907 0.58152 0.66468 ", "Background" : " 255 255 255 "}');



runPlugin('qupath.imagej.detect.cells.PositiveCellDetection', '{"detectionImageBrightfield": "Hematoxylin OD",  "requestedPixelSizeMicrons": 0.0,  "backgroundRadiusMicrons": 8.0,  "medianRadiusMicrons": 0.0,  "sigmaMicrons": 1.5,  "minAreaMicrons": 10.0,  "maxAreaMicrons": 100.0,  "threshold": 0.3,  "maxBackground": 2.0,  "watershedPostProcess": true,  "excludeDAB": false,  "cellExpansionMicrons": 5.0,  "includeNuclei": true,  "smoothBoundaries": true,  "makeMeasurements": true,  "thresholdCompartment": "Cytoplasm: DAB OD mean",  "thresholdPositive1": 0.3,  "thresholdPositive2": 0.4,  "thresholdPositive3": 0.5,  "singleThreshold": false}');
String name = server.getShortServerName()
//saveAnnotationMeasurements('D:/guptaIC/nfkbFeats/' + name + '_nfkbfeats.txt')