package pl.inventory.system.service;

import java.util.Optional;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;

public class RoomService {

  private final Database<Room> roomDatabase;
  private final Database<Item> itemDatabase;

  public RoomService(Database<Room> database, Database<Item> itemDatabase) {
    this.roomDatabase = database;
    this.itemDatabase = itemDatabase;
  }

  public Long save(Room room) {
    return roomDatabase.save(room);
  }

  public Optional<Room> getById(Long id) {
    return Optional.empty();
  }

  public Optional<Room> getByProperty(String property) {
    return Optional.empty();
  }
}
