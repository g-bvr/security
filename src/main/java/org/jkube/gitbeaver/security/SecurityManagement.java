package org.jkube.gitbeaver.security;

import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.jkube.logging.Log.onException;

public class SecurityManagement {

    private static final Path SECRETS_DIRECTORY = Path.of("/secret/");
    private static final String MASTER_KEY_ENV_VARIABLE = "gitbeaver-masterkey";

    private static final PublicPrivateEncryption ENCRYPTION = createEncryption();
    private static final SecretHolder SECRET_HOLDER = new SecretHolder(ENCRYPTION);

    private static final Random RANDOM = new Random();
    private static final String NOT_SET = "not-set";

    private static PublicPrivateEncryption createEncryption() {
        String keypair = burnAfterReading();
        if (keypair == null) {
            return null;
        }
        return onException(() -> new PublicPrivateEncryption(keypair)).fail("could not create Encryption object");
    }

    public static String createKeyPair(int keySize) {
        return onException(() -> PublicPrivateEncryption.createKeyPair(keySize)).fail("Could not create key of size "+keySize);
    }

    public static boolean masterKeyWasFound() {
        return ENCRYPTION != null;
    }

    private static String burnAfterReading() {
        String masterkey = System.getenv(MASTER_KEY_ENV_VARIABLE);
        if (masterkey == null) {
            Log.log("Master key env variable is not set.");
            return null;
        }
        if (NOT_SET.equals(masterkey)) {
            Log.warn("Master key is not set yet");
            return null;
        }
        onException(() -> EnvUntil.clear(MASTER_KEY_ENV_VARIABLE)).warn("Could not delete masterkey env variable");
        return masterkey;
    }

    public static String encrypt(String secret) {
        return onException(() -> ENCRYPTION.encrypt(secret)).fail("could not encrypt secret");
    }

    public static String getSecret(String keyId, SecretType type) {
        return onException(() -> SECRET_HOLDER.getSecret(keyId, type)).fail("Could not decrypt secret "+keyId+" of type "+type);
    }

    public static Path createSecretFile(String secret) {
        FileUtil.createIfNotExists(SECRETS_DIRECTORY);
        String filename = "secret"+Math.abs(RANDOM.nextLong());
        Path path = SECRETS_DIRECTORY.resolve(filename);
        Log.log("Creating secret file: "+path);
        FileUtil.store(path, List.of(secret));
        return path;
    }

    public static void deleteSecretFile(Path file) {
        Log.log("Deleting secret file: "+file);
        FileUtil.delete(file);
    }

}
