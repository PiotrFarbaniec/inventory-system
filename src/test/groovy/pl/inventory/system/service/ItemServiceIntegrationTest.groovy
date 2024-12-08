package pl.inventory.system.service

import pl.inventory.system.ObjectsProvider
import pl.inventory.system.database.Database
import pl.inventory.system.database.file.AbstractFileDatabase
import pl.inventory.system.database.file.FileBasedDatabase
import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.utils.FileManager
import pl.inventory.system.utils.FileService
import pl.inventory.system.utils.IdProvider
import pl.inventory.system.utils.JsonSerializer
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class ItemServiceIntegrationTest extends Specification {
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
    def service = new ItemService(database)
    def source = new ObjectsProvider()

    def "should create entries for tests if database is empty"() {
        given:
        def roomService = new RoomService(database)
        if (database.getAll().isEmpty()) {
            roomService.save(source.room1)
            roomService.save(source.room2)
            roomService.save(source.room3)
        }
        database.getAllItems().forEach {println(it)}
    }

    def "should save Item by specified Room id with current modification date if already exists"() {
        given:
        final def oldRoom = database.getByProperty(1L).get()
        def updateItem = source.table[0]
        updateItem.setModificationDate(LocalDate.of(2012, 02, 12))

        when:
        def result = service.saveToRoomId(oldRoom.getId(), updateItem)

        then:
        oldRoom.itemsList.get(0).getModificationDate() != result.get().itemsList.get(0).getModificationDate()
        result.get().itemsList.get(0).getModificationDate() == LocalDate.now()
    }

    def "should save given Item to Room with specified id if exists"() {
        given:
        def existingRoomId = 1L
        def nonexistentRoomId = 7L
        final def oldRoom = database.getByProperty(existingRoomId)
        def itemToSave = source.printer[1]

        when:
        def firstResult = service.saveToRoomId(nonexistentRoomId, itemToSave)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = service.saveToRoomId(existingRoomId, itemToSave)

        then:
        oldRoom.get().itemsList != secondResult.get().itemsList
        database.getByProperty(existingRoomId).get().itemsList.contains(itemToSave)
    }

    def "should overwrite with current modification date if Item exists in Room with specified number"() {
        given:
        def nonexistentRoomNumber = "603"
        def existingRoomNumber = "201"
        final def oldRoom = database.getByProperty(existingRoomNumber)
        def itemToSave = source.table[0]

        when:
        def firstResult = service.saveToRoomNumber(nonexistentRoomNumber, itemToSave)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = service.saveToRoomNumber(existingRoomNumber, itemToSave)

        then:
        secondResult.get().itemsList.get(0).modificationDate != oldRoom.get().itemsList.get(0).modificationDate
    }

    def "should save given Item to Room with specified number"() {
        given:
        def roomNumber = "201"
        final def oldRoom = database.getByProperty(roomNumber)
        def itemToSave = source.printer[0]

        when:
        def result = service.saveToRoomNumber(roomNumber, itemToSave)

        then:
        oldRoom.get() != result.get()
        database.getByProperty(roomNumber).get().itemsList.contains(itemToSave)
    }

    def "should return all items saved in the rooms"() {
        given:
        def currentItemsNumber = 26

        when:
        def result = service.getAll().size()

        then:
        result == currentItemsNumber
    }

    def "should return a list of all items saved in the room with the given number if exist"() {
        given:
        def firstRoomNumber = "101"
        def firstRoomItemsNumber = 7
        def thirdRoomNumber = "201"
        def thirdRoomItemsNumber = 11
        def noSuchRoomNumber = "507"

        when:
        def firstResult = service.getAllByRoomNumber(firstRoomNumber)

        then:
        firstResult.size() == firstRoomItemsNumber

        when:
        def secondResult = service.getAllByRoomNumber(thirdRoomNumber)

        then:
        secondResult.size() == thirdRoomItemsNumber

        when:
        def thirdResult = service.getAllByRoomNumber(noSuchRoomNumber)

        then:
        thirdResult.isEmpty()
    }

    def "should return a list of all items saved in the room with the given id if exist"() {
        given:
        def firstRoomId = 1L
        def firstRoomItemsNumber = 7
        def thirdRoomId = 3L
        def thirdRoomItemsNumber = 11
        def noSuchRoomId = 9L

        when:
        def firstResult = service.getAllByRoomId(firstRoomId)

        then:
        firstResult.size() == firstRoomItemsNumber

        when:
        def secondResult = service.getAllByRoomId(thirdRoomId)

        then:
        secondResult.size() == thirdRoomItemsNumber

        when:
        def thirdResult = service.getAllByRoomId(noSuchRoomId)

        then:
        thirdResult.isEmpty()
    }

    def "should return an Item with the given id if exists"() {
        given:
        def existingId = 7L
        def nonexistentId = 408L

        when:
        def firstResult = service.getById(existingId)

        then:
        firstResult.present
        println(database.getAllItems())

        when:
        def secondResult = service.getById(nonexistentId)

        then:
        secondResult == Optional.empty()
    }

    def "should return an Item with the given number if exists"() {
        given:
        def existingItemNumber = "PŚT-11/222"
        def noSuchItemNumber = "PŚT-88/770"

        when:
        def presentItem = service.getByNumber(existingItemNumber)

        then:
        presentItem.present

        when:
        def nonexistentItem = service.getByNumber(noSuchItemNumber)

        then:
        nonexistentItem.isEmpty()
    }

    def "should delete Item with specified id if exist"() {
        given:
        def roomId = 3L
        def nonexistentItemId = 47L
        def existedItemId = 26L
        final def oldRoom = database.getByProperty(roomId).get()

        when:
        def firstResult = service.deleteById(nonexistentItemId)

        then:
        oldRoom == database.getByProperty(roomId).get()
        firstResult == Optional.empty()

        when:
        def secondResult = service.deleteById(existedItemId)

        then:
        secondResult.get().getId() == existedItemId
        !database.getByProperty(roomId).get().itemsList.contains(secondResult.get())
    }

    def "should delete Item with specified number if exist"() {
        given:
        def roomNumber = "102"
        def nonexistentItemNumber = "NEI-22/455"
        def existedItemNumber = "PŚT-11/444"
        final def oldRoom = database.getByProperty(roomNumber).get()

        when:
        def firstResult = service.deleteByNumber(nonexistentItemNumber)

        then:
        oldRoom == database.getByProperty(roomNumber).get()
        firstResult == Optional.empty()

        when:
        def secondResult = service.deleteByNumber(existedItemNumber)

        then:
        secondResult.get().getInventoryNumber() == existedItemNumber
        service.getByNumber(existedItemNumber).empty
        !database.getByProperty(roomNumber).get().itemsList.contains(secondResult.get())
    }

    def "should not update and return EMPTY Optional when Item with given id does not exist"() {
        given:
        def id = 308L
        Item updateItem = source.chair[1]

        when:
        def result = service.updateById(id, updateItem)

        then:
        result == Optional.empty()
    }

    def "should update and return updated Item when given id exists in database"() {
        given:
        def id = 7L
        def updatedRoom = "102"
        final def oldItem = service.getById(id)
        final def oldItemsList = service.getAllByRoomNumber(updatedRoom)
        def updateItem = source.table[3]

        when:
        def result = service.updateById(id, updateItem)

        then:
        oldItem.get().id == result.get().id
        oldItem.get() != result.get()
        !service.getAllByRoomNumber(updatedRoom).contains(oldItem)
        oldItemsList.size() == service.getAllByRoomNumber(updatedRoom).size()
    }

    def "should not update and return EMPTY Optional when Item with given number does not exist"() {
        given:
        def itemNumber = "77/123"
        Item updateItem = source.chair[1]

        when:
        def result = service.updateByNumber(itemNumber, updateItem)

        then:
        result == Optional.empty()
    }

    def "should update and return updated Item when given number exists in database"() {
        given:
        def number = "pśt-55/222"
        def updatedRoom = "201"
        final def oldItem = service.getByNumber(number)
        final def oldItemsList = service.getAllByRoomNumber(updatedRoom)
        def updateItem = new Item()
        updateItem = source.printer[1]
        updateItem.setIncomingDate(LocalDate.of(2010, 10, 10))
        updateItem.setItemPrice(100.00)

        when:
        def result = service.updateByNumber(number, updateItem)

        then:
        result.get() != oldItem.get()
        result.get().incomingDate != oldItem.get().incomingDate
        result.get().itemPrice != oldItem.get().itemPrice
        !service.getAllByRoomNumber(updatedRoom).contains(oldItem)
    }

    def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        Files.deleteIfExists(idItemPath)
        Files.deleteIfExists(Path.of(directory))
    }
}
