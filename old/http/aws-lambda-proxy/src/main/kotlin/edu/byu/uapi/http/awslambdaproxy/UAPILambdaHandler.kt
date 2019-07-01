package edu.byu.uapi.http.awslambdaproxy

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.slf4j.LoggerFactory

abstract class UAPILambdaHandler(config: LambdaConfig): RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    val engine = LambdaProxyEngine(config)

    abstract fun setup(engine: LambdaProxyEngine)

    lateinit var isSetup: Any

    private fun ensureSetup() {
        if (!this::isSetup.isInitialized) {
            isSetup = Any()
            setup(engine)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UAPILambdaHandler::class.java)
    }

    override fun handleRequest(
        input: APIGatewayProxyRequestEvent,
        context: Context
    ): APIGatewayProxyResponseEvent {
        ensureSetup()
        LOG.info("Incoming Request: $input")

        val response = engine.dispatch(input, context)
        LOG.info("Response Code: ${response.statusCode}")
        return response
    }
}
