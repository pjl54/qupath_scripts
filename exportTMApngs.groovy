import javax.imageio.ImageIO
import qupath.lib.regions.RegionRequest

// Define resolution - 1.0 means full size
double downsample = 1.0

// Create output directory inside the project
//def dirOutput = buildFilePath(PROJECT_BASE_DIR, 'cores')
//def TMAname = 'TMA128_HE'
def TMAname = 'I be broke'
def dirOutput = 'D:\\JHU_TMA\\2018_4_spots\\' + TMAname
mkdirs(dirOutput)
// Write the cores
def server = getCurrentImageData().getServer()
def path = server.getPath()
getTMACoreList().parallelStream().forEach({core ->
    img = server.readBufferedImage(RegionRequest.createInstance(path, downsample, core.getROI()))
    ImageIO.write(img, 'PNG', new File(dirOutput, TMAname + '_' + core.getName() + '.png'))
})
print('Done!')