package gppDemos.nbody

import jcsp.lang.*
import groovyJCSP.*
 
import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.functionals.matrix.MultiCoreEngine
import gppLibrary.terminals.Collect
import gppLibrary.terminals.Emit
import gppDemos.nbody.NbodyData as nd
import gppDemos.nbody.NbodyResults as nr
 

// usage runDemo nbody ParNbody outFile N nodes iterations
 
int nodes
int N
String workingDirectory = System.getProperty('user.dir')
int iterations
double dt = 1e11
String readPath, writePath
 
if (args.size() == 0){
// assumed to be running form within Intellij
N = 128
nodes = 4
readPath = "./planets_list.txt"
writePath = "./${N}_planets_${nodes}_Par.txt"
iterations = 100
}
else {
// assumed to be running via runDemo
String folder = args[0]
N = Integer.parseInt(args[1])
nodes = Integer.parseInt(args[2])
iterations  = Integer.parseInt(args[3])
writePath = workingDirectory + "/src/main/groovy/gppDemos/${folder}/${N}_planets_${nodes}_Par.txt"
readPath = workingDirectory + "/src/main/groovy/gppDemos/${folder}/planets_list.txt"
}
 
System.gc()
print "ParNbody (matrix of Planet data), $N , $nodes, "
long startTime = System.currentTimeMillis()
 
 
def eDetails = new DataDetails (dName: nd.getName(),
dCreateMethod: nd.createMethod,
dInitMethod: nd.initMethod,
dInitData: [readPath, N, dt])
 
def rDetails = new ResultDetails(rName: nr.getName(),
rInitMethod: nr.init,
rInitData: [writePath],
rCollectMethod: nr.collector,
rFinaliseMethod: nr.finalise)
 

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
    partitionMethod: nd.partitionMethod,
    calculationMethod: nd.calculationMethod,
    updateMethod: nd.updateMethod )
 
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
 
 
