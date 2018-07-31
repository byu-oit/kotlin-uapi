package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.model.resource.identified.ops.SimpleListOperation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import kotlin.test.Ignore

@Ignore
internal class SimpleListOperationTest {

    private lateinit var instance: SimpleListOperation<
        FakeAuth, String, FakeDomain, FakeFilters>

    class FakeDomain {

    }

    class FakeAuth {

    }

    class FakeFilters {

    }

    @BeforeEach
    fun setUp() {
//        instance = SimpleListOperation(
//        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun createRequestContext() {
    }

    @Test
    fun idToModelCollection() {
    }

    @Test
    fun responseMetadata() {
    }

    @Test
    fun handleRequest() {
    }
}
