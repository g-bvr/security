package org.jkube.gitbeaver.security;


import org.jkube.gitbeaver.util.test.GitBeaverAbstractTest;
import org.jkube.gitbeaver.util.test.TestUtil;
import org.junit.jupiter.api.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class EncryptionTest  {

    @Test
    void encryptiontest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String keys = PublicPrivateEncryption.createKeyPair(2048);
        System.out.println("Key pair: "+keys);
        PublicPrivateEncryption e = new PublicPrivateEncryption(keys);
        String secret = "Hello World!";
        String encrypted = e.encrypt(secret);
        System.out.println("Encrypted: "+encrypted);
        String decypted = e.decrypt(encrypted);
        System.out.println("Decrypted: "+decypted);
        Assertions.assertEquals(decypted, secret);
    }


}
