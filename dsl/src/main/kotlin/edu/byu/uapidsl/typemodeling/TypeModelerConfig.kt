package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.Module

data class TypeModelerConfig(
    val jacksonModules: List<Module> = emptyList()
)
