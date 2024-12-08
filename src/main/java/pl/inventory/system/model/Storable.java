package pl.inventory.system.model;

public interface Storable {

  Long getId();

  void setId(Long id);

  default String getNumber() {
    return "";
  }
}
