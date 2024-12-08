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

class FileBasedDatabaseTest extends Specification {
    def filePath = Files.createTempFile("roomTest", ".txt")
    def idRoomPath = Files.createTempFile("idRoomTest", ".txt")
    def idItemPath = Files.createTempFile("idItemTest", ".txt")
    def fileService = new FileService()
    def serializer = new JsonSerializer()
    def itemIdProvider = new IdProvider(idItemPath, fileService)
    def roomIdProvider = new IdProvider(idRoomPath, fileService)

    private final Database<Room, Item> database = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
    private final ObjectsProvider source = new ObjectsProvider()

    def "should not update and return empty Optional if Room with provided id doesn't exist"() {
        given:
        def noSuchId = 4L
        database.save(source.room1)
        def updateRoom = source.room4

        when:
        def updateResult = database.updateByProperty(noSuchId, updateRoom)

        then:
        updateResult == Optional.empty()
    }

    def "should not update and return empty Optional if Room with provided number doesn't exist"() {
        given:
        def noSuchRoomNumber = "310"
        def updateRoom = source.room2
        when:
        def updateResult = database.updateByProperty(noSuchRoomNumber, updateRoom)

        then:
        updateResult == Optional.empty()
    }

    /*def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        Files.deleteIfExists(Path.of(directory))
    }*/
}
