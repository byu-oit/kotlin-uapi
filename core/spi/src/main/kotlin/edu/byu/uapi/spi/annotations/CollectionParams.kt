package edu.byu.uapi.spi.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CollectionParams

interface SearchContext {
    val fieldsInContext: Set<String>
}

annotation class SearchFields(vararg val value: String)
annotation class DefaultSort(val order: Int = 0)
