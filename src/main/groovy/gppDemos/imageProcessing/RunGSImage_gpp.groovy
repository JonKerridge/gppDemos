package gppDemos.imageProcessing

import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.functionals.matrix.ImageEngine
import gppLibrary.functionals.matrix.Matrix
import gppLibrary.terminals.*
import gppDemos.imageProcessing.CompositeGSImage as img
import gppDemos.imageProcessing.CompositeGSResult as imgRslt

//usage runDemo imageProcessing RunGSImage resultsFile nodes

int nodes
String workingDirectory = System.getProperty('user.dir')
String inFileName = "DSC_0120-001.jpg"
String outFileName = "DSC_0120-001_GS_${nodes}_K4_K4_K1.jpg"
String inFile
String outFile
if (args.size() == 0){
    nodes = 4
    inFile = "./$inFileName"
    outFile = "./$outFileName"
}
else {
    nodes = Integer.parseInt(args[1])
    String folder = args[0]
    inFile = workingDirectory + "/src/main/groovy/gppDemos/${folder}/$inFileName"
    outFile = workingDirectory + "/src/main/groovy/gppDemos/${folder}/$outFileName"
}

//edge
Matrix kernel1 = new Matrix(rows: 3, columns: 3)
kernel1.entries = new int[3][3]
kernel1.setByRow([-1, -1, -1], 0)
kernel1.setByRow([-1,  8, -1], 1)
kernel1.setByRow([-1, -1, -1], 2)
//edge
Matrix kernel2 = new Matrix(rows: 3, columns: 3)
kernel2.entries = new int[3][3]
kernel2.setByRow([ 0, -1,  0], 0)
kernel2.setByRow([-1,  4, -1], 1)
kernel2.setByRow([ 0, -1,  0], 2)
//edge
Matrix kernel3 = new Matrix(rows: 5, columns: 5)
kernel3.entries = new int[5][5]
kernel3.setByRow([ -1, -1, -1,  -1, -1], 0)
kernel3.setByRow([ -1, -1, -1,  -1, -1], 1)
kernel3.setByRow([ -1, -1, 24,  -1, -1], 2)
kernel3.setByRow([ -1, -1, -1,  -1, -1], 3)
kernel3.setByRow([ -1, -1, -1,  -1, -1], 4)
//blur
Matrix kernel4 = new Matrix(rows: 5, columns: 5)
kernel4.entries = new int[5][5]
kernel4.setByRow([ 0, 0,  1,  0, 0], 0)
kernel4.setByRow([ 0, 1,  1,  1, 0], 1)
kernel4.setByRow([ 1, 1,  1,  1, 1], 2)
kernel4.setByRow([ 0, 1,  1,  1, 0], 3)
kernel4.setByRow([ 0, 0,  1,  0, 0], 4)
//box blur
Matrix kernel5 = new Matrix(rows: 3, columns: 3)
kernel5.entries = new int[3][3]
kernel5.setByRow([1, 1, 1], 0)
kernel5.setByRow([1, 1, 1], 1)
kernel5.setByRow([1, 1, 1], 2)


def emitDetails = new DataDetails( dName: img.getName(),
                                   dInitMethod: img.initMethod,
                                   dCreateMethod: img.createMethod,
                                   dCreateData: [inFile, outFile])

def resultDetails = new ResultDetails( rName: imgRslt.getName(),
                                       rInitMethod: imgRslt.initMethod,
                                       rCollectMethod: imgRslt.collectMethod,
                                       rFinaliseMethod: imgRslt.finaliseMethod )

System.gc()
print "GS Image, $nodes, "
long startTime = System.currentTimeMillis()

def emit = new Emit(eDetails: emitDetails)
// greyscale
def engine1 = new ImageEngine( nodes : nodes,
                               partitionMethod: img.partitionMethod,
                               functionMethod: img.greyScaleMethod )
// blur
def engine2 = new ImageEngine( nodes: nodes,
                               convolutionMethod: img.convolutionMethod,
                               convolutionData: [kernel4, 13, 0],
                               updateImageIndexMethod: img.updateImageIndex )
//blur
def engine3 = new ImageEngine( nodes: nodes,
                               convolutionMethod: img.convolutionMethod,
                               convolutionData: [kernel4, 13, 0],
                               updateImageIndexMethod: img.updateImageIndex )
//edge detect
def engine4 = new ImageEngine( nodes: nodes,
                               convolutionMethod: img.convolutionMethod,
                               convolutionData: [kernel1, 1, 0],
                               updateImageIndexMethod: img.updateImageIndex )

def collector = new Collect( rDetails: resultDetails)



long endTime = System.currentTimeMillis()
println " ${endTime - startTime}"


