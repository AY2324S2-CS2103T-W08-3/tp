package scm.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static scm.address.logic.commands.FindAndExportCommand.DEFAULT_DATA_DIR;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scm.address.commons.core.index.Index;
import scm.address.commons.util.StringUtil;
import scm.address.logic.parser.exceptions.ParseException;
import scm.address.model.file.Filename;
import scm.address.model.person.Address;
import scm.address.model.person.Email;
import scm.address.model.person.Name;
import scm.address.model.person.Phone;
import scm.address.model.schedule.Description;
import scm.address.model.schedule.Title;
import scm.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index is not a non-zero unsigned integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses {@code Collection<String> filenames} into a {@code Set<File>}.
     *
     * @param filenames String filenames.
     * @return A set of files to be imported.
     * @throws ParseException If the filenames are in an invalid format or are duplicated.
     */
    public static Set<File> parseFiles(Collection<String> filenames) throws ParseException {
        requireNonNull(filenames);
        final Set<File> fileSet = new HashSet<>();
        final Set<Filename> filenameSet = new HashSet<>();
        for (String fname : filenames) {
            String trimmedFname = fname.trim();
            if (!Filename.isValidFilename(trimmedFname)) {
                throw new ParseException(Filename.MESSAGE_CONSTRAINTS);
            }
            trimmedFname = "./data/" + trimmedFname;
            Filename curFilename = new Filename(trimmedFname);
            if (filenameSet.contains(curFilename)) {
                throw new ParseException(Filename.MESSAGE_DUPLICATE);
            }
            fileSet.add(new File(trimmedFname));
            filenameSet.add(curFilename);
        }
        return fileSet;
    }

    /**
     * Parses the given {@code String} and returns a File object.
     *
     * @param filename The string to be parsed into a File object.
     * @return The parsed File object.
     * @throws ParseException If the given string does not match the expected format.
     */
    public static File parseFileForExport(String filename) throws ParseException {
        requireNonNull(filename);
        String trimmedFname = filename.trim();
        if (!Filename.isValidFilename(trimmedFname)) {
            throw new ParseException(Filename.MESSAGE_CONSTRAINTS);
        }
        trimmedFname = "./" + DEFAULT_DATA_DIR + trimmedFname;
        return new File(trimmedFname);
    }

    /**
     * Parses the given {@code String} of arguments and returns a LocalDateTime object.
     *
     * @param dateTimeStr The string to be parsed into a LocalDateTime object.
     * @return The parsed LocalDateTime object.
     * @throws ParseException If the given string does not match the expected format.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) throws ParseException {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            throw new ParseException("Invalid date time format. Correct format: yyyy-MM-dd HH:mm");
        }
    }

    /**
     * Parses the given {@code String} and returns a Title object.
     *
     * @param titleStr The string to be parsed into a Title object.
     * @return The parsed Title object.
     */
    public static Title parseTitle(String titleStr) {
        if (titleStr == null) {
            throw new NullPointerException("The title cannot be null.");
        }
        return new Title(titleStr);
    }

    /**
     * Parses the given {@code String} and returns a Description object.
     *
     * @param descriptionStr The string to be parsed into a Description object.
     * @return The parsed Description object.
     */
    public static Description parseDescription(String descriptionStr) {
        if (descriptionStr == null) {
            throw new NullPointerException("The description cannot be null.");
        }
        return new Description(descriptionStr);
    }
}
