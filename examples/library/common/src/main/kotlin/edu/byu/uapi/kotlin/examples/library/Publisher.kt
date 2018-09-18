package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */

class Publisher (val publisherId: Int,
                 val name: String){
    override fun toString(): String {
        return name
    }
}
