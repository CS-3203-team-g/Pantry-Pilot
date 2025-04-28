package pro.pantrypilot.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemHelper {

    private static final Logger logger = LoggerFactory.getLogger(SystemHelper.class);
    private static String pythonCommand = null;

    // Static initializer to find the Python command once
    static {
        pythonCommand = findPythonCommand();
    }

    /**
     * Tries to find 'python3' or 'python' executable in the system PATH.
     * Returns the command found, or defaults to "python".
     */
    private static String findPythonCommand() {
        String osName = System.getProperty("os.name").toLowerCase();
        String commandToCheck3 = "python3";
        String commandToCheck = "python";
        String whereCmd = osName.contains("win") ? "where" : "which";

        if (isCommandAvailable(whereCmd + " " + commandToCheck3)) {
            logger.info("Found Python command: {}", commandToCheck3);
            return commandToCheck3;
        } else if (isCommandAvailable(whereCmd + " " + commandToCheck)) {
            logger.info("Found Python command: {}", commandToCheck);
            return commandToCheck;
        } else {
            logger.warn("Could not find 'python3' or 'python' in PATH using '{}'. Defaulting to '{}'. Ensure Python is installed and in PATH.", whereCmd, commandToCheck);
            return commandToCheck; // Default fallback
        }
    }

    /**
     * Checks if a command is available by executing it.
     */
    private static boolean isCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            // Check error stream for potential errors like "command not found"
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                if (reader.readLine() != null) {
                    // Some output on error stream might indicate failure
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            // IOException (e.g., command not found on some systems) or InterruptedException
            logger.debug("Command check failed for '{}': {}", command, e.getMessage());
            return false;
        }
    }

    /**
     * Returns the determined Python command ('python3' or 'python').
     */
    public static String getPythonCommand() {
        return pythonCommand;
    }
} 