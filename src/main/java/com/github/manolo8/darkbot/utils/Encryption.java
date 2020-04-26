package com.github.manolo8.darkbot.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Encryption {

    private static final String CIPHER = "AES/CBC/PKCS5Padding", KEY_FACTORY = "PBKDF2WithHmacSHA512";
    private static final int KEY_LENGTH = 128, ITERATIONS = 10000;

    public static SecretKeySpec createSecretKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(SecretKeyFactory.getInstance(KEY_FACTORY)
                .generateSecret(new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)).getEncoded(), "AES");
    }

    public static String encrypt(String data, SecretKeySpec key) throws GeneralSecurityException {
        Cipher pbeCipher = Cipher.getInstance(CIPHER);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64Utils.encodeBytes(pbeCipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV()) + ":"
                + Base64Utils.encodeBytes(pbeCipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String data, SecretKeySpec key) throws GeneralSecurityException {
        Cipher pbeCipher = Cipher.getInstance(CIPHER);
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Base64Utils.decodeBytes(data.split(":", 2)[0])));
        return new String(pbeCipher.doFinal(Base64Utils.decodeBytes(data.split(":", 2)[1])), StandardCharsets.UTF_8);
    }

}
