import java.io.File;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import qupath.lib.roi.*
import qupath.lib.images.*
import qupath.lib.images.servers.ServerTools

// need to use "/" instead of "\" for windows paths, slashes are corrected later in this script
String saveDirectory = 'F:' 

DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance()
DocumentBuilder dBuilder

dBuilder = dbFactory.newDocumentBuilder()
Document doc = dBuilder.newDocument()

Element rootElement = doc.createElement('Annotations')
rootElement.setAttribute('MicronsPerPixel','0.25')
doc.appendChild(rootElement)

int annotationIdTracker = 1;
int regionIdTracker = 1;

// Loop through the annotations
for (drawnAnnotation in getAnnotationObjects()) {
annoClass = drawnAnnotation.getPathClass();

Element annotation = doc.createElement('Annotation')
annotation.setAttribute('Id',String.valueOf(annotationIdTracker))
annotationIdTracker++

if(annoClass != null) {
linecolor = annoClass.getColor()
}
else {
linecolor = 16711680
}

int r = (linecolor>>16)&0xFF;
int g = (linecolor>>8)&0xFF;
int b = (linecolor>>0)&0xFF;

convertedColor = ColorTools.makeRGB(r,g,b)
convertedColor = convertedColor - (255<<24)

annotation.setAttribute('LineColor',String.valueOf(convertedColor))

annotation.setAttribute('Name',annoClass.toString())

rootElement.appendChild(annotation)

Element regions = doc.createElement('Regions')
annotation.appendChild(regions)
regions.appendChild(doc.createElement('RegionAttributeHeaders')) // Matlab xml reading code skips first region

Element region = doc.createElement('Region')
region.setAttribute('Id',String.valueOf(regionIdTracker))
regionIdTracker++
region.setAttribute('NegativeROA','0')
regions.appendChild(region)

Element Vertices = doc.createElement('Vertices')
region.appendChild(Vertices)


// Get the QuPath ROI (rectangle, polygon, area, ellipse, line...)
def roi = drawnAnnotation.getROI()
// Create a java.awt.Shape from the ROI
//def shape = PathROIToolsAwt.getShape(roi)
def shape = roi.getShape()
// Get an iterator to access the vertices
def iterator = shape.getPathIterator(null, 0.5)

while (!iterator.isDone()) {

Element Vertex = doc.createElement('Vertex')

double[] coordinates = new double[2];

iterator.currentSegment(coordinates)

// the last coordinate is (0.0,0.0)
if(coordinates[0] > 0 && coordinates[1] > 0) {
Vertex.setAttribute('X',String.valueOf(coordinates[0]))
Vertex.setAttribute('Y',String.valueOf(coordinates[1]))
Vertices.appendChild(Vertex)
}

iterator.next()
}
}

def server = getCurrentImageData().getServer()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf("/") + 1;
int ind2 = path2.lastIndexOf(".") - 1;
name = path2[ind1..ind2]

String path = saveDirectory + '/' + name + '.xml'
String result = path.replaceAll( "/","\\\\");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
 
            StreamResult file = new StreamResult(new File(path));
 
            //write data
            transformer.transform(source, file);
            print("DONE");