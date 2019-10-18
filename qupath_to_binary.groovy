import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO

// Get java.awt.Shape objects for each annotation
def shapes = getAnnotationObjects().collect({it.getROI().getShape()})

// Create a grayscale image
double downsample = 1.0

// define output directory and suffix
// Use \\ instead of \ in Windows filepaths
def pathOutput = 'D:\\featuredImages'
String customSuffix = '_mask.png'

def server = getCurrentImageData().getServer()
int w = (server.getWidth() / downsample) as int
int h = (server.getHeight() / downsample) as int

String path2 = server.getPath()
int ind1 = path2.lastIndexOf("/") + 1;
int ind2 = path2.lastIndexOf(".") - 1;
String name = path2[ind1..ind2]


String maskFilename = name + customSuffix

print(maskFilename)

def img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)

// Paint the shapes (this is just 'standard' Java - you might want to modify)
def g2d = img.createGraphics()
g2d.scale(1.0/downsample, 1.0/downsample)
g2d.setColor(Color.WHITE)
for (shape in shapes)
    g2d.fill(shape)
g2d.dispose()

// Save the result
def fileMask = new File(pathOutput, maskFilename)
ImageIO.write(img, 'PNG', fileMask)