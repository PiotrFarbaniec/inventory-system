package pl.inventory.system.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

@Service
public class JsonSerializer {

  private final JsonMapper mapper;

  {
    mapper = new JsonMapper();
    mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
    mapper.disable(
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
        SerializationFeature.INDENT_OUTPUT
    );
  }

  public String objectToJson(Object object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      System.err.println(e.getLocation());
      System.err.println(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public <T> T jsonToObject(String jsonContent, Class<T> objClass) {
    try {
      return mapper.readValue(jsonContent, objClass);
    } catch (JsonProcessingException e) {
      System.err.println(e.getLocation());
      System.err.println(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
