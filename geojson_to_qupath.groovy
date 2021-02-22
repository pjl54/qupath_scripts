String annotation_dir = 'D:/ProstateBounds/blad_nuc/geoson_bounds'
boolean use_annotation_dir = true
def server = getCurrentImageData().getServer()

String imgPath = server.getPath()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf('/') + 1
int ind2 = path2.lastIndexOf(".") - 1
name = path2[ind1..ind2]

path = path2[path2.indexOf(File.separator)+1..path2.lastIndexOf("/")-1]

if (use_annotation_dir) {
jsonFilename = annotation_dir + File.separator + name + '_stardist_details.json'
} else {
jsonFilename = path + File.separator + name + '.json'
}

print(use_annotation_dir)
print(jsonFilename)

def gson = GsonTools.getInstance(true)

def json = new File(jsonFilename).text
//println json


// Read the annotations
def type = new com.google.gson.reflect.TypeToken<List<qupath.lib.objects.PathObject>>() {}.getType()
def deserializedAnnotations = gson.fromJson(json, type)

// Set the annotations to have a different name (so we can identify them) & add to the current image
// deserializedAnnotations.eachWithIndex {annotation, i -> annotation.setName('New annotation ' + (i+1))}   # --- THIS WON"T WORK IN CURRENT VERSION
addObjects(deserializedAnnotations)   