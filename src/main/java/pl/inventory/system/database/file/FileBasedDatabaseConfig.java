package pl.inventory.system.database.file;

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
import pl.inventory.system.utils.JsonService;

@Slf4j
@Configuration
@SuppressWarnings("unused")
@ConditionalOnProperty(name = "inventory_system.database.name", havingValue = "file")
public class FileBasedDatabaseConfig {

  private final String filesDirectory;
  private final String roomFileName;
  private final String roomIdFileName;
  private final String itemIdFileName;

  public FileBasedDatabaseConfig(
      @Value("${inventory_system.database.files_directory}") String filesDirectory,
      @Value("${inventory_system.database.room_file_name}") String roomFileName,
      @Value("${inventory_system.database.room_id_file_name}") String roomIdFileName,
      @Value("${inventory_system.database.item_id_file_name}") String itemIdFileName) {
    this.filesDirectory = filesDirectory;
    this.roomFileName = roomFileName;
    this.roomIdFileName = roomIdFileName;
    this.itemIdFileName = itemIdFileName;
  }

  @Bean
  public Path roomFilePath() {
    return FileManager.createFile(roomFileName, filesDirectory);
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
  public Database<Room, Item> roomDatabase(
      FileService fileService,
      JsonService serializer) {
    log.debug("File database has been initialised for objects of type Room");
    return new FileBasedDatabase(
        roomFilePath(),
        itemIdProvider(fileService),
        roomIdProvider(fileService),
        fileService,
        serializer,
        Room.class
    );
  }
}
