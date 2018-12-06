import qupath.lib.objects.TMACoreObject
import qupath.lib.objects.hierarchy.DefaultTMAGrid

// Enter the number of horizontal & vertical cores here
int numHorizontal = 20
int numVertical = 20
// Enter the core diameter, in millimetres
double diameterMM = 0.7

// Convert diameter to pixels
double diameterPixels = (diameterMM * 1000) / getCurrentImageData().getServer().getAveragedPixelSizeMicrons()

// Get the current ROI
def roi = getSelectedROI()

// Create the cores
def cores = []
double xSpacing = roi.getBoundsWidth() / numHorizontal
double ySpacing = roi.getBoundsHeight() / numVertical
for (int i = 0; i < numVertical; i++) {
    for (int j = 0; j < numHorizontal; j++) {
        double x = roi.getBoundsX() + xSpacing / 2 + xSpacing * j
        double y = roi.getBoundsY() + ySpacing / 2 + ySpacing * i
        cores << new TMACoreObject(x, y, diameterPixels, false)
    }
}

// Create & set the grid
def tmaGrid = new DefaultTMAGrid(cores, numHorizontal)
getCurrentHierarchy().setTMAGrid(tmaGrid)