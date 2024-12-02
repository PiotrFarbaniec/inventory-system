package pl.inventory.system.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import pl.inventory.system.utils.exceptions.InvalidArgumentException;
import pl.inventory.system.utils.exceptions.InvalidFileException;

public final class FileManager {

  private FileManager() {
  }

  public static void createFile(File file) {
    try {
      ArgumentValidator.validateArgument(file, "file");
      if (!file.exists()) {
        Files.createFile(file.toPath());
        validateFile(file);
      }
    } catch (InvalidArgumentException | IOException | InvalidFileException e) {
      throw new RuntimeException(e);
    }
  }

  public static Path createFile(String fileName, String dirName) {
    File dn = new File(dirName);
    Path directory = Path.of(dirName);
    Path file = directory.resolve(fileName);
    try {
      ArgumentValidator.validateArgument(fileName, "filePath in createFile(String filePath, String fileDir)");
      ArgumentValidator.validateArgument(dirName, "fileDir in createFile(String filePath, String fileDir)");
      if (!dn.exists()) {
        Files.createDirectory(directory);
      }
      if (Files.notExists(file)) {
        Files.createFile(file);
        validateFile(file.toFile());
      }
    } catch (InvalidArgumentException | IOException | InvalidFileException e) {
      throw new RuntimeException(e);
    }
    return file;
  }

  public static void deleteFile(File file) {
    try {
      ArgumentValidator.validateArgument(file, "file");
      Files.deleteIfExists(file.toPath());
    } catch (InvalidArgumentException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void copyFile(Path fromFilePath, Path toFilePath) {
    try {
      ArgumentValidator.validateArgument(fromFilePath, "source file path");
      ArgumentValidator.validateArgument(toFilePath, "destination file path");
      Files.copy(fromFilePath, toFilePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (InvalidArgumentException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void makeBackupFile(Path path) {
    try {
      ArgumentValidator.validateArgument(path, "file path to be backed up");
    } catch (InvalidArgumentException e) {
      throw new RuntimeException(e);
    }
    String backupName = path.toFile().getName().split("\\.")[0];
    File tempFile = new File(path.toFile().getParent(), String.format("%s_COPY.txt", backupName));
    try {
      createFile(tempFile);
      validateFile(path.toFile());
      validateFile(tempFile);
      copyFile(path, Path.of(tempFile.getPath()));
    } catch (IOException | InvalidFileException e) {
      throw new RuntimeException(e);
    }
  }

  public static void deleteBackupFile(Path sourcePath) {
    try {
      ArgumentValidator.validateArgument(sourcePath, "file path whose backup is to be deleted");
    } catch (InvalidArgumentException e) {
      throw new RuntimeException(e);
    }
    String backupName = sourcePath.toFile().getName().split("\\.")[0];
    File tempFile = new File(sourcePath.toFile().getParent(), String.format("%s_COPY.txt", backupName));
    try {
      validateFile(tempFile);
    } catch (InvalidFileException | FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    deleteFile(tempFile);
  }

  public static void validateFile(File file) throws InvalidFileException, FileNotFoundException {
    final String[] fileName = (file.getName()).split("\\.");
    if (!file.exists()) {
      throw new FileNotFoundException(String.format("Attempt to create file: {%s} unsuccessful", file.getName()));
    }
    if (!file.canWrite() || !file.canRead()) {
      throw new InvalidFileException(String.format("The file: {%s} is read- or write-protected.", file.getName()));
    }
    if (!file.isFile()) {
      throw new InvalidFileException("The file is not correct file type");
    }
    if (fileName.length < 2 || fileName[0].isBlank()) {
      throw new InvalidFileException("File name can't be empty");
    } else if (!fileName[1].equalsIgnoreCase("txt")
        && !fileName[1].equalsIgnoreCase("json")) {
      throw new InvalidFileException("Not correct file extension (required \"*.txt\" or \"*.json\")");
    }
  }
}
