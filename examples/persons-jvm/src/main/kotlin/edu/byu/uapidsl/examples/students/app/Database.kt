package edu.byu.uapidsl.examples.students.app

import edu.byu.uapidsl.dsl.CollectionWithTotal
import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.examples.students.dto.PersonFilters
import edu.byu.uapidsl.http.NotFoundException
import java.security.SecureRandom
import java.time.LocalDate
import java.time.Month


object Database {

    private val people: MutableMap<String, FullPerson> = mutableListOf(
        cosmo, alice, joe
    ).map { it.person.byuId to it }.toMap().toMutableMap()

    fun findPerson(byuId: String): Person? {
        return people[byuId]?.person
    }

    fun createPerson(name: Name, netId: String?): String {
        val byuId= generateId()
        val personId = generateId()

        val person = Person(personId = personId, byuId = byuId, netId = netId, name = name)

        val full = FullPerson(person)

        people[byuId] = full

        return byuId
    }

    fun savePerson(person: Person) {
        val full = people[person.byuId]?: throw NoSuchPersonException(person.byuId)
        full.person = person
    }

    fun deletePerson(byuId: String) {
        people.remove(byuId)
    }



    private val rand = SecureRandom()

    private fun generateId(): String {
        val value = (0 to 9).toList().map { rand.nextInt(10) }
            .map(Int::toString)
            .joinToString("")

        if (people.values.any { it.person.byuId == value || it.person.personId == value}) {
            return generateId()
        }
        return value
    }

    fun searchPeoplePaged(filters: PersonFilters, paging: PagingParams): CollectionWithTotal<String> {
        TODO("not implemented")
    }
}

data class NoSuchPersonException(val byuId: String): Exception("No record found for byuId $byuId")


internal class FullPerson(
    var person: Person,
    val addresses: MutableList<Address> = mutableListOf(),
    val emails: MutableList<EmailAddress> = mutableListOf(),
    val phones: MutableList<PhoneNumber> = mutableListOf(),
    val employee: EmployeeSummary? = null
)

internal val cosmo = FullPerson(
    person = Person(
        byuId = "000000000",
        personId = "112223333",
        netId = "cosmorocks",
        name = Name(
            first = "Abernathy",
            middle = "Cosmo",
            surname = "Cougarious",
            suffix = "II",
            preferredFirst = "Cosmo",
            preferredSurname = "Cougar"
        ),
        sex = Sex.UNKNOWN,
        deceased = false
    ),
    addresses = mutableListOf(
        Address(
            type = AddressType.RESIDENTIAL,
            lines = listOf(
                "123 Forest Way",
                "Y Mountain"
            ),
            city = "Provo",
            postalCode = "84602",
            stateCode = States.utah,
            countryCode = Countries.usa
        )
    ),
    emails = mutableListOf(
        EmailAddress(
            type = EmailType.WORK,
            value = "cosmo@byucougars.com",
            unlisted = false,
            verified = true
        )
    ),
    phones = mutableListOf(
        PhoneNumber(
            value = "801-267-6601",
            cell = true,
            countryCode = Countries.usa,
            primary = true
        )
    ),

    employee = EmployeeSummary(
        department = "Athletics",
        employeeType = "PT-ACT",
        hireDate = LocalDate.of(1953, Month.OCTOBER, 15),
        jobTitle = "Mascot",
        reportsToId = null
    )
)

internal val joe = FullPerson(
    person = Person(
        byuId = "111111111",
        personId = "123123123",
        netId = "jts1",
        name = Name(
            first = "Joseph",
            surname = "Student",
            preferredFirst = "Joe"
        ),
        sex = Sex.MALE,
        deceased = false
    ),
    addresses = mutableListOf(),
    emails = mutableListOf(),
    phones = mutableListOf()
)

internal val alice = FullPerson(
    person = Person(
        byuId = "222222222",
        personId = "321321321",
        netId = "ags1",
        name = Name(
            first = "Alice",
            surname = "Gradstudent"
        ),
        sex = Sex.FEMALE,
        deceased = false
    ),
    addresses = mutableListOf(),
    emails = mutableListOf(),
    phones = mutableListOf()
)

