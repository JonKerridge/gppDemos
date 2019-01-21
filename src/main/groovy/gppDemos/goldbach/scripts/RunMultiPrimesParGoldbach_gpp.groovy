package gppDemos.goldbach.scripts

import gppLibrary.DataDetails
import gppLibrary.GroupDetails
import gppLibrary.LocalDetails
import gppLibrary.ResultDetails
import gppLibrary.connectors.reducers.ListSeqOne
import gppLibrary.connectors.spreaders.OneParCastList
import gppLibrary.connectors.spreaders.OneSeqCastList
import gppLibrary.functionals.groups.ListGroupList
import gppLibrary.terminals.Collect
import gppLibrary.terminals.EmitWithLocal
import gppLibrary.functionals.transformers.CombineNto1

import gppDemos.goldbach.data.Prime as p
import gppDemos.goldbach.data.InternalPrimeList as ipl
import gppDemos.goldbach.data.ResultantPrimes as rp
import gppDemos.goldbach.data.Sieve as s
import gppDemos.goldbach.data.PartitionedPrimeList as ppl
import gppDemos.goldbach.data.GoldbachRange as gr
import gppDemos.goldbach.data.GoldbachParCollect as gpc

//usage runDemo goldbach/scripts RunMultiPrimesParGoldbach resultsFile maxN gWorkers pWorkers

int maxN
int pWorkers	// pWorkers must divide maxN exactly checked by assertion!
                    // number of parallel prime sieves
int gWorkers	// number of Goldbach partitions

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
    pWorkers = Integer.parseInt(args[3])
}

System.gc()
print "MultiParGB $maxN, $pWorkers, $gWorkers, "
def startime = System.currentTimeMillis()

assert((maxN % pWorkers) == 0 )

int N = maxN / pWorkers
int filter = Math.sqrt(maxN) + 1
def primeInitData = []
int start = 1
int end = 0
for ( i in 1.. pWorkers){
  end = i * N
  primeInitData << [ N, start, end]
  start = end + 1
}

def eDetails = new DataDetails( dName:  p.getName(),
                dInitMethod: p.init,
                dCreateMethod: p.create,
                lName: s.getName(),
                lInitMethod: s.init,
                lInitData: [filter])

def g1Details = new GroupDetails(workers: pWorkers,
    groupDetails: new LocalDetails [pWorkers])

for (w in 0 ..< pWorkers) {
g1Details.groupDetails[w] = new LocalDetails(lName: ppl.getName(),
            lInitMethod: ppl.init,
            lInitData: primeInitData[w],
            lFinaliseMethod: ppl.finalise)
}

def g2Details = new GroupDetails(workers: gWorkers,
    groupDetails: new LocalDetails [gWorkers])

for (w in 0 ..< gWorkers) {
g2Details.groupDetails[w] = new LocalDetails(lName: gr.getName(),
            lInitMethod: gr.init,
            lFinaliseMethod: ppl.finalise)
g2Details.groupDetails[w].lInitData = [w]
}

def combineLocal = new LocalDetails(lName: ipl.getName(),
    lInitMethod: ipl.init,)

def combineOut = new LocalDetails(lName: rp.getName(),
    lInitMethod: rp.init,
    lInitData: [pWorkers],
    lFinaliseMethod: rp.finalise)

def resDetails = new ResultDetails(rName: gpc.getName(),
    rInitMethod:gpc.init,
    rCollectMethod: gpc.collector,
    rFinaliseMethod: gpc.finalise)


def emitter = new EmitWithLocal(eDetails: eDetails)

def spread1 = new OneSeqCastList()

def group1 = new ListGroupList( 
                gDetails: g1Details,
                workers: pWorkers,
                outData: false,
                function: p.sievePrime
                 )

def reduce1 = new ListSeqOne ()

def combine = new CombineNto1( 
                 localDetails: combineLocal,
                 outDetails: combineOut,
                 combineMethod: ipl.toIntegers)

def spread2 = new OneParCastList()


def group2 = new ListGroupList ( 
                 gDetails: g2Details,
                 workers: gWorkers,
                 modifier:[[gWorkers], [gWorkers], [gWorkers], [gWorkers] ],
                 outData: false,
                 function: rp.getRange)

def reduce2 = new ListSeqOne ()

def collector = new Collect (
               rDetails: resDetails)


def endtime = System.currentTimeMillis()
def elapsedTime = endtime - startime
println " ${elapsedTime}"
