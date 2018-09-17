---
title: Response Model
order: 4
---

# Style 1 - Annotations and UAPIProp type

```kotlin

data class FooDTO(
  @AllowedApiTypes(MODIFIABLE, READ_ONLY)
  @HasDescription
  @HasLongDescription
  val field: UAPIProp<String>

)

fun foo() {
  FooDTO(
    field = uapiProp(
      value = "value",
      description = "desc",
      longDescription = "long description",
      apiType = MODIFIABLE
    )
  )
}


```

## Style 1.1 - Different annotations

```kotlin

data class FooDTO(
  @UAPIProp(
    modifiable = true,
    description = true,
    longDescription = true,
    docs = "These are docs about stuff"
  )
  val field: UAPIProp<String>
)

fun foo() {
  FooDTO(
    field = uapiProp(
      value = "value",
      description = "desc",
      longDescription = "long description",
      modifiable = true
    )
  )
}

```

```xml

<build>
        <plugins>
            <plugin>
                <artifactId>kotlin-maven-plugin</artifactId>
                <groupId>org.jetbrains.kotlin</groupId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>kapt</id>
                        <goals>
                            <goal>kapt</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>src/main/kotlin</sourceDir>
                                <sourceDir>src/main/java</sourceDir>
                            </sourceDirs>
                            <annotationProcessorPaths>
                                <annotationProcessorPath>
                                    <groupId>edu.byu.uapi</groupId>
                                    <artifactId>uapi-compiler</artifactId>
                                    <version>1.0.0</version>
                                </annotationProcessorPath>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                    <execution>
                        <id>compile</id>
                        <goals> 
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>src/main/kotlin</sourceDir>
                                <sourceDir>src/main/java</sourceDir>
                            </sourceDirs>
                        </configuration>
                    </execution>
                    <execution>
                        <id>test-kapt</id>
                        <goals>
                            <goal>test-kapt</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>src/test/kotlin</sourceDir>
                                <sourceDir>src/test/java</sourceDir>
                            </sourceDirs>
                            <annotationProcessorPaths>
                                <annotationProcessorPath>
                                    <groupId>edu.byu.uapi</groupId>
                                    <artifactId>uapi-compiler</artifactId>
                                    <version>1.0.0</version>
                                </annotationProcessorPath>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <goals> 
                            <goal>test-compile</goal> 
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>src/test/kotlin</sourceDir>
                                <sourceDir>src/test/java</sourceDir>
                                <sourceDir>target/generated-sources/kapt/test</sourceDir>
                            </sourceDirs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

```


# Style 2 - Declarative

```kotlin

/*
string
number
boolean
date
date-time
 */

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

