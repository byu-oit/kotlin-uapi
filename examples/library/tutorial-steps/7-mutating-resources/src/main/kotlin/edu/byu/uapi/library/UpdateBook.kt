package edu.byu.uapi.library

import org.hibernate.validator.constraints.ISBN
import java.time.Year
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

data class UpdateBook(
    @ISBN
    val isbn: String? = null,
    @NotBlank
    val title: String,
    val subtitles: List<@NotBlank String> = emptyList(),
    val publishedYear: Year,
    @Positive
    val publisherId: Int,
    val authorIds: List<@Positive Long>,
    val genreCodes: Set<@NotBlank String> = emptySet(),
    val restricted: Boolean = false
)
