package edu.byu.uapi.library

import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.input.SubsetParams

data class Big(
    val key: Int,
    val string: String = key.toString(),
    val even: Boolean = key % 2 == 0
)

data class BigParams(
    override val subset: SubsetParams
): ListParams.WithSubset

class BigResource: IdentifiedResource<LibraryUser, Int, Big>,
                   IdentifiedResource.Listable.WithSubset<LibraryUser, Int, Big, BigParams> {
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

    override fun list(
        userContext: LibraryUser,
        params: BigParams
    ): ListWithTotal<Big> {
        val start = params.subset.subsetStartOffset
        val size = params.subset.subsetSize
        val list = (start until (start + size)).map { Big(it) }
        return ListWithTotal(Integer.MAX_VALUE, list)
    }

    override val listDefaultSubsetSize: Int = 1
    override val listMaxSubsetSize: Int = 10_000
}
