package pl.inventory.system.database.file

import pl.inventory.system.ObjectsProvider
import pl.inventory.system.database.Database
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.service.ItemService
import pl.inventory.system.service.RoomService
import pl.inventory.system.utils.FileManager
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class FileBasedDatabaseTest extends Specification {
    private def filePath = Files.createTempFile("roomTest", ".txt")
    private def idRoomPath = Files.createTempFile("idRoomTest", ".txt")
    private def idItemPath = Files.createTempFile("idItemTest", ".txt")
    private def fileService = new FileService()
    private def serializer = new JsonSerializer()
    private def itemIdProvider = new IdProvider(idItemPath, fileService)
    private def roomIdProvider = new IdProvider(idRoomPath, fileService)

    private Database<Room, Item> database = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
    private ObjectsProvider source = new ObjectsProvider()

    def "should not save if type of given property is wrong"() {
        given:
        def wrongRoomProperty = LocalDate.of(2010, 9, 12)
        def itemToSave = source.computer[1]

        when:
        def savingResult = database.saveInObjectWithProperty(wrongRoomProperty, itemToSave)

        then:
        savingResult == Optional.empty()
    }

    def "should return empty list for wrong type of searched Room property"() {
        given:
        def wrongRoomProperty = LocalDate.of(2010, 9, 12)

        when:
        def searchingResult = database.getAllFromObjectWithProperty(wrongRoomProperty)

        then:
        searchingResult == List.of()
    }

    def "should return empty Optional for wrong type of searched Item property"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)

        when:
        def searchingResult = database.getItemByProperty(wrongItemProperty)

        then:
        searchingResult == Optional.empty()
    }

    def "should return empty Optional for wrong type of removed Item property"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)

        when:
        def deletingResult = database.deleteItemByProperty(wrongItemProperty)

        then:
        deletingResult == Optional.empty()
    }

    def "should return empty Optional for wrong property type of updating Item"() {
        given:
        def wrongItemProperty = LocalDate.of(2010, 9, 12)
        def updateItem = source.wardrobe[1]

        when:
        def deletingResult = database.updateItemByProperty(wrongItemProperty, updateItem)

        then:
        deletingResult == Optional.empty()
    }

    def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        //Files.deleteIfExists(Path.of(directory))
    }
}
