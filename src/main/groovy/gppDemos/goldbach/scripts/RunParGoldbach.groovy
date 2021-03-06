package gppDemos.goldbach.scripts

import jcsp.lang.*
import groovyJCSP.*
 
import gppLibrary.DataDetails
import gppLibrary.GroupDetails
import gppLibrary.LocalDetails
import gppLibrary.ResultDetails
import gppLibrary.connectors.reducers.ListSeqOne
import gppLibrary.connectors.spreaders.OneParCastList
import gppLibrary.functionals.groups.ListGroupList
import gppLibrary.terminals.Collect
import gppLibrary.terminals.EmitWithLocal
import gppLibrary.functionals.workers.ThreePhaseWorker
import gppDemos.goldbach.data.Prime as p
import gppDemos.goldbach.data.ResultantPrimes as rp
import gppDemos.goldbach.data.Sieve as s
import gppDemos.goldbach.data.CollectedPrimes as cp
import gppDemos.goldbach.data.GoldbachRange as gr
import gppDemos.goldbach.data.GoldbachParCollect as gpc
 

//usage runDemo goldbach/scripts RunParGoldbach resultsFile maxN gWorkers
 
int maxN
int gWorkers 	// number of Goldbach workers
 
if (args.size() == 0){
// assumed to be running form within Intellij
maxN = 20000
gWorkers = 4
}
else {
// assumed to be running via runDemo
// working directory folder assumed to be in args[0]
maxN = Integer.parseInt(args[1])
gWorkers = Integer.parseInt(args[2])
}
 
 
System.gc()
print "ParGB, $maxN, $gWorkers, "
def startime = System.currentTimeMillis()
 
int N = maxN
 
int filter = Math.sqrt(maxN) + 1
def primeInitData = [N, 1, N]
 
def eDetails = new DataDetails( dName:  p.getName(),
dInitMethod: p.init,
dCreateMethod: p.create,
lName: s.getName(),
lInitMethod: s.init,
lInitData: [filter])
 
def workerLocal = new LocalDetails(lName: cp.getName(),
lInitMethod: cp.init,
lInitData: primeInitData )
 
 
def gDetails = new GroupDetails(workers: gWorkers,
groupDetails: new LocalDetails [gWorkers])
 
for (w in 0 ..< gWorkers) {
gDetails.groupDetails[w] = new LocalDetails(lName: gr.getName(),
lInitMethod: gr.init,
lFinaliseMethod: gr.finalise)
gDetails.groupDetails[w].lInitData = [w]
}
 
def resDetails = new ResultDetails(rName: gpc.getName(),
rInitMethod:gpc.init,
rCollectMethod: gpc.collector,
rFinaliseMethod: gpc.finalise)
 
 
 

//NETWORK

def chan1 = Channel.one2one()
def chan2 = Channel.one2one()
def chan3 = Channel.one2oneArray(gWorkers)
def chan3OutList = new ChannelOutputList(chan3)
def chan3InList = new ChannelInputList(chan3)
def chan4 = Channel.one2oneArray(gWorkers)
def chan4OutList = new ChannelOutputList(chan4)
def chan4InList = new ChannelInputList(chan4)
def chan5 = Channel.one2one()

def emitter = new EmitWithLocal(
    // input channel not required
    output: chan1.out(),
    eDetails: eDetails)
 
def worker = new ThreePhaseWorker(
    input: chan1.in(),
    output: chan2.out(),
    lDetails: workerLocal,
    inputMethod: cp.inputMethod,
    workMethod: cp.workMethod,
    outFunction: cp.outFunction)
 
def spread = new OneParCastList(
    input: chan2.in(),
    outputList: chan3OutList )
 
def modifiers = []
for ( w in 0..<gWorkers) modifiers << [gWorkers]
 
def group = new ListGroupList (
    inputList: chan3InList,
    outputList: chan4OutList,
    gDetails: gDetails,
    workers: gWorkers,
    modifier:modifiers,
    outData: false,
    function: rp.getRange)
 
def reduce = new ListSeqOne (
    inputList: chan4InList,
    output: chan5.out(),
    )
 
def collector = new Collect (
    input: chan5.in(),
    // no output channel required
    rDetails: resDetails)

PAR network = new PAR()
 network = new PAR([emitter , worker , spread , group , reduce , collector ])
 network.run()
 network.removeAllProcesses()
//END

 
 
def endtime = System.currentTimeMillis()
println " ${endtime - startime}"
