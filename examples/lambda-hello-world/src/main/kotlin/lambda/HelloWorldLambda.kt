package lambda

import edu.byu.uapi.http.awslambdaproxy.LambdaConfig
import edu.byu.uapi.http.awslambdaproxy.LambdaProxyEngine
import edu.byu.uapi.http.awslambdaproxy.UAPILambdaHandler
import edu.byu.uapi.http.json.JavaxJsonTreeEngine
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level
import org.pmw.tinylog.Logger

class HelloWorldLambda : UAPILambdaHandler(
    LambdaConfig(JavaxJsonTreeEngine)
) {
    init {
        Configurator.defaultConfig()
            .level("edu.byu", Level.DEBUG)
            .level(Level.INFO)
            .activate()
    }

    override fun setup(engine: LambdaProxyEngine) {
        Logger.info("Starting Setup")
        engine.register(helloWorldRuntime)
        Logger.info("Finished Setup")
    }
}
