package quickcheck

import common._
import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

import scala.math.min
import scala.math.max

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = oneOf(
    const(empty),
    for {
      x <- arbitrary[A]
      h <- oneOf[H](genHeap, genHeap, genHeap, empty)
    } yield insert(x, h)
  )

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") =
    forAll { h: H =>
      val m = if (isEmpty(h)) 0 else findMin(h)
      findMin(insert(m, h)) == m
    }

  property("min1") =
    forAll { a: A =>
      val h = insert(a, empty)
      findMin(h) == a
    }

  def insertAll(a : List[A], h: H): H =
    if (a.isEmpty) h else insertAll(a.tail, insert(a.head,h))

  property("find smaller among list inserted in empty") =
    forAll { l : List[A]  =>
      if (l.isEmpty) true else findMin(insertAll(l,empty)) == l.min
    }


  def deleteMinN(h: H, n: Int): H =
    if (n == 0) h else deleteMinN(deleteMin(h), n-1)

  property("insert & delete all into empty should lead empty") =
    forAll { l : List[A]  =>
      if (l.isEmpty)
        true
      else {
        val h = insertAll(l, empty)
        empty == deleteMinN(h, l.length)
      }
    }

    def toList(heap: H): List[A] = {
      def toListPartial(partialList: List[A], heap: H) : List[A] = {
        if (isEmpty(heap))
          partialList
        else {
          val min = findMin(heap)
          toListPartial(min :: partialList, deleteMin(heap))
        }
      }
      toListPartial(Nil, heap)
    }
    property("empty a heap with min should lead a sorted list") =
      forAll { h: H =>
        val list = toList(h)
        list.sorted == list.reverse
      }

    property("break5: meld into one should find min among former two heaps") =
      forAll { (h2: H, h1: H) =>
        if (isEmpty(h1)) {
          if (isEmpty(h2))
            true
          else
            findMin(meld(h1, h2)) == findMin(h2)
        } else {
          if (isEmpty(h2))
            findMin(meld(h1, h2)) == findMin(h1)
          else
            findMin(meld(h1, h2)) == min(findMin(h2),findMin(h1))
        }
      }

  property("break3: link drops one of the trees instead of ordering the pair") =
    forAll { (a : A, b: A) =>
        val h1 = insert(min(a,b), empty)
        val h2 = insert(max(a,b), empty)
        val h21 = meld(h2, h1)
        val h = deleteMin(h21)
        max(a,b) == findMin(h)
    }

  property("break4: find min returns root of first list element in head rank instead of swipe through list") =
    forAll { (a : A, b: A) =>
      val hMin = insert(min(a,b), empty)
      val hMax2 = insert(max(a,b), empty)
      val hMixedR1 = meld(hMin, hMax2)

      val hMaxR0 = insert(max(a,b), empty)

      max(a,b) == findMin(deleteMin(meld(hMaxR0, hMixedR1)))
    }
}
