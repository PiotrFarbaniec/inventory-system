package pl.inventory.system.model;

public interface Numberable {
  Long getId();

  void setId(Long id);

  default String getNumber() {
    return "";
  }
}
