package com.digitalpanda.scala.playground

import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

import com.digitalpanda.scala.playground.HelloWorld.chapterSeparator

import scala.collection.immutable.TreeSet
import scala.collection.{LinearSeq, SortedSet, mutable}
import scala.reflect.ClassTag

object HelloWorld24 {

  def main(args: Array[String]): Unit = {
    chapterSeparator(chapter_24_the_scala_collections_api,24)( args )
  }

  def chapter_24_the_scala_collections_api(args: Array[String]): Unit = {
    /*
    Easy to use: A small vocabulary of twenty to fifty methods is enough to
        solve most collection problems in a couple of operations.
    Concise: You can achieve with a single word what used to take one or
        several loops.
    Safe: The statically typed and functional nature of Scala’s collections means
        that the overwhelming majority of errors you might make are caught at
        compile-time.
    Fast: Collection operations are tuned and optimized in the libraries.
        What’s more, collections are currently being adapted to parallel
        execution on multi-cores: https://docs.scala-lang.org/overviews/parallel-collections/overview.html
    Universal: Collections provide the same operations on any type where it
        makes sense to do so.
     */
    //=> 24.0 Parallel collections
    //As a general heuristic, speed-ups tend to be noticeable when the size of
    // the collection is large, typically several thousand elements.
    val list = (1 to 10000).toList
    list.map(_ + 42)
    //Parallel version then again converted to sequential version
    list.par.map(_ + 42).seq
    //Collections that are inherently sequential (in the sense that the elements
    // must be accessed one after the other), like lists, queues, and streams,
    // are converted to their parallel counterparts by copying the elements into
    // a similar parallel collection.
    //Conceptually, Scala’s parallel collections framework parallelizes an
    // operation on a parallel collection by recursively “splitting” a given
    // collection, applying an operation on each partition of the collection in
    // parallel, and re-“combining” all of the results that were completed in parallel.
    // => Side-effecting operations can lead to non-determinism (race condition on external variables)
    // => Non-associative (grouping of) operations lead to non-determinism => temporal order of operations should not matter
    // => Non-commutative operations are acceptable
    //The “out of order” semantics of parallel collections only means that
    // the operation will be executed out of order (in a temporal sense.
    // That is, non-sequentially), it does not mean that the result will
    // be re-“combined” out of order (in a spatial sense). On the contrary,
    // results will generally always be reassembled in order– that is, a
    // parallel collection broken into partitions A, B, C, in that order,
    // will be reassembled once again in the order A, B, C.
    /*
      1 - 2 -3 -4

      1-2    3 -4  |  1 -2 -3    -4

      -1     -1    |  -4         -4

      -2          !=  -8
    */

    val list1 = (1 to 1000).toList

    println("Side effect not ok for par collections: ")
    var sum = 0
    list1.par.foreach(sum += _)
    println("list1.par.foreach(sum += _):> " + sum)
    sum = 0
    list1.par.foreach(sum += _)
    println("list1.par.foreach(sum += _):> " + sum)

    println("Non associative not ok for par collections: ")
    println("list.par.reduce(_-_):> " + list1.par.reduce(_-_))
    println("list.par.reduce(_-_):> " + list1.par.reduce(_-_))
    println("list.par.reduce(_-_):> " + list1.par.reduce(_-_))
    println("list.par.reduce(_-_):>"  + list1.par.reduce(_-_))

    //=> 24.1 Mutable and immutable collections
    //A collection in package scala.collection.mutable is known to have
    // some operations that change the collection in place.
    //A collection in package scala.collection.immutable is guaranteed
    // to be immutable for everyone. Such a collection will never change after
    // it is created.
    //A collection in package scala.collection can be either mutable or im-
    // mutable. By default, Scala always picks immutable collections
    //The last package in the collection hierarchy is collection.generic .
    // This package contains building blocks for implementing collections. Typ-
    // ically, collection classes defer the implementations of some of their opera-
    // tions to classes in generic

    //=> 24.2 Collections consistency
    //-> Every kind of collection can be created by the same uniform syntax, writing the
    // collection class name followed by its elements:
    Traversable(1, 2, 3)
    Iterable("x", "y", "z")
    Map("x" -> 24, "y" -> 25, "z" -> 26)
    Set(Color.RED, Color.GREEN, Color.BLUE)
    SortedSet("hello", "world")
    mutable.Buffer(1, 2, 3)
    IndexedSeq(1.0, 2.0)
    LinearSeq(40L, 41L, 42L)
    /*
    Traversable
      Iterable
        Seq
          IndexedSeq
            Vector
            ResizableArray
            GenericArray
          LinearSeq
            MutableList
            List
            Stream
          Buffer
            ListBuffer
            ArrayBuffer
        Set
          SortedSet
            TreeSet
          HashSet (mutable)
          LinkedHashSet
          HashSet (immutable)
          BitSet
          EmptySet, Set1, Set2, Set3, Set4
        Map
          SortedMap
            TreeMap
          HashMap (mutable)
          LinkedHashMap (mutable)
          HashMap (immutable)
          EmptyMap, Map1, Map2, Map3, Map4

          Figure 24.1
     */
    //-> The same principle also applies for specific collection implementations
    // The toString methods for all collections produce output written as above,
    // with a type name followed by the elements of the collection in parentheses.
    // All collections support the API provided by Traversable , but their meth-
    // ods all return their own class rather than the root class Traversable .
    //-> Equality is also organized uniformly for all collection classes;
    //-> Most of the classes in Figure 24.1 exist in three variants: root, mutable,
    // and immutable. The only exception is the Buffer trait, which only exists as
    // a mutable collection.

    //=> 24.3 Trait Traversable
    //At the top of the collection hierarchy is trait Traversable.
    //-> Its only abstract operation is foreach:
    //  def foreach[U](f: Elem => U)
    //  The invocation of f is done for its side effect only; in fact any
    //  function result of f is discarded by foreach.
    //-> Traversable also defines many concrete methods, which are all listed
    //    in Table 24.1 on page 539.
    //--> ADDITION ++ : appends two traversables together, or appends all ele-
    //    ments of an iterator to a traversable.
    //--> MAP operations map , flatMap , and collect , which produce a new collec-
    //    tion by applying some function to collection elements.
    //--> CONVERSIONS toIndexedSeq , toIterable , toStream , toArray , toList ,
    //    toSeq , toSet , and toMap , which turn a Traversable collection into a
    //    more specific collection.
    //--> COPYING operations copyToBuffer and copyToArray . As their names im-
    //    ply, these copy collection elements to a buffer or array, respectively.
    //--> SIZE operations isEmpty , nonEmpty , size , and hasDefiniteSize . Col-
    //    lections that are traversable can be finite or infinite.
    //--> ELEMENT RETRIEVAL operations head , last , headOption , lastOption , and
    //    find .
    //    A collection is ordered if it always yields its elements in the same order.
    //--> SUBCOLLECTION RETRIEVAL operations takeWhile , tail , init , slice , take ,
    //    drop , filter , dropWhile , filterNot , and withFilter . These all
    //    return some subcollection identified by an index range or a predicate.
    //--> SUBDIVISION OPERATIONS  splitAt , span , partition , and groupBy , which
    //    split the elements of this collection into several subcollections.
    //--> ELEMENT TESTS exists , forall , and count , which test collection elements
    //    with a given predicate.
    //--> FOLDS foldLeft , foldRight , /: , :\ , reduceLeft , reduceRight , which
    //    apply a binary operation to successive elements.
    //--> SPECIFIC FOLDS sum , product , min , and max , which work on collections of
    //    specific types (numeric or comparable).
    //--> STRING OPERATIONS mkString , addString , and stringPrefix , which pro-
    //    vide alternative ways of converting a collection to a string.
    //--> VIEW operations consisting of two overloaded variants of the view method.
    //    A view is a collection that’s evaluated lazily.

    //=> 24.4 Trait Iterable
    //All methods in this trait are defined in terms of an an abstract method,
    // iterator , which yields the collection’s elements one by one.
    // The foreach method from trait Traversable is implemented in Iterable in
    // terms of iterator:
    class Elem;
    def foreach[U](f: Elem => U): Unit = {
      //val it = iterator
      //while (it.hasNext) f(it.next())
    }
    //Quite a few subclasses of Iterable override this standard implementation
    // of foreach in Iterable , because they can provide a more efficient imple-
    // mentation. Remember that foreach is the basis of the implementation of all
    // operations in Traversable , so ITS PERFORMANCE MATTERS !
    //Two more methods exist in Iterable that return iterators: grouped and
    // sliding. The grouped method chunks its elements into increments,
    // whereas sliding yields a sliding window over the elements:
    val xs = List(1, 2, 3, 4, 5)
    val git = xs grouped 3
    println("\ngit = xs grouped 3:> " + git)
    println("git.next():> " + git.next())
    println("git.next():> " + git.next())

    val sit = xs sliding 3
    println("\nsit = xs sliding 3:> " + sit)
    println("sit.next():> " + sit.next())
    println("sit.next():> " + sit.next())

    //Trait Iterable also adds some other methods to Traversable that can be
    // implemented EFFICIENTLY only if an iterator is available:
    //-> Iterators: iterator, grouped, sliding
    //-> Sub-collections: takeRight, takeLeft
    //-> Zippers: zip, zipAll, zipWithIndex
    //-> Comparision: sameElements
    //One reason for having Traversable is that sometimes it is
    // easier or more efficient to provide an implementation of foreach than to
    // provide an implementation of iterator.

    //In the inheritance hierarchy below Iterable you find three traits: Seq , Set ,
    // and Map . A common aspect of these three traits is that they all implement the
    // PartialFunction trait with its apply and isDefinedAt methods.
    //-> For sequences, apply is positional indexing, where elements are always
    //   numbered from 0 . That is, Seq(1, 2, 3)(1) == 2 .
    //-> For sets, apply is a
    //   membership test. For instance, Set('a', 'b', 'c')('b') == true whereas
    //   Set()('a') == false.
    //-> For maps, apply is a selection. For instance, Map('a' -> 1, 'b' -> 10, 'c' -> 100)('b') == 10 .
    println("Seq(4, 25, 42)(1) :> "
      + Seq(1, 25, 42)(1))
    println("Set('a', 'b', 'c')('b') :> "
      + Set('a', 'b', 'c')('b'))
    println("Map('a' -> 1, 'b' -> 10, 'c' -> 100)('b') :> "
      + Map('a' -> 1, 'b' -> 10, 'c' -> 100)('b'))

    //=> 24.5 The sequence traits Seq , IndexedSeq , and LinearSeq
    //The Seq trait represents sequences. A sequence is a kind of iterable that has
    // a length and whose elements have fixed index positions, starting from 0:
    //-> INDEXING AND LENGTH operations apply , isDefinedAt , length , indices ,
    //    and lengthCompare
    //-> INDEX SEARCH operations indexOf , lastIndexOf , indexOfSlice , lastIn -
    //   dexOfSlice , indexWhere , lastIndexWhere , segmentLength , and
    //   prefixLength , which return the index of an element equal to a given
    //   value or matching some predicate.
    //-> ADDITION operations +: , :+ , and padTo , which return new sequences ob-
    //   tained by adding elements at the front or the end of a sequence.
    //-> UPDATE operations updated and patch , which return a new sequence ob-
    //   tained by replacing some elements of the original sequence
    //-> SORTING operations sorted , sortWith , and sortBy , which sort sequence
    //   elements according to various criteria.
    //-> REVERSAL operations reverse , reverseIterator , and reverseMap , which
    //   yield or process sequence elements in reverse order, from last to first
    //-> COMPARISION operations startsWith , endsWith , contains , corresponds ,
    //   and containsSlice , which relate two sequences or search an element
    //   in a sequence.
    //-> MULTISET operations intersect , diff , union , and distinct , which per-
    //   form set-like operations on the elements of two sequences or remove
    //   duplicates. If multiple time the same value, return as many same
    //   values as there are in both sequences.
    //Syntax like seq(idx) = elem is just a shorthand for seq.update(idx, elem)
    //Each Seq trait has two subtraits, LinearSeq and IndexedSeq . These do
    //not add any new operations, but each offers different performance charac-
    //teristics.
    // - A linear sequence has efficient head and tail operations.
    // - An indexed sequence has efficient apply , length , and (if mutable) update zoohoperations
    //Two frequently used indexed sequences are Array and ArrayBuffer . The Vector
    //class provides an interesting compromise between indexed and linear access.
    //It has both effectively constant time indexing overhead and constant time lin-
    //ear access overhead. Because of this, vectors are a good foundation for mixed
    //access patterns where both indexed and linear accesses are used.

    //BUFFERS
    //An important sub-category of mutable sequences is buffers. Buffers allow
    // not only updates of existing elements but also element insertions, element
    // removals, and efficient additions of new elements at the end of the buffer.
    //The principal new methods supported by a buffer are += and ++= , for element
    // addition at the end, +=: and ++=: for addition at the front, insert and
    // insertAll for element insertions, as well as remove and -= for element
    // removal.

    //=> 24.6 Sets
    //Operation categories:
    //-> TESTS contains , apply , and subsetOf.
    //-> ADDITIONS + and ++ , which add one or more elements to a set, yielding a new
    //   set as a result.
    //-> REMOVALS - and -- , which remove one or more elements from a set, yielding
    //   a new set.
    //-> SET OPERATIONS for union, intersection, and set difference.
    //   The alphabetic versions are intersect , union , and diff , whereas
    //   the symbolic versions are & , | , and & ~ .
    // Mutable sets have methods that add, remove, or update elements
    //The choice of the method names += and -= means that very similar code
    // can work with either mutable or immutable sets.
    var s = Set(1, 2, 3);s += 4; s -= 2
    println("var s = Set(1, 2, 3);s += 4; s -= 2 :> " + s)
    val sMut = mutable.Set(1, 2, 3);sMut += 4; sMut -= 2
    println("val sMut = mutable.Set(1, 2, 3);sMut += 4; sMut -= 2:> " + sMut)
    //Comparing the two interactions shows an important principle. You often
    // can replace a mutable collection stored in a val by an immutable collection
    // stored in a var , and vice versa.
    //The current default implementation of a mutable set uses a hash table
    // to store the set’s elements.
    //Beyond four elements, immutable sets are implemented as hash tries. 2

    //Sorted sets
    //Elements are traversed in sorted order.
    //The default representation of a SortedSet is an ordered binary tree
    // maintaining the invariant that all elements in the left subtree of a
    // node are smaller than all elements in the right subtree.
    // Scala’s class immutable.TreeSet uses a red-black tree
    // implementation to maintain this ordering invariant, and at the same time keep
    // the tree balanced.
    //Sorted sets also support ranges of elements.
    val set = TreeSet.empty[String]
    val numbers = set + ("one", "two", "three", "four")
    println("numbers range (\"one\", \"two\") :>" + (numbers range ("one", "two")))
    println("numbers from \"three\" :>" + (numbers from "three"))

    //Bit sets
    //Bit sets are sets of non-negative integer elements that are implemented in one
    // or more words of packed bits. It follows that the size of a bit set
    // depends on the largest integer that’s stored
    // in it.

    //=> 24.7 Maps
    //Scala’s Predef class offers an implicit conversion that lets you write key
    // -> value as an alternate syntax for the pair (key, value).
    //The fundamental operations on maps are similar to those of sets.
    //Map operations fall into the following categories:
    //-> LOOKUPS apply , get , getOrElse , contains , and isDefinedAt
    //   Maps also define an apply method that returns the value associated
    //   with a given key directly, without wrapping it in an Option .
    //   If the key is not defined in the map, an exception is raised
    //-> ADDITIONS and UPDATES + , ++ , and updated , which let you add new bindings
    //   to a map or change existing bindings.
    //-> REMOVALS - and -- , which remove bindings from a map.
    //-> SUBCOLLECTION producers keys , keySet , keysIterator , valuesIterator ,
    //   and values
    //-> TRANSFORMATIONS filterKeys and mapValues , which produce a new map
    //   by filtering and transforming bindings of an existing map.
    //The getOrElseUpdate is useful for accessing mutable maps that act as caches
    // In the case where, update is expensive as the second argument to g
    // etOrElseUpdate is “by-name,”

    //=> 24.8 Synchronized sets and maps
    //If you needed a thread-safe map, you could mix the SynchronizedMap
    // trait into whatever particular map implementation you desired:
    object MapMaker {
      def makeMap: mutable.HashMap[String, String] with mutable.SynchronizedMap[String, String] {
        def default(key: String): String
      } = {
        new mutable.HashMap[String, String] with
          mutable.SynchronizedMap[String, String] {
          override def default(key: String) =
            "Why do you want to know?"
        }
      }
    }
    //<=>
    new ConcurrentHashMap[String,String]()
    //Alternatively, you may prefer to use unsynchronized collections
    //with Scala actors. Actors will be covered in detail in Chapter 32.


    //=> 24.9 Concrete IMMUTABLE collection classes
    //Update operations require deep copy of several elements pointing towards the updated field
    // As a result cost is multiplied by a "big" constant when compared to their mutable counterpart....

    //LISTS:
    //Lists are finite immutable sequences. They provide constant-time access to
    // their first element as well as the rest of the list, and they have a constant-time
    // cons operation for adding a new element to the front of the list
    // Accessing or modifying elements later in the list takes time linear in
    // the depth into the list.

    //STREAMS:
    //A stream is like a list except that its elements are computed lazily. Because
    // of this, a stream can be infinitely long. Only those elements requested will
    // be computed. Otherwise, streams have the same performance characteristics
    // as lists.
    val str = 1 #:: 2 #:: 3 #:: Stream.empty
    def fibFrom(a: Int, b: Int): Stream[Int] =
      a #:: fibFrom(b, a + b)
    // <- Since it uses #:: , though, the right-hand side is not evaluated
    //     until it is requested!
    val fibs = fibFrom(1, 1).take(7)

    //VECTORS:
    //(Constant time operations can be up to 32 * more expensive than the ones from arrays)
    // Access to any elements of a vector take only “effectively constant time,
    // ” as defined below. It’s a larger constant than for access to the head of
    // a list or for reading an element of an array, but it’s a constant nonetheless.
    //Vectors are represented as broad, shallow trees. Every tree node contains
    // up to 32 elements of the vector or contains up to 32 other tree nodes
    //Vectors are immutable, so you cannot change an element of a vector in
    // place. However, with the updated method you can create a new vector that
    // differs from a given vector only in a single element.
    //Updating an element in the middle of a vector can be done by
    // copying the node that contains the element, and every node that points to it,
    // starting from the root of the tree. This means that a functional update creates
    // between one and five nodes that each contain up to 32 elements or subtrees.
    //They are currently the default implementation of immutable indexed sequences.

    //IMMUTABLE STACKS:
    //You push an element onto a stack with push ,
    // pop an element with pop , and peek at the top of the stack without
    // removing it with top . All of these operations are constant time. (same as list)

    //IMMUTABLE QUEUE:
    // ...

    //RANGES: A range is an ordered sequence of integers that are equally spaced apart.
    println("5 to 14 by 3 :> " + (5 to 14 by 3))
    println("1 until 3 :> " + (1 until 3))
    //Ranges are represented in constant space.
    //Most operations on ranges are extremely fast.

    //HASH TRIES:
    //Their representation is similar to vectors in that they are also trees
    // where every node has 32 elements or 32 subtrees, but selection is done based
    // on a hash code. For instance, to find a given key in a map, you use the lowest
    // five bits of the hash code of the key to select the first subtree, the next five
    // bits the next subtree, and so on. Selection stops once all elements stored in a
    // node have hash codes that differ from each other in the bits that are selected
    // so far. https://en.wikipedia.org/wiki/Hash_array_mapped_trie
    //They underlie Scala’s default implementations of immutable maps and sets.

    //RED-BLACK TREES:
    //Red-black trees are a form of balanced binary trees
    // Like any balanced binary tree, operations on them reliably complete in
    // time logarithmic to the size of the tree.
    //Red-black trees are also the standard implementation of SortedSet in Scala,
    // because they provide an efficient iterator that returns all elements of the set
    // in sorted order.

    //IMMUTABLE BIT SETS:
    //A bit set represents a collection of small integers as the bits of a
    // larger integer. Internally, bit sets use an array of 64-bit Long s.
    //Testing for inclusion takes constant time. Adding an item to the set
    // takes time proportional to the number of Long s in the bit set’s array.

    //LIST MAPS:
    //A list map represents a map as a linked list of key-value pairs.
    // Practical only if the map is for some reason constructed in such a way that
    // the first elements in the list are selected much more often than the other elements.

    //=> 24.10 Concrete MUTABLE collection classes
    //ARRAY BUFFERS:
    //Most operations on an array buffer have the same speed
    // as an array, because the operations simply access and modify the underlying
    // array. Additionally, array buffers can have data efficiently added to the end.
    // Appending an item to an array buffer takes amortized constant time.
    // Array buffers are useful for efficiently building up a large collection whenever
    // the new items are always added to the end.

    //LIST BUFFERS:
    //A list buffer is like an array buffer except that it uses a linked list
    // internally instead of an array. If you plan to convert the buffer to
    // a list once it is built up, use a list buffer instead of an array buffer.

    //STRING BUILDERS:
    //...

    //LINKED LISTS:
    //Empty linked lists are encoded instead in a special way:
    // Their next field points back to the node itself. Like their immutable cousins,
    // linked lists are best operated on sequentially.

    //DOUBLE LINKED LISTS:
    //The main benefit of that additional link is that it makes element
    // removal very fast.

    //MUTABLE LISTS:
    //A MutableList consists of a single linked list together with a pointer that
    // refers to the terminal empty node of that list. This makes list append a
    // constant time operation.

    //QUEUES:
    //...

    //ARRAY SEQUENCES:
    //You would typically use an ArraySeq if you want an array for its per-
    // formance characteristics, but you also want to create generic instances of the
    // sequence where you do not know the type of the elements and do not have
    // a ClassManifest to provide it at run-time.

    //STACKS:
    //It works exactly the same as the immutable version except that modifications
    // happen in place.

    //ARRAY STACKS:
    //ArrayStack is an alternative implementation of a mutable stack, which is
    // backed by an Array that gets resized as needed. It provides fast indexing
    // and is generally slightly more efficient for most operations than a normal
    // mutable stack.

    //HASH TABLES:
    //A hash table stores its elements in an underlying array, placing each item at
    // a position in the array determined by the hash code of that item.
    // Hash tables are thus very fast so long as the objects placed in them
    // have a good distribution of hash codes. As a result, the default mutable
    // map and set types in Scala are based on hash tables. Iteration
    // simply proceeds through the underlying array in whichever order it happens
    // to be.

    //LINKED HASH map/set
    //A linked hash map or set is just like a regular hash map or set except that
    // it also includes a linked list of the elements in the order they were added.
    // Iteration over such a collection is always in the same order that the
    // elements were initially added.

    //WEAK HASH MAPS
    //A weak hash map is a special kind of hash map in which the garbage collector
    // does not follow links from the map to the keys stored in it. This means that a
    // key and its associated value will disappear from the map if there is no other
    // reference to that key !
    //Weak hash maps are useful for tasks such as caching. If keys and function
    // results are stored in a regular hash map, the map could grow without bounds,
    // and no key would ever become garbage. Weak hash maps in Scala are
    // implemented as a wrapper of an underlying Java implementation, java.util.WeakHashMap.

    //CONCURRENT MAPS
    //A concurrent map can be accessed by several threads at once. In addition to
    // the usual Map operations, it provides the following atomic operations.
    //Currently, its /only implementation is Java’s java.util.concurrent.ConcurrentMap

    //MUTABLE BIT SETS
    //A mutable bit set is just like an immutable one, except that it can be mod-
    // ified in place. Mutable bit sets are slightly more efficient at updating than
    // immutable ones, because they don’t have to copy around Long s that haven’t
    // changed !!!


    //=> 24.11 Arrays
    //Scala arrays correspond one-to-one to Java arrays.
    //- That is, you can have an Array[T] , where T is a type parameter or abstract type.
    //- Second, Scala arrays are compatible with Scala sequences—you can pass an Array[T]
    // where a Seq[T] is required.
    //- Finally, Scala arrays also support all sequence operations.
    //The Scala 2.8 array implementation makes systematic use of implicit
    // conversions. There is an implicit “wrapping” conversion between
    // arrays and instances of class scala.collection.mutable.WrappedArray ,
    // which is a subclass of Seq .
    val a1 = Array(1, 2, 3)
    println("a1 = Array(1, 2, 3) type :> " + a1.stringPrefix)
    val seq: Seq[Int] = a1
    println("seq: Seq[Int] = a1 type :> " + seq.stringPrefix)
    println("seq.toArray eq a1 :> " + (seq.toArray eq a1))
    //There is yet another implicit conversion that gets applied to arrays. This
    // conversion simply “adds” all sequence methods to arrays but does not turn
    // the array itself into a sequence. “Adding” means that the array is wrapped
    // in another object of type *ArrayOps , which supports all sequence methods.
    println("a1.reverse :>" + a1.reverse)
    println("intArrayOps(a1).reverse :>" + intArrayOps(a1).reverse)
    //The ArrayOps conversion has a higher priority
    // than the WrappedArray conversion. The first is defined in the Predef object
    // whereas the second is defined in a class scala.LowPriorityImplicits ,
    // which is a superclass of Predef . Implicits in subclasses and sub-objects take
    // precedence over implicits in base classes.
    //Genericity: AnyRef is the type to which the Scala compiler maps Array[T].
    //At run-time, when an element of an ar ray of type Array[T] is accessed or
    // updated there is a sequence of type tests that determine the actual array
    // type, followed by the correct array operation on the Java array.
    // These type tests SLOW down ARRAY OPERATIONS somewhat. You can expect accesses
    // to generic arrays to be three to four times slower than accesses to primitive
    // or object arrays.
    //Create a generic array:
    //Depending on the actual type parameter, the runtime representation might differ
    //What’s required here is that you help the compiler by providing a runtime
    // hint of what the actual type parameter of evenElems is. This runtime hint
    // takes the form of a class manifest

    //def evenElems[T](xs: Vector[T])(implicit m: ClassTag[T]): Array[T] = {
    // <=>
    def evenElems[T: ClassTag](xs: Vector[T]): Array[T] = { // <-demand that the type comes with a class manifest by using a context bound
      val arr = new Array[T]((xs.length + 1) / 2)
      for (i <- xs.indices by 2)
        arr(i / 2) = xs(i)
      arr
    }
    evenElems(Vector(1, 2, 3, 4, 5))

    //=> 24.12 Strings
    //Like arrays, strings are not directly sequences, but they can be converted to
    // them, and they also support all sequence operations.
    // These operations are supported by two implicit conversions similar to those of Array.

    //=> 24.12 Performance characteristics
    //LUT for collection choice depending on the most common usage

    //=> 24.14 Equality
    //The collection libraries have a uniform approach to equality and hashing.
    // The idea is, first, to divide collections into sets, maps, and sequences.
    // - Collections in different categories are always unequal.
    // - Within the same category, collections are equal iif they have the same elements.

    //=> 24.15 Views
    //Collections have quite a few methods that construct new collections. Some
    // examples are map , filter , and ++ . We call such methods transformers be-
    // cause they take at least one collection as their receiver object and produce
    // another collection in their result.
    //A strict transformer constructs a new collection with all of its
    // elements.
    //A non-strict, or lazy, transformer constructs only a proxy for the
    // result collection, and its elements are constructed on demand.
    //The given function f is instead applied to the elements of the new collection’s iterator as they are demanded.
    def lazyMap[T, U](coll: Iterable[T], f: T => U) =
      new Iterable[U] {
        def iterator = coll.iterator map f
      }
    val res = lazyMap(1::2::3::Nil, (x: Int) =>  x + 1)
              .filter( _ % 2 == 1)
    println("Lazymap example: " + res)

    //Scala collections are by default strict in all their transformers, except
    // for Stream , which implements all its transformer methods lazily.

    //A VIEW is a special kind of collection that represents some base collection,
    // but implements all of its transformers lazily.
    //To go from a collection to its view, you can use the view method on the
    // collection.
    val v = Vector(1 to 10: _*)
    val resV = v map (_ + 1) map (_ * 2)
    //In the last statement, the expression v map (_ + 1) constructs a new vector
    // that is then transformed into a third vector by the second call to map (_ * 2) .
    //A more general way to avoid the intermediate results
    // is by turning the vector first into a view, applying all transformations to the
    // view, and finally forcing (compute/materialize) the view to a vector:
    val resF = (v.view map (_ + 1) map (_ * 2)).force
    //Both stored functions (map, map) get applied as part of the execution of the force opera-
    // tion and a new vector is constructed. That way, no intermediate data structure
    // is needed....
    //One detail to note is that the static type of the final result is a Seq , not a Vector .
    // Indeed, the Scala collection libraries provide views mostly only for general
    // collection types, not for specific implementations.
    //There are two reasons why you might want to consider using views.
    // -> The first is performance (do not materialize intermediate collections at each step)
    //Why have strict collections at all ?
    // - For smaller collection sizes the added overhead of forming and applying
    //   closures in views is often greater than the gain from avoiding the intermediary
    //   data structures.
    // - A possibly more important reason is that evaluation in views can
    //   be very confusing if the delayed operations have side effects
    //   => It is executed as long as the view is not instructed to be materialized.
    //The only way to go from a strict to a lazy collection is via the view method.
    //The only way to go back is via force.
    //In summary, views are a powerful tool to reconcile concerns of efficiency
    // with concerns of modularity. But in order not to be entangled in aspects of
    // delayed evaluation, you should restrict views to two scenarios:
    // 1) Either you apply views in purely functional code where collection
    // transformations do not have side effects.
    // 2) Or you apply them over mutable collections where all
    //  modifications are done explicitly.

    //=> 24.16 Iterators
    //An iterator is not a collection, but rather a way to access the elements of
    // a collection one by one. The two basic operations on an iterator it are
    // next and hasNext
  }
}
