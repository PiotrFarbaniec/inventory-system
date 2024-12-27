package pl.inventory.system.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings(value = {"unused"})
public class User {

  @Schema(title = "An ID of the user, defined manually", example = "1")
  private Long id;

  @Schema(title = "A name of the user", example = "John")
  private String name;

  @Schema(title = "A surname of the user", example = "Smith")
  private String surname;

  @Schema(title = "Value true if the user is authorised to make changes, false otherwise", example = "true")
  private boolean isInventoryUser;

  public User() {
  }

  public User(String name, String surname, boolean isInventoryUser) {
    this.name = name;
    this.surname = surname;
    this.isInventoryUser = isInventoryUser;
  }

  public User(Long id, String name, String surname, boolean isInventoryUser) {
    this.id = id;
    this.name = name;
    this.surname = surname;
    this.isInventoryUser = isInventoryUser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return this.isInventoryUser == user.isInventoryUser
        && Objects.equals(this.id, user.id)
        && Objects.equals(this.name, user.name)
        && Objects.equals(this.surname, user.surname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, surname, isInventoryUser);
  }
}
