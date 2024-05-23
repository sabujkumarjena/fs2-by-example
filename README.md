learn fs2 by example

##Pure Strem ##

Finte Streams
-   apply
- empty
- emit
- emits
- range

Infinite streams
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
- 
