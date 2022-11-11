package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.PublicPrivateEncryption;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class EncryptCommand extends AbstractCommand {
    
    public EncryptCommand() {
        super(3, 3, "security", "encrypt");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String secret = arguments.get(0);
        expectArg(1, "=>", arguments);
        String variable = arguments.get(2);
        variables.put(variable, SecurityManagement.encrypt(secret));
        Log.log("Stored encrypted secret in variable "+variable);
    }

}
