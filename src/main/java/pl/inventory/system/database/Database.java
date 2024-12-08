package pl.inventory.system.database;

import java.util.List;
import java.util.Optional;
import pl.inventory.system.model.InternallyStorable;
import pl.inventory.system.model.Storable;

@SuppressWarnings(value = "unused")
public interface Database<T1 extends Storable, T2 extends InternallyStorable> {

  Long save(T1 item);

  List<T1> getAll();

  <P> Optional<T1> getByProperty(P prop);

  <P> Optional<T1> updateByProperty(P prop, T1 updateItem);

  <P> Optional<T1> deleteByProperty(P prop);

  default List<T2> getAllItems() {
    return null;
  }

  default <P> Optional<T1> saveInObjectWithProperty(P objectProperty, T2 item) {
    return Optional.empty();
  }

  default <P> List<T2> getAllFromObjectWithProperty(P objectProperty) {
    return List.of();
  }

  default <P> Optional<T2> getItemByProperty(P itemProperty) {
    return Optional.empty();
  }

  default <P> Optional<T2> deleteItemByProperty(P itemProperty) {
    return Optional.empty();
  }

  default <P> Optional<T2> updateItemByProperty(P itemProperty, T2 updateItem) {
    return Optional.empty();
  }
}
