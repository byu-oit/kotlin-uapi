package edu.byu.uapi.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.*
import java.util.*
import javax.annotation.Nonnull
import javax.annotation.Nullable
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass

object Names {
    val util_collections = className(Collections::class)
    val util_arrays = className(Arrays::class)
    val list = className(List::class)
    val set = className(Set::class)
    val map = className(Map::class)
    val linkedHashMap = className(LinkedHashMap::class)
    val string = className(String::class)

    val stringList = list.parameterize(string)
    val stringSet = set.parameterize(string)

    val hashSet = className(HashSet::class)
    val stringHashSet = hashSet.parameterize(string)

    val collectionParamsProvider = className(ListParamReader::class)
    val collectionParamsMeta = className(ListParamsMeta::class)
    val typeDictionary = className(TypeDictionary::class)
    val searchParamsMeta = className(SearchParamsMeta::class)
    val filterParamsMeta = className(FilterParamsMeta::class)
    val sortParamsMeta = className(SortParamsMeta::class)

    val nonNull = className(Nonnull::class)
    val nullable = className(Nullable::class)
    val override = className(Override::class)

}

fun className(of: KClass<*>): ClassName = ClassName.get(of.java)

fun ClassName.parameterize(vararg typeArgs: TypeName): ParameterizedTypeName =
    ParameterizedTypeName.get(this, *typeArgs)
