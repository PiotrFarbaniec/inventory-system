package pl.inventory.system.database.file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.InternallyStorable;
import pl.inventory.system.model.Storable;
import pl.inventory.system.utils.FileManager;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonSerializer;

@Slf4j
public abstract class AbstractFileDatabase<T1 extends Storable, T2 extends InternallyStorable> implements Database<T1, T2> {

  protected static final Lock LOCK = new ReentrantLock();
  private final Path filePath;
  private final IdProvider idProvider;
  private final FileService fileService;
  private final JsonSerializer serializer;
  private final Class<T1> cls;

  protected AbstractFileDatabase(Path roomFilePath,
                                 IdProvider idProvider,
                                 FileService fileService,
                                 JsonSerializer serializer,
                                 Class<T1> cls) {
    log.info("File database initialised for type {}", cls.getSimpleName());
    this.filePath = roomFilePath;
    this.idProvider = idProvider;
    this.fileService = fileService;
    this.serializer = serializer;
    this.cls = cls;
  }

  @Override
  public Long save(T1 item) {
    LOCK.lock();
    try {
      File itemFile = new File(filePath.toString());
      FileManager.createFile(itemFile);
      Long currentId = idProvider.getCurrentIdAndIncrement();
      item.setId(currentId);
      fileService.appendLineToFile(filePath, serializer.objectToJson(item));
      log.debug("\"{} {}\" successfully stored in database", cls.getSimpleName(), currentId);
      return currentId;
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public List<T1> getAll() {
    LOCK.lock();
    try {
      File itemFile = new File(filePath.toString());
      if (itemFile.exists()) {
        log.debug("Downloading all {} from the database successfully completed", cls.getSimpleName());
        return fileService.readAllFile(filePath).stream()
            .map(line -> serializer.jsonToObject(line, cls))
            .toList();
      }
      log.warn("Download from database failed. The file with the specified name \"{}\" does not exist",
          filePath.getFileName());
      return List.of();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<T1> getByProperty(P property) {
    LOCK.lock();
    try {
      Optional<T1> searchedObject = Optional.empty();
      if (property instanceof String roomNumber) {
        searchedObject = getAll().stream()
            .filter(item -> item.getNumber().equalsIgnoreCase(roomNumber))
            .findFirst();
      } else if (property instanceof Long roomId) {
        searchedObject = getAll().stream()
            .filter(item -> item.getId().compareTo(roomId) == 0)
            .findFirst();
      }
      if (searchedObject.isPresent()) {
        log.debug("Download \"{} {}\" successfully completed.", cls.getSimpleName(), property);
        return searchedObject;
      }
      log.debug("Download failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), property);
      return searchedObject;
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<T1> updateByProperty(P property, T1 updateItem) {
    LOCK.lock();
    try {
      if (property instanceof Long roomId) {
        return updateById(roomId, updateItem);
      } else if (property instanceof String roomNumber) {
        return updateByNumber(roomNumber, updateItem);
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<T1> deleteByProperty(P property) {
    LOCK.lock();
    try {
      if (property instanceof String roomNumber) {
        return deleteByNumber(roomNumber);
      } else if (property instanceof Long roomId) {
        return deleteById(roomId);
      }
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  private Optional<T1> updateById(Long roomId, T1 updateItem) {
    LOCK.lock();
    try {
      Optional<T1> optionalItem = getByProperty(roomId);
      if (optionalItem.isPresent()) {
        T1 oldItem = optionalItem.get();
        List<T1> itemsList = getAll();
        final int optionalIndex = itemsList.indexOf(oldItem);
        FileManager.makeBackupFile(filePath);
        updateItem.setId(roomId);
        List<T1> updatedList = itemsList.stream()
            .filter(item -> !Objects.equals(item, oldItem))
            .collect(Collectors.toList());
        updatedList.add(optionalIndex, updateItem);
        fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
        if (getAll().contains(updateItem)) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Update of the \"{} {}\" successfully completed.", cls.getSimpleName(), roomId);
        return Optional.of(updateItem);
      }
      log.debug("Update failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), roomId);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  private Optional<T1> updateByNumber(String roomNumber, T1 updateItem) {
    LOCK.lock();
    try {
      Optional<T1> optionalItem = getByProperty(roomNumber);
      if (optionalItem.isPresent()) {
        T1 oldItem = optionalItem.get();
        List<T1> itemsList = getAll();
        final int optionalIndex = itemsList.indexOf(oldItem);
        FileManager.makeBackupFile(filePath);
        updateItem.setId(oldItem.getId());
        List<T1> updatedList = itemsList.stream()
            .filter(item -> !Objects.equals(item, oldItem))
            .collect(Collectors.toList());
        updatedList.add(optionalIndex, updateItem);
        fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
        if (getAll().contains(updateItem)) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Update of the \"{} {}\" successfully completed.", cls.getSimpleName(), roomNumber);
        return Optional.of(updateItem);
      }
      log.debug("Update failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), roomNumber);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  private Optional<T1> deleteByNumber(String roomNumber) {
    LOCK.lock();
    try {
      Optional<T1> optionalToRemove = getByProperty(roomNumber);
      if (optionalToRemove.isPresent()) {
        FileManager.makeBackupFile(filePath);
        List<String> itemsToSave = getAll().stream()
            .filter(item -> !item.getNumber().equalsIgnoreCase(roomNumber))
            .map(serializer::objectToJson)
            .toList();
        fileService.writeLinesToFile(filePath, itemsToSave);
        if (!(getAll().isEmpty() && getAll().contains(optionalToRemove.get()))) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Delete of the \"{} {}\" successfully completed.", cls.getSimpleName(), roomNumber);
        return optionalToRemove;
      }
      log.debug("Delete failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), roomNumber);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  private Optional<T1> deleteById(Long roomId) {
    LOCK.lock();
    try {
      Optional<T1> optionalToRemove = getByProperty(roomId);
      if (optionalToRemove.isPresent()) {
        FileManager.makeBackupFile(filePath);
        List<String> itemsToSave = getAll().stream()
            .filter(item -> !Objects.equals(item.getId(), roomId))
            .map(serializer::objectToJson)
            .toList();
        fileService.writeLinesToFile(filePath, itemsToSave);
        if (!(getAll().isEmpty() && getAll().contains(optionalToRemove.get()))) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Delete of the \"{} {}\" successfully completed.", cls.getSimpleName(), roomId);
        return optionalToRemove;
      }
      log.debug("Delete failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), roomId);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }
}
