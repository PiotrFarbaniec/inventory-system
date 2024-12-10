package pl.inventory.system.service

import pl.inventory.system.database.Database
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.utils.FileManager
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification
import pl.inventory.system.ObjectsProvider

import java.nio.file.Files
import java.nio.file.Path

class RoomServiceIntegrationTest extends Specification {

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
    def roomService = new RoomService(database)
    def test = new ObjectsProvider()

    def "should return empty list when database is empty or file does not exist"() {
        when:
        def emptyDatabase = new FileBasedDatabase(Path.of("NoSuchFile.txt"), itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
        def emptyService = new RoomService(emptyDatabase)
        def firstResult = emptyService.getAll()

        then:
        firstResult == List.of()

        when:
        def result = roomService.getAll()
        println(result)

        then:
        result.size() == 0
        result == List.of()
    }

    def "should not save and return zero if provided room is null"() {
        when:
        def savingResult = roomService.save(null)

        then:
        savingResult == 0L
        roomService.getAll() == List.of()
    }

    def "should saved and retrieve all saved objects from database"() {
        given:
        def firstRoom = test.room1
        def secondRoom = test.room2
        def thirdRoom = test.room3

        when:
        def firstSave = roomService.save(firstRoom)
        def secondSave = roomService.save(secondRoom)
        def thirdSave = roomService.save(thirdRoom)
        def result = roomService.getAll()

        then:
        firstSave == 1 && secondSave == 2 && thirdSave == 3
        result.size() == 3
        result.get(0) == test.room1
        result.get(1) == test.room2
        result.get(2) == test.room3
    }

    def "should return an object by id if present"() {
        when:
        def firstResult = roomService.getById(1)
        def secondResult = roomService.getById(2)

        then:
        firstResult.isPresent()
        firstResult.get() == test.room1
        secondResult.isPresent()
        secondResult.get() == test.room2

        when:
        def notExiting = roomService.getById(4)

        then:
        notExiting.isEmpty()
    }

    def "should return empty Optional if given room number or room id is null"() {
        when:
        def firstResult = roomService.getByNumber(null)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = roomService.getById(null)

        then:
        secondResult == Optional.empty()
    }

    def "should get an object by unique number if present"() {
        when:
        def correctNumber = roomService.getByNumber("101")

        then:
        correctNumber.isPresent()

        when:
        def notCorrectNumber = roomService.getByNumber("311")

        then:
        notCorrectNumber == Optional.empty()
    }

    def "should not delete if provided id or number of Room is null"() {
        when:
        def firstResult = roomService.deleteById(null)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = roomService.deleteByNumber(null)

        then:
        secondResult == Optional.empty()
    }

    def "should not update if provided id or number or update Room is null"() {
        given:
        final def oldContent = roomService.getAll()
        def updateId = 1L
        def updateNumber = "101"
        def updateRoom = test.room4
        updateRoom.setRoomNumber(test.room1.getRoomNumber())

        when:
        def firstResult = roomService.updateById(null, updateRoom)

        then:
        firstResult == Optional.empty()
        oldContent == roomService.getAll()

        when:
        def secondResult = roomService.updateById(updateId, null)

        then:
        secondResult == Optional.empty()
        oldContent == roomService.getAll()

        when:
        def thirdResult = roomService.updateByNumber(null, updateRoom)

        then:
        thirdResult == Optional.empty()
        oldContent == roomService.getAll()

        when:
        def fourthResult = roomService.updateByNumber(updateNumber, null)

        then:
        fourthResult == Optional.empty()
        oldContent == roomService.getAll()
    }

    def "should not update if room with specified id does not exist"() {
        given:
        def wrongId = 5L
        def updateRoom = test.room4
        final def oldContent = roomService.getAll()

        when:
        def updateResult = roomService.updateById(wrongId, updateRoom)

        then:
        updateResult == Optional.empty()
        oldContent == roomService.getAll()
    }

    def "should not update if Room is empty or has wrong number"() {
        given:
        def updateRoom = test.room4
        def emptyItemList = new ArrayList()
        final def oldContent = roomService.getAll()

        when:
        def firstResult = roomService.updateById(1, updateRoom)

        then:
        firstResult == Optional.empty()
        oldContent == roomService.getAll()

        when:
        updateRoom.setItemsList(emptyItemList)
        updateRoom.setRoomNumber(oldContent.get(0).getNumber())
        def thirdResult = roomService.updateById(1, updateRoom)

        then:
        thirdResult == Optional.empty()
        oldContent == roomService.getAll()
    }

    def "should update if room with specified id or number exists"() {
        given:
        final def oldContent = roomService.getAll()
        def updateRoom = test.room4

        when:
        updateRoom.setRoomNumber(test.room1.number)
        def firstResult = roomService.updateById(1, updateRoom)

        then:
        firstResult == Optional.of(updateRoom)
        oldContent != roomService.getAll()

        when:
        updateRoom.setRoomNumber("906")
        def secondResult = roomService.updateByNumber(oldContent.get(1).number, updateRoom)

        then:
        secondResult.present
        oldContent != roomService.getAll()
        roomService.getById(2).get() == secondResult.get()
    }

    def "should delete Room by specified id if present"() {
        given:
        def existingRoomId = 1L
        def falseRoomId = 5L
        final def originalFileContent = roomService.getAll()

        when:
        def firstResult = roomService.deleteById(falseRoomId)

        then:
        firstResult == Optional.empty()
        roomService.getAll() == originalFileContent

        when:
        def secondResult = roomService.deleteById(existingRoomId)

        then:
        secondResult.get() == originalFileContent.get(0)
        roomService.getById(1).isEmpty()
        roomService.getById(2).isPresent()
        roomService.getById(3).isPresent()
    }

    def "should delete Room by specified number if present"() {
        given:
        def existingRoomNumber = "102"
        def nonexistentRoomNumber = "407"
        final def oldFileContent = roomService.getAll()

        when:
        def firstResult = roomService.deleteByNumber(nonexistentRoomNumber)

        then:
        firstResult == Optional.empty()
        roomService.getAll().size() == oldFileContent.size()

        when:
        def secondResult = roomService.deleteByNumber(existingRoomNumber)

        then:
        oldFileContent.contains(secondResult.get())
        roomService.getAll().size() == oldFileContent.size() - 1
        roomService.getByNumber(existingRoomNumber).isEmpty()
    }

    def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        Files.deleteIfExists(idItemPath)
        Files.deleteIfExists(Path.of(directory))
    }
}
