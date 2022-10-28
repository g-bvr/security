package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.PublicPrivateEncryption;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class CreateKeyPairCommand extends AbstractCommand {

    public CreateKeyPairCommand() {
        super(3, 3, "security", "create", "key", "pair");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        int keySize = Integer.parseInt(arguments.get(0));
        Expect.equal("into", arguments.get(1).toLowerCase()).elseFail("expected INTO keyword");
        String variable = arguments.get(2);
        variables.put(variable, SecurityManagement.createKeyPair(keySize));
        Log.log("Stored created key pair of size "+keySize+" into variable "+variable);
    }

}
