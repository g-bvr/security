package org.jkube.gitbeaver.security;

import org.jkube.application.Application;
import org.jkube.gitbeaver.security.PublicPrivateEncryption;
import org.jkube.gitbeaver.security.SecretHolder;
import org.jkube.logging.Log;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.jkube.logging.Log.onException;

public class SecurityManagement {

    private static final PublicPrivateEncryption ENCRYPTION = createEncryption();
    private static final SecretHolder SECRET_HOLDER = new SecretHolder(ENCRYPTION);
    private static final String KEY_PAIR_ENV_VARIABLE = "MASTER_KEY_PAIR";

    private static PublicPrivateEncryption createEncryption() {
        String keypair = burnAfterReading(KEY_PAIR_ENV_VARIABLE);
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

    private static String burnAfterReading(String envVariable) {
        String result = System.getenv(envVariable);
        if (result == null) {
            Log.warn("Master key environment variable was not set: "+envVariable);
        } else {
            onException(() -> setEnv(envVariable, "master key was burnt after reading")).fail("could not burn master key");
        }
        return result;
    }

    // https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
    public static void setEnv(String key, String value) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(key, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(key, value);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.put(key, value);
                }
            }
        }
    }

    public static String encrypt(String secret) {
        return onException(() -> ENCRYPTION.encrypt(secret)).fail("could not encrypt secret");
    }

    public static String getSecret(String keyId, SecretType type) {
        return onException(() -> SECRET_HOLDER.getSecret(keyId, type)).fail("Could not decrypt secret "+keyId+" of type "+type);
    }
}
