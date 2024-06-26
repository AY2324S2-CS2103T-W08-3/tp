package scm.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scm.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static scm.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static scm.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static scm.address.logic.parser.CliSyntax.PREFIX_AFTER_DATETIME;
import static scm.address.logic.parser.CliSyntax.PREFIX_BEFORE_DATETIME;
import static scm.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static scm.address.logic.parser.CliSyntax.PREFIX_DURING_DATETIME;
import static scm.address.logic.parser.CliSyntax.PREFIX_FILENAME;
import static scm.address.logic.parser.CliSyntax.PREFIX_NAME;
import static scm.address.logic.parser.CliSyntax.PREFIX_TAG;
import static scm.address.logic.parser.CliSyntax.PREFIX_TITLE;
import static scm.address.logic.parser.ScheduleDateTimeFormatter.FORMATTER;
import static scm.address.testutil.Assert.assertThrows;
import static scm.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import scm.address.commons.core.index.Index;
import scm.address.logic.commands.AddCommand;
import scm.address.logic.commands.AddScheduleCommand;
import scm.address.logic.commands.ClearCommand;
import scm.address.logic.commands.ClearScheduleCommand;
import scm.address.logic.commands.Command;
import scm.address.logic.commands.DeleteCommand;
import scm.address.logic.commands.DeleteScheduleCommand;
import scm.address.logic.commands.EditCommand;
import scm.address.logic.commands.EditCommand.EditPersonDescriptor;
import scm.address.logic.commands.EditScheduleCommand;
import scm.address.logic.commands.ExitCommand;
import scm.address.logic.commands.FindAndExportCommand;
import scm.address.logic.commands.FindCommand;
import scm.address.logic.commands.FindScheduleCommand;
import scm.address.logic.commands.HelpCommand;
import scm.address.logic.commands.ImportCommand;
import scm.address.logic.commands.ListCommand;
import scm.address.logic.commands.ListOngoingScheduleCommand;
import scm.address.logic.commands.ListScheduleCommand;
import scm.address.logic.commands.descriptors.EditScheduleDescriptor;
import scm.address.logic.parser.exceptions.ParseException;
import scm.address.model.person.AddressContainsKeywordsPredicate;
import scm.address.model.person.NameContainsKeywordsPredicate;
import scm.address.model.person.Person;
import scm.address.model.person.TagsContainKeywordsPredicate;
import scm.address.model.schedule.AfterDateTimePredicate;
import scm.address.model.schedule.BeforeDateTimePredicate;
import scm.address.model.schedule.Description;
import scm.address.model.schedule.DescriptionContainsKeywordsPredicate;
import scm.address.model.schedule.DuringDateTimePredicate;
import scm.address.model.schedule.Schedule;
import scm.address.model.schedule.Title;
import scm.address.model.schedule.TitleContainsKeywordsPredicate;
import scm.address.testutil.EditPersonDescriptorBuilder;
import scm.address.testutil.EditScheduleDescriptorBuilder;
import scm.address.testutil.PersonBuilder;
import scm.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> nameKeywords = Arrays.asList("foo", "bar", "baz");
        List<String> addressKeywords = Arrays.asList("Street", "Rd", "Ln");
        List<String> tagKeywords = Arrays.asList("friend", "workmate");

        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " "
                        + PREFIX_NAME + nameKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_ADDRESS + addressKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_TAG + tagKeywords.stream().collect(Collectors.joining(" ")));

        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(nameKeywords),
                                        new AddressContainsKeywordsPredicate(addressKeywords),
                                        new TagsContainKeywordsPredicate(tagKeywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_import() throws Exception {
        assertTrue(parser.parseCommand(ImportCommand.COMMAND_WORD + " f/filename.json")
                instanceof ImportCommand);
        assertTrue(parser.parseCommand(ImportCommand.COMMAND_WORD + " f/filename1.json f/filename2.csv")
                instanceof ImportCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_findAndExport() throws ParseException {
        String tag = "friends";
        String name = "John";
        String address = "123 Main St";
        String filename = "output.json";

        String input = FindAndExportCommand.COMMAND_WORD + " "
                + PREFIX_TAG + tag + " "
                + PREFIX_NAME + name + " "
                + PREFIX_ADDRESS + address + " "
                + PREFIX_FILENAME + filename;

        FindAndExportCommand expectedCommand = new FindAndExportCommand(tag, name, address, new File(filename));

        FindAndExportCommand resultCommand = (FindAndExportCommand) parser.parseCommand(input);
        assertEquals(expectedCommand.getName(), resultCommand.getName());
        assertEquals(expectedCommand.getAddress(), resultCommand.getAddress());
        assertEquals(expectedCommand.getFile().getName(), resultCommand.getFile().getName());
    }

    @Test
    public void parseCommand_addScheduleCommand() throws Exception {
        Schedule schedule = new Schedule(
                new Title("Meeting"),
                new Description("Project discussion"),
                LocalDateTime.of(2023, 3, 21, 15, 0),
                LocalDateTime.of(2023, 3, 21, 16, 0)
        );

        String commandString = AddScheduleCommand.COMMAND_WORD + " title/Meeting d/Project discussion "
                + "start/2023-03-21 15:00 end/2023-03-21 16:00";

        AddScheduleCommand command = new AddScheduleCommand(schedule);

        AddressBookParser parser = new AddressBookParser();
        Command result = parser.parseCommand(commandString);

        assertTrue(result instanceof AddScheduleCommand);
        assertEquals(command, result);
    }

    @Test
    public void parseCommand_clearScheduleCommand() throws Exception {
        assertTrue(parser.parseCommand(ClearScheduleCommand.COMMAND_WORD) instanceof ClearScheduleCommand);
        assertTrue(parser.parseCommand(ClearScheduleCommand.COMMAND_WORD + " 10") instanceof ClearScheduleCommand);
    }

    @Test
    public void parseCommand_editScheduleCommand() throws Exception {
        EditScheduleDescriptor descriptor = new EditScheduleDescriptorBuilder()
                .withTitle("Meeting")
                .withDescription("Project discussion")
                .withStartDateTime("2023-03-21 15:00")
                .withEndDateTime("2023-03-21 16:00")
                .build();
        Index index = Index.fromZeroBased(0);
        EditScheduleCommand command = new EditScheduleCommand(index, descriptor);
        String commandString = EditScheduleCommand.COMMAND_WORD
                + " 1 "
                + "title/Meeting "
                + "d/Project discussion "
                + "start/2023-03-21 15:00 "
                + "end/2023-03-21 16:00";
        EditScheduleCommand parsedCommand = (EditScheduleCommand) parser.parseCommand(commandString);
        assertEquals(command, parsedCommand);
    }

    @Test
    public void parseCommand_findScheduleCommand() throws Exception {
        List<String> titleKeywords = Arrays.asList("meeting", "Zoom", "Party");
        List<String> descriptionKeywords = Arrays.asList("project", "birthday", "exam");
        Optional<LocalDateTime> beforePredicateDateTime = Optional.of(LocalDateTime.of(2024, 4, 20, 15, 0));
        Optional<LocalDateTime> afterPredicateDateTime = Optional.of(LocalDateTime.of(2024, 4, 25, 23, 0));
        Optional<LocalDateTime> duringPredicateDateTime = Optional.empty();

        FindScheduleCommand firstCommand = (FindScheduleCommand) parser.parseCommand(
                FindScheduleCommand.COMMAND_WORD + " "
                        + PREFIX_TITLE + titleKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_DESCRIPTION + descriptionKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_BEFORE_DATETIME + beforePredicateDateTime.get().format(FORMATTER) + " "
                        + PREFIX_AFTER_DATETIME + afterPredicateDateTime.get().format(FORMATTER));

        assertEquals(new FindScheduleCommand(new TitleContainsKeywordsPredicate(titleKeywords),
                new DescriptionContainsKeywordsPredicate(descriptionKeywords),
                new BeforeDateTimePredicate(beforePredicateDateTime),
                new AfterDateTimePredicate(afterPredicateDateTime),
                new DuringDateTimePredicate(duringPredicateDateTime)), firstCommand);

        beforePredicateDateTime = Optional.empty();
        afterPredicateDateTime = Optional.empty();
        duringPredicateDateTime = Optional.of(LocalDateTime.of(2024, 4, 23, 12, 0));

        FindScheduleCommand secondCommand = (FindScheduleCommand) parser.parseCommand(
                FindScheduleCommand.COMMAND_WORD + " "
                        + PREFIX_TITLE + titleKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_DESCRIPTION + descriptionKeywords.stream().collect(Collectors.joining(" ")) + " "
                        + PREFIX_DURING_DATETIME + duringPredicateDateTime.get().format(FORMATTER));

        assertEquals(new FindScheduleCommand(new TitleContainsKeywordsPredicate(titleKeywords),
                new DescriptionContainsKeywordsPredicate(descriptionKeywords),
                new BeforeDateTimePredicate(beforePredicateDateTime),
                new AfterDateTimePredicate(afterPredicateDateTime),
                new DuringDateTimePredicate(duringPredicateDateTime)), secondCommand);
    }

    @Test
    public void parseCommand_listScheduleCommand() throws Exception {
        assertTrue(parser.parseCommand(ListScheduleCommand.COMMAND_WORD) instanceof ListScheduleCommand);

        assertTrue(parser.parseCommand(ListScheduleCommand.COMMAND_WORD + " 5") instanceof ListScheduleCommand);
    }

    @Test
    public void parseCommand_listOngoingScheduleCommand() throws Exception {
        assertTrue(parser.parseCommand(ListOngoingScheduleCommand.COMMAND_WORD) instanceof ListOngoingScheduleCommand);

        assertTrue(parser.parseCommand(ListOngoingScheduleCommand.COMMAND_WORD + " 8")
                instanceof ListOngoingScheduleCommand);
    }

    @Test
    public void parseCommand_deleteScheduleCommand() throws Exception {
        Index index = Index.fromZeroBased(0);
        DeleteScheduleCommand command = new DeleteScheduleCommand(index);
        String commandString = DeleteScheduleCommand.COMMAND_WORD
                + " 1";
        DeleteScheduleCommand parsedCommand = (DeleteScheduleCommand) parser.parseCommand(commandString);
        assertEquals(command, parsedCommand);
    }
}
