package gppDemos.mandelbrot.cluster

import gppDemos.mandelbrot.data.MandelbrotPixel as mp
import gppLibrary.*
import gppLibrary.cluster.*
import gppLibrary.cluster.connectors.*
import gppLibrary.terminals.Emit
import groovyJCSP.*
import jcsp.lang.*

class NodeEmit implements NodeInterface {

  ChannelInputList requests
  ChannelOutputList responses
  int width = 350
  int height = 200
  int maxIterations = 1600
  double pixelDelta = 0.01


  @Override
  public void run() {
    def connect = Channel.one2one()
    def emitDetails = new DataDetails(dName: mp.getName(),
                  dInitMethod: mp.init,
                  dInitData: [width, height, pixelDelta, maxIterations],
                  dCreateMethod: mp.create)

    def emit = new Emit(output : connect.out(),
              eDetails: emitDetails)

    def spreader = new OneNodeRequestedList(input: connect.in(),
                        request: requests,
                        response: responses)
    println "NE: starting Mandelbrot for $width x $height and maxIterations = $maxIterations"
    new PAR([emit, spreader]).run()
    println "NE: terminated"
  }

  @Override
  void connect(ChannelInputList inChannels, ChannelOutputList outChannels) {
    requests = inChannels
    responses = outChannels
  }

}