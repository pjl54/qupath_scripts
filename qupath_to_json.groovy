import java.io.File;

String saveDirectory = 'D:/test' 


def annotations = getAnnotationObjects()
boolean prettyPrint = true
def gson = GsonTools.getInstance(prettyPrint)
gson.toJson(annotations)

def server = getCurrentImageData().getServer()

String path2 = server.getPath()
int ind1 = path2.lastIndexOf("/") + 1;
int ind2 = path2.lastIndexOf(".") - 1;
name = path2[ind1..ind2]

String path = saveDirectory + '/' + name + '.json'
//String result = path.replaceAll( "/","\\\\");

new File(path).write(gson.toJson(annotations))