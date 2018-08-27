# Input Validation

To help implement robust input validation, the `Validating` class is used.  `Validating` allows you to build complex
chains of validation checks, then return all of the failures (instead of just the first one).  In addition, the class
and its extension functions take advantage of lots of Kotlin goodies to help you write concise rules, as well as make extending
`Validating` easy.


Let's say we need to validate an input of type `NewFoo`.  Here's what such a validation function may look like:

```kotlin

  data class NewFoo(
    val aString: String,
    val optionalInt: Int?,
    val patternedString: String
  )

  fun validateCreateInput(userContext: UserContext, input: NewFoo, validation: Validating) {
    validating.expectNotBlank(input::aString)
    
    if (input.optionalInt != null) {
      validating.expectPositiveInt(input::optionalInt)
    }
    
    if (validating.expectNotBlank(input::patternedString)) {
      validating.expectMatches(input::patternedString, "^Y[a-zA-Z0-9]*$".toRegex(), "alphanumeric, starts with 'Y'")
    }
  }

```

This validate four things:

* nonEmptyString is not null, empty, or blank (only contains whitespace)
* optionalPositiveInt, if specified, is positive
* patternedString is not null, empty, or blank
* patternedString starts with a 'Y' and is alphanumeric.

A few important things to note:

* These functions are all passed a Kotlin property reference (like `input::aString`). This usage is optional, but preferred,
as otherwise, you have to type the property name twice: `validating.expectNotBlank("aString", input.aString)`.

* All `expect*` methods return a boolean, indicating if the assertion passed. This is especially useful when you are
doing complex validation on a value like a database identifier. For example, I could use this to validate that the value
of a 'country_code' field is not blank before doing a more expensive lookup to see if there is a matching country code
in the database.

* All assertions will be run and their results collected. If you don't want one assertion to run if a previous one has
failed, nest it in an `if` block with the result of the previous assertion.

## Built-in assertions

### Nullability

Most of the time, the Runtime can validate nullability for you using Kotlin's type system. However, there may be complex
cases in your application, such as "if field 'foo' is set, field 'bar' must also be non-null".

#### Validating.expectNull
