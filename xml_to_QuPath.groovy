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

//import qupath.lib.roi.PathROIToolsAwt
import qupath.lib.objects.*
import qupath.lib.roi.*
import qupath.lib.regions.*

String xmlDirectory = 'D:/ccipd_data/MtSinai'
def server = getCurrentImageData().getServer()

String imgPath = server.getPath()

print('qupath.lib.images.servers.openslide.OpenslideImageServer: file:/'.length())
// If your file has a 5 character image extension, watch out
String path = imgPath[64..server.getPath().length()-6] + '.xml'
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
print(Region.getLength())
for (R = 0; R < Region.getLength(); R++) {
NodeList Vertices = Region.item(R).getElementsByTagName('Vertices')
NodeList Vertex = Vertices.item(0).getElementsByTagName('Vertex')

def coordinatesX = new float[Vertex.getLength()]
def coordinatesY = new float[Vertex.getLength()]
for (V = 0; V < Vertex.getLength(); V++) {
coordinatesX[V] = Float.parseFloat(Vertex.item(V).getAttribute('X'))
coordinatesY[V] = Float.parseFloat(Vertex.item(V).getAttribute('Y'))
}

//def roi = new PolygonROI(coordinatesX,coordinatesY,-1,0,0)
def roi = new PolygonROI(coordinatesX,coordinatesY,ImagePlane.getDefaultPlane())

def pathObject = new PathAnnotationObject(roi)
// Add object to hierarchy
addObject(pathObject)
}

// lock all annotations
getAnnotationObjects().each {it.setLocked(true)}