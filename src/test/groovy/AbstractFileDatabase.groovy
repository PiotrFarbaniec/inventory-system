import pl.inventory.system.ObjectsProvider
import pl.inventory.system.database.Database
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.service.ItemService
import pl.inventory.system.service.RoomService
import pl.inventory.system.utils.FileManager
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification

class AbstractFileDatabase extends Specification {
    def directory = "TestFiles"
    def file = "roomTest.txt"
    def idRoomFile = "idRoomTest.txt"
    def idItemFile = "idItemTest.txt"

    def filePath = FileManager.createFile(file, directory)
    def idRoomPath = FileManager.createFile(idRoomFile, directory)
    def idItemPath = FileManager.createFile(idItemFile, directory)


    def fileService = new FileService()
    def serializer = new JsonSerializer()
    def itemIdProvider = new IdProvider(idItemPath, fileService)
    def roomIdProvider = new IdProvider(idRoomPath, fileService)

    Database<Room, Item> database = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
    final def itemService = new ItemService(database)
    final def roomService = new RoomService(database)
    final def source = new ObjectsProvider()

    /*def "should create entries for tests if database is empty"() {
        given:
        if (database.getAll().isEmpty()) {
            roomService.save(source.room1)
            roomService.save(source.room2)
            roomService.save(source.room3)
        }
    }*/
}
