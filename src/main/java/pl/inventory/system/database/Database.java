package pl.inventory.system.database;

import java.util.List;
import java.util.Optional;
import pl.inventory.system.model.Numerable;

@SuppressWarnings(value = "unused")
public interface Database<T extends Numerable> {

  Long save(T item);

  List<T> getAll();

  <P> Optional<T> getByUniqueProperty(P prop);

  Optional<T> getById(Long id);

  Optional<T> updateById(Long id, T updateItem);

  <P> Optional<T> updateByUniqueProperty(P prop, T updateItem);

  Optional<T> deleteById(Long id);

  <P> Optional<T> deleteByUniqueProperty(P prop);
}
