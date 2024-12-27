package pl.inventory.system.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings({"unused"})
@Configuration
public class SpringdocConfig {

  @Bean
  public OpenAPI inventoryApi() {
    return new OpenAPI()
        .info(apiInfo())
        .tags(List.of(roomControllerTag(), itemControllerTag()));
  }

  private Info apiInfo() {
    return new Info()
        .title("Inventory API")
        .description("Application to manage the contents of the rooms in the building")
        .version("version: 1.0.0")
        .license(new License()
            .name("Apache 2.0")
            .url("http://springdoc.org")
        )
        .contact(contact());
  }

  private Contact contact() {
    return new Contact()
        .name("Piotr Farbaniec")
        .url("https://github.com/PiotrFarbaniec?tab=repositories")
        .email("piotr.farbaniec7@gmail.com");
  }

  private Tag roomControllerTag() {
    return new Tag()
        .name("Room Controller")
        .description("Allows operation of Room type objects (CRUD)");
  }

  private Tag itemControllerTag() {
    return new Tag()
        .name("Item Controller")
        .description("Allows the operation of objects of type Item (CRUD), modifying only the content of the objects Room");
  }
}
