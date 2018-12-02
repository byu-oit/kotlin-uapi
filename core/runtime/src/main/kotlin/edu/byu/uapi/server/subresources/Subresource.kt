package edu.byu.uapi.server.subresources

import edu.byu.uapi.server.subresources.singleton.SingletonSubresource
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceRuntime
import edu.byu.uapi.server.subresources.singleton.SubresourceRuntime
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.validation.ValidationEngine

interface Subresource<UserContext: Any, Parent: ModelHolder, Model: Any> {

}

fun <UserContext: Any, Parent: ModelHolder, Model: Any> Subresource<UserContext, Parent, Model>.createRuntime(
    typeDictionary: TypeDictionary,
    validationEngine: ValidationEngine
): SubresourceRuntime<UserContext, Parent, Model> {
    return when {
        this is SingletonSubresource -> SingletonSubresourceRuntime(this, typeDictionary, validationEngine)
        else -> throw IllegalStateException("Unknown subresource type: ${this::class.qualifiedName}")
    }
}



