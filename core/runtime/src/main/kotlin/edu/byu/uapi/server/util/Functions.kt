package edu.byu.uapi.server.util


inline fun <P, R1, R2> ((P) -> R1).then(crossinline fn: (R1) -> R2): (P) -> R2 {
    return { p -> fn(this(p)) }
}

inline fun <P1, P2, R1, R2> ((P1, P2) -> R1).then(crossinline fn: (R1) -> R2): (P1, P2) -> R2 {
    return { p1, p2 -> fn(this(p1, p2)) }
}

inline fun <P, PPrime, R> ((PPrime) -> R).before(crossinline fn: (P) -> PPrime): (P) -> R {
    return { p -> this(fn(p)) }
}

inline fun <P1, P1Prime, P2, P2Prime, R> ((P1Prime, P2Prime) -> R).before(crossinline fn: (P1, P2) -> Pair<P1Prime, P2Prime>): (P1, P2) -> R {
    return { p1, p2 ->
        val (p1p, p2p) = fn(p1, p2)
        this(p1p, p2p)
    }
}


inline fun <P1, P1Prime, P2, P2Prime, P3, P3Prime, R> ((P1Prime, P2Prime, P3Prime) -> R).before(crossinline fn: (P1, P2, P3) -> Triple<P1Prime, P2Prime, P3Prime>): (P1, P2, P3) -> R {
    return { p1, p2, p3 ->
        val (p1p, p2p, p3p) = fn(p1, p2, p3)
        this(p1p, p2p, p3p)
    }
}
