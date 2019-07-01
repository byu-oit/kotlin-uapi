package edu.byu.uapi.server.spi

interface UAPIPresenter<Response, Presentable> {
    suspend operator fun invoke(response: Response): Presentable
}
