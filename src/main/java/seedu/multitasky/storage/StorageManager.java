package seedu.multitasky.storage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import seedu.multitasky.commons.core.ComponentManager;
import seedu.multitasky.commons.core.LogsCenter;
import seedu.multitasky.commons.events.model.EntryBookChangedEvent;
import seedu.multitasky.commons.events.storage.DataSavingExceptionEvent;
import seedu.multitasky.commons.events.storage.EntryBookToRedoEvent;
import seedu.multitasky.commons.events.storage.EntryBookToUndoEvent;
import seedu.multitasky.commons.events.storage.FilePathChangedEvent;
import seedu.multitasky.commons.events.storage.LoadDataFromFilePathEvent;
import seedu.multitasky.commons.exceptions.DataConversionException;
import seedu.multitasky.model.EntryBook;
import seedu.multitasky.model.ReadOnlyEntryBook;
import seedu.multitasky.model.StorageUserPrefs;
import seedu.multitasky.model.UserPrefs;

/**
 * Manages storage of EntryBook data in local storage.
 */
public class StorageManager extends ComponentManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private EntryBookStorage entryBookStorage;
    private UserPrefsStorage userPrefsStorage;
    private StorageUserPrefs userPrefs;

    public StorageManager(EntryBookStorage entryBookStorage, UserPrefsStorage userPrefsStorage,
            StorageUserPrefs userPrefs) {
        super();
        this.entryBookStorage = entryBookStorage;
        this.userPrefsStorage = userPrefsStorage;
        this.userPrefs = userPrefs;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public String getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataConversionException, IOException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(UserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }

    // ================ EntryBook methods ==============================

    @Override
    public String getEntryBookFilePath() {
        return entryBookStorage.getEntryBookFilePath();
    }

    // @@author A0132788U
    /**
     * Gets and sets the proper filepath of the current snapshot with index
     */
    @Override
    public String getEntryBookSnapshotPath() {
        return UserPrefs.getEntryBookSnapshotPath() + UserPrefs.getIndex() + ".xml";
    }

    /** Sets the entryBookFilePath for */
    @Override
    public void setEntryBookFilePath(String newFilePath) {
        entryBookStorage.setEntryBookFilePath(newFilePath);
    }

    // @@author
    @Override
    public Optional<ReadOnlyEntryBook> readEntryBook() throws DataConversionException, IOException {
        return readEntryBook(entryBookStorage.getEntryBookFilePath());
    }

    @Override
    public Optional<ReadOnlyEntryBook> readEntryBook(String filePath) throws DataConversionException, IOException {
        logger.fine("Attempting to read data from file: " + filePath);
        return entryBookStorage.readEntryBook(filePath);
    }

    @Override
    public void saveEntryBook(ReadOnlyEntryBook entryBook) throws IOException {
        saveEntryBook(entryBook, entryBookStorage.getEntryBookFilePath());
    }

    @Override
    public void saveEntryBook(ReadOnlyEntryBook entryBook, String filePath) throws IOException {
        logger.fine("Attempting to write to data file: " + filePath);
        entryBookStorage.saveEntryBook(entryBook, filePath);
    }

    // @@author A0132788U
    // ================ StorageManager methods ==============================
    /**
     * Gets and sets the proper filepath of the previous snapshot needed for undo
     */
    public static String getPreviousEntryBookSnapshotPath() {
        UserPrefs.decrementIndexByOne();
        return UserPrefs.getEntryBookSnapshotPath() + UserPrefs.getIndex() + ".xml";
    }

    public static void setPreviousEntryBookSnapshotPath(String filepath) {
        UserPrefs.setEntryBookSnapshotPath(filepath);
    }

    /**
     * Gets and sets the proper filepath of the next snapshot needed for redo
     */
    public static String getNextEntryBookSnapshotPath() {
        UserPrefs.incrementIndexByOne();
        return UserPrefs.getEntryBookSnapshotPath() + UserPrefs.getIndex() + ".xml";
    }

    public static void setNextEntryBookSnapshotPath(String filepath) {
        UserPrefs.setEntryBookSnapshotPath(filepath);
    }

    /**
     * Gets the filepath of the most current snapshot xml file and increments index by one.
     */
    public String setEntryBookSnapshotPathAndUpdateIndex() {
        UserPrefs.incrementIndexByOne();
        return getEntryBookSnapshotPath();
    }

    /**
     * Saves the entryBookSnapshot at the file path given by above method.
     */
    public void saveEntryBookSnapshot(ReadOnlyEntryBook entryBook) throws IOException {
        saveEntryBook(entryBook, setEntryBookSnapshotPathAndUpdateIndex());
    }

    /**
     * Loads data from the given file.
     *
     * @throws Exception
     */
    public EntryBook loadDataFromFile(String filepath) throws Exception {
        try {
            ReadOnlyEntryBook dataFromFile = XmlFileStorage.loadDataFromSaveFile(new File(filepath));
            return new EntryBook(dataFromFile);
        } catch (Exception e) {
            throw new Exception("Nothing to load from!");
        }
    }

    // ================ Event Handling methods ==============================
    /**
     * Saves the data to the entrybook at the filepath specified and also creates a snapshot in data/snapshots.
     */
    @Override
    @Subscribe
    public void handleEntryBookChangedEvent(EntryBookChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "Local data changed, saving to file"));
        try {
            saveEntryBook(event.data);
            saveEntryBookSnapshot(event.data);
        } catch (IOException e) {
            raise(new DataSavingExceptionEvent(e));
        }
    }

    /**
     * Saves data from the previous snapshot to the current entrybook and passes back
     * the event data to ModelManager to reset and update the display.
     *
     * @throws Exception
     */
    @Override
    @Subscribe
    public void handleEntryBookToUndoEvent(EntryBookToUndoEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "Load previous snapshot"));
        try {
            EntryBook entry = loadDataFromFile(getPreviousEntryBookSnapshotPath());
            saveEntryBook(entry);
            event.setData(entry);
            event.setMessage("undo successful");
        } catch (Exception e) {
            event.setMessage("Nothing to undo");
            UserPrefs.incrementIndexByOne();
        }
    }

    /**
     * Saves data from the next snapshot to the current entrybook and passes back
     * the event data to ModelManager to reset and update the display.
     *
     * @throws Exception
     */
    @Override
    @Subscribe
    public void handleEntryBookToRedoEvent(EntryBookToRedoEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "Load next snapshot"));
        try {
            EntryBook entry = loadDataFromFile(getNextEntryBookSnapshotPath());
            saveEntryBook(entry);
            event.setData(entry);
            event.setMessage("redo successful");
        } catch (Exception e) {
            event.setMessage("Nothing to redo");
            UserPrefs.decrementIndexByOne();
        }
    }

    /**
     * Saves data of the entrybook at the filepath specified.
     *
     * @throws IOException
     */
    @Subscribe
    public void handleFilePathChangedEvent(FilePathChangedEvent event) throws IOException {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "File path changed, saving to file"));
        entryBookStorage.setEntryBookFilePath(event.getNewFilePath());
        userPrefs.setEntryBookFilePath(event.getNewFilePath());
        saveEntryBook(event.data, event.getNewFilePath());
    }

    /**
     * Opens data at the filepath specified.
     *
     * @throws DataConversionException
     */
    @Subscribe
    public void handleLoadDataFromFilePathEvent(LoadDataFromFilePathEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "Data changed, opening file and loading data"));
        try {
            EntryBook entry = loadDataFromFile(event.getFilepath());
            saveEntryBook(entry);
            event.setData(entry);
            event.setMessage("open successful");
        } catch (Exception e) {
            event.setMessage("Error in loading data!");
        }
    }

}
