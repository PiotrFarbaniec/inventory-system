package pl.inventory.system.database.file;

import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.inventory.system.database.Database;
import pl.inventory.system.model.Item;
import pl.inventory.system.model.Room;
import pl.inventory.system.utils.FileManager;
import pl.inventory.system.utils.FileService;
import pl.inventory.system.utils.IdProvider;
import pl.inventory.system.utils.JsonSerializer;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "inventory_system.database.name", havingValue = "file")
public class FileBasedDatabaseConfig {
  private final String filesDirectory;
  private final String fileName;
  private final String roomIdFileName;
  private final String itemIdFileName;

  public FileBasedDatabaseConfig(
      @Value("${inventory_system.database.files_directory}") String filesDirectory,
      @Value("${inventory_system.database.file_name}") String fileName,
      @Value("${inventory_system.database.room_id_file_name}") String roomIdFileName,
      @Value("${inventory_system.database.item_id_file_name}") String itemIdFileName) {
    this.filesDirectory = filesDirectory;
    this.fileName = fileName;
    this.roomIdFileName = roomIdFileName;
    this.itemIdFileName = itemIdFileName;
  }

  @Bean
  public Path roomFilePath() {
    return FileManager.createFile(fileName, filesDirectory);
  }

  @Bean
  public IdProvider roomIdProvider(FileService fileService) {
    Path roomIdPath = FileManager.createFile(roomIdFileName, filesDirectory);
    return new IdProvider(roomIdPath, fileService);
  }

  @Bean
  public IdProvider itemIdProvider(FileService fileService) {
    Path itemIdPath = FileManager.createFile(itemIdFileName, filesDirectory);
    return new IdProvider(itemIdPath, fileService);
  }

  @Bean
  public Database<Room> roomDatabase(
      FileService fileService,
      JsonSerializer serializer) throws IOException {
    log.debug("File database has been initialised for objects of type Room");
    return new FileBasedDatabase<>(
        roomFilePath(),
        roomIdProvider(fileService),
        fileService,
        serializer,
        Room.class
    );
  }

  @Bean
  public Database<Item> itemDatabase(
      FileService fileService,
      JsonSerializer serializer) throws IOException {
    log.debug("File database has been initialised for objects of type Item");
    return new FileBasedDatabase<>(
        roomFilePath(),
        itemIdProvider(fileService),
        fileService,
        serializer,
        Item.class
    );
  }
}
