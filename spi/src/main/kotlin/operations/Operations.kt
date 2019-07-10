package edu.byu.uapi.server.spi.operations

sealed class UAPIOperation {

}

sealed class ResourceOperation: UAPIOperation() {

}

sealed class SingletonResourceOperation: ResourceOperation() {
    object Load: SingletonResourceOperation()
}
