package edu.byu.uapidsl.dsl.subresource

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.dsl.subresource.list.SubResourceInit
import edu.byu.uapidsl.dsl.subresource.single.SingleSubResourceInit


class SubresourcesInit<AuthContext, IdType, ResourceModel : Any>(
) : DSLInit<SubresourcesModel>() {

    inline fun <reified SubResourceId, reified SubResourceModel> collection(
        name: String,
        init: SubResourceInit<AuthContext, IdType, ResourceModel, SubResourceId, SubResourceModel>.() -> Unit
    ) {

    }

    inline fun <reified SubResourceModel> single(
        name: String,
        init: SingleSubResourceInit<AuthContext, IdType, ResourceModel, SubResourceModel>.() -> Unit
    ) {

    }

    override fun toModel(context: ModelingContext): SubresourcesModel {
        TODO("not implemented")
    }

}

class SubresourcesModel()
