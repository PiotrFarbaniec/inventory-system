package pl.inventory.system.service


import pl.inventory.system.ObjectsProvider
import pl.inventory.system.database.Database
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
    private def directory = "TestFiles"
    private def file = "roomTest.txt"
    private def idRoomFile = "idRoomTest.txt"
    private def idItemFile = "idItemTest.txt"
    private final def filePath = FileManager.createFile(file, directory)
    private final def idRoomPath = FileManager.createFile(idRoomFile, directory)
    private final def idItemPath = FileManager.createFile(idItemFile, directory)

    private final def fileService = new FileService()
    private final def serializer = new JsonSerializer()
    private final def itemIdProvider = new IdProvider(idItemPath, fileService)
    private final def roomIdProvider = new IdProvider(idRoomPath, fileService)

    private final Database<Room, Item> database = new FileBasedDatabase(filePath, itemIdProvider, roomIdProvider, fileService, serializer, Room.class)
    private final def itemService = new ItemService(database)
    private final def roomService = new RoomService(database)
    private final def source = new ObjectsProvider()

    def "should create entries for tests if database is empty"() {
        setup:
        if (database.getAll().isEmpty()) {
            roomService.save(source.room1)
            roomService.save(source.room2)
            roomService.save(source.room3)
        }
    }

    def "should override Item by specified Room id with current modification date if already exists"() {
        given:
        final def oldRoom = database.getByProperty(1L).get()
        def itemToSave = source.table[0]
        itemToSave.setModificationDate(LocalDate.of(2012, 02, 12))

        when:
        def result = itemService.saveToRoomId(oldRoom.getId(), itemToSave)

        then:
        oldRoom.itemsList == result.get().itemsList && result.get().itemsList.size() == oldRoom.itemsList.size()
        oldRoom.itemsList.get(0).getModificationDate() != result.get().itemsList.get(0).getModificationDate()
        result.get().itemsList.get(0).getModificationDate() == LocalDate.now()
    }

    def "should add new Item to Room with specified id if Room exists"() {
        given:
        def existingRoomId = 1L
        def nonexistentRoomId = 7L
        final def oldRoom = database.getByProperty(existingRoomId)
        def itemToSave = source.printer[1]

        when:
        def firstResult = itemService.saveToRoomId(nonexistentRoomId, itemToSave)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = itemService.saveToRoomId(existingRoomId, itemToSave)

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
        def firstResult = itemService.saveToRoomNumber(nonexistentRoomNumber, itemToSave)

        then:
        firstResult == Optional.empty()

        when:
        def secondResult = itemService.saveToRoomNumber(existingRoomNumber, itemToSave)

        then:
        secondResult.get().itemsList.get(0).modificationDate != oldRoom.get().itemsList.get(0).modificationDate
        oldRoom.get().itemsList.size() == secondResult.get().itemsList.size()
    }

    def "should save given Item to Room with specified number"() {
        given:
        def roomNumber = "201"
        final def oldRoom = database.getByProperty(roomNumber)
        def itemToSave = source.printer[0]

        when:
        def result = itemService.saveToRoomNumber(roomNumber, itemToSave)

        then:
        oldRoom.get() != result.get()
        database.getByProperty(roomNumber).get().itemsList.contains(itemToSave)
    }

    def "should return all items saved in the rooms"() {
        given:
        def currentItemsNumber = 26

        when:
        def result = itemService.getAll().size()

        then:
        result == currentItemsNumber
    }

    def "should return a list of all items saved in the room with the given number if exist"() {
        given:
        def firstRoomNumber = "101"
        def firstRoomId = 1L
        def firstRoomItemsNumber = 7
        def thirdRoomNumber = "201"
        def thirdRoomId = 3L
        def thirdRoomItemsNumber = 11
        def noSuchRoomNumber = "507"
        def noSuchRoomId = 9L

        when:
        def firstResultByNumber = itemService.getAllByRoomNumber(firstRoomNumber)
        def firstResultById = itemService.getAllByRoomId(firstRoomId)

        then:
        firstResultByNumber.size() == firstRoomItemsNumber && firstResultById.size() == firstRoomItemsNumber

        when:
        def secondResultByNumber = itemService.getAllByRoomNumber(thirdRoomNumber)
        def secondResultById = itemService.getAllByRoomId(thirdRoomId)

        then:
        secondResultByNumber.size() == thirdRoomItemsNumber && secondResultById.size() == thirdRoomItemsNumber

        when:
        def thirdResultByNumber = itemService.getAllByRoomNumber(noSuchRoomNumber)
        def thirdResultById = itemService.getAllByRoomId(noSuchRoomId)

        then:
        thirdResultByNumber.isEmpty() && thirdResultById.isEmpty()
    }

    def "should return an Item with the given id if exists"() {
        given:
        def existingId = 7L
        def nonexistentId = 408L

        when:
        def firstResult = itemService.getById(existingId)

        then:
        firstResult.present

        when:
        def secondResult = itemService.getById(nonexistentId)

        then:
        secondResult == Optional.empty()
    }

    def "should return an Item with the given number if exists"() {
        given:
        def existingItemNumber = "PŚT-11/222"
        def noSuchItemNumber = "PŚT-88/770"

        when:
        def presentItem = itemService.getByNumber(existingItemNumber)

        then:
        presentItem.present

        when:
        def nonexistentItem = itemService.getByNumber(noSuchItemNumber)

        then:
        nonexistentItem.isEmpty()
    }

    def "should delete Item with specified id if exist"() {
        given:
        def roomId = 3L
        def nonexistentItemId = 47L
        def existedItemId = 26L
        final def oldRoom = roomService.getById(roomId).get()

        when:
        def firstResult = itemService.deleteById(nonexistentItemId)

        then:
        oldRoom == roomService.getById(roomId).get()
        firstResult == Optional.empty()

        when:
        def secondResult = itemService.deleteById(existedItemId)

        then:
        secondResult.get().getId() == existedItemId
        itemService.getById(existedItemId) == Optional.empty()
        !roomService.getById(roomId).get().itemsList.contains(secondResult.get())
    }

    def "should delete Item with specified number if exist"() {
        given:
        def roomNumber = "102"
        def nonexistentItemNumber = "NEI-22/455"
        def existedItemNumber = "PŚT-11/444"
        final def oldRoom = roomService.getByNumber(roomNumber).get()

        when:
        def firstResult = itemService.deleteByNumber(nonexistentItemNumber)

        then:
        oldRoom == roomService.getByNumber(roomNumber).get()
        firstResult == Optional.empty()

        when:
        def secondResult = itemService.deleteByNumber(existedItemNumber)

        then:
        secondResult.get().getInventoryNumber() == existedItemNumber
        itemService.getByNumber(existedItemNumber).empty
        !roomService.getByNumber(roomNumber).get().itemsList.contains(secondResult.get())
    }

    def "should not update and return EMPTY Optional when Item with given id does not exist"() {
        given:
        def id = 308L
        Item updateItem = source.chair[1]

        when:
        def result = itemService.updateById(id, updateItem)

        then:
        result == Optional.empty()
    }

    def "should update and return updated Item when given id exists in database"() {
        given:
        def id = 7L
        def updatedRoom = "102"
        final def oldItem = itemService.getById(id)
        final def oldItemsList = itemService.getAllByRoomNumber(updatedRoom)
        def updateItem = source.table[3]

        when:
        def result = itemService.updateById(id, updateItem)

        then:
        oldItem.get().id == result.get().id
        oldItem.get() != result.get()
        !itemService.getAllByRoomNumber(updatedRoom).contains(oldItem)
        oldItemsList.size() == itemService.getAllByRoomNumber(updatedRoom).size()
    }

    def "should not update and return EMPTY Optional when Item with given number does not exist"() {
        given:
        def itemNumber = "77/123"
        Item updateItem = source.chair[1]

        when:
        def result = itemService.updateByNumber(itemNumber, updateItem)

        then:
        result == Optional.empty()
    }

    def "should update and return updated Item when given number exists in database"() {
        given:
        def number = "pśt-55/222"
        def updatedRoom = "101"
        final def oldItemsList = itemService.getAllByRoomNumber(updatedRoom)
        def updateItem = source.printer[1]
        updateItem.setIncomingDate(LocalDate.of(2010, 10, 10))
        updateItem.setItemPrice(100.00)

        when:
        def result = itemService.updateByNumber(number, updateItem)

        then:
        result.get() == updateItem
        !oldItemsList.contains(result.get())
    }

    def "deletion of files after tests"() {
        cleanup:
        Files.deleteIfExists(filePath)
        Files.deleteIfExists(idRoomPath)
        Files.deleteIfExists(idItemPath)
        Files.deleteIfExists(Path.of(directory))
    }
}
