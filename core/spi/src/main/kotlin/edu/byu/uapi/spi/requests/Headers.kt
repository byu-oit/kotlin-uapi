package edu.byu.uapi.spi.requests

interface Headers {
    operator fun get(header: String): Set<String>
}
