package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class WithSecretCommand extends AbstractCommand {

    private static final String ENCRYPTED = "encrypted";
    private static final String DECRYPTED = "decrypted";
    private static final String VARIABLE = "variable";

    public WithSecretCommand() {
        super("Execute a command using a secret");
        commandlineVariant("WITH SECRET "+ENCRYPTED+" IN VARIABLE "+VARIABLE+" *", "store decrypted secret into variable before executing the command, deletee variable afterwards");
        commandlineVariant("WITH SECRET "+ENCRYPTED+" IN FILE "+DECRYPTED+" *", "store decrypted secret into file before executing the command, deletee variable afterwards");
        argument(ENCRYPTED, "the path to the file with the encrypted secret data (relative to current workspace)");
        argument(DECRYPTED, "the name of the file with decrypted secret data (this is not stored in workspace, but stored in a separate special folder)");
        argument(VARIABLE, "the name of the variable into with the decrypted secret data is stored");
        argument(REST, "the command to be executed (the command can use the decrypte secret, but it is deleted again after command execution)");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String secret = SecurityManagement.getSecret(workSpace.getAbsolutePath(arguments.get(ENCRYPTED)));
        String called = arguments.get(REST);
        Map<String, String> calledArguments = new HashMap<>();
        Command command = GitBeaver.commandParser().parseCommand(called, calledArguments);
        if (arguments.containsKey(VARIABLE)) {
            Map<String,String> variablesWithSecret = new HashMap<>(variables);
            variablesWithSecret.put(arguments.get(VARIABLE), secret);
            command.execute(variablesWithSecret, workSpace, calledArguments);
        } else {
            String target = arguments.get(DECRYPTED);
            SecurityManagement.createSecretFile(secret, target);
            command.execute(variables, workSpace, calledArguments);
            SecurityManagement.deleteSecretFile(target);
        }
    }

}
