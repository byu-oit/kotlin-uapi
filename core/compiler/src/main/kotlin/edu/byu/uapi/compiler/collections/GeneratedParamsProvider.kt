package edu.byu.uapi.compiler.collections

import com.squareup.javapoet.*
import edu.byu.uapi.compiler.Names
import edu.byu.uapi.compiler.parameterize
import java.util.*
import javax.lang.model.element.Modifier

internal class GeneratedParamsProvider(val model: ParamsModel) {


    fun generate(): JavaFile {
        val paramsType = ClassName.get(model.packageName, model.name)

        val typeSpec = TypeSpec.classBuilder(model.name + "\$CollectionParamsProvider")
            .addSuperinterface(Names.collectionParamsProvider.parameterize(paramsType))

        val searching = model.searching?.generate()?.also { it.addStaticBits(typeSpec) }
        val filtering = null
        val sorting = model.sorting?.generateSorting()?.also { it.addStaticBits(typeSpec) }

        val dictionary = ParameterSpec.builder(Names.typeDictionary, "dictionary")
            .addAnnotation(Names.nonNull)
            .build()

        val getMeta = MethodSpec.methodBuilder("getMeta")
            .addAnnotation(Names.override)
            .addAnnotation(Names.nonNull)
            .returns(Names.collectionParamsMeta)
            .addParameter(dictionary)

        getMeta.addStatement(assignOrNull(Names.searchParamsMeta, "searching", searching?.metaField))
        getMeta.addStatement(assignOrNull(Names.filterParamsMeta, "filtering", filtering))
        getMeta.addStatement(assignOrNull(Names.sortParamsMeta, "sorting", sorting?.metaField))

        getMeta.addStatement("return new \$T(searching, filtering, sorting)", Names.collectionParamsMeta)

        typeSpec.addMethod(getMeta.build())

        return JavaFile.builder(model.packageName, typeSpec.build())
            .skipJavaLangImports(true)
            .build()
    }

