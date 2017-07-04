package seedu.address.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.entry.Entry;
import seedu.address.model.entry.ReadOnlyEntry;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.TypicalEntries;

public class EntryBookTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final EntryBook entryBook = new EntryBook();

    @Test
    public void constructor() {
        assertEquals(Collections.emptyList(), entryBook.getEntryList());
        assertEquals(Collections.emptyList(), entryBook.getTagList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        entryBook.resetData(null);
    }

    @Test
    public void resetData_withValidReadOnlyEntryBook_replacesData() {
        entryBook newData = new TypicalEntries().getTypicalEntryBook();
        entryBook.resetData(newData);
        assertEquals(newData, entryBook);
    }

    @Test
    public void resetData_withDuplicateTags_throwsAssertionError() {
        EntryBook typicalEntryBook = new TypicalEntries().getTypicalEntryBook();
        List<ReadOnlyEntry> newPersons = typicalEntryBook.getEntryList();
        List<Tag> newTags = new ArrayList<>(typicalEntryBook.getTagList());
        // Repeat the first tag twice
        newTags.add(newTags.get(0));
        EntryBookStub newData = new EntryBookStub(newPersons, newTags);

        thrown.expect(AssertionError.class);
        entryBook.resetData(newData);
    }

    /**
     * A stub ReadOnlyEntryBook whose persons and tags lists can violate interface constraints.
     */
    private static class EntryBookStub implements ReadOnlyEntryBook {
        private final ObservableList<ReadOnlyEntry> entries = FXCollections.observableArrayList();
        private final ObservableList<Tag> tags = FXCollections.observableArrayList();

        EntryBookStub(Collection<? extends ReadOnlyEntry> entries, Collection<? extends Tag> tags) {
            this.entries.setAll(entries);
            this.tags.setAll(tags);
        }

        @Override
        public ObservableList<ReadOnlyEntry> getEntryList() {
            return entries;
        }

        @Override
        public ObservableList<Tag> getTagList() {
            return tags;
        }
    }

}
