package pl.inventory.system.utils

import com.fasterxml.jackson.core.JsonProcessingException
import spock.lang.Specification

class JsonServiceTest extends Specification {
    def serializer = new JsonService()

    def "should throw an exception when converting an invalid object to json"() {
        given:
        def invalidObject = new Object()

        when:
        serializer.objectToJson(invalidObject)

        then:
        def exception = thrown(RuntimeException.class)
        exception.cause instanceof JsonProcessingException
    }

    def "should throw an exception when converting an invalid json to object"() {
        given:
        def invalidJson = new String()

        when:
        serializer.jsonToObject(invalidJson, Object.class)

        then:
        def exception = thrown(RuntimeException.class)
        exception.cause instanceof JsonProcessingException
    }
}
