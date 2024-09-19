package pl.inventory.system.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;

@Service
public class ItemService {
  private final Database<Room> roomDatabase;
  private final Database<Item> itemDatabase;

  @Autowired
  public ItemService(Database<Room> roomDatabase, Database<Item> itemDatabase) {
    this.roomDatabase = roomDatabase;
    this.itemDatabase = itemDatabase;
  }

  public Optional<Room> saveToRoomWithId(Long roomId, Item item) {
    Optional<Room> optionalRoom = roomDatabase.getById(roomId);
    if (optionalRoom.isPresent()) {
      Room room = optionalRoom.get();
      List<Item> itemList = room.getItemsList();

      if (itemList.stream().noneMatch(i -> i.hashCode() == item.hashCode())) {
        item.setId(itemDatabase.getItemId());
        itemList.add(item);
        room.setItemsList(itemList);
      } else {
        Item replacedItem = itemList.stream()
            .filter(i -> i.hashCode() == item.hashCode())
            .findFirst()
            .get();
        item.setId(replacedItem.getId());
        itemList.add(item);
        room.setItemsList(itemList);
      }
      return roomDatabase.updateById(roomId, room);
    }
    return Optional.empty();
  }

  public Long saveToRoomNumber(String number, Item item) {
    return null;
  }

  public List<Item> getAll() {
    return roomDatabase.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .toList();
  }

  public List<Item> getAllByRoomNumber(String roomNumber) {
    Optional<Room> room = roomDatabase.getByUniqueProperty(roomNumber).stream().findFirst();
    if (room.isPresent()) {
      return room.get().getItemsList();
    }
    return List.of();
  }

  public List<Item> getAllByRoomId(Long id) {
    Optional<Room> room = roomDatabase.getById(id).stream().findFirst();
    if (room.isPresent()) {
      return room.get().getItemsList();
    }
    return List.of();
  }

  public Optional<Item> getById(Long id) {
    return Optional.empty();
  }

  public List<Item> getByProperty(String property) {
    return null;
  }

  public Optional<Item> deleteById(Long id) {
    return Optional.empty();
  }

  public Optional<Item> deleteByNumber(String number) {
    return Optional.empty();
  }
}
