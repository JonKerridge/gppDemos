package gppDemos.mandelbrot.scripts

import gppLibrary.connectors.reducers.AnyFanOne
import gppLibrary.connectors.spreaders.OneFanAny
import gppLibrary.functionals.groups.AnyGroupAny
import gppLibrary.terminals.Collect
import gppLibrary.terminals.Emit
import gppLibrary.*

import gppDemos.mandelbrot.data.MandelbrotLine as ml
import gppDemos.mandelbrot.data.MandelbrotLineCollect as mlc

// usage runDemo mandelbrot/scripts RunMandelBrotLineNoGUI workers maxInterations width height pixeldelta

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

System.gc()

print "Line noGUI, $width, $height, $maxIterations, $workers, "
long startTime = System.currentTimeMillis()

def emitDetails = new DataDetails(dName: ml.getName(),
                  dInitMethod: ml.init,
                  dInitData: [width, height, pixelDelta, maxIterations],
                  dCreateMethod: ml.create)

def resultDetails = new ResultDetails(rName: mlc.getName(),
                    rInitMethod: mlc.init,
                    rCollectMethod: mlc.collector,
                    rFinaliseMethod: mlc.finalise)

def emit = new Emit(eDetails: emitDetails)

def spread = new OneFanAny(destinations: workers)

def group = new AnyGroupAny (function: ml.calcColour,
                workers: workers)

def reduce = new AnyFanOne ( sources: workers)

def collector = new Collect(rDetails: resultDetails)

def endtime = System.currentTimeMillis()
println " ${endtime - startTime}"
