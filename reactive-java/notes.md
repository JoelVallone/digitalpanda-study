# RxJS2, Java 9 native observers, Akka reactive
course homepage: https://www.linkedin.com/learning/reactive-java-9

# 1. What is Reactive Programming ?
## Installation and setup
 - Web app server to install: https://tomcat.apache.org
   - Install guide: https://wolfpaulus.com/tomcat/
   - > /Library/Tomcat/bin/startup.sh # http://localhost:8080, V9.0.38
   - > /Library/Tomcat/bin/shutdown.sh

## What is reactive programming
In computing, reactive programming is a programming paradigm oriented around data flows and the propagation of change
Data stream/flow: Sequence of ongoing events ordered in time (asnychronous)

A = B + C // In reactive programming A will change whenever B or C change like a spreadsheet does (not only when evaluated once as done with the imperative)

ractivex.io // APIs do different programming languages (RXJava,RxScala,...)

## Benefits of reactive programming
Evolution of needs: 
    Independent services, distributed apps, real time processing, ... => increased concurency
Benefits of reactive systems: 
    flexible (loose coupling, code evolution), responsiveness, scalable, fault tolerance (?), less latency
Because: 
    Asynchronous and non-blocking, Uses few threads in practices
Relies on: 
    Lambdas, Concurent data structures

Push VS Pull
 - Imperative: pull and process synchronously using iterators
 - Reactive: Convert pull into push model by using datastructures on which we process events rather than iterate but use the same logic !

## Reactive explained in a simple way
Stream: Sequence of events ongoing events ordered it time from producer/source to consumer(s)

Events can be:
 - A value of a certain type
 - error signal : announce that something went wrong in the sourc => no more new messages => close data stream
 - completed signal: announce that the source has finished emitting events => close data stream

Consumer & Subscription:
The data is pushed to the consumer.
That is the consumer does not know when he will receive the data.
THe consumer subscribes to a source to start receiving data when emitted by the source
TO avoid overflowing consumers, the producer receives a backpressure signal

Cold VS Hot sources:
A cold source starts running upon subscription
A hot source always emits data (even before an active subscription)

Cold Subscription:
Producer gets 1) created 2) activated upon a consumer subscription function call. 
Then 3) counsumer listens to the source.
Unicast from producer to consumer

Hot subscription:
Consumer shares a reference of a source of information and starts listening to the consumer.
Multicast as many consumers can listen (if the producer supports it)

Four principles oguiding reactive applications (reactive manifesto):
Elastic/Scalable:
    Design with no contention point thanks to asnychronous processing
    Decouples time and space => can scale out with asynchronous message passing 
    => components loose coupling, isolation and location transparency
    => Delegate failures as messages + only consumer while active

Resilient : 
    System stays responsive after a failure on all levels(performance, endurance, security are facets of resilience).
    Isolate components from eachother and contain errors within the boundaries of the components another higher component is responsible for child component recovery. High availability by replication

Responsive : 
    System responds in a timelty manner with rapid and consistent response times

Message driven (architecture)
    Event driven application: events monitored by zero or more observers
    Actor based application: messages are sent specifically to recipients
        => Difference: events are directed to a clear destination, events just happen and can be observed by 0..N observers

Back pressure
    A fast source should not overwhelm a slow consumer
    Other Solutions:
        - Pull from consumer (backpressure is implicit)
        - Increase buffer size of the consumer if message source is finite enough
        - Drop messages
    Solution used in Rx : bidirectionnal data flow
        Consumer demand is signaled asynchronously to producer

Reactive in Java
No native support until V9 but many libraries !
 Reactive Streams: sset of java interfaces (just a contract...)
 RxJava: Netflix Lib
 Reactor: pivotal open source team built on top of Reactive Streams
 Spring Framework 5.0: has reactive features also for HTTP servers and clients
 Ratpack: High performance http services over HTTP using Netty and Reactive Streams interfaces for interoperability
 Akka: toolkit for Scala/Java inter process comunication using the actor pattern and Akka Streams for communication also implements Reactive Streams interfaces

## Reactive Streams (are only interfaces)
Standard for asnychronous stream processing with non-blocking back pressure
Aim Runtime environments (JVM an JS) + network protocols
Started in 2013 between Typesafe, Netflix, Pivotal

