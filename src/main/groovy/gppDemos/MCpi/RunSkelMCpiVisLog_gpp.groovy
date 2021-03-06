package gppDemos.MCpi

import gppLibrary.*
import gppLibrary.connectors.reducers.*
import gppLibrary.connectors.spreaders.*
import gppLibrary.terminals.*
import gppLibrary.functionals.groups.*
import gppDemos.MCpi.MCpiData as piData
import gppDemos.MCpi.MCpiResults as piResults

//usage runDemo MCpi RunSkelMCpi resultsFile workers instances iterations

int workers
int instances
int iterations

if (args.size() == 0 ) {
  workers = 4
  instances =1024
  iterations = 100000
}
else {
//    String folder = args[0] not required
  workers = Integer.parseInt(args[1])
  instances = Integer.parseInt(args[2])
  iterations = Integer.parseInt(args[3])
}

DataDetails emitData = new DataDetails( dName: piData.getName(),
        dInitMethod: piData.init,
        dInitData: [instances],
        dCreateMethod: piData.create,
        dCreateData: [iterations])


ResultDetails resultDetails = new ResultDetails(rName: piResults.getName(),
        rInitMethod: piResults.init,
        rCollectMethod: piResults.collector,
        rFinaliseMethod: piResults.finalise)

System.gc()

print """SkelMCpi, $workers, $instances, $iterations, """
def startime = System.currentTimeMillis()

//@log 1 "./LogFile-2-"

def emit = new Emit (eDetails: emitData,
        logPhaseName: "0-emit",
        logPropertyName: "instance")

def ofa = new OneFanAny (destinations: workers)

def group = new AnyGroupAny (workers: workers,
        function: piData.withinOp,
        logPhaseName: "1-split",
        logPropertyName: "instance" )

def afo = new AnyFanOne (sources: workers)

def collector = new Collect (rDetails: resultDetails,
        logPhaseName: "1-value",
        logPropertyName: "instance" )

def endtime = System.currentTimeMillis()
println " ${endtime - startime} "
