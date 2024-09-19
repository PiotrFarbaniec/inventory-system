package pl.inventory.system.database;

import java.util.List;
import java.util.Optional;
import pl.inventory.system.model.Numberable;

@SuppressWarnings(value = "unused")
public interface Database<T extends Numberable> {

  Long save(T item);

  List<T> getAll();

  <P> Optional<T> getByUniqueProperty(P prop);

  Optional<T> getById(Long id);

  Optional<T> updateById(Long id, T updateItem);

  <P> Optional<T> updateByProperty(P prop, T updateItem);

  Optional<T> deleteById(Long id);

  <P> Optional<T> deleteByProperty(P prop);

  default Long getItemId() {
    return 0L;
  }

  default boolean cleanupDatabase() {
    List<T> itemsList = getAll().stream()
        .peek(eachItem -> Optional.of(deleteById(eachItem.getId())))
        .toList();
    return itemsList.isEmpty();
  }
}
