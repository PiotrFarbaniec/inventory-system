package pl.inventory.system.database.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Numberable;
import pl.inventory.system.utils.FileManager;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonSerializer;
import pl.inventory.system.utils.exceptions.InvalidFileException;

@Slf4j
public class FileBasedDatabase<T extends Numberable> implements Database<T> {

  private final Path filePath;
  private final IdProvider idProvider;
  private final FileService fileService;

  private final JsonSerializer serializer;
  private final Class<T> cls;

  public FileBasedDatabase(Path roomFilePath,
                           IdProvider idProvider,
                           FileService fileService,
                           JsonSerializer serializer,
                           Class<T> cls) {
    log.info("File database initialised on objects of type {}", cls.getSimpleName());
    this.filePath = roomFilePath;
    this.idProvider = idProvider;
    this.fileService = fileService;
    this.serializer = serializer;
    this.cls = cls;
  }

  @Override
  public Long getItemId() {
    return idProvider.getCurrentIdAndIncrement();
  }

  @Override
  public Long save(T item) {
    log.debug("Saving {} (with id:{}) to the database", item.getClass().getSimpleName(), item.getId());
    File itemFile = new File(filePath.toString());
    Long currentId = idProvider.getCurrentIdAndIncrement();
    FileManager.createFile(itemFile);
    item.setId(currentId);
    fileService.appendLineToFile(filePath, serializer.objectToJson(item));
    log.debug("{} (with id: {}) successfully saved in database", item.getClass().getSimpleName(), item.getId());
    return currentId;
  }

  @Override
  public List<T> getAll() {
    File itemFile = new File(filePath.toString());
    log.debug("Downloading all {} from the database", cls.getSimpleName());
    if (itemFile.exists()) {
      return fileService.readAllFile(filePath).stream()
          .map(line -> serializer.jsonToObject(line, cls))
          .toList();
    }
    log.debug("Downloading all {} successfully completed", cls.getSimpleName());
    return List.of();
  }

  @Override
  public <P> Optional<T> getByUniqueProperty(P prop) {
    File itemFile = new File(filePath.toString());
    log.debug("Downloading {} (with: {}) from the database", cls.getSimpleName(), prop.toString());
    if (itemFile.exists()) {
      return getAll().stream()
          .filter(item -> item.getNumber().equalsIgnoreCase(prop.toString())).findAny();
    }
    log.debug("Downloading {} (with: {}) from the database completed", cls.getSimpleName(), prop);
    return Optional.empty();
  }

  @Override
  public Optional<T> getById(Long id) {
    File itemFile = new File(filePath.toString());
    if (itemFile.exists()) {
      return getAll().stream()
          .filter(item -> item.getId().compareTo(id) == 0)
          .findAny();
    }
    return Optional.empty();
  }

  @Override
  public Optional<T> updateById(Long id, T updateItem) {
    Optional<T> optionalItem = getById(id);
    if (optionalItem.isPresent()) {
      updateItem.setId(id);
      List<T> updatedList = getAll().stream()
          .filter(item -> !Objects.equals(item, optionalItem.get()))
          .collect(Collectors.toList());
      updatedList.add(Integer.parseInt(String.valueOf(id)), updateItem);
      fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
      return optionalItem;
    }
    return Optional.empty();
  }

  @Override
  public <P> Optional<T> updateByProperty(P prop, T updateItem) {
    Optional<T> optionalItem = getByUniqueProperty(prop);
    List<T> updatedList;
    if (optionalItem.isPresent()) {
      updateItem.setId(optionalItem.get().getId());
      updatedList = getAll().stream()
          .filter(item -> !item.equals(optionalItem.get()))
          .collect(Collectors.toList());
      updatedList.add(Integer.parseInt(String.valueOf(updateItem.getId())), updateItem);
      fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
      return optionalItem;
    }
    return Optional.empty();
  }

  @Override
  public Optional<T> deleteById(Long id) {
    File itemFile = new File(filePath.toString());
    List<String> itemsToSave;
    if (itemFile.exists()) {
      FileManager.makeBackupFile(filePath);
      itemsToSave = getAll().stream()
          .filter(item -> !Objects.equals(item.getId(), id))
          .map(serializer::objectToJson)
          .toList();
      fileService.writeLinesToFile(filePath, itemsToSave);
      if (!fileService.readAllFile(filePath).isEmpty()) {
        FileManager.deleteBackupFile(filePath);
      } else {
        try {
          FileManager.validateFile(itemFile);
        } catch (InvalidFileException | FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
      return getAll().stream()
          .filter(item -> item.getId().compareTo(id) == 0).findAny();
    }
    return Optional.empty();
  }

  @Override
  public <P> Optional<T> deleteByProperty(P prop) {
    File itemFile = new File(filePath.toString());
    List<String> itemsToSave;

    if (itemFile.exists()) {
      FileManager.makeBackupFile(filePath);
      itemsToSave = getAll().stream()
          .filter(item -> !item.getNumber().equalsIgnoreCase(prop.toString()))
          .map(serializer::objectToJson)
          .toList();
      fileService.writeLinesToFile(filePath, itemsToSave);
      if (!fileService.readAllFile(filePath).isEmpty()) {
        FileManager.deleteBackupFile(filePath);
      } else {
        try {
          FileManager.validateFile(itemFile);
        } catch (InvalidFileException | FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
      return getAll().stream()
          .filter(deleted -> deleted.getNumber().equalsIgnoreCase(prop.toString()))
          .findAny();
    }
    return Optional.empty();
  }
}
