package gppDemos.mandelbrot.cluster

import gppLibrary.cluster.NodeInterface
import gppLibrary.cluster.connectors.NodeRequestingFanAny
import gppLibrary.connectors.reducers.AnyFanOne
import gppLibrary.functionals.groups.AnyGroupAny
import groovyJCSP.*
import jcsp.lang.*

//import SerializedMandelbrotPixel as smp
import gppDemos.mandelbrot.data.MandelbrotLine as ml

class NodeLineWorker  implements NodeInterface {

  ChannelOutput request
  ChannelInput response
  ChannelOutput output
  int workers = 4

  @Override
  void connect(ChannelInputList inChannels, ChannelOutputList outChannels) {
    response = inChannels[0]
    request = outChannels[0]
    output = outChannels[1]
  }

  @Override
  public void run() {
    def toGroup = Channel.one2any()
    def fromGroup = Channel.any2one()
    def requester = new NodeRequestingFanAny (request: request,
                        response: response,
                        outputAny: toGroup.out(),
                        destinations: workers)
    def group = new AnyGroupAny(inputAny: toGroup.in(),
                  outputAny: fromGroup.out(),
                  workers: workers,
                  function: ml.calcColour)
    def compressor = new AnyFanOne(inputAny: fromGroup.in(),
                    output: output,
                    sources: workers)
    println "NW: about to run PAR"
    new PAR ([requester, group, compressor]).run()
    println "NW: terminated"
  }

}
