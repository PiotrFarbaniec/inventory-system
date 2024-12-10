package pl.inventory.system.service;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;

@Slf4j
@Service
@SuppressWarnings("unused")
public class RoomService {

  private final Database<Room, Item> database;

  @Autowired
  public RoomService(Database<Room, Item> database) {
    this.database = database;
  }

  public List<Room> getAll() {
    return database.getAll();
  }

  public Long save(Room room) {
    return database.save(room);
  }

  public Optional<Room> getById(Long id) {
    return database.getByProperty(id);
  }

  public Optional<Room> getByNumber(String number) {
    return database.getByProperty(number);
  }

  public Optional<Room> deleteById(Long id) {
    return database.deleteByProperty(id);
  }

  public Optional<Room> deleteByNumber(String number) {
    return database.deleteByProperty(number);
  }

  public Optional<Room> updateById(Long id, Room updateRoom) {
    return database.updateByProperty(id, updateRoom);
  }

  public Optional<Room> updateByNumber(String number, Room updateRoom) {
    return database.updateByProperty(number, updateRoom);
  }
}
