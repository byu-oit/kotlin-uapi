package edu.byu.uapi.server.style.receiverfn.requests

interface ListParams {

    interface WithFilter<F> : ListParams {
        val filters: F
    }

    interface WithSearch<C : Enum<C>> : ListParams {
        val search: SearchRequest<C>
    }

    interface WithSort<C : Enum<C>> : ListParams {
        val sort: SortRequest<C>
    }

    interface WithSubset : ListParams {
        val subset: SubsetRequest
    }

}

interface SearchRequest<C : Enum<C>> {
    val context: C
    val searchText: String
}

interface SortRequest<F : Enum<F>> {
    val field: F
    val ascending: Boolean
}

interface SubsetRequest {
    val startOffset: Int
    val size: Int
}
