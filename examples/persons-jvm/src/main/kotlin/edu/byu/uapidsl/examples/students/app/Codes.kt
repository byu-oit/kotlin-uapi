package edu.byu.uapidsl.examples.students.app

object Countries {

    fun find(code: String?) = all.find { it.id == code }

    val usa = CountryCode(
        id = "US",
        commonName = "United States of America",
        fullName = "United States of America",
        callingCode = "+1"
    )

    val canada = CountryCode(
        id = "CA",
        commonName = "Canada",
        fullName = "Canada",
        callingCode = "+1"
    )

    val all = listOf(usa, canada)

}

object States {

    fun find(code: String?) = all.find { it.id === code }

    val utah = StateCode(
        id = "UT",
        commonName = "Utah",
        fullName = "State of Utah",
        country = Countries.usa
    )
    val california = StateCode(
        id = "CA",
        commonName = "California",
        fullName = "State of California",
        country = Countries.usa
    )

    val nunavut = StateCode(
        id = "NU",
        commonName = "Nunavut",
        fullName = "Territory of Nunavut",
        country = Countries.canada
    )

    val britishColumbia = StateCode(
        id = "BC",
        commonName = "British Columbia",
        country = Countries.canada
    )

    val all = listOf(utah, california, nunavut, britishColumbia)
}

object Buildings {

    fun find(code: String?) = all.find { it.code == code }

    val stadium = Building(
        code = "LES",
        name = "LaVell Edwards Stadium"
    )

    val itb = Building(
        code = "ITB",
        name = "Information Technology Building"
    )

    val all = listOf(stadium, itb)
}

object HighSchools {
    fun find(highSchoolCode: String?): HighSchoolCode? {
        if (highSchoolCode == null) return null
        return all.find { it.id == highSchoolCode }
    }

    val provo = HighSchoolCode(
        id = "11111",
        name = "Provo High School",
        city = "Provo",
        state = States.utah
    )

    val sunnydale = HighSchoolCode(
        id="666",
        name = "Sunnydale High",
        city = "Sunnydale",
        state = States.california

    )

    val all = listOf(provo, sunnydale)
}

