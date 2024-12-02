package pl.inventory.system.utils

import pl.inventory.system.utils.exceptions.InvalidFileException
import spock.lang.Specification

import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path

class FileManagerTest extends Specification {

    // new test methods
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

    /*def "should not make backup on wrong argument"() {
        given:
        def fileName = "testFile.doc"
        def filePath = Files.createDirectory(Path.of(fileName))

        when:
        FileManager.makeBackupFile(filePath)

        then:
        thrown(RuntimeException.class)

    }*/




    // new test methods

    def "should crate a file with the given name"() {
        given:
        def fileName = "testFile.txt"
        def file = new File(fileName)

        when:
        FileManager.createFile(file)

        then:
        file.exists()
    }

    def "should not create a file for wrong arguments"() {
        given:
        def fileName = "testFile.txt"
        def file = new File(fileName)

        when:
        FileManager.createFile(fileName, fileName)

        then:
        thrown(RuntimeException.class)
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
        def isWriteOnly = otherFile.setReadOnly()
        FileManager.deleteFile(otherFile)

        then:
        isWriteOnly
        !otherFile.canWrite()
        thrown(RuntimeException.class)

        when:
        FileManager.validateFile(otherFile)
        FileManager.deleteFile(otherFile)

        then:
        thrown(InvalidFileException.class)

        cleanup:
        otherFile.setReadable(true)
        otherFile.setWritable(true)
        Files.deleteIfExists(otherFile.toPath())
    }

    /*def "file validation should throw exceptions in various cases"() {
        given:
        def filePath = Path.of("someFile.txt")

        when: 'when file does not exist'
        File testFile = new File("someFile.txt")
        FileManager.validateFile(testFile)

        then:
        thrown(FileNotFoundException.class)

        when: 'when file is a directory'
        def wrongFile = Files.createTempDirectory("someFileDir").toFile()
        FileManager.validateFile(wrongFile)

        then:
        thrown(InvalidFileException.class)

        when: 'when file has no write or read permission and cannot be modified'
        def noPermissionFile = File.createTempFile("noPermissionFile", ".txt")
        noPermissionFile.setReadable(false)
        noPermissionFile.setWritable(false)
        FileManager.validateFile(noPermissionFile)

        then:
        noExceptionThrown()
        *//*thrown(InvalidFileException.class)*//*

        when: 'when file name is empty'
        def emptyNameFile = new File(".txt")
        emptyNameFile.createNewFile()
        FileManager.validateFile(emptyNameFile)

        then: thrown(InvalidFileException.class)

        when: 'when file has incorrect extension'
        def incorrectExtensionFile = new File("file.wrong")
        incorrectExtensionFile.createNewFile()
        FileManager.validateFile(incorrectExtensionFile)

        then: thrown(InvalidFileException.class)

        when: 'when file is valid'
        def validFile = new File("validFile.txt")
        validFile.createNewFile()
        validFile.setWritable(true)
        validFile.setReadable(true)
        FileManager.validateFile(validFile)

        then: noExceptionThrown()
    }*/
}
