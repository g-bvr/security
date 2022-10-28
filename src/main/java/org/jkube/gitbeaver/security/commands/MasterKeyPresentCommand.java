package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class MasterKeyPresentCommand extends AbstractCommand {

    public MasterKeyPresentCommand() {
        super(1, 1, "security", "masterkey", "present", "into");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String variable = arguments.get(0);
        variables.put(variable, String.valueOf(SecurityManagement.masterKeyWasFound()));
    }

}