Goal : Handle streams of data with impredictible throughput variations : backpressure
Define rules tho which conforming implementations can easily interoperate.
Find minimum set of interfaces and protocols describing necessary operations and entities to achieve he goal.
End user DSL and protocol binding APIs have be voluntarily left away to allow freedom of implementation with each language specificities

Different working groups regarding the standard :
    Basic semantics: how transmission of data is regulated with back pressure
    JVM Interfaces: Apply semantics on set of interfaces (no impl only examples in april 2015 v1.0.0)
    JavaScript interfaces: Same as JVM
    Network Protocols: How to use network protocols for the goal: UDP, TPC, web circuits

Reactive JVM interfaces:
    Reactive Streams API (only interface)
    https://github.com/reactive-streams/reactive-streams-jvm (rules and methods)
    https://github.com/reactive-streams/reactive-streams-jvm/tree/master/api/src/main/java/org/reactivestreams
    - SOURCE of information : Publisher<T>
      - => void subscribe(Subscriber<? super T> s)
      - Rule: If fails it MUST signal onError to the subscriber
      - Rule: If terminates sucessfully (finite stream) it MUST signal onComplete
      - Rule: If as subscription is cancelled its Subscriber MUST eventually stop being signaled
      - Rule: subscribe many be called as many times as wanted but it MUST be with a different subscriber each time (do not register more than oce the same subscriber !)
  
    - CONSUMER: Subscriber<T>
      - => void onSubscribe(Subscription s)
        - Subscription established by publisher -> back pressure feedback in this object/interface
      - => void onNext(T t)
      - => onError(Throwable t)
      - => onComplete()
  
    - SUBSCRIPTION: Subscription
      - => request(long n) // signals ammount of messages/event to pull from Publisher/producer, is called by Subscriber
      - => cancel() // called when any want to cancel the subscription
      - Rule: A subscriber MUST signal demand via a request to receive onNext signals
  
    - PROCESSOR i.e Both Source and Consumer: Processor <T,R> extends Subscriber<T>, Publisher<R>
        receive, transform, send



# 2. Reactivity in java 9
## Java 9 features
New in java 9

Private interface methods
    reuse code between default methods in the interface without going public (hide implementation details)

Collection factory methods
     List.of(), Map.of(), Map.ofEntries(entry())
     Warning: Returns immutable collections which are not highlighted by the type system ... !?

Process API 
    Improved API for controling and managing OS processes

Stack Walking
    easy filtering and lazy access to stacktrace information

Http/2 & websocket client API (replacement for HTTP 1.1)
    Event based system for event notifications from server with asynchronous handling !

Modular souce code
    Project Jigsaw : breaks java runtime environment into portable components
    first step with Java 9 : reorganise JDK source code int modules
    Allows to embed java in small devices by only loading the minimum ammount of modules : better performance & security

Light-weight JSON API

Multi-resoulution images
    manipulate and display images

Platform logging API service
    route java logs to logging framework

Datagram Transport Layer Security (DLTS)
    secure communication with datagram protocols such as UDP

Deprecated:
    Applet API (Java in browser)
    Corba
    Explicit constructors for primitive wrappers => new Integer(5) replaced by factory methods Integer.valueOf(5) // better performance

## Flow API
Native reactive programming handling with the Flow API
Flow API is the JDK9  interfacedefinition of the Reactive specifications

Stream let's process data in a declarative way and automatically parallelizes the computation (ParallelStream)
Streams versus Collections
    Collections are in memory data structures where each element is computed before being part of the collection or being called (eager)
    Streams only compute on demand: lazy collection
    pipelining: chain of stream operations (map, filter, collect, skip, limit, forEach ...) expressed as anonymous functions (labdas) => no more imperative for, if,... internal interations hidden by the streams API

ExecutorService  executor = ForkJoinPool.commonPool(); // Execute taks in the background within a given thread pool

... implementation example by hand of the Flow API on a local machine using Futures and the ExecutorService
    // Should onNext() be synchronized !? (subscription.request() is) => threading model ?

## Functionnal programming
Fucntionnal pogramming is a programming paradigm that treats computation as the evaluation of mathematical functions and avoids changing the state and mutable data. 
It is a declarative programming : use expressions or declarations instead of statements : describe what to do not how.
Rooted in lambda calculus: mathematical paradigm to investigate computability

