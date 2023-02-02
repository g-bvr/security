package org.jkube.gitbeaver.security.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.util.Expect;

import java.net.URL;
import java.util.Map;

import static org.jkube.logging.Log.onException;

public class WithCredentialsGitCloneCommand extends AbstractCommand {

    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String CREDENTIALS_SEPARATOR = "@";
    private static final String CREDENTIALS = "credentials";

    private static final String BASE_URL = "baseurl";
    private static final String REPOSITORY = "repository";
    private static final String TAG = "tag";


    public WithCredentialsGitCloneCommand() {
        super("clone a git repository using encrypted credentials");
        commandlineVariant("GIT WITH CREDENTIALS "+CREDENTIALS+" CLONE "+BASE_URL+" "+REPOSITORY, "clone the default branch of the repository");
        commandlineVariant("GIT WITH CREDENTIALS "+CREDENTIALS+" CLONE "+BASE_URL+" "+REPOSITORY+" "+TAG, "clone the specified branch or tag of the repository");
        argument(CREDENTIALS, "Path of encrypted credentials file (relative to current workspace)");
        argument(BASE_URL, "The url prefix of the repository (not including the actual repo name)");
        argument(REPOSITORY, "The name of the repository (which together with the base url constitutes the url of the repository)");
        argument(TAG, "Optional argument to specify either a branch or a tag which shall be checked out (if omitted the default branch will be used)");
    }

    @Override
    public void execute(Map<String, String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String credentials = SecurityManagement.getSecret(workSpace.getAbsolutePath(arguments.get(CREDENTIALS)));
        URL url = addGitCredentials(arguments.get(BASE_URL), credentials);
        String repository = arguments.get(REPOSITORY);
        String tag = arguments.get(TAG);
        GitBeaver.gitCloner().clone(workSpace.getWorkdir(), url, repository, tag, GitBeaver.getApplicationLogger(variables));
    }

    private URL addGitCredentials(String url, String credentials) {
        String[] split = url.split(PROTOCOL_SEPARATOR, 2);
        Expect.equal(split.length, 2).elseFail("Illegal url format: "+url);
        return onException(
                () -> new URL(split[0] + PROTOCOL_SEPARATOR + credentials + CREDENTIALS_SEPARATOR + split[1])
        ).fail("Could not parse url "+url);
    }

}
