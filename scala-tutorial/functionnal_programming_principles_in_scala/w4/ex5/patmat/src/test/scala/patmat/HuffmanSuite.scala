package patmat

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import patmat.Huffman._

@RunWith(classOf[JUnitRunner])
class HuffmanSuite extends FunSuite {
	trait TestTrees {
		val t1 = Fork(Leaf('a',2), Leaf('b',3), List('a','b'), 5)
		val t2 = Fork(Fork(Leaf('a',2), Leaf('b',3), List('a','b'), 5), Leaf('d',4), List('a','b','d'), 9)
	}


  test("weight of a larger tree") {
    new TestTrees {
      assert(weight(t1) === 5)
    }
  }


  test("chars of a larger tree") {
    new TestTrees {
      assert(chars(t2) === List('a','b','d'))
    }
  }

  test("times counts correctly") {
    assert( times(List('h', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd')).toSet === List(('h',1), ('e',1), ('l',3), ('o',2), (',',1), (' ',1), ('w',1), ('r',1), ('d',1)).toSet)
  }


  test("string2chars(\"hello, world\")") {
    assert(string2Chars("hello, world") === List('h', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd'))
  }


  test("makeOrderedLeafList for some frequency table") {
    assert(makeOrderedLeafList(List(('t', 2), ('e', 1), ('x', 3))) === List(Leaf('e',1), Leaf('t',2), Leaf('x',3)))
  }

  test("combine of some leaf list") {
    val leaflist = List(Leaf('e', 1), Leaf('t', 2), Leaf('x', 4))
    assert(combine(leaflist) === List(Fork(Leaf('e',1),Leaf('t',2),List('e', 't'),3), Leaf('x',4)))
  }

  test("singleton of multiple trees should return false") {
    new TestTrees {
      val treeList = List(t1, t2, Leaf('z',33))
      assert(singleton(treeList) === false)
    }
  }

  test("singleton of a single tree should return true") {
    new TestTrees {
      val treeList = List(t2)
      assert(singleton(treeList) === true)
    }
  }

  test(" createCodeTree should return optimal tree"){
    new TestTrees {
      private val tree: CodeTree = createCodeTree('a' :: 'b' :: 'a' :: 'b' :: 'b' :: Nil)
      print(tree)
      assert(tree === t1)
    }
  }


  test("decode and encode a very short text should be identity") {
    new TestTrees {
      private val encodedBits: List[Bit] = encode(t2)("abbdbadb".toList)
      System.out.println(encodedBits)
      private val decodedChars: List[Char] = decode(t2, encodedBits)
      System.out.println(decodedChars)
      assert(decodedChars === "abbdbadb".toList)
    }
    System.out.println(decodedSecret)
  }

  test("quick decode and encode a very short text should be identity") {
    new TestTrees {
      private val encodedBits: List[Bit] = quickEncode(t2)("abbdbadb".toList)
      System.out.println(encodedBits)
      private val decodedChars: List[Char] = decode(t2, encodedBits)
      System.out.println(decodedChars)
      assert(decodedChars === "abbdbadb".toList)
    }
    System.out.println(decodedSecret)
  }

}
