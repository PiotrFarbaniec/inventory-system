package pl.inventory.system.controller;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.inventory.system.model.Room;
import pl.inventory.system.service.RoomService;

@Slf4j
@SuppressWarnings(value = {"unused"})
@RestController
@RequestMapping(value = {"v1/room"}, produces = {"application/json;charset=UTF-8"})
public class RoomController {

  private final RoomService service;

  @Autowired
  public RoomController(RoomService service) {
    this.service = service;
  }

  @RequestMapping(method = RequestMethod.POST, value = {"/save"})
  ResponseEntity<Long> save(@RequestBody Room room) {
    try {
      Long savedRoomId = service.save(room);
      return savedRoomId > 0
          ? ResponseEntity.status(HttpStatus.CREATED).body(savedRoomId)
          : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (Exception e) {
      log.error("Unexpected error while Room saving: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get/all"})
  ResponseEntity<List<Room>> getAll() {
    try {
      List<Room> roomList = service.getAll();
      return !roomList.isEmpty()
          ? ResponseEntity.ok(roomList)
          : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("Error occurred in RoomController while fetching all rooms: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-by/id/{id}"})
  ResponseEntity<Room> getById(@PathVariable(name = "id") Long id) {
    try {
      Optional<Room> searchedRoom = service.getById(id);
      return searchedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(room))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while fetching room (id {}): ", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-by/number/"})
  ResponseEntity<Room> getByNumber(@RequestParam(value = "n") String number) {
    try {
      Optional<Room> searchedRoom = service.getByNumber(number);
      return searchedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(room))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while fetching room (number {}): ", number, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.DELETE, value = {"/delete-by/id/{id}"})
  ResponseEntity<String> deleteById(@PathVariable(name = "id") Long id) {
    try {
      Optional<Room> removedRoom = service.deleteById(id);
      return removedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(String.valueOf(id)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while room removing (id {}): ", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.DELETE, value = {"/delete-by/number/"})
  ResponseEntity<String> deleteByNumber(@RequestParam(name = "n") String number) {
    try {
      Optional<Room> removedRoom = service.deleteByNumber(number);
      return removedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(number))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while room removing (number {}): ", number, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/update-by/id/{id}"})
  ResponseEntity<Room> updateById(@PathVariable(name = "id") Long id, @RequestBody Room updateRoom) {
    try {
      Optional<Room> updatedRoom = service.updateById(id, updateRoom);
      return updatedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(room))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while room updating (id {}): ", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/update-by/number/"})
  ResponseEntity<Room> updateByNumber(@RequestParam(value = "n") String number, @RequestBody Room updateRoom) {
    try {
      Optional<Room> updatedRoom = service.updateByNumber(number, updateRoom);
      return updatedRoom
          .map(room -> ResponseEntity.status(HttpStatus.OK).body(room))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (Exception e) {
      log.error("Error occurred in RoomController while room updating (number {}): ", number, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
