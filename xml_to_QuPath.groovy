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

import qupath.lib.objects.classes.*

String xmlDirectory = 'D:/test'
def server = getCurrentImageData().getServer()

String imgPath = server.getPath()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf("/") + 1;
int ind2 = path2.lastIndexOf(".") - 1;
name = path2[ind1..ind2]

path = path2[path2.indexOf('/')+1..path2.lastIndexOf("/")-1]


maskFilename = path + File.separator + name + '.xml'

File fileMask = new File(maskFilename)
if(!fileMask.exists()) {
print(maskFilename + ' does not exist')
	maskFilename = xmlDirectory + File.separator + maskFilename
	//maskFilename = maskFilename.replaceFirst('[\\.].*$',customSuffix)
	fileMask = new File(maskFilename)
}
if(!fileMask.exists()) {
	print(maskFilename + ' does not exist')
	return
	}
else {
		print('Loading mask file ' + fileMask)
	}

File xmlFile = fileMask

DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance()
DocumentBuilder dBuilder

dBuilder = dbFactory.newDocumentBuilder()
Document doc = dBuilder.parse(xmlFile)


doc.getDocumentElement().normalize();

NodeList Annotation = doc.getElementsByTagName('Annotations');
NodeList Annotations = Annotation.item(0).getElementsByTagName('Annotation');

for (A = 0; A < Annotations.getLength(); A++) {

Integer linecolor = Integer.parseInt(Annotations.item(A).getAttribute('LineColor'));
print(linecolor)
NodeList Regions = Annotations.item(A).getElementsByTagName('Regions');
NodeList Region = Regions.item(0).getElementsByTagName('Region');

// Loop through the annotations

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

//pathclass = PathClassFactory.getPathClass('ru',linecolor)

// decode linecolor

linecolor = linecolor
Integer[] gbr = [linecolor>>16,linecolor>>8&255,linecolor&255]
print(gbr)

pathClasses = getQuPath().getAvailablePathClasses()

pathclass = PathClassFactory.getPathClass(String.valueOf(linecolor),ColorTools.makeRGB(gbr[0],gbr[1],gbr[2]))


def pathObject = new PathAnnotationObject(roi, pathclass)
// Add object to hierarchy
addObject(pathObject)
}
}
// lock all annotations
getAnnotationObjects().each {it.setLocked(true)}