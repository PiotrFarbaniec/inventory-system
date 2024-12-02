package pl.inventory.system.model

import pl.inventory.system.ObjectsProvider
import spock.lang.Specification

class UserTest extends Specification {
    private ObjectsProvider testSource = new ObjectsProvider()
    def firstUser = testSource.user[0]
    def secondUser = testSource.user[1]
    def thirdUser = testSource.user[0]

    def "should return true when two objects are equal false otherwise"() {
        when:
        def firstResult = firstUser.equals(secondUser)
        def secondResult = secondUser.equals(thirdUser)
        def thirdResult = firstUser.equals(thirdUser)

        then:
        !firstResult && firstUser.hashCode().compareTo(secondUser.hashCode()) != 0
        !secondResult && secondUser.hashCode().compareTo(thirdUser.hashCode()) != 0
        thirdResult && firstUser.hashCode().compareTo(thirdUser.hashCode()) == 0
    }

    def "should return false if one of comparing objects is null or different type"() {
        given:
        def differentTypeObj = testSource.table[0]

        when:
        def firstResult = firstUser.equals(null)
        def secondResult = firstUser.equals(differentTypeObj)

        then:
        !(firstResult && secondResult)

    }
}
