package gppDemos.goldbach.scripts

import gppLibrary.DataDetails
import gppLibrary.GroupDetails
import gppLibrary.LocalDetails
import gppLibrary.ResultDetails
import gppLibrary.connectors.reducers.ListSeqOne
import gppLibrary.connectors.spreaders.OneSeqCastList
import gppLibrary.functionals.groups.ListGroupList
import gppLibrary.terminals.Collect
import gppLibrary.terminals.EmitWithLocal
import gppDemos.goldbach.data.Prime as p
import gppDemos.goldbach.data.Sieve as s
import gppDemos.goldbach.data.PartitionedPrimeList as ppl
import gppDemos.goldbach.data.PrimeList as pl

//usage runDemo goldbach/scripts/RunCombiningPrimes resultsFile maxN pWorkers

int maxN = 0
int pWorkers = 0    // number of prime workers

if (args.size() == 0){
    maxN = 100000
    pWorkers = 2
}
else {
    maxN = Integer.parseInt(args[0])
    pWorkers = Integer.parseInt(args[1])
}

System.gc()
print "Combining Primes, $maxN, $pWorkers, "
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

def rDetails = new ResultDetails(rName: pl.getName(),
                 rInitMethod:pl.init,
                 rCollectMethod: pl.collector,
                 rFinaliseMethod: pl.finalise)

def eDetails = new DataDetails(dName:  p.getName(),
                dInitMethod: p.init,
                dCreateMethod: p.create,
                lName: s.getName(),
                lInitMethod: s.init,
                lInitData: [filter])

def gDetails = new GroupDetails(workers: pWorkers,
                groupDetails: new LocalDetails [pWorkers])

for (w in 0 ..< pWorkers) {
  gDetails.groupDetails[w] = new LocalDetails(lName: ppl.getName(),
                        lInitMethod: ppl.init,
                        lInitData: primeInitData[w],
                        lFinaliseMethod: ppl.finalise)
}

def emitter = new EmitWithLocal(eDetails: eDetails)


def spread = new OneSeqCastList( )

def group = new ListGroupList ( gDetails: gDetails,
                workers: pWorkers,
                outData: false,
                function: p.sievePrime
                 )

def reduce = new ListSeqOne ( )

def collector = new Collect(rDetails: rDetails)

def endtime = System.currentTimeMillis()
println " ${endtime - startime} "
