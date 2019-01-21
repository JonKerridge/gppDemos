package gppDemos.concordance

import jcsp.lang.*
import groovyJCSP.*
 
import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.connectors.spreaders.OneFanAny
import gppLibrary.functionals.composites.GroupOfPipelineCollects
import gppLibrary.terminals.Emit
 
import gppDemos.concordance.ConcordanceData as cd
import gppDemos.concordance.ConcordanceResults as cr
 
 

//usage runDemo concordance RunGoPConcordanceLog resultFile groups title N runNo
 
int workers
String title
int N
int minSeqLen = 2
boolean doFileOutput = false
String workingDirectory = System.getProperty('user.dir')
String fileName
String outFileName
 
if (args.size() == 0){
// assumed to be running form within Intellij
workers = 4
title = "bible"
N = 8
fileName = "./${title}.txt"
outFileName = "./${title}GoPLog"
}
else {
// assumed to be running via runDemo
String folder = args[0]
title = args[2]
fileName = workingDirectory + "/src/main/groovy/gppDemos/${folder}/${title}.txt"
outFileName = workingDirectory + "/src/main/groovy/gppDemos/${folder}/${title}GoPLog"
workers = Integer.parseInt(args[1])
N = Integer.parseInt(args[3])
}
 
 
def dDetails = new DataDetails( dName: cd.getName(),
dInitMethod: cd.init,
dInitData: [N, fileName, outFileName],
dCreateMethod: cd.create,
dCreateData: [null])
 
def rDetails = new ResultDetails( rName: cr.getName(),
rInitMethod: cr.init,
rInitData: [minSeqLen, doFileOutput],
rCollectMethod: cr.collector,
rFinaliseMethod: cr.finalise,
rFinaliseData: [null])
 
List <ResultDetails>  resultDetails = []
 
for ( g in 0..< groups) resultDetails << rDetails
 
System.gc()
print "RunGoPConcordanceLog $doFileOutput,  $groups, "
 
def startime = System.currentTimeMillis()
 
//@log groups "./GPPLogs/LogFile-2-"

import gppLibrary.Logger
import gppLibrary.LoggingVisualiser

def logChan = Channel.any2one()
Logger.initLogChannel(logChan.out())
def logVis = new LoggingVisualiser ( logInput: logChan.in(), 
                     collectors: groups,
                     logFileName: "./GPPLogs/LogFile-2-" )

 

//NETWORK

def chan1 = Channel.one2one()
def chan2 = Channel.one2any()

def emitter = new Emit(
    // input channel not required
    output: chan1.out(),
    eDetails: dDetails,
    logPhaseName: "0-emit",
    logPropertyName: "strLen")
 
def fanOut = new OneFanAny(
    input: chan1.in(),
    outputAny: chan2.out(),
    destinations: groups)
 
def poG = new GroupOfPipelineCollects(
    inputAny: chan2.in(),
    visLogChan : logChan.out(),
    // no output channel required
    stages: 3,
    rDetails: resultDetails,
    stageOp: [cd.valueList, cd.indicesMap, cd.wordsMap],
    groups: groups,
    logPhaseNames: ["1-value", "2-indeces", "3-words", "4-collect"],
    logPropertyName: "strLen" )

PAR network = new PAR()
 network = new PAR([logVis, emitter , fanOut , poG ])
 network.run()
 network.removeAllProcesses()
//END

 
def endtime = System.currentTimeMillis()
println " ${endtime - startime}"
 
 
