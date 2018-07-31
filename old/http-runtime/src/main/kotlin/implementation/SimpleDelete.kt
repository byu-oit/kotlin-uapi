package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.DeleteContext
import edu.byu.uapidsl.http.DeleteHandler
import edu.byu.uapidsl.http.DeleteRequest
import edu.byu.uapidsl.model.resource.identified.DeleteResourceRequest
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import edu.byu.uapidsl.types.UAPIResponse

class SimpleDelete<AuthContext: Any, IdType: Any, ModelType: Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: IdentifiedResource<AuthContext, IdType, ModelType>,
    jsonWriter: ObjectWriter
): BaseHttpHandler<DeleteRequest, AuthContext>(
    apiModel, jsonWriter
), DeleteHandler {

    override fun handleAuthenticated(request: DeleteRequest, authContext: AuthContext): UAPIResponse<*> {
        val id = resource.idModel.reader.read(request.path)

        return resource.handleDeleteRequest(
            DeleteResourceRequest(authContext, id)
        )
    }
}

