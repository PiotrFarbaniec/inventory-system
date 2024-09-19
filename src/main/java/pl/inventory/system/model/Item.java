package pl.inventory.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item implements Numberable {

  @JsonIgnore
  private Long id;

  private String inventoryNumber;

  private String description;

  private LocalDate incomingDate;

  private LocalDate outgoingDate;

  private LocalDate modificationDate;

  private Integer itemQuantity;

  private BigDecimal itemPrice;

  private String documentNumber;

  private User user;

  @Override
  public String getNumber() {
    return inventoryNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Item item = (Item) o;
    return Objects.equals(this.inventoryNumber, item.inventoryNumber)
        && Objects.equals(this.description, item.description)
        && Objects.equals(this.incomingDate, item.incomingDate)
        && Objects.equals(this.outgoingDate, item.outgoingDate)
        && Objects.equals(this.modificationDate, item.modificationDate)
        && Objects.equals(this.itemQuantity, item.itemQuantity)
        && Objects.equals(this.itemPrice, item.itemPrice)
        && Objects.equals(this.documentNumber, item.documentNumber)
        && Objects.equals(this.user, item.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.inventoryNumber,
        this.description,
        this.incomingDate,
        this.outgoingDate,
        this.modificationDate,
        this.itemQuantity,
        this.itemPrice,
        this.documentNumber,
        this.user
    );
  }
}
