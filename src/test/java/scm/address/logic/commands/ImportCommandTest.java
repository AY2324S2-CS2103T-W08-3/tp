package scm.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scm.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static scm.address.testutil.Assert.assertThrows;
import static scm.address.testutil.TypicalPersons.JAMES;
import static scm.address.testutil.TypicalPersons.getTypicalAddressBook;
import static scm.address.testutil.TypicalSchedules.getTypicalScheduleList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import scm.address.commons.exceptions.DataLoadingException;
import scm.address.commons.exceptions.IllegalValueException;
import scm.address.logic.commands.exceptions.CommandException;
import scm.address.model.AddressBook;
import scm.address.model.Model;
import scm.address.model.ModelManager;
import scm.address.model.ScheduleList;
import scm.address.model.UserPrefs;
import scm.address.storage.JsonAdaptedPerson;


public class ImportCommandTest {
    private static final String UNKNOWN_FILE_NAME = "./src/test/data/ImportCommandTest/abcdefgh_abcdefgh.json";
    private static final String ADDRESS_BOOK_PATH = "./src/test/data/ImportCommandTest/addressbook.json";
    private static final String TEST_CSV_FILE_PATH = "./src/test/data/ImportCommandTest/contacts.csv";
    private static final String INVALID_DATA_CSV_FILE_PATH = "./src/test/data/ImportCommandTest/datasample.csv";
    private static final String UNKNOWN_FILE_EXTENSION = "./src/test/data/ImportCommandTest/contacts.xyz";
    private static final String NO_FILE_EXTENSION = "./src/test/data/ImportCommandTest/contacts";
    private static final String UNKNOWN_CSV_FILE = "./src/test/data/ImportCommandTest/abcdefg.csv";
    private static final String ADDRESS_BOOK_CSV_PATH = "./src/test/data/ImportCommandTest/addressbook.csv";
    private static final String ADDRESS_BOOK_CSV_NO_TAGS_PATH = "./src/test/data/ImportCommandTest"
            + "/addressbookNoTags.csv";
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());

    @Test
    public void constructor_nullFileSet_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ImportCommand(null));
    }

    @Test
    public void execute_existingJsonFile_success() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_PATH));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs(), new ScheduleList());
        String expectedMessage = "Contacts from files imported";
        expectedModel.addPerson(JAMES);
        assertCommandSuccess(importCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void readPersonsFromCsv_invalidFileFormat_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(UNKNOWN_CSV_FILE));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        try {
            importCommand.execute(model);
            //Fail if no exception thrown
            assertTrue(false);
        } catch (Exception e) {
            assertEquals(e.getClass(), CommandException.class);
        }
    }

    @Test
    public void execute_importingFromCsv_success() throws CommandException {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_CSV_PATH));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        importCommand.execute(testModel);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        expectedModel.addPerson(JAMES);

        assertEquals(testModel, expectedModel);
    }

    @Test
    public void readPersonsFromCsv_validFileNoTags_success() throws CommandException {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_CSV_NO_TAGS_PATH));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        importCommand.execute(testModel);

        //Success if no exceptions are thrown
        assertTrue(true);
    }


    @Test
    public void retrievePersonsFromFile_validJsonFile_success() throws CommandException {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_PATH));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        importCommand.execute(testModel);

        assert true;
    }

    @Test
    public void retrievePersonsFromFile_validCsvFile_success() throws CommandException {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_CSV_PATH));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        importCommand.execute(testModel);

        //Success if no exceptions are thrown
        assertTrue(true);
    }

    @Test
    public void retrievePersonsFromFile_invalidCsvFile_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(UNKNOWN_CSV_FILE));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        assertThrows(CommandException.class, () -> importCommand.execute(testModel));
    }

    @Test
    public void retrievePersonsFromFile_invalidDataCsvFile_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(INVALID_DATA_CSV_FILE_PATH));

        Model testModel = new ModelManager(getTypicalAddressBook(), new UserPrefs(), getTypicalScheduleList());
        ImportCommand importCommand = new ImportCommand(curHashSet);
        assertThrows(CommandException.class, () -> importCommand.execute(testModel));
    }

    @Test
    public void execute_importingFromUnknownFileFormat_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(UNKNOWN_FILE_EXTENSION));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        assertThrows(CommandException.class, () -> importCommand.execute(model));
    }

    @Test
    public void execute_importingFromNoFileExtension_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(NO_FILE_EXTENSION));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        assertThrows(CommandException.class, () -> importCommand.execute(model));
    }

    @Test
    public void execute_hasDuplicatePerson_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_PATH));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs(), new ScheduleList());
        expectedModel.addPerson(JAMES);
        String expectedMessage = String.format(ImportCommand.MESSAGE_DUPLICATE_PERSON, JAMES.getName(),
                JAMES.getPhone());
        assertThrows(CommandException.class, expectedMessage, () -> importCommand.execute(expectedModel));
    }

    @Test
    public void retrievePersonsFromFile_fileNotExist_failure() {
        List<JsonAdaptedPerson> list = new ArrayList<>();
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(UNKNOWN_FILE_NAME));
        ImportCommand importCommand = new ImportCommand(curHashSet);
        assertThrows(IllegalValueException.class, () -> importCommand.retrievePersonsFromFile(list));
    }

    @Test
    public void execute_dataLoadingException_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_PATH));
        assertThrows(CommandException.class, () -> new ImportCommandStubDataLoadingException(curHashSet)
                .execute(model));
    }

    @Test
    public void execute_illegalValueException_failure() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(ADDRESS_BOOK_PATH));
        assertThrows(CommandException.class, () -> new ImportCommandStubIllegalValueException(curHashSet)
                .execute(model));
    }

    @Test
    public void equals() {
        HashSet<File> curHashSet = new HashSet<>();
        curHashSet.add(new File(UNKNOWN_FILE_NAME));
        HashSet<File> otherHashSet = new HashSet<>();

        final ImportCommand importCommand = new ImportCommand(curHashSet);

        assertTrue(importCommand.equals(importCommand));

        assertFalse(importCommand.equals(null));

        assertFalse(importCommand.equals(new ClearCommand()));

        assertFalse(importCommand.equals(new ImportCommand(otherHashSet)));
    }

    private class ImportCommandStubDataLoadingException extends ImportCommand {
        /**
         * Constructs a new ImportCommand to add the contacts in the specified files in {@code fileSet}.
         *
         * @param fileSet A set of Files.
         */
        public ImportCommandStubDataLoadingException(Set<File> fileSet) {
            super(fileSet);
        }

        @Override
        public void retrievePersonsFromFile(List<JsonAdaptedPerson> savedPersons)
                throws IllegalValueException, DataLoadingException {
            throw new DataLoadingException(new Exception("Data loading exception."));
        }
    }

    private class ImportCommandStubIllegalValueException extends ImportCommand {

        /**
         * Constructs a new ImportCommand to add the contacts in the specified files in {@code fileSet}.
         *
         * @param fileSet A set of Files.
         */
        public ImportCommandStubIllegalValueException(Set<File> fileSet) {
            super(fileSet);
        }

        @Override
        public void retrievePersonsFromFile(List<JsonAdaptedPerson> savedPersons)
                throws IllegalValueException, DataLoadingException {
            throw new IllegalValueException("Illegal value exception.");
        }
    }
}
