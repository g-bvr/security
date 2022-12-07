package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecretType;
import org.jkube.gitbeaver.security.SecurityManagement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class WithSecretCommand extends AbstractCommand {

    private static final String SECRET_FILE_VARIABLE = "secret";

    public WithSecretCommand() {
        super(2, null, "with", "secret");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String secret = SecurityManagement.getSecret(arguments.get(0), SecretType.FILE);
        Path secretfile = SecurityManagement.createSecretFile(secret);
        List<String> called = arguments.subList(1, arguments.size());
        List<String> calledArguments = new ArrayList<>();
        Command command = GitBeaver.commandParser().parseCommand(called.toArray(new String[0]), calledArguments);
        Map<String,String> variablesWithSecret = new HashMap<>(variables);
        variablesWithSecret.put(SECRET_FILE_VARIABLE, secretfile.toString());
        command.execute(variablesWithSecret, workSpace, calledArguments);
        SecurityManagement.deleteSecretFile(secretfile);
    }

}
