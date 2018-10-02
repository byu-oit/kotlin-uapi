package edu.byu.uapi.server.types

import io.kotlintest.specs.DescribeSpec
import java.net.URL

class DefaultLinkBuilderSpec: DescribeSpec() {
    init {
        describe("DefaultLinkBuilder") {
            it("renders path params") {
                val builder = DefaultLinkBuilder(URL("https://example.com/base/"))
                builder.addPath(listOf("foo", "bar", "123"))

                println(builder.build())
            }
        }
    }
}
