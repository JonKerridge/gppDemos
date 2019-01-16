package gppDemos.solarSystem

import jcsp.lang.*
import groovyJCSP.*
 
import gppDemos.solarSystem.PlanetrySystem as ps
import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.functionals.matrix.MultiCoreEngine
import gppLibrary.terminals.Collect
import gppLibrary.terminals.Emit
import gppDemos.solarSystem.PlanetryResult as pr
 

// usage runDemo nbody/ParNbody resultFile N nodes iterations
 
int nodes = 0
int N = 0
int iterations = 0
double dt = 1e11
 
if (args.size() == 0){
N = 100
nodes = 2
iterations = 100
}
else {
N = Integer.parseInt(args[0])
nodes = Integer.parseInt(args[1])
iterations = Integer.parseInt(args[2])
}
 
 
String readPath = "./planets_list.txt"
String writePath = "./result_${iterations}_${N}_planets_${nodes}_Par.txt"
 
System.gc()
print "RunPlanets (arrayList of plants) $iterations, $N , $nodes ->"
long startTime = System.currentTimeMillis()
 
 
def eDetails = new DataDetails (dName: ps.getName(),
dCreateMethod: ps.createMethod,
dInitMethod: ps.initMethod,
dCreateData: [readPath, N, dt])
 
def rDetails = new ResultDetails(rName: pr.getName(),
rInitMethod: pr.init,
rInitData: [writePath,],
rCollectMethod: pr.collector,
rFinaliseMethod: pr.finalise)
 

//NETWORK

def chan1 = Channel.one2one()
def chan2 = Channel.one2one()

def emit = new Emit(
    // input channel not required
    output: chan1.out(),
    eDetails: eDetails)
 
def mcEngine = new MultiCoreEngine (
    input: chan1.in(),
    output: chan2.out(),
    nodes: nodes,
    finalOut: true,
    iterations: iterations,
    partitionMethod: ps.partitionMethod,
    calculationMethod: ps.calculationMethod,
    updateMethod: ps.updateMethod )
 
def collector = new Collect(
    input: chan2.in(),
    // no output channel required
    rDetails: rDetails)

PAR network = new PAR()
 network = new PAR([emit , mcEngine , collector ])
 network.run()
 network.removeAllProcesses()
//END

 
 
long endTime = System.currentTimeMillis()
println " ${endTime - startTime}"
 
 
 
 
 
 
 
 
 
