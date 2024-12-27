package pl.inventory.system

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import pl.inventory.system.controller.ItemController
import pl.inventory.system.controller.RoomController
import pl.inventory.system.database.Database
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.service.ItemService
import pl.inventory.system.service.RoomService
import pl.inventory.system.utils.FileManager
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

abstract class AbstractDatabaseTest extends Specification {
    Path filePath
    Path idRoomPath
    Path idItemPath

    FileService fileService
    JsonService serializer
    IdProvider itemIdProvider
    IdProvider roomIdProvider
    Database<Room, Item> fileDatabase
    ItemService itemService
    RoomService roomService
    ObjectsProvider source
    ItemController itemController
    RoomController roomController
    MockMvc itemMVC
    MockMvc roomMVC

    def setup() {
        def directory = "TestFiles"
        def file = "roomTest.txt"
        def idRoomFile = "idRoomTest.txt"
        def idItemFile = "idItemTest.txt"

        filePath = FileManager.createFile(file, directory)
        idRoomPath = FileManager.createFile(idRoomFile, directory)
        idItemPath = FileManager.createFile(idItemFile, directory)

        fileService = new FileService()
        serializer = new JsonService()
        itemIdProvider = new IdProvider(idItemPath, fileService)
        roomIdProvider = new IdProvider(idRoomPath, fileService)

        fileDatabase = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
        itemService = new ItemService(fileDatabase)
        roomService = new RoomService(fileDatabase)
        source = new ObjectsProvider()

        itemController = new ItemController(itemService)
        roomController = new RoomController(roomService)

        itemMVC = MockMvcBuilders.standaloneSetup(itemController).build()
        roomMVC = MockMvcBuilders.standaloneSetup(roomController).build()
    }

    def cleanDatabase() {
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        Files.deleteIfExists(idItemPath)
        //Files.deleteIfExists(Path.of(directory))
        if (Files.isDirectory(Path.of("TestFiles"))
                && Files.list(Path.of("TestFiles")).count() == 0) {
            Files.delete(Path.of("TestFiles"))
        }
    }
}
