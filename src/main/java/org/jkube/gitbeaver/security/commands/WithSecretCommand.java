package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class WithSecretCommand extends AbstractCommand {

    public WithSecretCommand() {
        super(5, null, "with", "secret");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String secret = SecurityManagement.getSecret(workSpace.getAbsolutePath(arguments.get(0)));
        expectArg(1, "in", arguments);
        String targetType = arguments.get(2);
        String target = arguments.get(3);
        List<String> called = arguments.subList(4, arguments.size());
        List<String> calledArguments = new ArrayList<>();
        Command command = GitBeaver.commandParser().parseCommand(called.toArray(new String[0]), calledArguments);
        if (targetType.equalsIgnoreCase("variable")) {
            Map<String,String> variablesWithSecret = new HashMap<>(variables);
            variablesWithSecret.put(target, secret);
            command.execute(variablesWithSecret, workSpace, calledArguments);
        } else if (targetType.equalsIgnoreCase("file")) {
            SecurityManagement.createSecretFile(secret, target);
            command.execute(variables, workSpace, calledArguments);
            SecurityManagement.deleteSecretFile(target);
        } else Log.error("expected FILE or VARIABLE, found: "+targetType);
    }

}
