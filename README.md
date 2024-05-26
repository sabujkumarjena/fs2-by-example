# learn fs2 by example #

## Pure Strem ##

### Finte Streams ###
-   apply
- empty
- emit
- emits
- range

### Infinite streams ###
- iterate
- unfold
- constant

Combinators
- map
- flatmap
- filter
- zip
- zipWith
- fold
- ++

Streams can be finite or infinite
Streams can be pure or effectful
Pure streams are basically lazy collections

Effectful Stream

Agenda
- Creating effectfull stream
- - From an existing effect(eval & exec) 
- - From a pure stream (covary)
-  - By iterating (iterateEval)
-  - By iterating while keeping a state (unfoldEval)
-  Combinator
-  - repeat
-  - map & flatMap
-  - evalMap, evalTap & evalFilter
-  - ++

- Compiling streams
-  - To get their values (toList)
-  - To get only the effects( drain)
- Error handling
- - raiseError
- - handleErrorWith
- Resources
-  - bracket
-  - resource
- - fromAutoClosable
- Time related methods
- - timeout
- - interruptAfter
- - delayBy
- - metered
- - debounce
- Retry

Streams are MonadErrors

Transforming Streams
Agenda
- Pull based vs Push based
- Chunks
- Pulls
- Pipes

Pull Based Streams
- The stream contains a recipe to produce more elements.
- Element are pulled from the stream when needed.
- When our process needs no more elements, we can stop.

Push Based Stream
- The producer emits elements at its own pace and notifies subscribers.
- The consumers subscribe to updates and provide callbacks

SUMMARY
- fs2 streams are pull based (only emit when asked to)
- Streams emit values in chunks
- Pulls represent processes
- - Useful when transforming streams
- - Useful when transforming streams
-  Pipes are functions from stream to streams
- - The through operator can be used to transform a stream with a pipe

# Concurrency #
## Agenda ##
- Running streams concurrently
- - merge
- - parJoinUnbounded
- - Concurrently
- Processing elements in parallel
- - parEvalMap
- Processing elements in parallel
- - parEvalMap
- Zipping streams
- - zip
- - zipRight
- - parZip
- Timed behaviour
- - fixedRate
- - fixedDelay
- - awakeEvery
- - awakeDelay
### SUMMARY ###
- There are many ways to launch streams concurrently
- Elements in a stream can be processed in parallel
- Zipping can help implement interesting patterns (such as metered)

# COMMUNICATION #
## AGENDA ##
- Signals
- - Interruption
- - Sending values from one stream and consuming from other
- Channels
- - Multiple producers
- - Buffering
- Topic
- - Pub/Sub model
- - Buffering (in each subscriber) 
- Queues
- - Multiple producer / Multiple consumer
- - Buffering
- - Communication with outside world
- - We can connect to the outside world

## SUMMARY ##
- Many ways to send messages between streams
- Buffering helps deal with fast producers / slow consumers
- 