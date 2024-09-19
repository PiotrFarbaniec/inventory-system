package pl.inventory.system.utils

import pl.inventory.system.utils.exceptions.InvalidFileException
import spock.lang.Specification

import java.nio.file.Files

class FileManagerTest extends Specification {

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
    }

    def "should delete created backup file"() {
        given:
        def existingFile = new File("testFile.txt")
        def backupFile = new File("testFile_COPY.txt")

        when:
        FileManager.deleteBackupFile(existingFile.toPath())

        then:
        existingFile.exists()
        !backupFile.exists()
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

    def "file validation should throw an exception"() {
        when: 'when file dose not exist'
        File testFile = new File("someFile.txt")
        FileManager.validateFile(testFile)

        then:
        thrown(FileNotFoundException.class)
    }
}
