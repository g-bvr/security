package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.PublicPrivateEncryption;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class CreateKeyPairCommand extends AbstractCommand {

    private static final String SECRET_FILE_VARIABLE = "masterkey";

    public CreateKeyPairCommand() {
        super(3, null,  "with", "new", "master", "key");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        int keySize = Integer.parseInt(arguments.get(0));
        expectArg(1, "bits", arguments);
        Path secretfile = SecurityManagement.createSecretFile(SecurityManagement.createKeyPair(keySize));
        List<String> called = arguments.subList(2, arguments.size());
        List<String> calledArguments = new ArrayList<>();
        Command command = GitBeaver.commandParser().parseCommand(called.toArray(new String[0]), calledArguments);
        Map<String,String> variablesWithSecret = new HashMap<>(variables);
        variablesWithSecret.put(SECRET_FILE_VARIABLE, secretfile.toString());
        command.execute(variablesWithSecret, workSpace, calledArguments);
        SecurityManagement.deleteSecretFile(secretfile);
    }

}
