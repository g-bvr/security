package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.util.Date;
import java.util.Map;

import static org.jkube.gitbeaver.CommandParser.REST;

public class EncryptCommand extends AbstractCommand {

    private static final int LINE_LENGTH = 80;

    private static final String VARIABLE = "variable";

    public EncryptCommand() {
        super("encrypt a secret value");
        commandlineVariant("SECURITY ENCRYPT VARIABLE "+VARIABLE, "encrypt the content of specified variable (and store it back into the same variable)");
        commandlineVariant("SECURITY ENCRYPT * => "+VARIABLE, "encrypt a string into a variable");
        argument(REST, "the string to be encrypted");
        argument(VARIABLE, "the variable into which the result of the encryption shall be stored");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String variable = arguments.get(VARIABLE);
        String secret = arguments.get(REST);
        if (secret == null) {
            secret = variables.get(VARIABLE);
            Expect.notNull(secret).elseFail("Variable is not set: "+variable);
        }
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
