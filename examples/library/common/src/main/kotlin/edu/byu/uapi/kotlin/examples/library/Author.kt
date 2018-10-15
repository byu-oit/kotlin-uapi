package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */

class Author (val authorId: Int,
              val name: String,
              val order: Int): Comparable<Author> {

    override fun compareTo(other: Author): Int = this.order - other.order

    override fun toString(): String {
        return name
    }
}
