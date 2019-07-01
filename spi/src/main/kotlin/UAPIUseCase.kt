package edu.byu.uapi.server.spi

/**
 * Use cases represent the core capabilities of the UAPI runtime, and are the pieces of the runtime that choose
 * when to invoke most application code.
 *
 * The use case makes calls to the application code, builds a response, and then passes that response to the presenter
 * and returns the presentable object.
 */
interface UAPIUseCase<Req, Resp> {
    suspend operator fun <P> invoke(req: Req, present: UAPIPresenter<Resp, P>): P
}
