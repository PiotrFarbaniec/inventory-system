package pl.inventory.system.utils;

import java.nio.file.Path;

public class IdProvider {

  private final Path idPath;
  private final FileService fileService;

  /**
   * Constructing a class object. Takes parameters:
   *
   * @param idPath      {@link Path} to the file storing the identifier currently saved object;
   * @param fileService {@link FileService} object, responsible for writing/reading
   *                    the 'id' in the specified file;
   */
  public IdProvider(Path idPath, FileService fileService) {
    this.idPath = idPath;
    this.fileService = fileService;
  }

  /**
   * When called, it returns the current {@link java.lang.Long} id value for the object being stored in the database,
   * then increments it by 1 and stores it in the indicated file. For correct operation,
   * it requires the creation of an instance of the IdProvider class with the given value
   * for the Path field idPath and an object of the {@link FileService} class
   *
   * @return {@link java.lang.Long} value of currently stored id retrieved from specified {@link java.nio.file.Path}
   */
  public synchronized Long getCurrentIdAndIncrement() {
    long currentId;
    FileManager.createFile(idPath.toFile());
    currentId = fileService.readAllFile(idPath).isEmpty()
        ? 1L : Long.parseLong((fileService.readAllFile(idPath)).get(0));
    fileService.writeToFile(idPath, String.valueOf(currentId + 1L));
    return currentId;
  }
}
