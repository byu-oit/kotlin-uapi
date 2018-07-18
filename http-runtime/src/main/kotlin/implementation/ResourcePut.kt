package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.PutHandler
import edu.byu.uapidsl.http.PutRequest
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.model.resource.UpdateResourceRequest
import edu.byu.uapidsl.model.resource.ops.UpdateOperation
import edu.byu.uapidsl.types.UAPIResponse

class ResourcePut<AuthContext : Any, IdType : Any, ModelType : Any, InputType : Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val operation: UpdateOperation<AuthContext, IdType, ModelType, InputType, *>,
    jsonWriter: ObjectWriter
) : BaseHttpHandler<PutRequest, AuthContext>(
    apiModel,
    jsonWriter
), PutHandler {
    override fun handleAuthenticated(request: PutRequest, authContext: AuthContext): UAPIResponse<*> {
        val body: InputType = request.body.readWith(operation.input.reader)
        val id = resource.idModel.reader.read(request.path)

        return resource.handleUpdateRequest(
            UpdateResourceRequest(
                authContext,
                id,
                body
            )
        )
    }
}
