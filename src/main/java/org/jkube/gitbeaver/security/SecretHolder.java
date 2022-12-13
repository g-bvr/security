package org.jkube.gitbeaver.security;

import org.jkube.logging.Log;
import org.jkube.util.Expect;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SecretHolder {

    private final PublicPrivateEncryption encryption;
    private final Map<SecretType, Map<String, String>> secrets = new HashMap<>();

    public SecretHolder(PublicPrivateEncryption encryption) {
        this.encryption = encryption;
    }

    public String getSecret(String secretId, SecretType secretType) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String encrypted = secrets.get(secretType).get(secretId);
        Expect.notNull(encrypted).elseFail("No secret registered of type "+secretId+" with id: "+secretId);
        return encryption.decrypt(encrypted);
    }

    public void registerSecret(String secretId, SecretType secretType, String encryptedSecret) {
        secrets.putIfAbsent(secretType, new HashMap<>());
        secrets.get(secretType).put(secretId, encryptedSecret);
        Log.log("Secret of type {} registered for id {}", secretType, secretId);
    }

}
