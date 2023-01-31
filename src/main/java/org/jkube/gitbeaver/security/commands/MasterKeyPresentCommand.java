package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.SecurityManagement;

import java.util.Map;

public class MasterKeyPresentCommand extends AbstractCommand {

    private static final String VARIABLE = "variable";

    public MasterKeyPresentCommand() {
        super("Cchek if masterkey is available");
        commandline("SECURITY MASTERKEY PRESENT => "+VARIABLE);
        argument(VARIABLE, "the variable which obtains true/false as value depending if the masterkey is set");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String variable = arguments.get(VARIABLE);
        variables.put(variable, String.valueOf(SecurityManagement.masterKeyWasFound()));
    }

}
