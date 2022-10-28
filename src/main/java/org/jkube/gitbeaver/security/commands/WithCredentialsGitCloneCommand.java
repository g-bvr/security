package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.SecretType;
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
public class WithCredentialsGitCloneCommand extends AbstractCommand {

    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String CREDENTIALS_SEPARATOR = "@";

    public WithCredentialsGitCloneCommand() {
        super(4, 5, "git", "with", "credentials");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, List<String> arguments) {
        String credentials = SecurityManagement.getSecret(arguments.get(0), SecretType.GIT);
        URL url = addGitCredentials(arguments.get(1), credentials);
        expectArg(2, "clone", arguments);
        String repository = arguments.get(3);
        String tag = arguments.size() == 5 ? arguments.get(4) : null;
        GitBeaver.gitCloner().clone(workSpace.getWorkdir(), url, repository, tag);
    }

    private URL addGitCredentials(String url, String credentials) {
        String[] split = url.split(PROTOCOL_SEPARATOR, 2);
        Expect.equal(split.length, 2).elseFail("Illegal url format: "+url);
        return onException(
                () -> new URL(split[0] + PROTOCOL_SEPARATOR + credentials + CREDENTIALS_SEPARATOR + split[1])
        ).fail("Could not parse url "+url);
    }

}
