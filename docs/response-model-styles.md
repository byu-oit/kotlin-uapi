---
title: Response Model
order: 4
---
# Style 2 - Declarative

```kotlin

override fun responseModel() {
  return uapiResponse {
    prop<String>("byu_id") {
       key = true
       doc = """this describes what a byu id is"""
       value { userContext, id, model ->
         return model.byuId
       }
       description { userContext, id, model ->
         return buildDescription(model.field)
       }
    }
    datetime("byu_id") {
      doc = """this describes what a byu id is"""
      value { userContext, id, model ->
        return model.field
      }
      description { userContext, id, model ->
        return buildDescription(model.field)
      }
    }
    prop<String>("person_id") {
      value {
        return model.field
      }
    }
    prop<String>("full_name") {
      value {
        
      }
      modifiable {
        userContext.canModifyUserName(id)
      }
    }
    prop<String>("state_code") {
      value {
        model.state.code
      }
      description {
        model.state.name
      }
    }
  } 
}

data class PersonDTO(
  val state: StateDTO
)

```

