// NOTE: This code does not import holes in annotations, and splits non-contiguous annoations

import com.google.gson.Gson
import qupath.lib.geom.Point2
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.roi.PolygonROI
import qupath.lib.roi.RectangleROI
import qupath.lib.roi.LineROI
import qupath.lib.roi.EllipseROI
import qupath.lib.roi.PolylineROI
import qupath.lib.roi.PointsROI

import java.io.File;

import qupath.lib.scripting.QP

def hierarchy = QP.getCurrentHierarchy()


String annotation_dir = 'D:/test'
boolean use_annotation_dir = true
def server = getCurrentImageData().getServer()

String imgPath = server.getPath()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf('/') + 1
int ind2 = path2.lastIndexOf(".") - 1
name = path2[ind1..ind2]

path = path2[path2.indexOf(File.separator)+1..path2.lastIndexOf("/")-1]

if (use_annotation_dir) {
jsonFilename = annotation_dir + File.separator + name + '.json'
} else {
jsonFilename = path + File.separator + name + '.json'
}

print(use_annotation_dir)
print(jsonFilename)

File file = new File(jsonFilename)
def fileContent = file.text


// Read into a map
def map = new Gson().fromJson(fileContent, Map[])
// Extract tumor & annotations
// Convert to QuPath annotations
for (anno in map) {
    name = 'null'
    print(anno)
    if (anno['properties']['classification'] != null) {
        name = anno['properties']['classification']['name']   
    }
    if (anno['geometry']['type'] == 'Polygon') {
        vertices = anno['geometry']['coordinates']
    }
    else if (anno['geometry']['type'] == 'MultiPolygon') {
            vertices = anno['geometry']['coordinates']
    }
    else {
        vertices = [anno['geometry']['coordinates']]
    }
    
    for (vert_set in vertices) {
    if (anno['geometry']['type'] == 'MultiPolygon') {
         vert_set = vert_set[0]
    }
    def points = vert_set.collect {new Point2(it[0], it[1])}
    def ann_object = new PolygonROI(points)

// This block still always produces a polygon object, I don't know why
//    switch (anno['geometry']['type']) {
//     case 'Polygon':
//        ann_object = new PolygonROI(points)
//        break
//     case 'Rectangle':
//        ann_object = new RectangleROI(points)
//        break
//     case 'Line':
//        ann_object = new LineROI(points)
//        break
//     case 'Ellipse':
//        ann_object = new EllipseROI(points)
//        break
//     case 'Points':
//        ann_object = new PointsROI(points)
//        break
//        }
    path_class = PathClassFactory.getPathClass(name)
    def pathAnnotation = new PathAnnotationObject(ann_object,path_class)
//    hierarchy.addPathObject(pathAnnotation)
    addObject(pathAnnotation)
}
}

//fireHierarchyUpdate()

// Add to current hierarchy
