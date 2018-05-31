package gppDemos.imageProcessing

import jcsp.lang.*
import groovyJCSP.*
 
import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.functionals.matrix.ImageEngine
import gppLibrary.functionals.matrix.Matrix
import gppLibrary.terminals.*
import gppDemos.imageProcessing.CompositeARGBImage as img
import gppDemos.imageProcessing.CompositeARGBResult as imgRslt
 

int nodes = 1
if (args.size() == 0){
nodes = 4
}
else {
nodes = Integer.parseInt(args[0])
}
 
String inFile = "src\\demos\\imageProcessing\\DSC_0120-001.jpg"
String outFile = "src\\demos\\imageProcessing\\DSC_0120-001_RGB_${nodes}_K6_K6.jpg"
 
Matrix kernel1 = new Matrix(rows: 3, columns: 3)
kernel1.entries = new int[3][3]
kernel1.setByRow([-1, -1, -1], 0)
kernel1.setByRow([-1,  8, -1], 1)
kernel1.setByRow([-1, -1, -1], 2)
int factor1 = 1
 
Matrix kernel2 = new Matrix(rows: 3, columns: 3)
kernel2.entries = new int[3][3]
kernel2.setByRow([ 0, -1,  0], 0)
kernel2.setByRow([-1,  4, -1], 1)
kernel2.setByRow([ 0, -1,  0], 2)
int factor2 = 1
 
Matrix kernel3 = new Matrix(rows: 5, columns: 5)
kernel3.entries = new int[5][5]
kernel3.setByRow([ 0, 0, -1,  0, 0], 0)
kernel3.setByRow([ 0, 0, -1,  0, 0], 1)
kernel3.setByRow([ 0, 0,  4,  0, 0], 2)
kernel3.setByRow([ 0, 0, -1,  0, 0], 3)
kernel3.setByRow([ 0, 0, -1,  0, 0], 4)
int factor3 = 1
 
Matrix kernel4 = new Matrix(rows: 5, columns: 5)
kernel4.entries = new int[5][5]
kernel4.setByRow([ -1, 0,  0,  0, 0], 0)
kernel4.setByRow([ 0, -2,  0,  0, 0], 1)
kernel4.setByRow([ 0,  0,  6,  0, 0], 2)
kernel4.setByRow([ 0,  0,  0, -2, 0], 3)
kernel4.setByRow([ 0,  0,  0,  0, -1], 4)
int factor4 = 1
 
// sharpen
Matrix kernel5 = new Matrix(rows: 3, columns: 3)
kernel5.entries = new int[3][3]
kernel5.setByRow([-1, -1, -1], 0)
kernel5.setByRow([-1,  9, -1], 1)
kernel5.setByRow([-1, -1, -1], 2)
int factor5 = 1
 
// blur
Matrix kernel6 = new Matrix(rows: 3, columns: 3)
kernel6.entries = new int[3][3]
kernel6.setByRow([1, 1, 1], 0)
kernel6.setByRow([1, 1, 1], 1)
kernel6.setByRow([1, 1, 1], 2)
int factor6 = 9
 
def emitDetails = new DataDetails( dName: img.getName(),
dInitMethod: img.initMethod,
dCreateMethod: img.createMethod,
dCreateData: [inFile, outFile])
 
def resultDetails = new ResultDetails( rName: imgRslt.getName(),
rInitMethod: imgRslt.initMethod,
rCollectMethod: imgRslt.collectMethod,
rFinaliseMethod: imgRslt.finaliseMethod )
 
 
System.gc()
print "Image Engine $nodes ->"
long startTime = System.currentTimeMillis()
 

//NETWORK

def chan1 = Channel.one2one()
def chan2 = Channel.one2one()
def chan3 = Channel.one2one()

def emit = new Emit(
    // input channel not required
    output: chan1.out(),
    eDetails: emitDetails)
 
def engine1 = new ImageEngine(
    input: chan1.in(),
    output: chan2.out(),
    nodes: nodes,
    partitionMethod: img.partitionMethod,
    convolutionMethod: img.convolutionRGBMethod,
    convolutionData: [kernel6, factor6, 0],
    updateImageIndexMethod: img.updateImageIndex )
 
def engine2 = new ImageEngine(
    input: chan2.in(),
    output: chan3.out(),
    nodes: nodes,
    partitionMethod: img.partitionMethod,
    convolutionMethod: img.convolutionRGBMethod,
    convolutionData: [kernel6, factor6, 0],
    updateImageIndexMethod: img.updateImageIndex )
 
def collector = new Collect(
    input: chan3.in(),
    // no output channel required
    rDetails: resultDetails)

PAR network = new PAR()
 network = new PAR([emit , engine1 , engine2 , collector ])
 network.run()
 network.removeAllProcesses()
//END

 
long endTime = System.currentTimeMillis()
println " ${endTime - startTime}"
 
 