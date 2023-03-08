package org.jkube.gitbeaver.security;

import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.jkube.logging.Log.onException;

public class SecurityManagement {

    private static final Path MASTER_KEY_FILE = Path.of("/masterkey");

    private static final PublicPrivateEncryption ENCRYPTION = createEncryption();
    private static final Random RANDOM = new Random();
    private static final String NOT_SET = "not-set";
    private static final String COMMENT_MARKER = "//";

    private static PublicPrivateEncryption createEncryption() {
        String keypair = burnAfterReading();
        if (keypair == null) {
            return null;
        }
        Log.log("Read key string with length "+keypair.length());
        return onException(() -> new PublicPrivateEncryption(keypair)).fail("could not create Encryption object");
    }

    public static String createKeyPair(int asymmetricKeySize, int symmetricKeySize) {
        return onException(() -> PublicPrivateEncryption.createKeyPair(asymmetricKeySize, symmetricKeySize)).fail("Could not create key");
    }

    public static boolean masterKeyWasFound() {
        return ENCRYPTION != null;
    }

    private static String burnAfterReading() {
        if (!MASTER_KEY_FILE.toFile().exists()) {
            Log.warn("File with masterkey was not found");
            return null;
        }
        List<String> lines = FileUtil.readLines(MASTER_KEY_FILE);
        Expect.size(lines, 1).elseFail("masterkey file has multiple lines");
        String masterkey = lines.get(0);
        if (masterkey.isEmpty()) {
            Log.log("Master key env variable is not set.");
            return null;
        }
        if (NOT_SET.equals(masterkey)) {
            Log.warn("Master key is not set yet");
            return null;
        }
        FileUtil.delete(MASTER_KEY_FILE);
        Expect.isFalse(MASTER_KEY_FILE.toFile().exists()).elseFail("File with masterkey is still present");
        return masterkey;
    }

    public static String encrypt(String secret) {
        if (ENCRYPTION == null) {
            Log.warn("No master key was found, encryption is not possible");
            return null;
        }
        return onException(() -> ENCRYPTION.encrypt(secret)).fail("could not encrypt secret");
    }

    public static String getSecret(Path path) {
        String data = FileUtil.readLines(path)
                .stream()
                .filter(line -> !line.trim().startsWith(COMMENT_MARKER))
                //.map(line -> line.replaceAll("\\\\n", "\n"))
                .collect(Collectors.joining(""));
        return ENCRYPTION == null
                ? data
                : onException(() -> ENCRYPTION.decrypt(data)).fail("Could not decrypt secret in file "+path);
    }

    public static Path createSecretFile(String secret) {
        return createSecretFile(secret, "secret"+Math.abs(RANDOM.nextLong()));
    }

    public static Path createSecretFile(String secret, String filename) {
        FileUtil.createIfNotExists(GitBeaver.SECRETS_DIRECTORY);
        Path path = GitBeaver.SECRETS_DIRECTORY.resolve(filename);
        Log.log("Creating secret file: "+path+" ("+secret.length()+" characters)");
        FileUtil.storeWithoutNewline(path, secret);
        return path;
    }

    public static void deleteSecretFile(String filename) {
      deleteSecretFile(GitBeaver.SECRETS_DIRECTORY.resolve(filename));
    }

    public static void deleteSecretFile(Path path) {
        Log.log("Deleting secret file: "+path);
        FileUtil.delete(path);
    }

}
