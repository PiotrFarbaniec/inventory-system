package pl.inventory.system.database.file

import pl.inventory.system.model.Room
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class FileBasedDatabaseConfigTest extends Specification {
    String filesDirectory = "Test Files"
    String roomFile = "Room Test File.txt"
    String roomIdFile = "Room Id File.txt"
    String itemIdFile = "Item Id File.txt"

    def databaseConfig = new FileBasedDatabaseConfig(
            filesDirectory,
            roomFile,
            roomIdFile,
            itemIdFile
    )

    def "should create a file storing Room objects"() {
        when:
        def roomPath = databaseConfig.roomFilePath()

        then:
        roomPath.toFile().exists()
        roomPath.getName(0).toString() == filesDirectory
        roomPath.getName(1).toString() == roomFile

    }

    def "should create a file for store Room id"() {
        when:
        def roomIdProvider = databaseConfig.roomIdProvider(new FileService())
        File roomId = new File(new File(filesDirectory), roomIdFile)

        then:
        roomId.getName() == roomIdFile
        roomIdProvider.getCurrentIdAndIncrement() == 1
        roomIdProvider.getCurrentIdAndIncrement() == 2
        roomIdProvider.getCurrentIdAndIncrement() == 3

    }

    def "ItemIdProvider"() {
        when:
        def itemIdProvider = databaseConfig.itemIdProvider(new FileService())
        File itemId = new File(new File(filesDirectory), itemIdFile)

        then:
        itemId.getName() == itemIdFile
        itemIdProvider.getCurrentIdAndIncrement() == 1
        itemIdProvider.getCurrentIdAndIncrement() == 2
        itemIdProvider.getCurrentIdAndIncrement() == 3

    }

    def "should create an instance of Database for Room objects"() {
        given:
        def fileService = new FileService()
        def serializer = new JsonSerializer()

        when:
        def dbInstance = databaseConfig.roomDatabase(fileService, serializer)

        then:
        dbInstance != null
        //println dbInstance.getClass().getDeclaredFields().toList().contains(Room)
    }

    /*def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(Path.of(roomFile))
        Files.deleteIfExists(Path.of(roomIdFile))
        Files.deleteIfExists(Path.of(itemIdFile))
        Files.deleteIfExists(Path.of(filesDirectory))
    }*/

    def "deletion of files after tests"() {
        cleanup:
        deleteDirectoryRecursively(Path.of(filesDirectory))
    }

    private void deleteDirectoryRecursively(Path path) {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach { p -> deleteDirectoryRecursively(p) }
        }
        Files.deleteIfExists(path)
    }
}
