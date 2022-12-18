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

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class EncryptCommand extends AbstractCommand {

    private static final int LINE_LENGTH = 80;

    public EncryptCommand() {
        super(3, null, "security", "encrypt");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        int num = arguments.size();
        String secret = String.join(" ", arguments.subList(0, num-2));
        expectArg(num-2, "=>", arguments);
        String variable = arguments.get(num-1);
        variables.put(variable, format(SecurityManagement.encrypt(secret)));
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
