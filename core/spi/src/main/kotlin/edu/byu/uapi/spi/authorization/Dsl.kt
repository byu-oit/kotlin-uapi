package edu.byu.uapi.spi.authorization

val viewRules: AuthorizationRule<Any> = authorizations {
    anyOf {
        rule("self-service") { user.userId == model.ownerId }
        rule("admin group") { user.isAdmin }
        allOf {
            rule("something") {true}
            rule("something else") {false}
        }
    }
}
