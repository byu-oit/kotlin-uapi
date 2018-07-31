package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.model.resource.identified.FetchResourceRequest
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import edu.byu.uapidsl.types.*

class ResourceGet<AuthContext : Any, IdType : Any, ModelType : Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: IdentifiedResource<AuthContext, IdType, ModelType>,
    jsonWriter: ObjectWriter
) : BaseHttpHandler<GetRequest, AuthContext>(
    apiModel, jsonWriter
), GetHandler {

    override fun handleAuthenticated(request: GetRequest, authContext: AuthContext): UAPIResponse<*> {
        val basic = resource.handleFetchRequest(FetchResourceRequest(
            authContext, resource.idModel.reader.read(request.path)
        ))

        return SimpleResourceResponse(
            mapOf("basic" to basic),
            UAPIResourceMeta(
                validationResponse = basic.metadata.validationResponse,
                validationInformation = basic.metadata.validationInformation
            )
        )
    }
}

