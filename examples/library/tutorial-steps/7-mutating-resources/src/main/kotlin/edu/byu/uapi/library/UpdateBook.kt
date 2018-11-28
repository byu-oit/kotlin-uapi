package edu.byu.uapi.library

import org.hibernate.validator.constraints.ISBN
import java.time.Year
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

data class UpdateBook(
    @get:ISBN.List(ISBN(type = ISBN.Type.ISBN_10), ISBN(type = ISBN.Type.ISBN_13))
    val isbn: String? = null,
    @get:NotBlank
    val title: String,
    @get:Valid
    val subtitles: List<@NotBlank String> = emptyList(),
    val publishedYear: Year,
    @get:Positive
    val publisherId: Int,
    val authorIds: List<@Positive Long>,
    val genreCodes: Set<@NotBlank String> = emptySet(),
    val restricted: Boolean = false
)
