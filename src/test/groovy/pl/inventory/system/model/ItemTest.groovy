package pl.inventory.system.model

import pl.inventory.system.ObjectsProvider
import spock.lang.Specification

import java.time.LocalDate

class ItemTest extends Specification {
    private ObjectsProvider testSource = new ObjectsProvider()
    def firstItem = testSource.table[0]
    def secondItem = testSource.table[1]
    def thirdItem = testSource.table[0]

    def "should correctly compare all fields in equals method"() {
        given:
        def item1 = new Item(
                id: 1L,
                inventoryNumber: "INV001",
                description: "Item1",
                incomingDate: LocalDate.of(2021, 1, 1),
                outgoingDate: LocalDate.of(2022, 1, 1),
                modificationDate: LocalDate.of(2021, 6, 1),
                itemQuantity: 5,
                itemPrice: new BigDecimal("100.00"),
                documentNumber: "DOC001",
                user: new User(id: 1L, name: "User1")
        )
        def item2 = new Item(
                id: 1L,
                inventoryNumber: "INV001",
                description: "Item1",
                incomingDate: LocalDate.of(2021, 1, 1),
                outgoingDate: LocalDate.of(2022, 1, 1),
                modificationDate: LocalDate.of(2021, 6, 1),
                itemQuantity: 5,
                itemPrice: new BigDecimal("100.00"),
                documentNumber: "DOC001",
                user: new User(id: 1L, name: "User1")
        )
        when:
        def result = item1.equals(item2)
        then:
        result

        when:
        item2.setDescription("Different Description")

        then:
        !item1.equals(item2)

        when:
        item2.setDescription(item1.getDescription())
        item2.setOutgoingDate(LocalDate.of(2023, 1, 1))

        then:
        !item1.equals(item2)

        when:
        item2.setOutgoingDate(item1.getOutgoingDate())
        item2.setItemQuantity(10)

        then:
        !item1.equals(item2)

        when:
        item2.setItemQuantity(item1.getItemQuantity())
        item2.setItemPrice(new BigDecimal("200.00"))

        then:
        !item1.equals(item2)

        when:
        item2.setItemPrice(item1.getItemPrice())
        item2.setDocumentNumber("Different Doc")

        then:
        !item1.equals(item2)

        when:
        item2.setDocumentNumber(item1.getDocumentNumber())
        item2.setUser(new User(id: 2L, name: "User2"))

        then:
        !item1.equals(item2)
    }

    def "should return same hash when two objects are the same false otherwise"() {
        when:
        def firstResult = firstItem.hashCode() != secondItem.hashCode()
        def secondResult = secondItem.hashCode() != thirdItem.hashCode()
        def thirdResult = firstItem.hashCode() == thirdItem.hashCode()

        then:
        firstResult
        secondResult
        thirdResult
    }

    def "should return false if one of comparing objects is null or different type"() {
        given:
        def differentTypeObj = testSource.room1

        when:
        def firstResult = secondItem.equals(null)

        then:
        !firstResult

        when:
        def secondResult = firstItem.equals(differentTypeObj)

        then:
        !secondResult
    }

    def "getNumber() called should return a number of Item object"() {
        when:
        def callingResult = firstItem.getNumber()

        then:
        callingResult == firstItem.inventoryNumber
    }
}
