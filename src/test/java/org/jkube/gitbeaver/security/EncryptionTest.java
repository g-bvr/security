package org.jkube.gitbeaver.security;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class EncryptionTest  {

    @Test
    void encryptiontest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String keys = PublicPrivateEncryption.createKeyPair(2048, 128);
        System.out.println("Key pair: "+keys);
        PublicPrivateEncryption e = new PublicPrivateEncryption(keys);
        String secret = "Hello World!";
        String encrypted = e.encrypt(secret);
        System.out.println("Encrypted: "+encrypted);
        String decypted = e.decrypt(encrypted);
        System.out.println("Decrypted: "+decypted);
        Assertions.assertEquals(decypted, secret);
    }

    @Test
    void encryptionTestLong() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        for (int repeat = 1; repeat < 1000; repeat++) {
            System.out.println("Repeat: "+repeat);
            String keys = PublicPrivateEncryption.createKeyPair(2048, 256);
            PublicPrivateEncryption e = new PublicPrivateEncryption(keys);
            String secret = "Hello World" + "!".repeat(repeat);
            String encrypted = e.encrypt(secret);
            String decypted = e.decrypt(encrypted);
            Assertions.assertEquals(decypted, secret);
        }
    }


}