Concepts
    Functions are first-class citizens: they can als be treated as a value.
    Higher order functions: functions taking functions as inputs
    Pure functions: only operate on their input and have no side effects. They only return an output
    No variables only constants and immutable data structures
    Recursion to loop : recurse until the base case is reached then return

Combine Functional (lambdas, pure functions mostly) & Reactive programming to write code in a faster, clearer way
Lambda: single function in an anonymous class written in a concise form. Available since Java 8



# 3. Introducing RxJava 2.0
Implements Reactive specifications with back pressure and additional functionnality (operators)
Made by Netflix
Relies on the events processing model : whoever subscribes to observe - 0 to N observers - can see events happen (no targeted to any entity specifically)
RxJava is an implementation of the Reactive Streams JVM interface

## Introduction
RxJava: A library for composing asynchronous and event-bases programs by using observable sequences
RxJava 2.0 has been completely rewriten
Lightweight : 1.1 MiB; reactivex.io; https://github.com/ReactiveX/RxJava

## Backpressure
Trigger backpressure to avoid consumer being overflown by the source
Source back pressure handling
 Drop : drop events that are not timely pushed to the source when it emits availability (request(n) but more than n available at source)
 Latest: only return n latests elements of the source when calling request(n)
 Buffer: return all elements ordered ... but source buffer can overflow

## Reactive streams implementation
Flowable<T> <-> Publisher
    - RXJ 1's Observable // no backpressure in V1

Subscriber<T> <-> Subscriber
    - Similar to RXJ 1
    - SingleSubscriber: Consumes only one item, No backpressure needed, No onNext, No onComplete
    - CompletableSubscriber: Does not consume the value  (no onSuccess() ) but can complete
    - MaybeSubscriber: Single + Completable: Succeeds with an item OR completes with no items OR throws an error

Subscription <-> Subscription
    - Similar to RXJ 1

FlowableProcessor<T> <-> Processor<T,R>
    - input and output types are the same...
    - Similar to RXJ 1's Subject // no backpressure in V1


## Creating sources
Emitting scalar values: Flowable.FromIterable(list), Observable.just(object)
Wrapping non-reactive behaviours: 
    {Flowable, Observable, Completable, Single, Maybe}
        .fromCallable( () -> return something;)
            Will be called for every subscriber, if exception is thrown by lambda, will be caught forwarded to onError by RXJ
        .fromRunnable( () -> doSomethingOnSubscription();)
            Will be called upon subscription by every subscriber, no exception allowed, no return type but only processing
        .create( () -> new ObservableOnSubscribe<>(){})
            Full control on Observable

## Observing sources
Observables have no backpressure but Flowables have

Use DisposableSubscriber or DisposableObserver

var consumer = new DisposableSubscriber{}
var source = Observable.just("Hello World");
source.subscribe(consumer);
consumer.dispose(); // unsubscribe

OR 

var consumer = new DisposableSubscriber{}
var source = Observable.just("Hello World");
var disposable = source.subscribe(consumer);
disposable.dispose(); // unsubscribe consumer without needing ref to consumer (better practice ?)



# 4. Operators
Chain operations on reactive streams

## Marble diagrams
Operator: Transform a Publisher's output
Operators can be chained one after the other
Most operators return a new child publisher when called
Big collection of operators

Represent data streams with marble diagrams:
    ordered sequence of values 
    X axis is time leftmost is first, rightmost is last "|" marks complete "X" marks end with error)
    Y axis new sequences/publishers as a result of a transofmration (top parent, bottom child with result operations)

## Operators
Most operators return a new child publisher when called

Different type of operators :
Transform: 
    Map, FlatMap, Cast,...
Math:
    Max, Sum,... 
Aggregate: 
    Concat: concatenate multiple publishers into one, emitt all of publisher 1 (until complete), then all of publisher 2 (until complete), etc..
    To: convert an Observable into another object or data structure when it completes (similar to  stream.collect())
Utility: perform an action whnever a specific event happens
    doOnSubscribe
    doOnTerminate // complete or error
    doOnEach

Combining Publisher
    Merge: interleaves messages from publisher as their event appear (order)
    Zip: pair publisher together. Waits for both publishers to emit a new value before emitting a new pair.

