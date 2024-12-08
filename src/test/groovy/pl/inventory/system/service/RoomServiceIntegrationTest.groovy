package pl.inventory.system.service

import pl.inventory.system.database.Database
import pl.inventory.system.database.file.AbstractFileDatabase
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification
import pl.inventory.system.ObjectsProvider

import java.nio.file.Files
import java.nio.file.Path

class RoomServiceIntegrationTest extends Specification {

    def filePath = Files.createTempFile("roomTest", ".txt")
    def idRoomPath = Files.createTempFile("idRoomTest", ".txt")
    def idItemPath = Files.createTempFile("idItemTest", ".txt")
    def fileService = new FileService()
    def serializer = new JsonSerializer()
    def itemIdProvider = new IdProvider(idItemPath, fileService)
    def roomIdProvider = new IdProvider(idRoomPath, fileService)

    Database<Room, Item> database = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
    def roomService = new RoomService(database)
    def itemService = new ItemService(database)
    ObjectsProvider test = new ObjectsProvider()

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
        given:
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        def firstResult = roomService.getById(1)
        def secondResult = roomService.getById(2)

        then:
        firstResult.isPresent()
        firstResult.get() == test.room1
        secondResult.isPresent()
        secondResult.get() == test.room2

        when:
        def notExiting = roomService.getById(3)

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
        given:
        roomService.save(test.room1)

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
        given:
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        def firstResult = roomService.deleteById(null)

        then:
        firstResult == Optional.empty()
        roomService.getAll().size() == 2

        when:
        def secondResult = roomService.deleteByNumber(null)