    private fun SortingModel.generateSorting(): SortingElements {
        val nameFieldsList = this.fields.map {
            val field = FieldSpec.builder(Names.string, "SORT_PARAMS_\$${it.name}\$", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\$S", it.name)
                .build()
            it.name to field!!
        }

        val nameFieldsMap = nameFieldsList.toMap()

        val all = FieldSpec.builder(
            Names.stringList,
            "SORT_PARAMS_FIELDS",
            Modifier.FINAL,
            Modifier.STATIC,
            Modifier.PRIVATE
        ).initializer(nameList(nameFieldsList.map { it.second }))
            .build()

        val defaults = FieldSpec.builder(
            Names.stringList,
            "SORT_PARAMS_DEFAULT_FIELDS",
            Modifier.FINAL,
            Modifier.STATIC,
            Modifier.PRIVATE
        ).initializer(nameList(this.defaults.map { nameFieldsMap.getValue(it) }))
            .build()

        val metaField = FieldSpec.builder(
            Names.sortParamsMeta,
            "SORT_PARAMS_META",
            Modifier.FINAL,
            Modifier.STATIC,
            Modifier.PRIVATE
        ).initializer("new \$T(\$N, \$N)", Names.sortParamsMeta, all, defaults)
            .build()

        return SortingElements(nameFieldsMap, all, defaults, metaField)
    }

    private fun SearchingModel.generate(): SearchingElements {
        val searching = this

        val fieldNames = mutableListOf<FieldSpec>()
        val nameFields = sortedMapOf<String, FieldSpec>()
        val fieldLists = sortedMapOf<String, FieldSpec>()
        val staticBlock = CodeBlock.builder()

        val metaField = FieldSpec.builder(
            Names.searchParamsMeta,
            "SEARCH_PARAMS_META",
            Modifier.PRIVATE,
            Modifier.STATIC,
            Modifier.FINAL
        ).build()

        staticBlock.addStatement("final \$1T search_build = new \$1T()",
                                 Names.linkedHashMap.parameterize(Names.string, Names.stringSet)
        )

        searching.contexts.forEach { context, fields ->
            val names = fields.map {
                it to FieldSpec.builder(
                    Names.string,
                    "SEARCH_PARAMS_\$$context\$_\$$it\$",
                    Modifier.PRIVATE,
                    Modifier.STATIC,
                    Modifier.FINAL
                ).initializer("\$S", it).build()
            }.toMap()

            val contextName = FieldSpec.builder(
                Names.string,
                "SEARCH_PARAMS_\$$context\$",
                Modifier.PRIVATE,
                Modifier.STATIC,
                Modifier.FINAL
            ).initializer("\$S", context)
                .build()

            val fieldList = FieldSpec.builder(
                Names.stringSet,
                "SEARCH_PARAMS_\$$context\$_FIELDS",
                Modifier.PRIVATE,
                Modifier.STATIC,
                Modifier.FINAL
            ).initializer(nameSet(names.values)).build()

            fieldNames.addAll(names.values)
            nameFields[context] = contextName
            fieldLists[context] = fieldList

            staticBlock.addStatement("search_build.put(\$N, \$N)", contextName, fieldList)
        }
        staticBlock.addStatement("\$N = \$T.unmodifiableMap(search_build)", metaField, Names.util_collections)

        return SearchingElements(
            fieldNames,
            nameFields,
            fieldLists,
            metaField,
            staticBlock.build()
        )
    }

    private data class SearchingElements(
        val fieldNameFields: List<FieldSpec>,
        val contextNameFields: SortedMap<String, FieldSpec>,
        val contextFieldLists: SortedMap<String, FieldSpec>,
        val metaField: FieldSpec,
        val staticBlock: CodeBlock
    ) {
        fun addStaticBits(typeSpec: TypeSpec.Builder) {
            typeSpec.addFields(fieldNameFields)
            typeSpec.addFields(contextNameFields.values)
            typeSpec.addFields(contextFieldLists.values)
            typeSpec.addField(metaField)
            typeSpec.addStaticBlock(staticBlock)
        }
    }

    private data class SortingElements(
        val nameFields: Map<String, FieldSpec>,
        val allSortFields: FieldSpec,
        val defaultSortFields: FieldSpec,
        val metaField: FieldSpec
    ) {
        fun addStaticBits(typeSpec: TypeSpec.Builder) {
            typeSpec.addFields(nameFields.values)
            typeSpec.addField(allSortFields)
            typeSpec.addField(defaultSortFields)
            typeSpec.addField(metaField)
        }
    }
}

fun nameList(nameFields: Collection<FieldSpec>): CodeBlock {
    if (nameFields.isEmpty()) {
        return CodeBlock.of("\$T.emptyList()", Names.util_collections)
    } else if (nameFields.size == 1) {
        return CodeBlock.of("\$T.singletonList(\$N)", Names.util_collections, nameFields.first())
    }
    val format = nameFields.joinToString(", ") { "\$N" }
    return CodeBlock.of("\$T.unmodifiableList(\$T.asList($format))",
                        Names.util_collections,
                        Names.util_arrays,
                        *nameFields.toTypedArray()
    )
}

fun nameSet(nameFields: Collection<FieldSpec>): CodeBlock {
    if (nameFields.isEmpty()) {
        return CodeBlock.of("\$T.emptySet()", Names.util_collections)
    } else if (nameFields.size == 1) {
        return CodeBlock.of("\$T.singleton(\$N)", Names.util_collections, nameFields.first())
    }
    val format = nameFields.joinToString(", ") { "\$N" }
    return CodeBlock.of("\$T.unmodifiableSet(new \$T(\$T.asList($format)))",
                        Names.util_collections,
                        Names.stringHashSet,
                        Names.util_arrays,
                        *nameFields.toTypedArray()
    )
}

fun assignOrNull(
    type: TypeName,
    name: String,
    fieldSpec: FieldSpec?
): CodeBlock =
    if (fieldSpec == null) {
        CodeBlock.of("final \$T $name = null", type)
    } else {
        CodeBlock.of("final \$T $name = \$N", type, fieldSpec)
    }

