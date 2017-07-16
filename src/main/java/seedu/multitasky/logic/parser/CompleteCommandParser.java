package seedu.multitasky.logic.parser;

import static seedu.multitasky.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.multitasky.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.multitasky.logic.parser.CliSyntax.PREFIX_EVENT;
import static seedu.multitasky.logic.parser.CliSyntax.PREFIX_FLOATINGTASK;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import seedu.multitasky.commons.core.index.Index;
import seedu.multitasky.commons.exceptions.IllegalValueException;
import seedu.multitasky.logic.commands.CompleteByFindCommand;
import seedu.multitasky.logic.commands.CompleteByIndexCommand;
import seedu.multitasky.logic.commands.CompleteCommand;
import seedu.multitasky.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new CompleteCommand object
 */
public class CompleteCommandParser {
    private ArgumentMultimap argMultimap;

    public ArgumentMultimap getArgMultimap() {
        return argMultimap;
    }

    /**
     * Parses the given {@code String} of arguments in the context of the CompleteCommand and returns a
     * CompleteCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    // @@author A0132788U-reused
    public CompleteCommand parse(String args) throws ParseException {
        argMultimap = ArgumentTokenizer.tokenize(args, ParserUtil.toPrefixArray(CompleteCommand.VALID_PREFIXES));

        if (args.trim().isEmpty()) {
            throw new ParseException(CompleteCommand.MESSAGE_USAGE);
        }

        if (hasIndexFlag(argMultimap)) { // process to complete by indexes
            if (hasInvalidFlagCombination(argMultimap)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        CompleteCommand.MESSAGE_USAGE));
            }

            try {
                Prefix listIndicatorPrefix = ParserUtil.getMainPrefix(argMultimap, PREFIX_FLOATINGTASK,
                        PREFIX_DEADLINE, PREFIX_EVENT);
                Index index = ParserUtil.parseIndex(argMultimap.getValue(listIndicatorPrefix).get());
                return new CompleteByIndexCommand(index, listIndicatorPrefix);
            } catch (IllegalValueException ive) {
                throw new ParseException(ive.getMessage(), ive);
            }

        } else { // process to complete by find.
            String searchString = argMultimap.getPreamble().get()
                    .replaceAll("\\" + CliSyntax.PREFIX_ESCAPE, "");
            final String[] keywords = searchString.split("\\s+");
            final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));

            return new CompleteByFindCommand(keywordSet);
        }
    }

    /**
     * A method that returns true if flags are given in an illogical manner for complete commands.
     * illogical := any 2 of /float, /deadline, /event used together.
     */
    private boolean hasInvalidFlagCombination(ArgumentMultimap argMultimap) {
        assert argMultimap != null;
        return ParserUtil.areAllPrefixesPresent(argMultimap, PREFIX_FLOATINGTASK, PREFIX_DEADLINE)
               || ParserUtil.areAllPrefixesPresent(argMultimap, PREFIX_DEADLINE, PREFIX_EVENT)
               || ParserUtil.areAllPrefixesPresent(argMultimap, PREFIX_FLOATINGTASK, PREFIX_EVENT);
    }

    /**
     * A method that returns true if flags in given ArgumentMultimap has at least one index-indicating
     * Prefix mapped to some arguments.
     * Index-indicating := /float or /deadline or /event
     */
    private boolean hasIndexFlag(ArgumentMultimap argMultimap) {
        assert argMultimap != null;
        return ParserUtil.arePrefixesPresent(argMultimap, PREFIX_FLOATINGTASK, PREFIX_DEADLINE,
                PREFIX_EVENT);
    }

}
