---
title: Built-in Data Types
short_title: Data Types
---

# Built-in Data Types

The UAPI runtime knows how to convert to and from all of these types. This includes serializing to
JSON and reading in path, query, or body parameters.

## Primitives and pseudo-primitives

Conversions are supported for all primitive types and built-in number types, plus Strings.

Kotlin Type | JSON Type
------------|----------
String      | `string` 
Boolean     | `boolean`
Char        | `string` 
Byte        | `integer`
Short       | `integer`
Int         | `integer`
Long        | `integer`
Float       | `number` 
Double      | `number`
BigInteger  | `integer`
BigDecimal  | `number`

## Date/Time Types

All types from the Java 8 release of `java.time` are supported. They all
serialize to strings that are compatible with RFC-3339 or ISO-8601, using the built-in
string formatting and parsing.

### Timestamp

* Instant
* ZonedDateTime
* OffsetDateTime

### Zone-less Date/Time

* LocalDate
* LocalDateTime
* LocalTime

### Timestamp Fragments

* OffsetTime
* YearMonth
* MonthDay
* Year
* DayOfWeek
* Month

### Intervals

* Period
* Duration

### Compatibility

For reasons of backwards-compatibility, all built-in subtypes of `java.util.Date` are supported. They are serialized in
the same way as the `java.time` types.

* `java.util.Date`
* `java.sql.Date`
* `java.sql.Timestamp`

## Binary strings

All binary values are represented as Base-64 encoded strings.

* ByteArray
* ByteBuffer

## Miscellaneous Platform Types

Kotlin Type | JSON Type
------------|----------
`java.util.UUID` | `string`

## UAPI-Specific Types

* APIType

## Enum Values

All enums are serializable by default. They are serialized using their 'toString' method,
and are deserialized using a tolerant algorithm, which accepts the following variants:

* UPPER_SNAKE_CASE
* lower_snake_case
* UPPER-KEBAB-CASE
* lower-kebab-case

In addition, if the Enum's toString method yields a string that is camelCased (first letter is lower case, contains no
underscores or dashes, and has at least one uppercase letter), the camel case version will be accepted and the value
will be automatically transformed into the other case variants.