Conditional and boolean
    Take while: Source until condition no longer true then terminate
    Contains: emits true as soon as sees the value OR false if source terminates without seeing the value.
    isEmpty: returns true if source never emits anything

Filtering operators
    filter: only allow items which pass a condition

Error Handling operators
    onErrorResumeNext: intercept error, publish additional elements and complete normally directly after.
    retry: when error do not forward error but rather re-subscribe to source (migh re emit old values if still in buffer of the source)

More operators: http://reactivex.io/documentation/operators.html



# 5. Concurency and Unit Testing
## Concurrency
By default, producer, consumer and operators work on the same thread.
Schedulers : used for multithreading
    Schedulers.computation() // Returns a fixed size thread pool equal to the number of CPU hyper threads, use for CPU bound tasks
    Schedulers.io() // Thread pools for I/O bound operations : thread pool which will grow as needed due to blocking I/O operations (DB, REST call, file access, ...)
    Schedulers.immediate() // Execute work on the current thread, interesting for unit testing (deterministic)
    Schedulers.newThread() // New thread for each unit of work...

Concurency operators
    SubscribeOn : 
        Scheduler on which a publisher will operate instead of main thread
        If multiple calls to this method in the chain of operators, the nearest to the source wins
    ObserveOn :
        Scheduler on which a consumer/observer will operate instead of main thread
        Use it to allocate operators to specific treads

## Unit testing
BlockingObservable
    Option1:
    Use the toBlocking() on a Publisher to get a blocking observable (synchronous)
    Blocking observable has blocking calls such as next() which waits for the enxt element comming from the publisher

    Option2:
    Use Schedulers.immediate() // execute publisher code immediately
    Works only if the code under test does not explicitly set another scheduler

Mocking Observables
    Observable.just(object) // will emit the single object element and terminate normally
    Observable.empty() // no output but terminates normally
    Observable.never() // no output and never terminates
    Observable.error(throwable) // no output and throws exception to terminate

TestSubscriber: 
    perform assertions on the results of the subscription
    inspect subscription activity

# 6. Akka Streams
Actor model for reactive streams: send stream of messages to specific targets.

## Introduction to Akka
Message-driven architecutre using actors : messages are directed to a specific recipient (the actor)
Is an Implementation of Reactive Streams : express and run a chain of asynchronous processing stps

Actor:
    has a mailbox receving messages : uses pattern matching for detecting message type and related action/transformation/usage of the data
    pass messages to other actors or himself
    each actor has its own private state/context
    each actor run on a dedicated thread
Can easily pass network boundaries : location transparency => scalable (compute & complexity)

Akka: A toolkit and runtime for building concurent, distributed and fault tolerant applications on the JVM
Concurency with the actor model
Has three traits: 
    Behaviour: how to react to events
    State model:
    Send and receive (immutable) messages asynchronously
Also supports exception handling, fault tolerance (let it crash ?) and location transparency (abstract away where the actor is !).

## Reactive concepts in Akka
Akka streams API is decoupled from the Reactive Streams JVM interfaces altough it uses them internally for interoperability's sake.
https://doc.akka.io/docs/akka/current/stream/index.html

