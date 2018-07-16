package edu.byu.uapidsl.model.resource.ops

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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
