package edu.byu.uapidsl.dsl.subresource

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.dsl.subresource.list.SubResourceDSL
import edu.byu.uapidsl.dsl.subresource.single.SingleSubResourceDSL


class SubresourcesDSL<AuthContext, IdType, ResourceModel : Any>(
) : DslPart<SubresourcesModel>() {

    inline fun <reified SubResourceId, reified SubResourceModel> collection(
        name: String,
        init: SubResourceDSL<AuthContext, IdType, ResourceModel, SubResourceId, SubResourceModel>.() -> Unit
    ) {

    }

    inline fun <reified SubResourceModel> single(
        name: String,
        init: SingleSubResourceDSL<AuthContext, IdType, ResourceModel, SubResourceModel>.() -> Unit
    ) {

    }

    override fun toModel(context: ModelingContext): SubresourcesModel {
        TODO("not implemented")
    }

}

class SubresourcesModel()
