package edu.byu.uapi.compiler.collections

import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

internal data class ParamsModel(
    val name: String,
    val packageName: String,
    val filters: FiltersModel?,
    val sorting: SortingModel?,
    val searching: SearchingModel?
)

internal class FiltersModel(

)
internal data class FilterFieldModel(
    val name: String,
    val type: TypeName,
    val collectionType: TypeName?
)

internal class SortingModel(
//    val fieldEnumType: TypeMirror,
    val fields: List<SortFieldModel>,
    val defaults: List<String>
)

internal data class SortFieldModel(
    val name: String
)

//    internal class PagingModel()
internal data class SearchingModel(
    val contexts: Map<String, List<String>>
)
