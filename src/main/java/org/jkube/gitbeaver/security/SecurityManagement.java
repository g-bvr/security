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
    private static final String MASTER_KEY_FILE = "git-beaver-master-key";

    private static final PublicPrivateEncryption ENCRYPTION = createEncryption();
    private static final SecretHolder SECRET_HOLDER = new SecretHolder(ENCRYPTION);

    private static final Random RANDOM = new Random();
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
        Path path = SECRETS_DIRECTORY.resolve(MASTER_KEY_FILE);
        if (path.toFile().exists()) {
            List<String> keyLines = FileUtil.readLines(path);
            Log.log("Read {} lines for master key", keyLines.size());
            Expect.equal(1, keyLines.size()).elseFail("expected exactly one line in master key file");
            // this is not permitted, is there some way to unmount a volume in docker?
            // Log.log("Deleting secret directory {}", SECRETS_DIRECTORY);
            //FileUtil.delete(SECRETS_DIRECTORY);
            //Log.log("Directory {} exists: ", SECRETS_DIRECTORY.toFile().exists());
            return keyLines.get(0);
        } else {
            Log.warn("Master key file was not found: "+path);
            return null;
        }
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
        Log.log("Deleting^ secret file: "+file);
        FileUtil.delete(file);
    }

}
