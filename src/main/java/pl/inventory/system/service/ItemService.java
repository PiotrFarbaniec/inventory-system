package pl.inventory.system.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;
import pl.inventory.system.utils.IdProvider;

@Service
public class ItemService {
  private final Database<Room> roomDatabase;
  private final Database<Item> itemDatabase;
  private final IdProvider itemIdProvider;
  private final IdProvider roomIdProvider;

  @Autowired
  public ItemService(
      Database<Room> roomDatabase,
      Database<Item> itemDatabase,
      @Qualifier("itemIdProvider") IdProvider itemIdProvider,
      @Qualifier("roomIdProvider") IdProvider roomIdProvider) {
    this.roomDatabase = roomDatabase;
    this.itemDatabase = itemDatabase;
    this.itemIdProvider = itemIdProvider;
    this.roomIdProvider = roomIdProvider;
  }

  public Optional<Room> saveToRoomId(Long roomId, Item item) {
    Optional<Room> optionalRoom = roomDatabase.getById(roomId);
    if (optionalRoom.isPresent()) {
      Room room = optionalRoom.get();
      List<Item> itemList = room.getItemsList();

      if (itemList.stream().noneMatch(i -> i.hashCode() == item.hashCode())) {
        item.setId(itemIdProvider.getCurrentIdAndIncrement());
        itemList.add(item);
        room.setItemsList(itemList);
      } else {
        Optional<Item> replacedItem = itemList.stream()
            .filter(i -> i.hashCode() == item.hashCode())
            .findFirst();
        if (replacedItem.isPresent()) {
          item.setId(replacedItem.get().getId());
          itemList.set(itemList.indexOf(replacedItem.get()), item);
          room.setItemsList(itemList);
        }
      }
      return roomDatabase.updateById(roomId, room);
    }
    return Optional.empty();
  }

  public Optional<Room> saveToRoomNumber(String number, Item item) {
    Optional<Room> optionalRoom = roomDatabase.getByUniqueProperty(number);
    if (optionalRoom.isPresent()) {
      Room room = optionalRoom.get();
      List<Item> itemList = room.getItemsList();

      if (itemList.stream().noneMatch(i -> i.hashCode() == item.hashCode())) {
        item.setId(itemIdProvider.getCurrentIdAndIncrement());
        itemList.add(item);
        room.setItemsList(itemList);
      } else {
        Optional<Item> replacedItem = itemList.stream()
            .filter(i -> i.hashCode() == item.hashCode())
            .findFirst();
        if (replacedItem.isPresent()) {
          item.setId(replacedItem.get().getId());
          itemList.set(itemList.indexOf(replacedItem.get()), item);
          room.setItemsList(itemList);
        }
      }
      return roomDatabase.updateByProperty(number, room);
    }
    return Optional.empty();
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
    return roomDatabase.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .filter(item -> Objects.equals(item.getId(), id))
        .findFirst();
  }

  public Optional<Item> getByNumber(String number) {
    return roomDatabase.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .filter(item -> Objects.equals(item.getInventoryNumber(), number))
        .findFirst();
  }

  public Optional<Item> deleteById(Long id) {
    Optional<Room> optionalRoom = roomDatabase.getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getId().equals(id)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getById(id);
      if (itemToRemove.isPresent()) {
        searchedRoom.setItemsList(
            searchedRoom.getItemsList().stream()
                .filter(item -> !Objects.equals(item.getId(), id))
                .toList()
        );
        roomDatabase.updateById(searchedRoom.getId(), searchedRoom);
        return itemToRemove;
      }
    }
    return Optional.empty();
  }

  public Optional<Item> deleteByNumber(String number) {
    Optional<Room> optionalRoom = roomDatabase.getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getInventoryNumber().equals(number)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getByNumber(number);
      if (itemToRemove.isPresent()) {
        searchedRoom.setItemsList(
            searchedRoom.getItemsList().stream()
                .filter(item -> !Objects.equals(item.getInventoryNumber(), number))
                .toList()
        );
        roomDatabase.updateByProperty(number, searchedRoom);
        return itemToRemove;
      }
    }
    return Optional.empty();
  }
}