        then:
        secondResult == Optional.empty()
        roomService.getAll().size() == 2
    }

    def "should delete Room by specified id if present"() {
        given:
        def existingRoomId = 1L
        def nonexistentRoomId = 5L
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        roomService.deleteById(nonexistentRoomId)

        then:
        roomService.getAll().size() == 2
        roomService.getById(1).isPresent()
        roomService.getById(2).isPresent()

        when:
        roomService.deleteById(existingRoomId)

        then:
        roomService.getAll().size() == 1
        roomService.getById(1).isEmpty()
        roomService.getById(2).isPresent()
    }

    def "should delete Room by specified number if present"() {
        given:
        def existingRoomNumber = "102"
        def nonexistentRoomNumber = "407"
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        roomService.deleteByNumber(nonexistentRoomNumber)

        then:
        roomService.getAll().size() == 2
        roomService.getById(1).isPresent()
        roomService.getById(2).isPresent()

        when:
        roomService.deleteByNumber(existingRoomNumber)

        then:
        roomService.getAll().size() == 1
        roomService.getById(1).isPresent()
        roomService.getById(2).isEmpty()
    }

    def "should not update if provided id or number or update Room is null"() {
        given:
        def storedRoom = roomService.save(test.room1)
        def updateRoom = test.room2
        updateRoom.setId(storedRoom)
        updateRoom.setRoomNumber(test.room1.getRoomNumber())

        when:
        def firstResult = roomService.updateById(null, updateRoom)

        then:
        firstResult == Optional.empty()
        roomService.getAll().get(0) != updateRoom

        when:
        def secondResult = roomService.updateById(storedRoom, null)

        then:
        secondResult == Optional.empty()
        roomService.getAll().get(0) == test.room1

        when:
        def thirdResult = roomService.updateByNumber(null, updateRoom)

        then:
        thirdResult == Optional.empty()
        roomService.getAll().get(0) != updateRoom

        when:
        def fourthResult = roomService.updateByNumber(test.room1.number, null)

        then:
        fourthResult == Optional.empty()
        roomService.getAll().get(0) == test.room1
    }

    def "should not update if room with specified id does not exist"() {
        given:
        def updateRoom = test.room3
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        def noSuchRoom = roomService.updateById(3, updateRoom)

        then:
        roomService.getAll().size() == 2
        roomService.getById(1).get() == test.room1
        roomService.getById(2).get() == test.room2
        noSuchRoom == Optional.empty()
    }

    def "should not update if Room is empty or has wrong number"() {
        given:
        roomService.save(test.room1)
        roomService.save(test.room2)
        def updateRoom = test.room4
        def emptyItemList = new ArrayList()

        when:
        def firstResult = roomService.updateById(1, updateRoom)

        then:
        firstResult == Optional.empty()

        when:
        updateRoom.setItemsList(emptyItemList)
        updateRoom.setRoomNumber(test.room1.number)
        def secondResult = roomService.updateById(1, updateRoom)

        then:
        secondResult == Optional.empty()
    }

    def "should update if room with id and matching number is in database"() {
        given:
        def firstUpdateRoom = test.room3
        def secondUpdateRoom = test.room2
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        firstUpdateRoom.setRoomNumber(test.room1.number)
        def firstResult = roomService.updateById(1, firstUpdateRoom)

        then:
        firstUpdateRoom.number == roomService.getById(1).get().number
        roomService.getAll().size() == 2
        roomService.getById(1).get() == test.room3
        roomService.getById(2).get() == test.room2
        firstResult == Optional.of(test.room3)

        when:
        def secondResult = roomService.updateById(2, secondUpdateRoom)

        then:
        secondResult.present
        roomService.getById(2).get() == secondResult.get()
    }

    def "should not update if room with specified number not exists or has no items"() {
        given:
        def updateRoom = test.room4
        def emptyItemsList = new ArrayList()
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        def firstResult = roomService.updateByNumber("808", updateRoom)

        then:
        firstResult == Optional.empty()
        roomService.getById(1).get() == test.room1
        roomService.getById(2).get() == test.room2

        when:
        updateRoom.setRoomNumber(test.room1.number)
        updateRoom.setItemsList(emptyItemsList)
        def secondResult = roomService.updateByNumber(test.room1.number, updateRoom)

        then:
        secondResult == Optional.empty()
        roomService.getById(1).get() == test.room1
        roomService.getById(2).get() == test.room2
    }

    /*def "should set correct room number and update if number of given room is null or empty"() {
        given:
        def updateRoom = test.room4
        def updatedNumber = test.room2.number
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        updateRoom.setRoomNumber(null)
        def firstResult = roomService.updateByNumber(updatedNumber, updateRoom)

        then:
        firstResult.present
        firstResult.get().number == updatedNumber
        firstResult.get().itemsList.size() == test.room4.itemsList.size()

        when:
        updateRoom.setRoomNumber(" ")
        def secondResult = roomService.updateByNumber(updatedNumber, updateRoom)

        then:
        secondResult.present
        secondResult.get().number == updatedNumber
        secondResult.get().itemsList.size() == test.room4.itemsList.size()
    }*/

    def "should set correct room number and update if number of given room is null or empty"() {
        given:
        def updateRoom = test.room4
        def updatedNumber = test.room2.number
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        updateRoom.setRoomNumber(null)
        def firstResult = roomService.updateByNumber(updatedNumber, updateRoom)

        then:
        firstResult.present
        firstResult.get().number == updatedNumber
        firstResult.get().itemsList.size() == test.room4.itemsList.size()

        when:
        updateRoom.setRoomNumber(" ")
        def secondResult = roomService.updateByNumber(updatedNumber, updateRoom)

        then:
        secondResult.present
        secondResult.get().number == updatedNumber
        secondResult.get().itemsList.size() == test.room4.itemsList.size()
    }





    def "should update if room with specified number exists"() {
        given:
        def updateRoom = test.room4
        roomService.save(test.room1)
        roomService.save(test.room2)

        when:
        updateRoom.setRoomNumber(test.room1.getRoomNumber())
        def updateResult =
                roomService.updateByNumber(test.room1.number, updateRoom)

        then:
        updateResult == Optional.of(updateRoom)
        roomService.getById(1).get() == test.room4
        roomService.getById(2).get() == test.room2
    }
}
