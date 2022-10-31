package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.interfaces.Command;
import org.jkube.gitbeaver.security.SecretType;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.onException;

/**
 * Usage: git clone providerUrl repositoryName [tag]
 */
public class WithSecretInFileCommand extends AbstractCommand {

    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String CREDENTIALS_SEPARATOR = "@";

    public WithSecretInFileCommand() {
        super(4, null, "with", "secret");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String secret = SecurityManagement.getSecret(arguments.get(0), SecretType.FILE);
        expectArg(1, "in", arguments);
        expectArg(2, "file", arguments);
        Path file = workSpace.getAbsolutePath(arguments.get(3));
        List<String> called = arguments.subList(4, arguments.size());
        List<String> calledArguments = new ArrayList<>();
        Command command = GitBeaver.commandParser().parseCommand(called.toArray(new String[0]), calledArguments);
        createSecretFile(secret, file);
        command.execute(variables, workSpace, calledArguments);
        deleteSecretFile(file);
    }

    private void createSecretFile(String secret, Path file) {
        Log.log("Creating secret file: "+file);
        FileUtil.store(file, List.of(secret));
    }

    private void deleteSecretFile(Path file) {
        Log.log("Creating secret file: "+file);
        FileUtil.delete(file);
    }

}
