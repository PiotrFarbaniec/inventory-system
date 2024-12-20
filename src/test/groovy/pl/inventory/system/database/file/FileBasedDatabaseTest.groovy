package pl.inventory.system.database.file

import pl.inventory.system.AbstractDatabaseTest

import java.time.LocalDate

class FileBasedDatabaseTest extends AbstractDatabaseTest {

    def "should not save if type of given property is wrong"() {
        given:
        def wrongRoomProperty = LocalDate.of(2010, 9, 12)
        def itemToSave = source.computer[1]

        when:
        def savingResult = fileDatabase.saveInObjectWithProperty(wrongRoomProperty, itemToSave)

        then:
        savingResult == Optional.empty()
    }

    def "should return empty list for wrong type of searched Room property"() {
        given:
        def wrongRoomProperty = LocalDate.of(2010, 9, 12)

        when:
        def searchingResult = fileDatabase.getAllFromObjectWithProperty(wrongRoomProperty)

        then:
        searchingResult == List.of()
    }

    def "should return empty Optional for wrong type of searched Item property"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)

        when:
        def searchingResult = fileDatabase.getItemByProperty(wrongItemProperty)

        then:
        searchingResult == Optional.empty()
    }

    def "should return empty Optional for wrong type of removed Item property"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)

        when:
        def deletingResult = fileDatabase.deleteItemByProperty(wrongItemProperty)

        then:
        deletingResult == Optional.empty()
    }

    def "should return empty Optional for wrong property type of updating Item"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)
        def updateItem = source.wardrobe[1]

        when:
        def deletingResult = fileDatabase.updateItemByProperty(wrongItemProperty, updateItem)

        then:
        deletingResult == Optional.empty()
    }

    def "deletion of files after tests"() {
        cleanup:
        cleanDatabase()
    }
}
