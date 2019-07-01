package edu.byu.uapi.utility.collections

inline fun <C : Collection<*>> C.ifNotEmpty(action: (C) -> Unit) = this.apply {
    if (isNotEmpty()) {
        action(this)
    }
}

