package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */

class Author (val authorId: Int,
              val name: String) {
    override fun toString(): String {
        return name
    }
}
