package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.logging.Log;
import org.jkube.gitbeaver.util.Expect;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;

public class CreateKeyPairCommand extends AbstractCommand {

    private static final String SECRET_FILE_MARKER = "%";
    private static final String KEY_SIZE = "keysize";

    public CreateKeyPairCommand() {
        super("create a new public/private key pair and execute a command");
        commandline("WITH NEW MASTER KEY SIZE "+KEY_SIZE+" BITS DO *");
        argument(KEY_SIZE, "number of bits to use for asymmetric/symmetric key, format: #,#");
        argument(REST, "command to be executed, the name of the file with the created key can be referred to with "+SECRET_FILE_MARKER);
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String[] keySizes = arguments.get(KEY_SIZE).split(",");
        Expect.equal(keySizes.length, 2).elseFail("Expected key size format: #,#");
        int asymmetricKeySize = Integer.parseInt(keySizes[0]);
        int symmetricKeySize = Integer.parseInt(keySizes[1]);
        Path secretfile = SecurityManagement.createSecretFile(SecurityManagement.createKeyPair(asymmetricKeySize, symmetricKeySize));
        String called = arguments.get(REST).replaceAll(SECRET_FILE_MARKER, secretfile.toString());
        Map<String, String> calledArguments = new HashMap<>();
        Command command = GitBeaver.commandParser().parseCommand(called, calledArguments);
        Log.log("Calling command: "+called);
        command.execute(variables, workSpace, calledArguments);
        SecurityManagement.deleteSecretFile(secretfile);
    }

}
