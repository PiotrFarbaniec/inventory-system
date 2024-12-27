package pl.inventory.system.database.file;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonService;

@Slf4j
public class FileBasedDatabase extends AbstractFileDatabase<Room, Item> {

  private final IdProvider itemIdProvider;

  public FileBasedDatabase(Path roomFilePath,
                           IdProvider itemIdProvider,
                           IdProvider roomIdProvider,
                           FileService fileService,
                           JsonService serializer,
                           Class<Room> cls) {
    super(roomFilePath, roomIdProvider, fileService, serializer, cls);
    this.itemIdProvider = itemIdProvider;
  }

  @Override
  public Long save(Room room) {
    LOCK.lock();
    try {
      if (room != null && room.getItemsList() != null && !room.getItemsList().isEmpty()) {
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
    if (prop == null) {
      log.warn("Download from database failed. Specified argument is null.");
    }
    return super.getByProperty(prop);
  }

  @Override
  public <P> Optional<Room> updateByProperty(P property, Room updateRoom) {
    LOCK.lock();
    try {
      if (property == null || updateRoom == null) {
        log.warn("Update failed. One of the provided arguments (number/id or update Room) is null");
        return Optional.empty();
      } else if (updateRoom.getItemsList() == null || updateRoom.getItemsList().isEmpty()) {
        log.warn("Update failed. Wrong content of update Room has been specified ({})", updateRoom);
        return Optional.empty();
      }
      Optional<Room> optionalToUpdate = super.getByProperty(property);
      if (optionalToUpdate.isEmpty()) {
        log.warn("Update failed. Room: {} does not exist", property);
      } else {
        Room oldRoom = optionalToUpdate.get();
        updateRoom.setRoomNumber(oldRoom.getRoomNumber());

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
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Room> deleteByProperty(P prop) {
    if (prop != null) {
      log.debug("Deletion of Room with specified {} successfully completed", prop);
    } else {
      log.warn("Deletion failed. Room not found");
    }
    return super.deleteByProperty(prop);
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
      if (objectProperty instanceof String number) {
        log.debug("Item number: {} has been successfully stored in Room number: {}", item.getInventoryNumber(), number);
        return saveByNumber(number, item);
      } else if (objectProperty instanceof Long roomId) {
        log.debug("Item number: {} has been successfully stored in Room id: {}", item.getInventoryNumber(), roomId);
        return saveById(roomId, item);
      }
      log.warn("Storage of an Item number: {} in Room: {} failed", item.getInventoryNumber(), objectProperty);
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
      final Optional<Item> optionalItem = getItemByProperty(itemProperty);
      if (optionalItem.isPresent()) {
        Item itemToRemove = optionalItem.get();
        Room room = getRoomContaining(itemToRemove);
        room.setItemsList(room.getItemsList().stream()
            .filter(item -> !Objects.equals(item.getId(), itemToRemove.getId()))
            .toList());
        super.updateByProperty(room.getId(), room);
        return optionalItem;
      } else {
        return Optional.empty();
      }
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<Item> updateItemByProperty(P itemProperty, Item updateItem) {
    LOCK.lock();
    try {
      Optional<Item> optionalItem = getItemByProperty(itemProperty);
      if (optionalItem.isPresent()) {
        final Item oldItem = optionalItem.get();
        updateItem.setId(oldItem.getId());
        updateItem.setModificationDate(LocalDate.now());
        Room room = getRoomContaining(oldItem);
        List<Item> itemList = room.getItemsList();
        int itemIndex = itemList.indexOf(oldItem);
        itemList.set(itemIndex, updateItem);
        room.setItemsList(itemList);
        super.updateByProperty(room.getId(), room);
        return Optional.of(updateItem);
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

  private Room getRoomContaining(Item oldItem) {
    return getAll().stream().filter(room ->
            room.getItemsList().stream().anyMatch(item ->
                item.getInventoryNumber().equalsIgnoreCase(oldItem.getInventoryNumber())
                && item.getId().equals(oldItem.getId())))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Room containing searched Item not found."));
  }
}
