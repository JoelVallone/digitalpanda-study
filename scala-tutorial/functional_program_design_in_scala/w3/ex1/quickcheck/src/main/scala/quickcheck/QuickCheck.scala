package quickcheck

import common._
import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

import scala.math.min

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = oneOf(
    const(empty),
    for {
      x <- arbitrary[A]
      h <- oneOf[H](empty, genHeap)
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

  property("find smaller among pair inserted in empty") =
    forAll { (a : A, b: A)  =>
      val h = insert(a, insert(b, empty))
      findMin(h) == min(a, b)
    }

  property("insert & delete into empty should lead empty") =
    forAll { a: A =>
      val h = insert(a, empty)
      deleteMin(h) == empty
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

  property("meld into one should find min among former two heaps") =
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
}
