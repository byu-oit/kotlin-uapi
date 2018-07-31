package edu.byu.uapidsl

import com.google.common.base.CaseFormat
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import java.util.*

data class UApiModel<AuthContext : Any>(
    val info: ApiInfo,
    val authContextCreator: AuthContextCreator<AuthContext>,
    val resources: List<IdentifiedResource<AuthContext, *, *>>

) {

}

data class ApiInfo(
    val name: String,
    val description: String?,
    val version: String
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

fun String.toSnakeCase(): String {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)
}

