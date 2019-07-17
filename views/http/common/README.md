# UAPI HTTP Common

This package has to main parts:

* Defining an API for different HTTP engines to implement so that they can be used by this module
* Defining the mapping between abstract UAPI models and concrete HTTP implementations.

This package isn't useful by itself, and must be used together with an HTTP engine implementation. You can
find a list in the [HTTP module README](../README.md).
