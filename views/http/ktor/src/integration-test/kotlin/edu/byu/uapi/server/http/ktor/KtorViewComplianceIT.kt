package edu.byu.uapi.server.http.ktor

//@Disabled("Ktor server has not been implemented yet")
//internal class KtorViewComplianceIT : HttpViewComplianceTests<ApplicationEngine>() {
//
//    override fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): ApplicationEngine {
//        return embeddedServer(
//            Netty,
//            host = address.hostAddress,
//            port = port
//        ) {
//            routing {
//                uapi(routes)
//            }
//        }
//    }
//
//    override fun stopServer(handle: ApplicationEngine) {
//        handle.stop(0, 2, TimeUnit.SECONDS)
//    }
//
//}
