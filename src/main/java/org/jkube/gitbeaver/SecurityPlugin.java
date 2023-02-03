package org.jkube.gitbeaver;

import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.security.SecurityManagement;
import org.jkube.gitbeaver.security.commands.*;
import org.jkube.logging.Log;

    public class SecurityPlugin extends SimplePlugin {

        public SecurityPlugin() {
            super("""
                                encryption and decryption of secret information using an asymmetric (aka. public/private) master key pair
                            """,
                    CreateKeyPairCommand.class,
                    EncryptCommand.class,
                    MasterKeyPresentCommand.class,
                    WithCredentialsGitCloneCommand.class,
                    WithSecretCommand.class
            );
        }

        @Override
        public void init() {
            Log.log("Security master key present: "+ SecurityManagement.masterKeyWasFound());
        }

    }
