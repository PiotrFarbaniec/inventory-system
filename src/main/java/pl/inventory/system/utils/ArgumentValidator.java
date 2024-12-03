package pl.inventory.system.utils;

import org.springframework.stereotype.Component;
import pl.inventory.system.utils.exceptions.InvalidArgumentException;

@Component
public final class ArgumentValidator {

  public static <T> void validateArgument(T arg, String name) throws InvalidArgumentException {
    if (arg == null) {
      throw new InvalidArgumentException(String.format("Required argument (%s) is null", name));
    }
  }
}
