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

    private static final String SECRET_FILE_MARKER = "%";

    public CreateKeyPairCommand() {
        super(4, null,  "with", "new", "master", "key");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        int asymmetricKeySize = Integer.parseInt(arguments.get(0));
        int symmetricKeySize = Integer.parseInt(arguments.get(1));
        expectArg(2, "bits", arguments);
        Path secretfile = SecurityManagement.createSecretFile(SecurityManagement.createKeyPair(asymmetricKeySize, symmetricKeySize));
        String[] called = createCalledArray(arguments.subList(3, arguments.size()), secretfile.toString());
        List<String> calledArguments = new ArrayList<>();
        Command command = GitBeaver.commandParser().parseCommand(called, calledArguments);
        Log.log("Calling command: "+String.join(" ",command.keywords())+" "+String.join(" ", calledArguments));
        command.execute(variables, workSpace, calledArguments);
        SecurityManagement.deleteSecretFile(secretfile);
    }

    private String[] createCalledArray(List<String> arguments, String secretPath) {
        String[] res = new String[arguments.size()];
        int i = 0;
        for (String arg : arguments) {
            res[i++] = SECRET_FILE_MARKER.equals(arg) ? secretPath : arg;
        }
        return res;
    }

}
