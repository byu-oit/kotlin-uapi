package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */

class Genre (val code: String,
             val name: String){
    override fun toString(): String {
        return name
    }
}
