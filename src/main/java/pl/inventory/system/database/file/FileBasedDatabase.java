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
import pl.inventory.system.model.Numerable;
import pl.inventory.system.utils.FileManager;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonSerializer;

@Slf4j
public class FileBasedDatabase<T extends Numerable> implements Database<T> {

  private static final Lock LOCK = new ReentrantLock();
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
  public Long save(T item) {
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
  public List<T> getAll() {
    LOCK.lock();
    try {
      File itemFile = new File(filePath.toString());
      if (itemFile.exists()) {
        log.debug("Downloading {} objects from the database successfully completed", cls.getSimpleName());
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
  public <P> Optional<T> getByUniqueProperty(P prop) {
    LOCK.lock();
    try {
      Optional<T> searchedObject = getAll().stream()
          .filter(item -> item.getNumber().equalsIgnoreCase(String.valueOf(prop)))
          .findFirst();
      if (searchedObject.isPresent()) {
        log.debug("Download \"{} {}\" successfully completed.", cls.getSimpleName(), prop);
        return searchedObject;
      } else {
        log.debug("Download failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), prop);
        return Optional.empty();
      }
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public Optional<T> getById(Long id) {
    LOCK.lock();
    try {
      Optional<T> searchedObject = getAll().stream()
          .filter(item -> item.getId().compareTo(id) == 0)
          .findFirst();
      if (searchedObject.isPresent()) {
        log.debug("Download \"{} {}\" successfully completed.", cls.getSimpleName(), id);
        return searchedObject;
      } else {
        log.debug("Download failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), id);
        return Optional.empty();
      }
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public Optional<T> updateById(Long id, T updateItem) {
    LOCK.lock();
    try {
      Optional<T> optionalItem = getById(id);
      if (optionalItem.isPresent()) {
        T oldItem = optionalItem.get();
        List<T> itemsList = getAll();
        final int optionalIndex = itemsList.indexOf(oldItem);
        FileManager.makeBackupFile(filePath);
        updateItem.setId(id);
        List<T> updatedList = itemsList.stream()
            .filter(item -> !Objects.equals(item, oldItem))
            .collect(Collectors.toList());
        updatedList.add(optionalIndex, updateItem);
        fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
        if (getAll().contains(updateItem)) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Update of the \"{} {}\" successfully completed.", cls.getSimpleName(), id);
        return Optional.of(updateItem);
      }
      log.debug("Update failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), id);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<T> updateByUniqueProperty(P prop, T updateItem) {
    LOCK.lock();
    try {
      Optional<T> optionalItem = getByUniqueProperty(prop);
      if (optionalItem.isPresent()) {
        T oldItem = optionalItem.get();
        List<T> itemsList = getAll();
        final int optionalIndex = itemsList.indexOf(oldItem);
        FileManager.makeBackupFile(filePath);
        updateItem.setId(oldItem.getId());
        List<T> updatedList = itemsList.stream()
            .filter(item -> !Objects.equals(item, oldItem))
            .collect(Collectors.toList());
        updatedList.add(optionalIndex, updateItem);
        fileService.writeLinesToFile(filePath, updatedList.stream().map(serializer::objectToJson).toList());
        if (getAll().contains(updateItem)) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Update of the \"{} {}\" successfully completed.", cls.getSimpleName(), prop);
        return Optional.of(updateItem);
      }
      log.debug("Update failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), prop);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public Optional<T> deleteById(Long id) {
    LOCK.lock();
    try {
      Optional<T> optionalToRemove = getById(id);
      if (optionalToRemove.isPresent()) {
        FileManager.makeBackupFile(filePath);
        List<String> itemsToSave = getAll().stream()
            .filter(item -> !Objects.equals(item.getId(), id))
            .map(serializer::objectToJson)
            .toList();
        fileService.writeLinesToFile(filePath, itemsToSave);
        if (!(getAll().isEmpty() && getAll().contains(optionalToRemove.get()))) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Delete of the \"{} {}\" successfully completed.", cls.getSimpleName(), id);
        return optionalToRemove;
      }
      log.debug("Delete failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), id);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public <P> Optional<T> deleteByUniqueProperty(P prop) {
    LOCK.lock();
    try {
      Optional<T> optionalToRemove = getByUniqueProperty(prop);
      if (optionalToRemove.isPresent()) {
        FileManager.makeBackupFile(filePath);
        List<String> itemsToSave = getAll().stream()
            .filter(item -> !item.getNumber().equalsIgnoreCase(prop.toString()))
            .map(serializer::objectToJson)
            .toList();
        fileService.writeLinesToFile(filePath, itemsToSave);
        if (!(getAll().isEmpty() && getAll().contains(optionalToRemove.get()))) {
          FileManager.deleteBackupFile(filePath);
        }
        log.debug("Delete of the \"{} {}\" successfully completed.", cls.getSimpleName(), prop);
        return optionalToRemove;
      }
      log.debug("Delete failed. The \"{} {}\" does not exist in the database.", cls.getSimpleName(), prop);
      return Optional.empty();
    } finally {
      LOCK.unlock();
    }
  }
}
