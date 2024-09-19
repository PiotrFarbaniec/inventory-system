package pl.inventory.system.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileService {

  /**
   * A no-argument constructor.
   */
  public FileService() {
  }

  /**
   * Adds a line of text to existing content in the specified source file.
   * The content in the file is not overwritten, but is modified by adding
   * the currently processed text content.
   * @param line {@link java.lang.String} text to be added to the file;
   * @param path {@link java.nio.file.Path} value of the file in which the text is to be saved;
   */
  public void appendLineToFile(Path path, String line) {
    try {
      Files.write(path, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes multiple lines of text to the source file and overwrites the existing file content.
   * @param path  {@link java.nio.file.Path} value of the file in which the text is to be saved;
   * @param lines content (as a {@link  List}) to be added to existing content in the specified source file;
   */
  public void writeLinesToFile(Path path, List<String> lines) {
    try {
      Files.write(path, (lines.get(0) + System.lineSeparator()).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    lines.stream()
        .filter(line -> !line.equals(lines.get(0)))
        .forEach(line -> {
          try {
            Files.write(path, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Saves the current text value to an existing source file.
   * The old contents of the file are overwritten with the current one.
   * @param path    {@link java.nio.file.Path} value of the file in which the text is to be saved;
   * @param content ({@link  java.lang.String}) to be added to the specified source file;
   */
  public void writeToFile(Path path, String content) {
    try {
      Files.write(path, (content + System.lineSeparator()).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads and returns the contents of the specified file as {@link java.util.List}<{@link java.lang.String}>.
   * @param path the {@link java.nio.file.Path} value of the file whose contents are to be read;
   * @return content as a {@link java.util.List} to be returned from specified source file;
   */
  public List<String> readAllFile(Path path) {
    try {
      return Files.readAllLines(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
