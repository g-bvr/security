package org.jkube.gitbeaver.security;

import org.jkube.logging.Log;
import org.jkube.util.Expect;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicPrivateEncryption {

    private static final String SEPARATOR = "#";
    public static final String PUBLIC_PRIVATE_ALGORITHM = "RSA";
    public static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    public static final int PADDING_BYTES = 42;

    public static final String SYMMETRIC_ALGORITHM = "AES";
    public static final int SYMMETRIC_KEYSIZE = 256;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    private final int asymmetricKeySize;

    private final int symmetricKeySize;

    public static String createKeyPair(int asymmetricKeySize, int symmetricKeySize) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(PUBLIC_PRIVATE_ALGORITHM);
        generator.initialize(asymmetricKeySize);
        KeyPair pair = generator.generateKeyPair();
        Base64.Encoder enc = Base64.getEncoder();
        return asymmetricKeySize+SEPARATOR+symmetricKeySize
                +SEPARATOR+enc.encodeToString(pair.getPublic().getEncoded())
                +SEPARATOR+enc.encodeToString(pair.getPrivate().getEncoded());
    }

    public PublicPrivateEncryption(String keypair) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] split = keypair.split(SEPARATOR);
        Expect.equal(4, split.length).elseFail("expected 4 parts in key definition");
        asymmetricKeySize = Integer.parseInt(split[0]);
        symmetricKeySize = Integer.parseInt(split[1]);
        Log.log("Key sizes: "+asymmetricKeySize+","+symmetricKeySize);
        Log.log("Split key string into public-private pair with sizes: "+split[2].length()+","+split[3].length());
        Base64.Decoder dec = Base64.getDecoder();
        KeyFactory keyFactory = KeyFactory.getInstance(PUBLIC_PRIVATE_ALGORITHM);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(dec.decode(split[2]));
        publicKey = keyFactory.generatePublic(publicKeySpec);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(dec.decode(split[3]));
        privateKey = keyFactory.generatePrivate(privateKeySpec);
    }

    public String decrypt(String encrypted) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String[] split = encrypted.split(SEPARATOR);
        Expect.atMost(split.length, 2).elseFail("expected at most 2 parts in encrypted data");
        Cipher privateCipher = createPrivateDecryptCipher();
        if (split.length == 1) {
            // no symmetric key was used
            return decryptToString(privateCipher, split[0]);
        }
        // decrypt symmetric key
        byte[] symmetricKeyBytes = decryptToBytes(privateCipher, split[0]);
        SecretKey symmetricKey = new SecretKeySpec(symmetricKeyBytes , 0, symmetricKeyBytes .length, SYMMETRIC_ALGORITHM);
        // decrypt message
        Cipher symmetricCipher = createCipher(SYMMETRIC_ALGORITHM, Cipher.DECRYPT_MODE, symmetricKey);
        return decryptToString(symmetricCipher, split[1]);
    }

    public String encrypt(String secret) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        Cipher publicCipher = createPublicEncryptCipher();
        if (secretBytes.length <= getNumBytesInBlock()) {
            // can do it in one block
            return encrypt(publicCipher, secretBytes);
        }
        // message is longer than one block, have to use symmetric key
        KeyGenerator generator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        generator.init(SYMMETRIC_KEYSIZE);
        SecretKey symmetricKey = generator.generateKey();
        String encryptedKey = encrypt(publicCipher, symmetricKey.getEncoded());

        Cipher symmetricCipher = createCipher(SYMMETRIC_ALGORITHM, Cipher.ENCRYPT_MODE, symmetricKey);
        String encryptedSecret = encrypt(symmetricCipher, secret);

        return encryptedKey+SEPARATOR+encryptedSecret;
    }

    private int getNumBytesInBlock() {
        return asymmetricKeySize/8 - PADDING_BYTES;
    }

    private Cipher createPublicEncryptCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return createCipher(TRANSFORMATION, Cipher.ENCRYPT_MODE, publicKey);
    }

    private Cipher createPrivateDecryptCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return createCipher(TRANSFORMATION, Cipher.DECRYPT_MODE, privateKey);
    }

    private Cipher createCipher(String transformation, int opCode, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(opCode, key);
        return cipher;
    }

    private String encrypt(Cipher cipher, String message) throws IllegalBlockSizeException, BadPaddingException {
        return encrypt(cipher, message.getBytes(StandardCharsets.UTF_8));
    }

    private String encrypt(Cipher cipher, byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        return Base64.getEncoder().encodeToString(cipher.doFinal(data));
    }

    private byte[] decryptToBytes(Cipher cipher, String message) throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(Base64.getDecoder().decode(message));
    }

    private String decryptToString(Cipher cipher, String message) throws IllegalBlockSizeException, BadPaddingException {
        return new String(decryptToBytes(cipher, message), StandardCharsets.UTF_8);
    }


}
