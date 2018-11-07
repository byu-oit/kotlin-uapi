---
title: Customizing Input Parsing
short_title: Custom Parsing
order: 3
---

# Identifier Params

A custom ID param reader must implement two methods: `read` and `describe`:

```kotlin
class MyIdParamReader: IdParamReader<MyId> {
  override fun describe(): IdParamMeta {
    TODO("not implemented")
  }
  
  override fun read(input: IdParams): String {
    TODO("not implemented")
  }
}
```

{% capture custom_id_parsing %}
This API contract is subject to change. Because ID parsing is a core part of the runtime, it may be necessary to
add to or change the methods you must implement.
{% endcapture %}
{% include callouts/might-change.html content=custom_id_parsing %}

## 'describe'

`describe` returns a description of this ID type, including a description of each field in the identifier. This metadata
is used to generate the URL path and to generate documentation.

For simple identifiers, you should only return one field description. The actual return type is 
`IdParamMeta`, which is an interface containing one value: `val idParams: List<IdParamMeta.Param>`. `Param` is a data
class with a `name` string and a `scalarFormat` which describes the format of a valid value.

If there are multiple values in `idParams`, a compound identifier will be created. The URL parameters will appear in
the same order as the values in the `idParams` list.

`describe` can throw a `UAPITypeError` to signify that something went wrong while constructing the metadata.

## 'read'

`read` is used to actually read the parameters from the path. It is passed an instance of `IdParams`, which is a map
of parameter names to values.

# List Parameters


