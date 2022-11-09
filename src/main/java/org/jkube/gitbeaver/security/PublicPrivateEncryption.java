package org.jkube.gitbeaver.security;

import org.jkube.logging.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicPrivateEncryption {

    private static final String SEPARATOR = "#";
    public static final String ALGORITHM = "RSA";

    public static final String SALT = "lkjbeioxhekjbcbcklemcnekjcbekjnclkenckjbekjcnelcnjeowijweobckwlhnwcobwoncownco";

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public static String createKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(keySize);
        KeyPair pair = generator.generateKeyPair();
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(pair.getPublic().getEncoded())+SEPARATOR
                +enc.encodeToString(pair.getPrivate().getEncoded());
    }

    public PublicPrivateEncryption(String keypair) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        String[] split = keypair.split(SEPARATOR);
        Log.log("Split key string into public-private pair with sizes: "+split[0].length()+","+split[0].length());
        Base64.Decoder dec = Base64.getDecoder();
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(dec.decode(split[0]));
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        encryptCipher = Cipher.getInstance(ALGORITHM);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(dec.decode(split[1]));
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        decryptCipher = Cipher.getInstance(ALGORITHM);
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
    }

    public String decrypt(String encrypted) throws IllegalBlockSizeException, BadPaddingException {
        byte[] encryptedMessageBytes = Base64.getDecoder().decode(encrypted);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String res = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return res.substring(0, res.length()-SALT.length());
    }

    public String encrypt(String secret) throws IllegalBlockSizeException, BadPaddingException {
        byte[] secretMessageBytes = (secret+SALT).getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }

}
