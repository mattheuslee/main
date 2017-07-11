package seedu.multitasky.storage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import seedu.multitasky.model.entry.Entry;
import seedu.multitasky.model.entry.ReadOnlyEntry;
import seedu.multitasky.model.tag.Tag;
import seedu.multitasky.model.util.EntryBuilder;

/**
 * JAXB-friendly version of the Entry.
 */
public class XmlAdaptedEntry {

    @XmlElement(required = true)
    private String name;

    @XmlElement
    private String startDateAndTime;
    @XmlElement
    private String endDateAndTime;
    @XmlElement
    private List<XmlAdaptedTag> tagged = new ArrayList<>();

    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm");

    /**
     * Constructs an XmlAdaptedEntry. This is the no-arg constructor that is
     * required by JAXB.
     */
    public XmlAdaptedEntry() {
    }

    /**
     * Converts a given Entry into this class for JAXB use. Future changes to
     * this will not affect the created XmlAdaptedEntry
     */
    public XmlAdaptedEntry(ReadOnlyEntry source) {
        name = source.getName().fullName;

        if (source.getStartDateAndTime() != null) {
            startDateAndTime = convertDateToString(source.getStartDateAndTime());
        }
        if (source.getEndDateAndTime() != null) {
            endDateAndTime = convertDateToString(source.getEndDateAndTime());
        }
        tagged = new ArrayList<>();
        for (Tag tag : source.getTags()) {
            tagged.add(new XmlAdaptedTag(tag));
        }
    }

    public String convertDateToString(Calendar given) {
        String dateToString = df.format(given.getTime());
        return dateToString;
    }

    public Calendar convertStringToDate(String given) throws NullPointerException {
        Calendar setDate = null;
        Date toConvert = new Date();
        try {
            toConvert = df.parse(given);
        } catch (ParseException e) {
            System.out.println("Invalid date!");
        }
        setDate.setTime(toConvert);
        return setDate;
    }

    /**
     * Converts this jaxb-friendly adapted entry object into the model's Entry
     * object.
     *
     * @throws Exception
     */

    public Entry toModelType() throws Exception {
        final List<Tag> personTags = new ArrayList<>();
        for (XmlAdaptedTag tag : tagged) {
            personTags.add(tag.toModelType());
        }
        Calendar startDateAndTimeToUse = Calendar.getInstance();
        Calendar endDateAndTimeToUse = Calendar.getInstance();

        if (startDateAndTime != null) {
            try {
                startDateAndTimeToUse = convertStringToDate(startDateAndTime);
            } catch (Exception e) {
                startDateAndTimeToUse = Calendar.getInstance();
            }
        }

        if (endDateAndTime != null) {
            try {
                endDateAndTimeToUse = convertStringToDate(endDateAndTime);
            } catch (Exception e) {
                endDateAndTimeToUse = Calendar.getInstance();
            }
        }

        final Set<Tag> tags = new HashSet<>(personTags);
        Entry entry = new EntryBuilder().withName(this.name).withStartDateAndTime(startDateAndTimeToUse)
                .withEndDateAndTime(endDateAndTimeToUse).withTags(tags).build();
        return entry;
    }
}
