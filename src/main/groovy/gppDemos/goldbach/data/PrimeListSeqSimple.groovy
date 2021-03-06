package gppDemos.goldbach.data

import groovy.transform.CompileStatic

@CompileStatic
class PrimeListSeqSimple extends gppLibrary.DataClass {

  List primes = []

  static final String init = "initClass"
  static final String collector = "collector"
  static final String finalise = "finalise"

  int initClass (List d){
    return completedOK
  }
// simple and sequential version
  int collector(Prime o){
    primes << o.p
    return completedOK
  }

  int finalise(List p) {
/*
    println "primes are: "
    int c = 0
    for ( i in 0..< primes.size()){
       print "${primes[i]}\t"
       c = c + 1
       if ( c == 10) {
         println " "
         c = 0
       }
    }
    println "\nfinished"
*/
//    println "There are ${primes.size()} primes"
    return completedOK
  }

}
