package gppDemos.goldbach.scripts

import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.terminals.Collect
import gppLibrary.terminals.EmitWithLocal

import gppDemos.goldbach.data.Prime as p
import gppDemos.goldbach.data.PrimeListSeqSimple as pl
import gppDemos.goldbach.data.Sieve as s

//usage runDemo goldbach/scripts/RunSimplePrimes N

int N = 0

if (args.size() == 0){
    N = 50000
}
else {
    N = Integer.parseInt(args[0])
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
                lInitData: [N])
print "Simple Primes, "
def startime = System.currentTimeMillis()

def emitter = new EmitWithLocal(eDetails: eDetails)

def collector = new Collect(rDetails: rDetails)

def endtime = System.currentTimeMillis()
def elapsedTime = endtime - startime
println "${elapsedTime} "
