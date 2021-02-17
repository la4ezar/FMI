package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommandCreatorTest {
    @Test
    public void testCommandCreatorNewCommandWithNoArguments() {
        String commandStr = "test";
        Command command = CommandCreator.newCommand(commandStr);

        assertEquals("Unexpected command returned for command 'test'", commandStr, command.command());
        assertNotNull("Command arguments should not be null", command.arguments());
        assertEquals("Unexpected command arguments length", 0, command.arguments().length);
    }
    @Test
    public void testCommandCreatorNewCommandWithOneArgument() {
        String commandStr = "test firstArgument";
        Command command = CommandCreator.newCommand(commandStr);

        String commandName = commandStr.split(" ")[0];
        String argument = commandStr.split(" ")[1];
        assertEquals("Unexpected command name returned for command 'test firstArgument'",
                commandName, command.command());
        assertNotNull("Command arguments should not be null", command.arguments());
        assertEquals("Unexpected command arguments length", 1, command.arguments().length);
        assertEquals("Unexpected command argument returned for command 'test fristArgument'",
                argument, command.arguments()[0]);
    }
    @Test
    public void testCommandCreatorNewCommandWithCoupleArguments() {
        String commandStr = "test firstArgument secondArgument";
        Command command = CommandCreator.newCommand(commandStr);

        String commandName = commandStr.split(" ")[0];
        String argument1 = commandStr.split(" ")[1];
        String argument2 = commandStr.split(" ")[2];
        assertEquals("Unexpected command name returned for command 'test firstArgument secondArgument'",
                commandName, command.command());
        assertNotNull("Command arguments should not be null", command.arguments());
        assertEquals("Unexpected command arguments length", 2, command.arguments().length);
        assertEquals("Unexpected argument returned for command 'test firstArgument secondArgument' first argument",
                argument1, command.arguments()[0]);
        assertEquals("Unexpected argument returned for command 'test firstArgument secondArgument' second argument",
                argument2, command.arguments()[1]);
    }
    @Test
    public void testCommandCreatorNewCommandWithQuoteArgument() {
        String commandStr = "test \"quoted argument\"";
        Command command = CommandCreator.newCommand(commandStr);

        String commandName = commandStr.split(" ")[0];
        String argument = "quoted argument";
        assertEquals("Unexpected command name returned for command 'test \"quoted argument\"'",
                commandName, command.command());
        assertNotNull("Command arguments should not be null", command.arguments());
        assertEquals("Unexpected command arguments length", 1, command.arguments().length);
        assertEquals("Unexpected argument returned for command 'test \"quoted argument\"'",
                argument, command.arguments()[0]);
    }

    @Test
    public void testCommandCreatorWithNull() {
        Command command = CommandCreator.newCommand(null);

        String commandExpected = "";

        assertEquals("Unexpected command name returned for null command",
                commandExpected, command.command());
        assertNotNull("Command arguments should not be null", command.arguments());
    }
}
