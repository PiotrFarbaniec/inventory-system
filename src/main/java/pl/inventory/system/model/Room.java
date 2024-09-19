package pl.inventory.system.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class Room implements Numberable {

  private Long id;

  private String roomNumber;

  private List<Item> itemsList;

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
