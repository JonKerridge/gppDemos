package gppDemos.MCpi

import jcsp.lang.*
import groovyJCSP.*
 
import gppLibrary.DataDetails
import gppLibrary.GroupDetails
import gppLibrary.LocalDetails
import gppLibrary.ResultDetails
import gppLibrary.patterns.DataParallelCollect
import gppDemos.MCpi.MCpiPartData as piPartData
import gppDemos.MCpi.MCpiPartResults as piPartResults
import gppDemos.MCpi.MCpiPartWorker as piPartWorker
 

//usage runDemo MCpi RunMCpiWorkerDPCPattern resultsFile workers instances iterations
 
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
 
print "Run MCpi Worker DPC Pattern $workers, $instances, $iterations, "
System.gc()
def startime = System.currentTimeMillis()
 
DataDetails emitData = new DataDetails( dName: piPartData.getName(),
dInitMethod: piPartData.initClass,
dInitData: [instances],
dCreateMethod: piPartData.createInstance,
dCreateData: [iterations])
 
ResultDetails resultDetails = new ResultDetails(rName: piPartResults.getName(),
rInitMethod: piPartResults.initClass,
rCollectMethod: piPartResults.collector,
rFinaliseMethod: piPartResults.finalise)
 
LocalDetails mcpiWorker = new LocalDetails( lName: piPartWorker.getName(),
lInitMethod: piPartWorker.init,
lInitData: [0, 0],
lFinaliseMethod: piPartWorker.finalise )
 
GroupDetails localGroup = new GroupDetails(	workers: workers,
groupDetails: new LocalDetails[workers])
 
for ( w in 0 ..< workers) localGroup.groupDetails[w] = mcpiWorker
 

//NETWORK


def farming = new DataParallelCollect (
    eDetails: emitData,
    gDetails: localGroup,
    rDetails: resultDetails,
    workers: workers,
    function: MCpiPartData.doCalc,
    outData: false )

farming .run()

//END

 
 
def endtime = System.currentTimeMillis()
println "\t${endtime - startime}"
