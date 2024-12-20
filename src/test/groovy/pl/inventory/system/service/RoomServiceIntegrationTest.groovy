package pl.inventory.system.service

import pl.inventory.system.AbstractDatabaseTest
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Room

import java.nio.file.Path

class RoomServiceIntegrationTest extends AbstractDatabaseTest {

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
        def firstRoom = source.room1
        def secondRoom = source.room2
        def thirdRoom = source.room3

        when:
        def firstSave = roomService.save(firstRoom)
        def secondSave = roomService.save(secondRoom)
        def thirdSave = roomService.save(thirdRoom)
        def result = roomService.getAll()

        then:
        firstSave == 1 && secondSave == 2 && thirdSave == 3
        result.size() == 3
        result.get(0) == source.room1
        result.get(1) == source.room2
        result.get(2) == source.room3
    }

    def "should return an object by id if present"() {
        when:
        def firstResult = roomService.getById(1)
        def secondResult = roomService.getById(2)

        then:
        firstResult.isPresent()
        firstResult.get() == source.room1
        secondResult.isPresent()
        secondResult.get() == source.room2

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
        def updateRoom = source.room4
        updateRoom.setRoomNumber(source.room1.getRoomNumber())

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
        def updateRoom = source.room4
        final def oldContent = roomService.getAll()

        when:
        def updateResult = roomService.updateById(wrongId, updateRoom)

        then:
        updateResult == Optional.empty()
        oldContent == roomService.getAll()
    }

    def "should not update if Room contains null/empty Items list"() {
        given:
        def updateRoom = source.room4
        def emptyItemList = new ArrayList()
        final def oldContent = roomService.getAll()

        when:
        updateRoom.setItemsList(emptyItemList)
        def thirdResult = roomService.updateById(1, updateRoom)

        then:
        thirdResult == Optional.empty()
        oldContent == roomService.getAll()

        when:
        updateRoom.setItemsList(null)
        def fourthResult = roomService.updateById(1, updateRoom)

        then:
        fourthResult.empty
        oldContent == roomService.getAll()
    }

    def "should update if room with specified id or number exists"() {
        given:
        final def oldContent = roomService.getAll()
        final def updatedRoomNumber = roomService.getById(1).get().number
        def updateRoom = source.room4

        when:   'when the updated room number is null it should be set according to the found'
        updateRoom.setRoomNumber(null)
        def firstResult = roomService.updateById(1, updateRoom)

        then:
        firstResult.get().number == updatedRoomNumber
        oldContent != roomService.getAll()

        when:   'while Room updating, only Item list may change, not its number'
        updateRoom.setRoomNumber('936')
        def secondResult = roomService.updateByNumber(oldContent.get(1).number, updateRoom)

        then:
        secondResult.present
        oldContent != roomService.getAll()
        secondResult.get() != oldContent.get(1)
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
        cleanDatabase()
    }
}
