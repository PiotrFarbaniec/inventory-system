package pl.inventory.system.model

import pl.inventory.system.ObjectsProvider
import spock.lang.Specification

class RoomTest extends Specification {
    private ObjectsProvider testSource = new ObjectsProvider()
    def firstRoom = testSource.room1
    def secondRoom = testSource.room2
    def thirdRoom = testSource.room1

    def "should return false if one of comparing objects is null or different type"() {
        given:
        def differentTypeObj = testSource.user[0]
        def sameType = thirdRoom

        when:
        def firstResult = firstRoom.equals(null)

        then:
        !firstResult

        when:
        def secondResult = firstRoom.equals(differentTypeObj)

        then:
        !secondResult

        when:
        def thirdResult = firstRoom.equals(sameType)

        then:
        thirdResult
    }

    def "should return same hash when two objects are the same false otherwise"() {
        when:
        def firstResult = firstRoom.hashCode() != secondRoom.hashCode()
        def secondResult = secondRoom.hashCode() != thirdRoom.hashCode()
        def thirdResult = firstRoom.hashCode() == thirdRoom.hashCode()

        then:
        firstResult
        secondResult
        thirdResult
    }
}
