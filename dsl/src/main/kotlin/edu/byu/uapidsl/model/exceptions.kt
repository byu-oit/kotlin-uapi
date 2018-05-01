package edu.byu.uapidsl.model

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class ModelValidationException(
  val path: String,
  val details: String
): Exception(
  "Model Validation Failed: $details (path: $path)"
)
