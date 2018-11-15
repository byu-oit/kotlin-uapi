package edu.byu.uapi.library

import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.input.ListParams

data class Big(
    val key: Int,
    val string: String = key.toString(),
    val even: Boolean = key % 2 == 0
)

class BigResource: IdentifiedResource<LibraryUser, Int, Big>,
                   IdentifiedResource.Listable.Simple<LibraryUser, Int, Big> {
    override fun loadModel(
        userContext: LibraryUser,
        id: Int
    ): Big? {
        return Big(id)
    }

    override fun canUserViewModel(
        userContext: LibraryUser,
        id: Int,
        model: Big
    ): Boolean = true

    override fun idFromModel(model: Big): Int = model.key

    override val responseFields = fields {
        value(Big::key) {key = true}
        value(Big::string) {}
        value(Big::even) {}
    }

    private val listSize = 5000

    override fun list(
        userContext: LibraryUser,
        params: ListParams.Empty
    ): List<Big> {
        return (0 until listSize).map { Big(it) }
    }
}
