package edu.byu.uapi.spi.authorization

fun <UserContext : Any> AuthorizationRule<UserContext>.and(
    first: AuthorizationRule<UserContext>
): AuthorizationRule<UserContext> {
    return AndAuthorizationRule(listOf(this, first))
}

fun <UserContext : Any> AuthorizationRule<UserContext>.and(
    first: AuthorizationRule<UserContext>,
    vararg others: AuthorizationRule<UserContext>
): AuthorizationRule<UserContext> {
    return AndAuthorizationRule(listOf(this, first) + others)
}

fun <UserContext : Any> AuthorizationRule<UserContext>.or(
    first: AuthorizationRule<UserContext>
): AuthorizationRule<UserContext> {
    return OrAuthorizationRule(listOf(this, first))
}

fun <UserContext : Any> AuthorizationRule<UserContext>.or(
    first: AuthorizationRule<UserContext>,
    vararg others: AuthorizationRule<UserContext>
): AuthorizationRule<UserContext> {
    return OrAuthorizationRule(listOf(this, first) + others)
}

class AndAuthorizationRule<UserContext : Any>(
    val rules: List<AuthorizationRule<UserContext>>
) : AuthorizationRule<UserContext> {
    override fun evaluate(context: UserContext): Boolean {
        return rules.all { it.evaluate(context) }
    }

    override fun describe(): String {
        return rules.joinToString(prefix = "(", separator = ") AND (", postfix = ")") { it.describe() }
    }
}

class OrAuthorizationRule<UserContext : Any>(
    val rules: List<AuthorizationRule<UserContext>>
) : AuthorizationRule<UserContext> {
    override fun evaluate(context: UserContext): Boolean {
        return rules.any { it.evaluate(context) }
    }

    override fun describe(): String {
        return rules.joinToString(prefix = "(", separator = ") OR (", postfix = ")") { it.describe() }
    }
}
