package pl.inventory.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room implements Storable {

  private Long id;

  private String roomNumber;

  private List<Item> itemsList;

  @JsonIgnore
  @Override
  public String getNumber() {
    return roomNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Room room)) {
      return false;
    }
    return Objects.equals(roomNumber, room.roomNumber)
        && Objects.equals(itemsList, room.itemsList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomNumber, itemsList);
  }
}
