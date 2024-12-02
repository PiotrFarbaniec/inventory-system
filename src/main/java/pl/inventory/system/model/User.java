package pl.inventory.system.model;

import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

  private Long id;

  private String name;

  private String surname;

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
