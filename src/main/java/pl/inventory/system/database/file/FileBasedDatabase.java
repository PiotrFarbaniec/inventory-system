package pl.inventory.system.database.file;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonSerializer;

@Slf4j
public class FileBasedDatabase extends AbstractFileDatabase<Room, Item> {

  private final IdProvider itemIdProvider;

  public FileBasedDatabase(Path roomFilePath,
                           IdProvider itemIdProvider,
                           IdProvider roomIdProvider,
                           FileService fileService,
                           JsonSerializer serializer,
                           Class<Room> cls) {
    super(roomFilePath, roomIdProvider, fileService, serializer, cls);
    this.itemIdProvider = itemIdProvider;
  }

  @Override
  public Long save(Room room) {
    LOCK.lock();
    try {
      if (room != null) {
        room.getItemsList()
            .forEach(item -> item.setId(itemIdProvider.getCurrentIdAndIncrement()));
        return super.save(room);
      }
      log.warn("The Room object to be saved must not be null");
      return 0L;
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public List<Room> getAll() {
    return super.getAll();
  }

  @Override
  public <P> Optional<Room> getByProperty(P prop) {
    if (prop != null) {
      return super.getByProperty(prop);
    }
    log.warn("Download from database failed. Specified argument is null.");
    return Optional.empty();
  }

  @Override
  public <P> Optional<Room> updateByProperty(P property, Room updateRoom) {
    LOCK.lock();
    try {
      if (property == null || updateRoom == null) {
        log.warn("Update failed. One of the provided arguments (number/id or update Room) is null");
        return Optional.empty();
      }
      Optional<Room> optionalToUpdate = getByProperty(property);
      if (optionalToUpdate.isEmpty()) {
        log.warn("Update failed. Room: {} does not exist", property);
      } else {
        Room oldRoom = optionalToUpdate.get();
        if (property instanceof String && !oldRoom.getRoomNumber().equalsIgnoreCase(updateRoom.getRoomNumber())) {
          updateRoom.setRoomNumber(oldRoom.getRoomNumber());
        }
        if (Objects.equals(oldRoom.getRoomNumber(), updateRoom.getRoomNumber())
            && !updateRoom.getItemsList().isEmpty()) {
          if (oldRoom.getItemsList().size() == updateRoom.getItemsList().size()) {
            for (int i = 0; i < updateRoom.getItemsList().size(); i++) {
              updateRoom.getItemsList().get(i).setId(oldRoom.getItemsList().get(i).getId());
            }
          } else {
            updateRoom.getItemsList().forEach(item -> item.setId(itemIdProvider.getCurrentIdAndIncrement()));
          }
          log.debug("Update of \"Room: {}\" successfully completed.", property);
          return super.updateByProperty(property, updateRoom);
        }
        log.warn("Update of \"Room: {}\" failed. Mismatch number ({}) or empty Items list of provided Room (size: {})",
            property, updateRoom.getRoomNumber(), updateRoom.getItemsList().size());
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Room> deleteByProperty(P prop) {
    if (prop != null) {
      log.debug("Deletion of Room with specified {} successfully completed", prop);
      return super.deleteByProperty(prop);
    }
    log.warn("Deletion failed. Room {} not found", prop);
    return Optional.empty();
  }

  @Override
  public List<Item> getAllItems() {
    LOCK.lock();
    try {
      return getAll().stream()
          .flatMap(room -> room.getItemsList().stream())
          .toList();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Room> saveInObjectWithProperty(P objectProperty, Item item) {
    LOCK.lock();
    try {
      if (objectProperty instanceof String property) {
        return saveByNumber(property, item);
      } else if (objectProperty instanceof Long roomId) {
        return saveById(roomId, item);
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> List<Item> getAllFromObjectWithProperty(P objectProperty) {
    LOCK.lock();
    try {
      if (objectProperty instanceof String roomNumber) {
        Optional<Room> room = getByProperty(roomNumber).stream().findFirst();
        return room.isPresent() ? room.get().getItemsList() : List.of();
      } else if (objectProperty instanceof Long roomId) {
        Optional<Room> room = getByProperty(roomId).stream().findFirst();
        return room.isPresent() ? room.get().getItemsList() : List.of();
      }
      return List.of();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Item> getItemByProperty(P itemProperty) {
    LOCK.lock();
    try {
      if (itemProperty instanceof String roomNumber) {
        return getAll().stream()
            .flatMap(room -> room.getItemsList().stream())
            .filter(item -> item.getInventoryNumber().equalsIgnoreCase(roomNumber))
            .findFirst();
      } else if (itemProperty instanceof Long roomId) {
        return getAll().stream()
            .flatMap(room -> room.getItemsList().stream())
            .filter(item -> Objects.equals(item.getId(), roomId))
            .findFirst();
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Item> deleteItemByProperty(P itemProperty) {
    LOCK.lock();
    try {
      if (itemProperty instanceof String number) {
        return deleteItemWithNumber(number);
      } else if (itemProperty instanceof Long id) {
        return deleteItemWithId(id);
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Item> updateItemByProperty(P itemProperty, Item updateItem) {
    LOCK.lock();
    try {
      if (itemProperty instanceof String number) {
        return updateItemWithNumber(number, updateItem);
      } else if (itemProperty instanceof Long id) {
        return updateItemWithId(id, updateItem);
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  private Optional<Room> saveByNumber(String roomNumber, Item item) {
    Optional<Room> optionalRoom = getByProperty(roomNumber);
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
      return super.updateByProperty(roomNumber, room);
    }
    return Optional.empty();
  }

  private Optional<Room> saveById(Long roomId, Item item) {
    Optional<Room> optionalRoom = getByProperty(roomId);
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
      return super.updateByProperty(roomId, room);
    }
    return Optional.empty();
  }

  private Optional<Item> deleteItemWithId(Long id) {
    Optional<Room> optionalRoom = getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getId().equals(id)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getItemByProperty(id);
      searchedRoom.setItemsList(
          searchedRoom.getItemsList().stream()
              .filter(item -> !Objects.equals(item.getId(), id))
              .toList()
      );
      super.updateByProperty(searchedRoom.getId(), searchedRoom);
      return itemToRemove;
    }
    return Optional.empty();
  }

  private Optional<Item> deleteItemWithNumber(String number) {
    Optional<Room> optionalRoom = getAll().stream()
        .filter(room -> room.getItemsList().stream()
            .anyMatch(item -> item.getInventoryNumber().equalsIgnoreCase(number)))
        .findFirst();
    if (optionalRoom.isPresent()) {
      Room searchedRoom = optionalRoom.get();
      Optional<Item> itemToRemove = getItemByProperty(number);
      searchedRoom.setItemsList(
          searchedRoom.getItemsList().stream()
              .filter(item -> !Objects.equals(item.getInventoryNumber(), number))
              .toList()
      );
      super.updateByProperty(searchedRoom.getRoomNumber(), searchedRoom);
      return itemToRemove;
    }
    return Optional.empty();
  }

  private Optional<Item> updateItemWithId(Long id, Item updateItem) {
    Optional<Item> optionalItem = getItemByProperty(id);
    if (optionalItem.isPresent()) {
      final Item oldItem = optionalItem.get();
      updateItem.setId(id);
      updateItem.setModificationDate(LocalDate.now());
      Long roomId = getRoomId(oldItem);
      Optional<Room> optionalRoom = getByProperty(roomId);
      if (optionalRoom.isPresent()) {
        Room room = optionalRoom.get();
        int itemIndex = room.getItemsList().indexOf(oldItem);
        List<Item> itemList = room.getItemsList();
        itemList.set(itemIndex, updateItem);
        room.setItemsList(itemList);
        super.updateByProperty(roomId, room);
        return getItemByProperty(id);
      }
    }
    return Optional.empty();
  }

  private Optional<Item> updateItemWithNumber(String number, Item updateItem) {
    Optional<Item> optionalItem = getItemByProperty(number);
    if (optionalItem.isPresent()) {
      final Item oldItem = optionalItem.get();
      updateItem.setId(oldItem.getId());
      updateItem.setModificationDate(LocalDate.now());
      Long roomId = getRoomId(oldItem);
      Optional<Room> optionalRoom = getByProperty(roomId);
      if (optionalRoom.isPresent()) {
        Room room = optionalRoom.get();
        int itemIndex = room.getItemsList().indexOf(oldItem);
        List<Item> itemList = room.getItemsList();
        itemList.set(itemIndex, updateItem);
        room.setItemsList(itemList);
        super.updateByProperty(roomId, room);
        return getItemByProperty(number);
      }
    }
    return Optional.empty();
  }

  private Long getRoomId(Item oldItem) {
    Optional<Long> searchedRoomId = getAll().stream()
        .filter(room -> (room.getItemsList()).contains(oldItem))
        .map(Room::getId)
        .findFirst();
    return searchedRoomId.isPresent() ? searchedRoomId.get() : 0;
  }
}
