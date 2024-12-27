package pl.inventory.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class Item implements InternallyStorable {

  @Schema(title = "An ID of the item, generated automatically", example = "1")
  private Long id;

  @Schema(title = "An unique inventory number of the each item", example = "PŚT-11/123")
  private String inventoryNumber;

  @Schema(title = "Description of the item", example = "Glass table")
  private String description;

  @Schema(title = "Date of receipt of the item", example = "2024-01-01", nullable = true)
  private LocalDate incomingDate;

  @Schema(title = "Date of disposal of the item", example = "2024-12-31", nullable = true)
  private LocalDate outgoingDate;

  @Schema(title = "Modification date of the item", example = "2024-06-20", nullable = true)
  private LocalDate modificationDate;

  @Schema(title = "Number of items", example = "1")
  private Integer itemQuantity;

  @Schema(title = "Price of the item", example = "290.89")
  private BigDecimal itemPrice;

  @Schema(title = "Number of the specific document describing the item", example = "STD/02/2023", nullable = true)
  private String documentNumber;

  @Schema(title = "Details of the user who recorded or modified the object", nullable = true)
  private User user;

  @JsonIgnore
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
        this.itemQuantity,
        this.itemPrice,
        this.documentNumber,
        this.user
    );
  }
}
