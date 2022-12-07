package org.jkube.gitbeaver;

import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.security.commands.*;
import org.jkube.logging.Log;

    public class SecurityPlugin extends SimplePlugin {

        public SecurityPlugin() {
            super(
                    CreateKeyPairCommand.class,
                    EncryptCommand.class,
                    MasterKeyPresentCommand.class,
                    WithCredentialsGitCloneCommand.class,
                    WithSecretCommand.class,
                    WithSecretInCommand.class
            );
        }

        @Override
        public void init() {
            Log.log("Security master key present: "+ SecurityManagement.masterKeyWasFound());
        }

    }
