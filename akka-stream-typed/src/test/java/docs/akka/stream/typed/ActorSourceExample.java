/**
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.akka.stream.typed;

// #actor-source-ref
import akka.actor.typed.ActorRef;
import akka.japi.JavaPartialFunction;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.typed.javadsl.ActorSource;
// #actor-source-ref

public class ActorSourceExample {

  // #actor-source-ref

  interface Protocol {}
  class Message implements Protocol {
    private final String msg;
    public Message(String msg) {
      this.msg = msg;
    }
  }
  class Complete implements Protocol {}
  class Fail implements Protocol {
    private final Exception ex;
    public Fail(Exception ex) {
      this.ex = ex;
    }
  }
  // #actor-source-ref

  final ActorMaterializer mat = null;

  {
    // #actor-source-ref

    final JavaPartialFunction<Protocol, Throwable> failureMatcher =
      new JavaPartialFunction<Protocol, Throwable>() {
        public Throwable apply(Protocol p, boolean isCheck) {
          if (p instanceof Fail) {
            return ((Fail)p).ex;
          } else {
            throw noMatch();
          }
        }
      };

    final Source<Protocol, ActorRef<Protocol>> source = ActorSource.actorRef(
      (m) -> m instanceof Complete,
      failureMatcher,
      8,
      OverflowStrategy.fail()
    );

    final ActorRef<Protocol> ref = source.collect(new JavaPartialFunction<Protocol, String>() {
      public String apply(Protocol p, boolean isCheck) {
        if (p instanceof Message) {
          return ((Message)p).msg;
       } else {
          throw noMatch();
       }
     }
    }).to(Sink.foreach(System.out::println)).run(mat);

    ref.tell(new Message("msg1"));
    // ref.tell("msg2"); Does not compile
    // #actor-source-ref
  }
}