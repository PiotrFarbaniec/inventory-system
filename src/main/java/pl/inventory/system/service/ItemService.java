package pl.inventory.system.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;

@Service
@SuppressWarnings("unused")
public class ItemService {

  private final Database<Room, Item> database;

  @Autowired
  public ItemService(Database<Room, Item> database) {
    this.database = database;
  }

  public Optional<Room> saveToRoomId(Long roomId, Item item) {
    return database.saveInObjectWithProperty(roomId, item);
  }

  public Optional<Room> saveToRoomNumber(String number, Item item) {
    return database.saveInObjectWithProperty(number, item);
  }

  public List<Item> getAll() {
    return database.getAllItems();
  }

  public List<Item> getAllByRoomNumber(String roomNumber) {
    return database.getAllFromObjectWithProperty(roomNumber);
  }

  public List<Item> getAllByRoomId(Long roomId) {
    return database.getAllFromObjectWithProperty(roomId);
  }

  public Optional<Item> getById(Long id) {
    return database.getItemByProperty(id);
  }

  public Optional<Item> getByNumber(String number) {
    return database.getItemByProperty(number);
  }

  public Optional<Item> deleteById(Long id) {
    return database.deleteItemByProperty(id);
  }

  public Optional<Item> deleteByNumber(String number) {
    return database.deleteItemByProperty(number);
  }

  public Optional<Item> updateById(Long id, Item updateItem) {
    return database.updateItemByProperty(id, updateItem);
  }

  public Optional<Item> updateByNumber(String number, Item updateItem) {
    return database.updateItemByProperty(number, updateItem);
  }
}
