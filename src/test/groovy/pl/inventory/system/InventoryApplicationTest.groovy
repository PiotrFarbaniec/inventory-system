package pl.inventory.system

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.inventory.system.service.ItemService
import pl.inventory.system.service.RoomService
import spock.lang.Specification

@SpringBootTest
class InventoryApplicationTest extends Specification {

    def "successful launching the Spring Boot application test"() {
        given:
        def app = new InventoryApplication()

        when:
        app.main()

        then:
        noExceptionThrown()
    }
}
