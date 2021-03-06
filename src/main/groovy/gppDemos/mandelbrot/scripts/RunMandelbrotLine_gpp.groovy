package gppDemos.mandelbrot.scripts

import gppLibrary.connectors.reducers.AnyFanOne
import gppLibrary.connectors.spreaders.OneFanAny
import gppLibrary.functionals.groups.AnyGroupAny
import gppLibrary.terminals.CollectUI
import gppLibrary.terminals.Emit
import gppLibrary.*

import java.awt.*

import gppDemos.mandelbrot.data.MandelbrotLine as ml
import gppDemos.mandelbrot.data.MandelbrotLineResult as mlr

//usage runDemo mandelbrot\scripts RunMadelbrotLine resultsFile workers iterations width height delta

int workers
int maxIterations
int width                  //1400   700        350
int height                 //800    400        200
double pixelDelta         //0.0025 0.005      0.01


if (args.size() == 0){
    workers = 4
    maxIterations = 100
    width = 700
    height = 400
    pixelDelta = 0.005
}
else {
    // assumed to be running via runDemo
//    String folder = args[0] not required
    workers = Integer.parseInt(args[1])
    maxIterations = Integer.parseInt(args[2])
    width = Integer.parseInt(args[3])
    height = Integer.parseInt(args[4])
    pixelDelta = Double.parseDouble(args[5])
}

print "Line GUI, $width, $height, $maxIterations, $workers, "
System.gc()

long startTime = System.currentTimeMillis()

def emitDetails = new DataDetails(dName: ml.getName(),
                  dInitMethod: ml.init,
                  dInitData: [width, height, pixelDelta, maxIterations],
                  dCreateMethod: ml.create)

def guiDetails = new ResultDetails( rName: mlr.getName(),
                  rInitMethod: mlr.init,
                  rInitData: [width, height, Color.CYAN],
                  rCollectMethod : mlr.updateDList,
                  rFinaliseMethod : mlr.finalise )


def emit = new Emit(eDetails: emitDetails)

def spread = new OneFanAny(destinations: workers)

def group = new AnyGroupAny (function: ml.calcColour,
                workers: workers)

def reduce = new AnyFanOne ( sources: workers)

def gui = new CollectUI(guiDetails: guiDetails)


long endTime = System.currentTimeMillis()
println " ${endTime - startTime}"
