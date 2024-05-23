import fs2.*
val s:  Stream[Pure, Int] = Stream(1,2,3)
val s1 = Stream(2,3,4,5)
val s2 : Stream[Pure, Int] = Stream.empty
val s3 = Stream.emit(45)
val s4 = Stream.emits(List(1,2,3,4))

val nats: Stream[Pure, Int] = Stream.iterate(1)(_ + 1)
val s5 = Stream.unfold(1)(s => Some((s, s+1)))
val s6 = Stream.unfold(1)(s => if s > 6 then None else Some((s, s+1)))
val s7 = Stream.range(1, 15)
val s8 = Stream.constant(45) // infinite stream of

s.toList
s2.toList
s3.toList
s4.toList
nats.take(5).toList
s5.take(5).toList
s6.toList
s7.toList
s8.take(5).toList
/**
 * Excerise
 *
 * Stream('a', 'b', ...)
*/

//using iterate
def letters: Stream[Pure, Char] =
  Stream.iterate('a')(c => (c.toInt + 1).toChar)

letters.take(26).toList
// using unfold
def lettersV2: Stream[Pure, Char] =
  Stream.unfold('a')(s => if s == 'z' + 1 then None else Some((s, (s + 1).toChar)))

lettersV2.toList

//Exercise implement iterate using unfold
def myIterate[A](initial: A)(next: A => A): Stream[Pure, A] =
  Stream.unfold(initial)(s => Some((s, next(s))))

myIterate(1)(_+1).take(10).toList

(s ++ s1).toList
(s ++ nats).take(15).toList

//map
val doubled = s1.map(_*2)
val evens = nats.map(_ * 2 )
//flatMap
s.flatMap(i => Stream(i, i + 1)).toList

//filter
val odds = nats.filter(_ % 2 != 0)


val pairs =
  for
    n <- nats
    e <- evens.take(3)
  yield (n,e)


pairs.take(15).toList

//zip
val oePairs = odds.zip(evens).take(5).toList

nats.zip(s1).toList

//zipWith
Stream.constant(2).zipWith(nats)(_*_).take(10).toList

//fold
val length = s.fold(0)((a,_) => a + 1).toList
val sum = nats.take(5).fold(0)(_+_).toList

// implement repeat
def repeat[A](s: Stream[Pure, A]): Stream[Pure, A] =
  s ++ repeat(s)
repeat(s).take(15).toList

//Exercise 3
//unNone(Stream(Some(1), None, Some(2))) == Stream(1,2)
def unNone[A](s: Stream[Pure, Option[A]]): Stream[Pure, A] =
  s.filter(_.isDefined).map(_.get)

def unNoneV2[A](s: Stream[Pure, Option[A]]): Stream[Pure, A] =
  for
    elemOpt <- s
    elem <- Stream.fromOption(elemOpt)
  yield elem

unNone(Stream(Some(1), None, Some(2), None, None, Some(3))).toList

unNoneV2(Stream(Some(1), None, Some(2), None, None, Some(3))).toList