Entities
    Source (Publisher)
    Sink (Subscriber)
    Flow (Processor)
    Example:
        // Materializer and ActorSystem are the stream/actor setup
        final ActorSystem system = ActorSystem.create(Example"); // Actory stem config
        final Materialiter materializer = ActorMaterialiser.create(system); // Factory for stream execution engine (makes the stream run)

        // Integer is the output type of the (lazy) source. It does not produce auxiliary info (i.e NotUSed)
        final Source <Integer, NotUsed> source = source
            .range(1,100)
            .map( int -> int*2);

        // Sink runForEach consumes the data produced by the source.
        // Attaching source and sink with runForEach() produces a RunnableFlow
        CompletionStage<Done> sink =  source.runForEach(i -> System.out.println(i), materializer)
    
    To transform into the classical Reactive Stream api:
        Sink.asSubscriber(true)
        Source.asPublisher(true)

## Graphs
Graph
    Directed, Multiple inputs, Multiple outputs
    Combination of flows, where a flow is a linear connections (single source & sink pairs with sequential linear processing stages in the between)
    Junctions are multiple flows connected at a single sink/source point.

Building the graph:
    Fan-in : Multiple sources/inputs to a single output/sink
        Merge<In>  // randomly pick from input to produce output
        MergePReffered<In> // same as Merge but picks from prefered port in priority
        ZipWith<A,B,...,Out> // takes function with input N-tuple from N inputs and produces one output element
        Zip<A,B> // takes separate a and b inputs and emits pair (a,b) 
        Concat<A> // combines two streams as ???
    Fan-out : Single source/input to multiple ouptut/sink
        Broadcast<T> // Receives on einput and produces N outputs (one for each output port/sink)
        Balance<T> // Receives one input and forwards it to one of its N output port/sink
        UnzipWith<In,A,B,...> // Receives an input applies a function which returns a (max 20) tuple to emit each tuple value to each of its respective port
        Unzip<A,B> // Split pair into two separate ports

    Example graph building (Java): https://doc.akka.io/docs/akka/current/stream/stream-graphs.html
        - Elegant and concise in scala... but verbose and hard to build in java 

## Error handling
The error handling behaviours can be defined with the materializer
These strategies are inspired by actor supervision strategies:
    Stop : Completes stream with failure => Is used by default
    Resume : Element having provoked the error is dropped and the stream continues
    Start : Element having provoked the error is dropped and the stream stage restarts (clears stage state)
Pass the error handling in the materializer
    depending on the exception, decide with the Supervision class to resume, stop or restart the processing
    WARNING: droping elements can cause dedlocks in graphs with cycles.

Error VS Failure (reactive manifesto)
    Errors are accessible within the stream as normal data element (can choose what to do try/catch-like to avoid propagation)
    Failures are not accessible within the stream and just make the stream fail (cannot handle in-stream)
        but can access remaining elements in implicit or explicit buffers.



# 7. Real-Life Reactive Application
Forex RESTful web service : exchange rate info service
    1) rate of two currencies: GET /rates/EUR/USD; return rate
    2) currency stronger than last month: GET /stronger/EUR/USD; return boolean

External API to get the currencies: fixer.io // free and no auth

Exercice solutions:
 https://github.com/manuelvicnt/reactivejava9
 with different git tags of development steps (one step per video)

Tomcat start/stop:
> /Library/Tomcat/bin/startup.sh # http://localhost:8080, V9.0.38
> /Library/Tomcat/bin/shutdown.sh

## Introducing Spring and Jersey

JAX-RS
    Java API for RESTful web services ( javax.ws.rs.* package )
    Annotations: @Path, @GET, @PUT, @POST, ...

Jersey:
    SUN's Framework implementation of JAX-RS specifications
    several components:
        Core server: Build Restful services with JAX-RS annotations
        Core clients: communicate with other REST services
        JSON support
    Open source
    We will only use core-server

Spring
    Application Framework and inversion of control container (depenency injection and class instantiations)
    Open source
    functionnality :
        Core can be used in any application : depenency injection
        Web extension: RESTful (like Jersey), Authentication, Authorization, Data access (ORM, DB access, ...)

## Importing libraries and the project
export JAVA_HOME with target JDK 8
set pom.xml to jdk 1.8
In project folder:
    mvn clean package && cp target/ROOT.war /Library/Tomcat/webapps && /Library/Tomcat/bin/catalina.sh run

## Communicating with external APIs
Outdated approach:
Wraps syhnchronous Java HTTP/1.2 client call to external API with JavRX to make it asynchronous.... returns Single<T>
Mostly sync to async boilerplate code due to poor HTTP client
=> WsClient from Playframework can do this transparently

Observable.zip() used to combine two asynchronous calls to third party REST APIs
=> Java's concurent library (like CompletionStage/CompletableFuture.allOf())


## Responding to the client

Outdated approach:
Uses JavaRx to read/SingleObserver data from foreign system and binds the results to the JAX-RS AsyncResponse : Uses lots of async boilerplate code transparently. Now handled in the Playframework async RESTfull controllers automatically dealing with CompletableFuture results from WsClient results

Use/Throw custom exception and exception-mapper to HTTP response error mapper
=> Generic handling cleaner than returning manually error code without throwing

## Unit testing
nil