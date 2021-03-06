package gppDemos.concordance

import gppLibrary.DataDetails
import gppLibrary.ResultDetails
import gppLibrary.connectors.spreaders.OneIndexedList
import gppLibrary.functionals.groups.ListGroupCollect
import gppLibrary.functionals.pipelines.OnePipelineOne
import gppLibrary.terminals.Emit

import gppDemos.concordance.ConcordanceData as cd
import gppDemos.concordance.ConcordanceResults as cr


//usage runDemo concordance RunGroupCollectConcordance resultsFile collectors title N

int collectors
String title
int N
int minSeqLen = 2
boolean doFileOutput = false
String workingDirectory = System.getProperty('user.dir')
String fileName
String outFileName

if (args.size() == 0){
    // assumed to be running form within Intellij
    collectors = 4
    title = "bible"
    N = 8
    fileName = "./${title}.txt"
    outFileName = "./${title}GCol"
}
else {
    // assumed to be running via runDemo
    String folder = args[0]
    title = args[2]
    fileName = workingDirectory + "/src/main/groovy/gppDemos/${folder}/${title}.txt"
    outFileName = workingDirectory + "/src/main/groovy/gppDemos/${folder}/${title}GCol"
    collectors = Integer.parseInt(args[1])
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

for ( g in 0..< collectors) resultDetails << rDetails

System.gc()
print "GroupCollect, $doFileOutput, $title, $collectors, "
def startime = System.currentTimeMillis()


def emitter = new Emit( eDetails: dDetails)

def pipe = new OnePipelineOne(stages : 3,
                stageOp: [cd.valueList, cd.indicesMap, cd.wordsMap])

def oil = new OneIndexedList(indexFunction : cd.indexer,
                             indexBounds: [0, collectors])

def lgc = new ListGroupCollect(workers: collectors,
                 rDetails: resultDetails)


def endtime = System.currentTimeMillis()
println " ${endtime - startime} "

