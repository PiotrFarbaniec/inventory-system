package pl.inventory.system.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Room;
import pl.inventory.system.utils.IdProvider;

@Slf4j
@Service
@SuppressWarnings("unused")
public class RoomService {

  private final Database<Room> database;
  private final IdProvider itemIdProvider;

  @Autowired
  public RoomService(
      Database<Room> database,
      @Qualifier("itemIdProvider") IdProvider itemIdProvider) {
    this.database = database;
    this.itemIdProvider = itemIdProvider;
  }

  public List<Room> getAll() {
    return database.getAll();
  }

  public Long save(Room room) {
    if (room != null) {
      room.getItemsList()
          .forEach(item -> item.setId(itemIdProvider.getCurrentIdAndIncrement()));
      return database.save(room);
    } else {
      log.warn("The Room object to be saved must not be null");
      return 0L;
    }
  }

  public Optional<Room> getById(Long id) {
    if (id == null) {
      log.warn("Specified id of the retrieving Room cannot be null");
      return Optional.empty();
    }
    return database.getById(id);
  }

  public Optional<Room> getByNumber(String number) {
    if (number == null) {
      log.warn("Specified number of the retrieving Room cannot be null");
      return Optional.empty();
    }
    return database.getByUniqueProperty(number);
  }

  public Optional<Room> deleteById(Long id) {
    if (id == null) {
      log.warn("Specified id of removing Room cannot be null");
      return Optional.empty();
    }
    return database.deleteById(id);
  }

  public Optional<Room> deleteByNumber(String number) {
    if (number == null) {
      log.warn("Specified number of removing Room cannot be null");
      return Optional.empty();
    }
    return database.deleteByUniqueProperty(number);
  }

  public Optional<Room> updateById(Long id, Room updateRoom) {
    if (id == null || updateRoom == null) {
      log.warn("Update failed. One of the provided arguments (id or update Room) is null");
      return Optional.empty();
    }
    Optional<Room> optionalToUpdate = getById(id);
    if (optionalToUpdate.isEmpty()) {
      log.debug("Update failed. Room with id: {} does not exist", id);
    } else {
      Room oldRoom = optionalToUpdate.get();
      if (Objects.equals(oldRoom.getRoomNumber(), updateRoom.getRoomNumber())
          && !updateRoom.getItemsList().isEmpty()) {
        if (oldRoom.getItemsList().size() == updateRoom.getItemsList().size()) {
          for (int i = 0; i < updateRoom.getItemsList().size(); i++) {
            updateRoom.getItemsList().get(i).setId(oldRoom.getItemsList().get(i).getId());
          }
        } else {
          updateRoom.getItemsList().forEach(item -> item.setId(itemIdProvider.getCurrentIdAndIncrement()));
        }
        log.debug("Update of \"Room, id: {}\" successfully completed.", id);
        return database.updateById(id, updateRoom);
      }
      log.warn("Update of \"Room, id: {}\" failed. Mismatch number ({}) or empty Items list of provided Room ({})",
          id, updateRoom.getRoomNumber(), updateRoom.getItemsList().size());
    }
    return Optional.empty();
  }

  public Optional<Room> updateByNumber(String number, Room updateRoom) {
    if (number == null || updateRoom == null) {
      log.warn("Update failed. One of the provided arguments (number or update Room) is null");
      return Optional.empty();
    }
    Optional<Room> optionalToUpdate = getByNumber(number);
    if (optionalToUpdate.isEmpty()) {
      log.debug("Update failed. Room with number: {} does not exist", number);
    } else {
      Room oldRoom = optionalToUpdate.get();
      if (!updateRoom.getItemsList().isEmpty()) {
        String updatedRoomNumber = updateRoom.getRoomNumber();
        if (!Objects.equals(updatedRoomNumber, number)) {
          updateRoom.setRoomNumber(number);
        }
        if (oldRoom.getItemsList().size() == updateRoom.getItemsList().size()) {
          for (int i = 0; i < updateRoom.getItemsList().size(); i++) {
            updateRoom.getItemsList().get(i).setId(oldRoom.getItemsList().get(i).getId());
          }
        } else {
          updateRoom.getItemsList().forEach(item -> item.setId(itemIdProvider.getCurrentIdAndIncrement()));
        }
        log.debug("Update of \"Room, number: {}\" successfully completed.", number);
        return database.updateByUniqueProperty(number, updateRoom);
      }
      log.warn("Update of \"Room, number: {}\" failed because of empty Items list.", number);
    }
    return Optional.empty();
  }
}
