package pl.inventory.system.model;

public interface Numerable {

  Long getId();

  void setId(Long id);

  default String getNumber() {
    return "";
  }
}
