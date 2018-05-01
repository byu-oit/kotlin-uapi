package edu.byu.uapidsl

import edu.byu.uapidsl.model.ResourceModel
import java.util.*

data class UApiModel<AuthContext : Any>(
    val authContextCreator: AuthContextCreator<AuthContext>,
    val resources: List<ResourceModel<AuthContext, *, *>>
)


class ValidationContext {
    private val stack: Deque<String> = LinkedList<String>()

    val current: String
        get() = this.stack.toString()

    fun push(value: String) {
        stack.push(value)
    }

    fun pop() {
        stack.pop()
    }
}
