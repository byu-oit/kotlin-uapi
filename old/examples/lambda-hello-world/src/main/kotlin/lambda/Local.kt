package lambda

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import java.util.*

val handler = HelloWorldLambda()

/*
{
    resource: /{proxy+},
    path: /greetings,
    httpMethod: GET,
    pathParameters: {
        proxy=greetings
    },
    requestContext: {
        accountId: 398230616010,
        resourceId: j0lchx,
        stage: test-invoke-stage,
        requestId: 36ede93c-e9f9-11e8-9b82-2f533ad8a11c,
        identity: {
            accountId: 398230616010,
            caller: AROAJ6CXMH4TRFOQCE2PA:jmooreoa,
            apiKey: test-invoke-api-key,
            sourceIp: test-invoke-source-ip,
            userArn: arn:aws:sts::398230616010:assumed-role/AccountAdministrator/jmooreoa,
            userAgent: aws-internal/3 aws-sdk-java/1.11.432 Linux/4.9.124-0.1.ac.198.71.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.181-b13 java/1.8.0_181,
            user: AROAJ6CXMH4TRFOQCE2PA:jmooreoa,
            accessKey: ASIAVZODE67FNCNAIPGD
        },
        resourcePath: /{proxy+},
        httpMethod: GET,
        apiId: dpnnoyomt8,
        path: /{proxy+}
    },
    isBase64Encoded: false
}
 */

fun main() {
    val request = APIGatewayProxyRequestEvent()
        .withResource("/{proxy+}")
        .withPath("/greetings/EN")
        .withHttpMethod("GET")
        .withPathParamters(mapOf("proxy" to "greetings"))
        .withRequestContext(APIGatewayProxyRequestEvent.ProxyRequestContext()
                                .withAccountId("00000000")
                                .withResourceId("abcdef")
                                .withStage("stage")
                                .withRequestId(UUID.randomUUID().toString())
                                .withIdentity(APIGatewayProxyRequestEvent.RequestIdentity()
                                                  .withAccountId("00000000")
                                )
                                .withResourcePath("/{proxy+}")
                                .withHttpMethod("GET")
                                .withApiId("apiid")
                                .withPath("/{proxy+}")
        )
        .withIsBase64Encoded(false)

    val response = handler.handleRequest(request, FakeContext)

    println(response)
}

object FakeContext: Context {
    override fun getAwsRequestId(): String {
        TODO("not implemented")
    }

    override fun getLogStreamName(): String {
        TODO("not implemented")
    }

    override fun getClientContext(): ClientContext {
        TODO("not implemented")
    }

    override fun getFunctionName(): String {
        TODO("not implemented")
    }

    override fun getRemainingTimeInMillis(): Int {
        TODO("not implemented")
    }

    override fun getLogger(): LambdaLogger {
        TODO("not implemented")
    }

    override fun getInvokedFunctionArn(): String {
        TODO("not implemented")
    }

    override fun getMemoryLimitInMB(): Int {
        TODO("not implemented")
    }

    override fun getLogGroupName(): String {
        TODO("not implemented")
    }

    override fun getFunctionVersion(): String {
        TODO("not implemented")
    }

    override fun getIdentity(): CognitoIdentity {
        TODO("not implemented")
    }

}
