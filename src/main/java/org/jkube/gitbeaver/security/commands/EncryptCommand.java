package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.PublicPrivateEncryption;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;
import static org.jkube.logging.Log.onException;

public class EncryptCommand extends AbstractCommand {

    private static final int LINE_LENGTH = 80;
    private static final String VARIABLE = "variable";

    public EncryptCommand() {
        super("encrypt a secret value");
        commandline("SECURITY ENCRYPT * INTO VARIABLE "+VARIABLE);
        argument(REST, "the string to be encrypted");
        argument(VARIABLE, "the variable into which the result of the encryption shall be stored");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String variable = arguments.get(VARIABLE);
        variables.put(variable, format(SecurityManagement.encrypt(arguments.get(REST))));
        Log.log("Stored encrypted secret in variable "+variable);
    }

    private String format(String encrypted) {
        StringBuilder sb = new StringBuilder();
        sb.append("// encrypted secret (created on ").append(new Date()).append(")\\n");
        String remain = encrypted;
        while (remain.length() > LINE_LENGTH) {
            sb.append(remain, 0, LINE_LENGTH);
            sb.append("\\n");
            remain = remain.substring(LINE_LENGTH);
        }
        sb.append(remain);
        return sb.toString();
    }

}
