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


String xmlDirectory = 'D:/ccipd_data/UH Bladder Cancer Project/Blad 170830'
def server = getCurrentImageData().getServer()
String path = xmlDirectory + '/' + server.getShortServerName() + '.xml'
String result = path.replaceAll( "/","\\\\");
print(path)
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

def roi = new PolygonROI(coordinatesX,coordinatesY,-1,0,0)

def pathObject = new PathAnnotationObject(roi)
// Add object to hierarchy
addObject(pathObject)
}

// lock all annotations
getAnnotationObjects().each {it.setLocked(true)}