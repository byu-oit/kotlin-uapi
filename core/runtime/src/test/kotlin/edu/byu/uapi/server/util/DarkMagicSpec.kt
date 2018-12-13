package edu.byu.uapi.server.util

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class DarkMagicSpec : DescribeSpec() {

    init {
        describe("findMatchingSupertype") {
            context("flat trees") {
                it("finds the correct interface") {
                    val type = DarkMagic.findMatchingSupertype(Simple.Concrete::class, Simple.I::class)
                    type shouldBe Simple.I::class.starProjectedType
                }
                it("finds the correct superclass") {
                    val type = DarkMagic.findMatchingSupertype(Simple.Concrete::class, Simple.Abs::class)
                    type shouldBe Simple.Abs::class.starProjectedType
                }
            }
            context("deep trees") {
                it("traverses up the type tree") {
                    val type = DarkMagic.findMatchingSupertype(Deep.Concrete::class, Deep.I::class)
                    type shouldBe Deep.I::class.starProjectedType
                }
            }
            context("generics") {
                it("finds a generic interface") {
                    val type = DarkMagic.findMatchingSupertype(SimpleGeneric.Concrete::class, SimpleGeneric.I::class)

                    type should {
                        it shouldNotBe null
                        it?.isSubtypeOf(SimpleGeneric.I::class.starProjectedType) shouldBe true
                        it?.classifier shouldBe SimpleGeneric.I::class
                    }
                }
                it("finds a generic superclass") {
                    val type = DarkMagic.findMatchingSupertype(SimpleGeneric.Concrete::class, SimpleGeneric.Abs::class)

                    type should {
                        it shouldNotBe null
                        it?.isSubtypeOf(SimpleGeneric.Abs::class.starProjectedType) shouldBe true
                        it?.classifier shouldBe SimpleGeneric.Abs::class
                    }
                }
            }
        }
    }

    object Simple {
        interface I

        abstract class Abs

        class Concrete : I,
                         Abs()
    }

    object SimpleGeneric {
        interface I<T>

        abstract class Abs<T>

        class Concrete : I<String>,
                          Abs<Number>()
    }

    object DeepGeneric {
        interface I<T>

        abstract class SubI<T> : I<T>

        class Concrete : SubI<Any>()
    }

    object Deep {
        interface I

        abstract class SubI : I

        class Concrete : SubI()
    }


}
