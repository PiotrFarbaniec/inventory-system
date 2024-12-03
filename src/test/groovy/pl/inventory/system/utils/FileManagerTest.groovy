package pl.inventory.system.utils

import pl.inventory.system.utils.exceptions.InvalidFileException
import spock.lang.Specification

import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class FileManagerTest extends Specification {

    def "create a backup file should fail on wrong file type"() {
        def notCorrectFile = Path.of("wrongFile.html")

        when:
        FileManager.makeBackupFile(notCorrectFile)

        then:
        thrown(RuntimeException.class)

        cleanup:
        FileManager.deleteBackupFile(notCorrectFile)
    }

    def "delete backup file should fail on wrong file type"() {
        def notCorrectFile = Path.of("someFile.html")

        when:
        FileManager.deleteBackupFile(notCorrectFile)

        then:
        thrown(RuntimeException.class)
    }

    def "should crate a file with the given name"() {
        given:
        def fileName = "testFile.txt"
        def file = new File(fileName)

        when:
        FileManager.createFile(file)

        then:
        file.exists()
    }

    def "should make a backup of specified file with '_COPY' extension"() {
        given:
        def file = new File("testFile.txt")

        when:
        FileManager.makeBackupFile(file.toPath())
        File bacupFile = new File("testFile_COPY.txt")

        then:
        file.exists()
        bacupFile.exists()

        cleanup:
        FileManager.deleteBackupFile(file.toPath())
    }

    def "should delete created source file"() {
        given:
        def existingFile = new File("testFile.txt")

        when:
        FileManager.deleteFile(existingFile)

        then:
        !existingFile.exists()
    }

    def "an exception should be thrown in the various cases for null argument"() {
        when:
        FileManager.createFile(null)

        then:
        thrown(RuntimeException.class)

        when:
        FileManager.makeBackupFile(null)

        then:
        thrown(RuntimeException.class)

        when:
        FileManager.deleteFile(null)

        then:
        thrown(RuntimeException.class)

        when:
        FileManager.deleteBackupFile(null)

        then:
        thrown(RuntimeException.class)
    }

    def "file validation should throw an exceptions in various cases"() {
        given:
        def filePath = Path.of("someFile.txt")

        when: 'when file dose not exist'
        File testFile = new File("someFile.txt")
        FileManager.validateFile(testFile)

        then:
        thrown(FileNotFoundException.class)

        when:
        def wrongFile = Files.createTempDirectory("someFile.txt")
        FileManager.validateFile(wrongFile.toFile())

        then:
        thrown(InvalidFileException.class)

        when:
        def otherFile = new File("otherFile.txt")
        FileManager.createFile(otherFile)
        otherFile.setWritable(false)
        FileManager.validateFile(otherFile)

        then:
        thrown(InvalidFileException.class)

        when:
        File noNameFile = new File(".txt")
        FileManager.createFile(noNameFile)

        then:
        thrown(RuntimeException.class)

        when:
        File wrongName = new File("wrongName")
        FileManager.createFile(wrongName)

        then:
        def exception1 = thrown(RuntimeException.class)
        assert exception1.cause.message == "File name or extension not specified"

        when:
        File wrongExtension = new File("wrongExtenson.doc")
        FileManager.createFile(wrongExtension)

        then:
        def exception2 = thrown(RuntimeException.class)
        assert exception2.cause.message == 'Not correct file extension (required "*.txt" or "*.json")'
        assert exception2.cause instanceof InvalidFileException

        cleanup:
        otherFile.setReadable(true)
        otherFile.setWritable(true)
        Files.deleteIfExists(otherFile.toPath())
        Files.deleteIfExists(noNameFile.toPath())
        Files.deleteIfExists(wrongName.toPath())
        Files.deleteIfExists(wrongExtension.toPath())
    }
}
