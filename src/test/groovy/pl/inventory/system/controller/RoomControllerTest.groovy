package pl.inventory.system.controller

import org.springframework.http.MediaType
import pl.inventory.system.AbstractDatabaseTest
import pl.inventory.system.model.Room

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class RoomControllerTest extends AbstractDatabaseTest {

    def "should return 404 (NOT FOUND) when database doesnt exist or is empty"() {
        when:
        def noResult = roomMVC.perform(get("/v1/room/get/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        noResult.response.status == 404
        noResult.response.contentAsString.empty
    }

    def "should return 400 (BAD REQUEST) when saving content is null"() {
        when:
        def result = roomMVC.perform(post("/v1/room/save")
                .content("{ }")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        result.response.status == 400
        result.response.contentAsString.empty
    }

    def "sholud save and retrieve saved rooms"() {
        given:
        def room1 = source.room1
        def room2 = source.room2
        def room3 = source.room3
        room1.setId(1L)
        room2.setId(2L)
        room3.setId(3L)
        def firstToSave = serializer.objectToJson(room1)
        def secondToSave = serializer.objectToJson(room2)
        def thirdToSave = serializer.objectToJson(room3)

        when:
        def firstResult = roomMVC.perform(post("/v1/room/save")
                .content(firstToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()

        def secondResult = roomMVC.perform(post("/v1/room/save")
                .content(secondToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def thirdResult = roomMVC.perform(post("/v1/room/save")
                .content(thirdToSave)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 201
        firstResult.response.contentAsString == "1"
        secondResult.response.status == 201
        secondResult.response.contentAsString == "2"
        thirdResult.response.status == 201
        thirdResult.response.contentAsString == "3"
    }

    def "should retrieve all saved objects"() {
        when:
        def result = roomMVC.perform(get("/v1/room/get/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()

        then:
        result.response.status == 200
        (result.response.contentAsString).contains("\"id\":1,\"roomNumber\":\"101\"")
        (result.response.contentAsString).contains("\"id\":2,\"roomNumber\":\"102\"")
    }

    def "should retrieve Room by id if exists"() {
        given:
        def existedId = "1"
        def noSuchId = "15"

        when:
        def firstResult = roomMVC.perform(get("/v1/room/get-by/id/" + existedId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
        def secondResult = roomMVC.perform(get("/v1/room/get-by/id/" + noSuchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString.contains("\"id\":1,\"roomNumber\":\"101\"")
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
    }

    def "should retrieve Room by number if exists"() {
        given:
        def existedNumber = "101"
        def noSuchNumber = "312"

        when:
        def firstResult = roomMVC.perform(get("/v1/room/get-by/number/?n=" + existedNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = roomMVC.perform(get("/v1/room/get-by/number/?n=" + noSuchNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString.contains("\"roomNumber\":\"101\"")
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
    }

    def "should update Room by specified id if exists"() {
        given:
        def existedId = "1"
        def noSuchId = "15"
        def updateRoom = source.room4
        updateRoom.setId(1L)
        updateRoom.setRoomNumber(source.room1.number)

        when:
        def firstResult = roomMVC.perform(put("/v1/room/update-by/id/" + existedId)
                .content(serializer.objectToJson(updateRoom))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = roomMVC.perform(put("/v1/room/update-by/id/" + noSuchId)
                .content(serializer.objectToJson(updateRoom))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        serializer.jsonToObject(firstResult.response.contentAsString, Room.class) == updateRoom
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
    }

    def "should update Room by specified number if exists"() {
        given:
        def existedNumber = "102"
        def noSuchNumber = "302A"
        def updateRoom = source.room3
        updateRoom.setId(2L)
        updateRoom.setRoomNumber(source.room2.number)

        when:
        def firstResult = roomMVC.perform(put("/v1/room/update-by/number/?n=" + existedNumber)
                .content(serializer.objectToJson(updateRoom))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = roomMVC.perform(put("/v1/room/update-by/number/?n=" + noSuchNumber)
                .content(serializer.objectToJson(updateRoom))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        serializer.jsonToObject(firstResult.response.contentAsString, Room.class) == updateRoom
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
    }

    def "should delete Room by specified id if exists"() {
        given:
        def existedId = "1"
        def noSuchId = "15"

        when:
        def firstResult = roomMVC.perform(delete("/v1/room/delete-by/id/" + existedId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = roomMVC.perform(delete("/v1/room/delete-by/id/" + noSuchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def actualSize = roomMVC.perform(get("/v1/room/get/all")).andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString == "1"
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
        !actualSize.response.contentAsString.contains("\"id\":1,\"roomNumber\":\"101\"")
    }

    def "should delete Room by specified number if exists"() {
        given:
        def existedNumber = "102"
        def noSuchNumber = "312"

        when:
        def firstResult = roomMVC.perform(delete("/v1/room/delete-by/number/?n=" + existedNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        def secondResult = roomMVC.perform(delete("/v1/room/delete-by/number/?n=" + noSuchNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        firstResult.response.status == 200
        firstResult.response.contentAsString == "102"
        secondResult.response.status == 404
        secondResult.response.contentAsString.empty
    }

    def "deletion of files after tests"() {
        cleanup:
        cleanDatabase()
    }
}
