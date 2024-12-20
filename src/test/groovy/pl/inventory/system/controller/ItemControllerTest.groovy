package pl.inventory.system.controller

import org.springframework.http.MediaType
import pl.inventory.system.AbstractDatabaseTest

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

class ItemControllerTest extends AbstractDatabaseTest {

    def "should return 404 (NOT FOUND) if database is empty or not exists"() {
        when:
        def result = itemMVC.perform(get("/v1/item/get-all")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
        then:
        result.response.status == 404
        result.response.contentAsString.empty
    }

    def "should return a list of all stored elements if database is not empty"() {
        given:
        roomService.save(source.room1)
        roomService.save(source.room2)
        roomService.save(source.room3)

        when:
        def result = itemMVC.perform(get("/v1/item/get-all")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        result.response.status == 200
        !result.response.contentAsString.empty
    }

    def "should return Item by specified id or number if exists"() {
        given:
        def existedId = "1"
        def existedNumber = "PŚT-55/222"
        def noSuchId = "30"
        def noSuchNumber = "PŚT-78/234"

        when:
        def firstResult = itemMVC.perform(get("/v1/item/get-by/id/" + existedId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = itemMVC.perform(get("/v1/item/get-by/number/?n=" + existedNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = itemMVC.perform(get("/v1/item/get-by/id/" + noSuchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def fourthResult = itemMVC.perform(get("/v1/item/get-by/number/?n=" + noSuchNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString.contains("\"id\":1")

        secondResult.response.status == 200
        secondResult.response.contentAsString.contains("\"inventoryNumber\":\"PŚT-55/222\"")

        thirdResult.response.status == 404
        thirdResult.response.contentAsString.empty

        fourthResult.response.status == 404
        fourthResult.response.contentAsString.empty
    }

    def "should get all Items by specified Room id or number if exists"() {
        given:
        def existedId = "1"
        def existedNumber = "102"
        def noSuchId = "8"
        def noSuchNumber = "412"

        when:
        def firstResult = itemMVC.perform(get("/v1/item/get-all-by/id/" + existedId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = itemMVC.perform(get("/v1/item/get-all-by/number/?n=" + existedNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = itemMVC.perform(get("/v1/item/get-all-by/id/" + noSuchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def fourthResult = itemMVC.perform(get("/v1/item/get-all-by/number/?n=" + noSuchNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString.findAll("\"inventoryNumber\":\"\\D{3}-\\d{2}/\\d{3}\"")
                .stream().toList().size() == source.room1.itemsList.size()

        secondResult.response.status == 200
        secondResult.response.contentAsString.findAll("\"inventoryNumber\":\"\\D{3}-\\d{2}/\\d{3}\"")
                .stream().toList().size() == source.room2.itemsList.size()

        thirdResult.response.status == 404
        thirdResult.response.contentAsString.empty

        fourthResult.response.status == 404
        fourthResult.response.contentAsString.empty
    }

    def "should save new Item in Room with specified id or number if exists"() {
        given:
        def existedRoomId = "1"
        def existedRoomNumber = "102"
        def noSuchRoomId = "8"
        def noSuchRoomNumber = "412"
        def itemToSave = serializer.objectToJson(source.table[2])

        when:
        def firstResult = itemMVC.perform(put("/v1/item/save-by/id/" + existedRoomId)
                .content(itemToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = itemMVC.perform(put("/v1/item/save-by/number/?n=" + existedRoomNumber)
                .content(itemToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = itemMVC.perform(put("/v1/item/save-by/id/" + noSuchRoomId)
                .content(itemToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def fourthResult = itemMVC.perform(put("/v1/item/save-by/number/?n=" + noSuchRoomNumber)
                .content(itemToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 201
        firstResult.response.contentAsString == existedRoomId

        secondResult.response.status == 201
        secondResult.response.contentAsString == existedRoomNumber

        thirdResult.response.status == 404
        thirdResult.response.contentAsString.empty

        fourthResult.response.status == 404
        fourthResult.response.contentAsString.empty
    }

    def "should delete Item by specified id or number if any Room contains it"() {
        given:
        def existedItemId = "25"
        def existedItemNumber = "PŚT-11/333"
        def noSuchItemId = "57"
        def noSuchItemNumber = "PŚT-12/123"

        when:
        def firstResult = itemMVC.perform(delete("/v1/item/delete-by/id/" + existedItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = itemMVC.perform(delete("/v1/item/delete-by/number/?n=" + existedItemNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = itemMVC.perform(delete("/v1/item/delete-by/id/" + noSuchItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def fourthResult = itemMVC.perform(delete("/v1/item/delete-by/number/?n=" + noSuchItemNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString == "25"

        secondResult.response.status == 200
        secondResult.response.contentAsString == "PŚT-11/333"

        thirdResult.response.status == 404
        thirdResult.response.contentAsString.empty

        fourthResult.response.status == 404
        fourthResult.response.contentAsString.empty
    }

    def "should update Item with specified id or number if any Room contains it"() {
        given:
        def existedItemId = "1"
        def existedItemNumber = "PŚT-11/444"
        def noSuchItemId = "42"
        def noSuchItemNumber = "PŚT-12/123"
        def firstUpdateItem = source.table[0]
        firstUpdateItem.setDescription("Glass table")
        def secondUpdateItem = source.table[3]
        secondUpdateItem.setDescription("Buffet table")

        when:
        def firstResult = itemMVC.perform(put("/v1/item/update-by/id/" + existedItemId)
                .content(serializer.objectToJson(firstUpdateItem))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = itemMVC.perform(put("/v1/item/update-by/number/?n=" + existedItemNumber)
                .content(serializer.objectToJson(secondUpdateItem))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = itemMVC.perform(put("/v1/item/update-by/id/" + noSuchItemId)
                .content(serializer.objectToJson(firstUpdateItem))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def fourthResult = itemMVC.perform(put("/v1/item/update-by/number/?n=" + noSuchItemNumber)
                .content(serializer.objectToJson(secondUpdateItem))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString.contains("Glass table")

        secondResult.response.status == 200
        secondResult.response.contentAsString.contains("Buffet table")

        thirdResult.response.status == 404
        thirdResult.response.contentAsString.empty

        fourthResult.response.status == 404
        fourthResult.response.contentAsString.empty
    }

    def "deletion of files after tests"() {
        cleanup:
        cleanDatabase()
    }
}
