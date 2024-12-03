package pl.inventory.system.service

import pl.inventory.system.database.Database
import pl.inventory.system.database.file.FileBasedDatabase
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

    Database<Room> database = new FileBasedDatabase<>(filePath, roomIdProvider, fileService, serializer, Room.class)
    def service = new RoomService(database, itemIdProvider)
    ObjectsProvider test = new ObjectsProvider()

    def "should return empty list when database is empty or file does not exist"() {
        when:
        def emptyDatabase = new FileBasedDatabase<>(Path.of("NoSuchFile.txt"), roomIdProvider, fileService, serializer, Room.class)
        def emptyService = new RoomService(emptyDatabase, itemIdProvider)
        def firstResult = emptyService.getAll()

        then:
        firstResult == List.of()

        when:
        def result = service.getAll()

        then:
        result.size() == 0
        result == List.of()
    }

    def "should not save and return zero if provided room is null"() {
        when:
        def savingResult = service.save(null)

        then:
        savingResult == 0L
        service.getAll() == List.of()
    }

    def "should saved and retrieve all saved objects from database"() {
        given:
        def firstRoom = test.room1
        def secondRoom = test.room2
        def thirdRoom = test.room3

        when:
        def firstSave = service.save(firstRoom)
        def secondSave = service.save(secondRoom)
        def thirdSave = service.save(thirdRoom)
        def result = service.getAll()

        then:
        firstSave == 1 && secondSave == 2 && thirdSave == 3
        result.size() == 3
        result.get(0) == test.room1
        result.get(1) == test.room2
        result.get(2) == test.room3
    }

    /*def "should saved rooms in database"() {
        given:
        def firstRoom = test.room1
        def secondRoom = test.room2
        def thirdRoom = test.room3

        when:
        def firstResult = service.save(firstRoom)
        def secondResult = service.save(secondRoom)
        def thirdResult = service.save(thirdRoom)

        then:
        firstResult == 1 && secondResult == 2 && thirdResult == 3
    }*/

    /*def "should retrieve all saved objects from database"() {
        given:
        service.save(test.room1)
        service.save(test.room2)
        service.save(test.room3)

        when:
        def result = service.getAll()

        then:
        result.size() == 3
        result.get(0) == test.room1
        result.get(1) == test.room2
        result.get(2) == test.room3
    }*/

    def "should return an object by id if present"() {
        given:
        service.save(test.room1)
        service.save(test.room2)

        when:
        def firstResult = service.getById(1)
        def secondResult = service.getById(2)

        then:
        firstResult.isPresent()
        firstResult.get() == test.room1
        secondResult.isPresent()
        secondResult.get() == test.room2

        when:
        def notExiting = service.getById(3)

        then:
        notExiting.isEmpty()
    }

    def "should return empty Optional if given room number or room id is null"() {
        when:
        def firstResult = service.getByNumber(null)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = service.getById(null)

        then:
        secondResult == Optional.empty()
    }

    def "should get an object by unique number if present"() {
        given:
        service.save(test.room1)

        when:
        def correctNumber = service.getByNumber("101")

        then:
        correctNumber.isPresent()

        when:
        def notCorrectNumber = service.getByNumber("311")

        then:
        notCorrectNumber == Optional.empty()
    }

    def "should not delete if provided id or number of Room is null"() {
        given:
        service.save(test.room1)
        service.save(test.room2)

        when:
        def firstResult = service.deleteById(null)

        then:
        firstResult == Optional.empty()
        service.getAll().size() == 2

        when:
        def secondResult = service.deleteByNumber(null)

        then:
        secondResult == Optional.empty()
        service.getAll().size() == 2
    }

    def "should delete Room by specified id if present"() {
        given:
        def existingRoomId = 1L
        def nonexistentRoomId = 5L
        service.save(test.room1)
        service.save(test.room2)

        when:
        service.deleteById(nonexistentRoomId)

        then:
        service.getAll().size() == 2
        service.getById(1).isPresent()
        service.getById(2).isPresent()

        when:
        service.deleteById(existingRoomId)

        then:
        service.getAll().size() == 1
        service.getById(1).isEmpty()
        service.getById(2).isPresent()
    }

    def "should delete Room by specified number if present"() {
        given:
        def existingRoomNumber = "102"
        def nonexistentRoomNumber = "407"
        service.save(test.room1)
        service.save(test.room2)

        when:
        service.deleteByNumber(nonexistentRoomNumber)

        then:
        service.getAll().size() == 2
        service.getById(1).isPresent()
        service.getById(2).isPresent()

        when:
        service.deleteByNumber(existingRoomNumber)

        then:
        service.getAll().size() == 1
        service.getById(1).isPresent()
        service.getById(2).isEmpty()
    }

    def "should not update if provided id or number or update Room is null"() {
        given:
        def storedRoom = service.save(test.room1)
        def updateRoom = test.room2
        updateRoom.setId(storedRoom)
        updateRoom.setRoomNumber(test.room1.getRoomNumber())

        when:
        def firstResult = service.updateById(null, updateRoom)

        then:
        firstResult == Optional.empty()
        service.getAll().get(0) != updateRoom

        when:
        def secondResult = service.updateById(storedRoom, null)

        then:
        secondResult == Optional.empty()
        service.getAll().get(0) == test.room1

        when:
        def thirdResult = service.updateByNumber(null, updateRoom)

        then:
        thirdResult == Optional.empty()
        service.getAll().get(0) != updateRoom

        when:
        def fourthResult = service.updateByNumber(test.room1.number, null)

        then:
        fourthResult == Optional.empty()
        service.getAll().get(0) == test.room1
    }

    def "should not update if room with specified id does not exist"() {
        given:
        def updateRoom = test.room3
        service.save(test.room1)
        service.save(test.room2)

        when:
        def noSuchRoom = service.updateById(3, updateRoom)

        then:
        service.getAll().size() == 2
        service.getById(1).get() == test.room1
        service.getById(2).get() == test.room2
        noSuchRoom == Optional.empty()
    }

    def "should not update if Room is empty or has wrong number"() {
        given:
        service.save(test.room1)
        service.save(test.room2)
        def updateRoom = test.room4
        def emptyItemList = new ArrayList()

        when:
        def firstResult = service.updateById(1, updateRoom)

        then:
        firstResult == Optional.empty()

        when:
        updateRoom.setItemsList(emptyItemList)
        updateRoom.setRoomNumber(test.room1.number)
        def secondResult = service.updateById(1, updateRoom)

        then:
        secondResult == Optional.empty()
    }

    def "should update if room with id and matching number is in database"() {
        given:
        def firstUpdateRoom = test.room3
        def secondUpdateRoom = test.room2
        service.save(test.room1)
        service.save(test.room2)

        when:
        firstUpdateRoom.setRoomNumber(test.room1.number)
        def firstResult = service.updateById(1, firstUpdateRoom)

        then:
        firstUpdateRoom.number == service.getById(1).get().number
        service.getAll().size() == 2
        service.getById(1).get() == test.room3
        service.getById(2).get() == test.room2
        firstResult == Optional.of(test.room3)

        when:
        def secondResult = service.updateById(2, secondUpdateRoom)

        then:
        secondResult.present
        service.getById(2).get() == secondResult.get()
    }

    def "should not update if room with specified number not exists or has no items"() {
        given:
        def updateRoom = test.room4
        def emptyItemsList = new ArrayList()
        service.save(test.room1)
        service.save(test.room2)

        when:
        def firstResult = service.updateByNumber("808", updateRoom)

        then:
        firstResult == Optional.empty()
        service.getById(1).get() == test.room1
        service.getById(2).get() == test.room2

        when:
        updateRoom.setRoomNumber(test.room1.number)
        updateRoom.setItemsList(emptyItemsList)
        def secondResult = service.updateByNumber(test.room1.number, updateRoom)

        then:
        secondResult == Optional.empty()
        service.getById(1).get() == test.room1
        service.getById(2).get() == test.room2
    }

    def "should set correct room number and update if number of given room is null or empty"() {
        given:
        def updateRoom = test.room4
        def updatedNumber = test.room2.number
        service.save(test.room1)
        service.save(test.room2)

        when:
        updateRoom.setRoomNumber(null)
        def firstResult = service.updateByNumber(updatedNumber, updateRoom)

        then:
        firstResult.present
        firstResult.get().number == updatedNumber
        firstResult.get().itemsList.size() == test.room4.itemsList.size()

        when:
        updateRoom.setRoomNumber(" ")
        def secondResult = service.updateByNumber(updatedNumber, updateRoom)

        then:
        secondResult.present
        secondResult.get().number == updatedNumber
        secondResult.get().itemsList.size() == test.room4.itemsList.size()

    }

    def "should update if room with specified number exists"() {
        given:
        def updateRoom = test.room4
        service.save(test.room1)
        service.save(test.room2)

        when:
        updateRoom.setRoomNumber(test.room1.getRoomNumber())
        def updateResult =
                service.updateByNumber(test.room1.number, updateRoom)

        then:
        updateResult == Optional.of(updateRoom)
        service.getById(1).get() == test.room4
        service.getById(2).get() == test.room2
    }
}
