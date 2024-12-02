package pl.inventory.system.service;

import java.time.LocalDate;
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
@SuppressWarnings("unused")
public class ItemService {
  private final Database<Room> database;
  private final IdProvider itemIdProvider;

  @Autowired
  public ItemService(
      Database<Room> database,
      @Qualifier("itemIdProvider") IdProvider itemIdProvider) {
    this.database = database;
    this.itemIdProvider = itemIdProvider;
  }

  public Optional<Room> saveToRoomId(Long roomId, Item item) {
    Optional<Room> optionalRoom = database.getById(roomId);
    if (optionalRoom.isPresent()) {
      Room room = optionalRoom.get();
      List<Item> itemList = room.getItemsList();
      Optional<Item> replacedItem = itemList.stream()
          .filter(i -> i.equals(item))
          .findFirst();
      if (replacedItem.isPresent()) {
        item.setId(replacedItem.get().getId());
        item.setModificationDate(LocalDate.now());
        itemList.set(itemList.indexOf(replacedItem.get()), item);
      } else {
        item.setId(itemIdProvider.getCurrentIdAndIncrement());
        item.setModificationDate(LocalDate.now());
        itemList.add(item);
      }
      room.setItemsList(itemList);
      return database.updateById(roomId, room);
    }
    return Optional.empty();
  }

  public Optional<Room> saveToRoomNumber(String number, Item item) {
    Optional<Room> optionalRoom = database.getByUniqueProperty(number);
    if (optionalRoom.isPresent()) {
      Room room = optionalRoom.get();
      List<Item> itemList = room.getItemsList();
      Optional<Item> replacedItem = itemList.stream()
          .filter(i -> i.equals(item))
          .findFirst();
      if (replacedItem.isPresent()) {
        item.setId(replacedItem.get().getId());
        item.setModificationDate(LocalDate.now());
        itemList.set(itemList.indexOf(replacedItem.get()), item);
      } else {
        item.setId(itemIdProvider.getCurrentIdAndIncrement());
        item.setModificationDate(LocalDate.now());
        itemList.add(item);
      }
      room.setItemsList(itemList);
      return database.updateByUniqueProperty(number, room);
    }
    return Optional.empty();
  }

  public List<Item> getAll() {
    return database.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .toList();
  }

  public List<Item> getAllByRoomNumber(String roomNumber) {
    Optional<Room> room = database.getByUniqueProperty(roomNumber).stream().findFirst();
    if (room.isPresent()) {
      return room.get().getItemsList();
    }
    return List.of();
  }

  public List<Item> getAllByRoomId(Long roomId) {
    Optional<Room> room = database.getById(roomId).stream().findFirst();
    if (room.isPresent()) {
      return room.get().getItemsList();
    }
    return List.of();
  }

  public Optional<Item> getById(Long id) {
    return database.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .filter(item -> Objects.equals(item.getId(), id))
        .findFirst();
  }

  public Optional<Item> getByNumber(String number) {
    return database.getAll().stream()
        .flatMap(room -> room.getItemsList().stream())
        .filter(item -> item.getInventoryNumber().equalsIgnoreCase(number))
        .findFirst();
  }

  public Optional<Item> deleteById(Long id) {
    Optional<Room> optionalRoom = database.getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getId().equals(id)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getById(id);
      searchedRoom.setItemsList(
          searchedRoom.getItemsList().stream()
              .filter(item -> !Objects.equals(item.getId(), id))
              .toList()
      );
      database.updateById(searchedRoom.getId(), searchedRoom);
      return itemToRemove;
    }
    return Optional.empty();
  }

  public Optional<Item> deleteByNumber(String number) {
    Optional<Room> optionalRoom = database.getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getInventoryNumber().equalsIgnoreCase(number)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getByNumber(number);
      searchedRoom.setItemsList(
          searchedRoom.getItemsList().stream()
              .filter(item -> !Objects.equals(item.getInventoryNumber(), number))
              .toList()
      );
      database.updateByUniqueProperty(searchedRoom.getRoomNumber(), searchedRoom);
      return itemToRemove;
    }
    return Optional.empty();
  }

  public Optional<Item> updateById(Long id, Item updateItem) {
    Optional<Item> optionalItem = getById(id);
    if (optionalItem.isPresent()) {
      final Item oldItem = optionalItem.get();
      updateItem.setId(id);
      updateItem.setModificationDate(LocalDate.now());
      Long roomId = getRoomId(oldItem);
      if (roomId > 0) {
        Optional<Room> optionalRoom = database.getById(roomId);
        if (optionalRoom.isPresent()) {
          Room room = optionalRoom.get();
          int itemIndex = room.getItemsList().indexOf(oldItem);
          List<Item> itemList = room.getItemsList();
          itemList.set(itemIndex, updateItem);
          room.setItemsList(itemList);
          database.updateById(roomId, room);
          return getById(id);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<Item> updateByNumber(String number, Item updateItem) {
    Optional<Item> optionalItem = getByNumber(number);
    if (optionalItem.isPresent()) {
      final Item oldItem = optionalItem.get();
      updateItem.setId(oldItem.getId());
      updateItem.setModificationDate(LocalDate.now());
      Long roomId = getRoomId(oldItem);
      if (roomId > 0) {
        Optional<Room> optionalRoom = database.getById(roomId);
        if (optionalRoom.isPresent()) {
          Room room = optionalRoom.get();
          int itemIndex = room.getItemsList().indexOf(oldItem);
          List<Item> itemList = room.getItemsList();
          itemList.set(itemIndex, updateItem);
          room.setItemsList(itemList);
          database.updateById(roomId, room);
          return getByNumber(number);
        }
      }
    }
    return Optional.empty();
  }

  private Long getRoomId(Item oldItem) {
    Optional<Long> searchedRoomId = database.getAll().stream()
        .filter(room -> (room.getItemsList()).contains(oldItem))
        .map(Room::getId)
        .findFirst();
    return searchedRoomId.isPresent() ? searchedRoomId.get() : 0;
  }
}
