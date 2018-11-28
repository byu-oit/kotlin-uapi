package edu.byu.uapi.utility.collections

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class TypeMapSpec : DescribeSpec(
    {
        describe("getMatching") {
            it("Should get exact matches") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"

                map.getMatching(IfA::class) shouldBe "a"
            }
            it("Should get matches for implemented interfaces") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"
                map.getMatching(object : IfA {}::class) shouldBe "a"
            }
            it("Should get matches for extended classes") {
                val map = TypeMap.create<String>()
                map[AbsA::class] = "a"
                map.getMatching(object : AbsA() {}::class) shouldBe "a"
            }
            it("Should get matches for super-super-interfaces") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"
                map.getMatching(object : IfSubA {}::class) shouldBe "a"
            }
            it("Should get matches for super-super-classes") {
                val map = TypeMap.create<String>()
                map[AbsA::class] = "a"
                map.getMatching(object: AbsSubA() {}::class) shouldBe "a"
            }
            it("Should resolve in declaration order") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"
                map[IfB::class] = "b"
                map.getMatching(object: IfA, IfB {}::class) shouldBe "a"
            }
            it("prefers direct supertypes to distant supertypes") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"
                map[IfB::class] = "b"
                map.getMatching(object: IfSubB, IfA {}::class) shouldBe "a"
            }
            it("Should resolve super-super types in declaration order") {
                val map = TypeMap.create<String>()
                map[IfA::class] = "a"
                map[IfB::class] = "b"
                map.getMatching(object: IfSubA, IfSubB {}::class) shouldBe "a"
            }
        }

    }
) {
    private interface IfA
    private interface IfB
    private interface IfSubA : IfA
    private interface IfSubB : IfB

    private abstract class AbsA
    private abstract class AbsB
    private abstract class AbsSubA : AbsA()

}
