package pl.inventory.system.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;
import pl.inventory.system.service.ItemService;

@SuppressWarnings(value = {"unused"})
@RestController()
@RequestMapping(value = {"v1/item"}, produces = {"application/json;charset=UTF-8"})
public class ItemController {

  private final ItemService service;

  @Autowired
  public ItemController(ItemService service) {
    this.service = service;
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-all"})
  ResponseEntity<List<Item>> getAllItems() {
    List<Item> itemsList = service.getAll();
    return !itemsList.isEmpty()
        ? ResponseEntity.status(HttpStatus.OK).body(itemsList)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-by/id/{id}"})
  ResponseEntity<Item> getItemById(@PathVariable(value = "id") Long id) {
    Optional<Item> optionalItem = service.getById(id);
    return optionalItem.map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-by/number/"})
  ResponseEntity<Item> getItemByNumber(@RequestParam (value = "n") String number) {
    Optional<Item> optionalItem = service.getByNumber(number);
    return optionalItem.map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/save-by/id/{id}"})
  ResponseEntity<Long> saveInRoomId(@PathVariable(name = "id") Long id, @RequestBody Item item) {
    Optional<Room> savedRoom = service.saveToRoomId(id, item);
    return savedRoom
        .map(room -> ResponseEntity.status(HttpStatus.CREATED).body(room.getId()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/save-by/number/"})
  ResponseEntity<String> saveInRoomNumber(@RequestParam(value = "n") String number, @RequestBody Item item) {
    Optional<Room> savedRoom = service.saveToRoomNumber(number, item);
    return savedRoom
        .map(room -> ResponseEntity.status(HttpStatus.CREATED).body(room.getRoomNumber()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-all-by/number/"})
  ResponseEntity<List<Item>> getAllByRoomNumber(@RequestParam(value = "n") String number) {
    List<Item> itemList = service.getAllByRoomNumber(number);
    return !itemList.isEmpty()
        ? ResponseEntity.status(HttpStatus.OK).body(itemList)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/get-all-by/id/{id}"})
  ResponseEntity<List<Item>> getAllByRoomId(@PathVariable(name = "id") Long id) {
    List<Item> itemList = service.getAllByRoomId(id);
    return !itemList.isEmpty()
        ? ResponseEntity.status(HttpStatus.OK).body(itemList)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @RequestMapping(method = RequestMethod.DELETE, value = {"/delete-by/id/{id}"})
  ResponseEntity<String> deleteByRoomId(@PathVariable(name = "id") Long id) {
    Optional<Item> removedItem = service.deleteById(id);
    return removedItem
        .map(item -> ResponseEntity.status(HttpStatus.OK).body(String.valueOf(id)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.DELETE, value = {"/delete-by/number/"})
  ResponseEntity<String> deleteByRoomNumber(@RequestParam(value = "n") String number) {
    Optional<Item> removedItem = service.deleteByNumber(number);
    return removedItem
        .map(item -> ResponseEntity.status(HttpStatus.OK).body(number))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/update-by/id/{id}"})
  ResponseEntity<Item> updateByRoomId(@PathVariable(name = "id") Long id, @RequestBody Item updateItem) {
    Optional<Item> updatedItem = service.updateById(id, updateItem);
    return updatedItem
        .map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @RequestMapping(method = RequestMethod.PUT, value = {"/update-by/number/"})
  ResponseEntity<Item> updateByRoomNumber(@RequestParam(name = "n") String number, @RequestBody Item updateItem) {
    Optional<Item> updatedItem = service.updateByNumber(number, updateItem);
    return updatedItem
        .map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
