package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.util.ExternalProcess;

import java.util.HashMap;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class WithSecretCommand extends AbstractCommand {

    private static final String ENCRYPTED = "encrypted";
    private static final String DECRYPTED = "decrypted";
    private static final String VARIABLE = "variable";
    private static final String ENV_VARIABLE = "envvariable";

    public WithSecretCommand() {
        super("Execute a command using a secret");
        commandlineVariant("WITH SECRET "+ENCRYPTED+" IN VARIABLE "+VARIABLE+" *", "store decrypted secret into gitbeaver variable before executing the command, deletee variable afterwards");
        commandlineVariant("WITH SECRET "+ENCRYPTED+" IN ENV VARIABLE "+ENV_VARIABLE+" *", "store decrypted secret into environment variable before executing the command, deletee variable afterwards");
        commandlineVariant("WITH SECRET "+ENCRYPTED+" IN FILE "+DECRYPTED+" *", "store decrypted secret into file before executing the command, deletee variable afterwards");
        argument(ENCRYPTED, "the path to the file with the encrypted secret data (relative to current workspace)");
        argument(DECRYPTED, "the name of the file with decrypted secret data (this is not stored in workspace, but stored in a separate special folder)");
        argument(VARIABLE, "the name of the variable into with the decrypted secret data is stored");
        argument(ENV_VARIABLE, "the name of the env variable into with the decrypted secret data is stored");
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
        }  if (arguments.containsKey(VARIABLE)) {
            // extend existing env variables by secret
            Map<String,String> variablesWithEnv = new HashMap<>(variables);
            String envOld = variablesWithEnv.get(ExternalProcess.ENV_MAP_KEY);
            String envKVString = arguments.get(VARIABLE) + "=" + secret;
            String envNew = (envOld == null) ? envKVString : envOld + "," + envKVString;
            variablesWithEnv.put(ExternalProcess.ENV_MAP_KEY, envNew);
            command.execute(variablesWithEnv, workSpace, calledArguments);
        } else {
            String target = arguments.get(DECRYPTED);
            SecurityManagement.createSecretFile(secret, target);
            command.execute(variables, workSpace, calledArguments);
            SecurityManagement.deleteSecretFile(target);
        }
    }

}
