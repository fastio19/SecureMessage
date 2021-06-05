package com.example.securemessage.EncryptionDecryptionHybrid;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncDeHybrid {
    // Symmetric encryption algorithms supported - AES, RC4, DES
    // encryption algorithm - DES, key size - 56
    protected static String DEFAULT_ENCRYPTION_ALGORITHM = "AES";
    protected static int DEFAULT_ENCRYPTION_KEY_LENGTH = 256;

    // key encryption algorithms supported - RSA, Diffie-Hellman, DSA
    // key pair generator - RSA: keyword - RSA, key size: 1024, 2048
    // key pair generator - Diffie-Hellman: keyword i DiffieHellman, key size - 1024
    // key pair generator - DSA: keyword - DSA, key size: 1024
    // NOTE: using asymmetric algorithms other than RSA needs to be worked out
    protected static String DEFAULT_KEY_ENCRYPTION_ALGORITHM = "RSA";
    protected static int DEFAULT_KEY_ENCRYPTION_KEY_LENGTH = 1024;
    protected static String DEFAULT_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    protected SecretKey mSecretKey;
    protected String mEncryptionAlgorithm, mKeyEncryptionAlgorithm, mTransformation;
    protected int mEncryptionKeyLength, mKeyEncryptionKeyLength;
    protected PublicKey mPublicKey;
    protected PrivateKey mPrivateKey;

    public EncDeHybrid()
    {
        mSecretKey = null;
        mEncryptionAlgorithm = EncDeHybrid.DEFAULT_ENCRYPTION_ALGORITHM;
        mEncryptionKeyLength = EncDeHybrid.DEFAULT_ENCRYPTION_KEY_LENGTH;
        mKeyEncryptionAlgorithm = EncDeHybrid.DEFAULT_KEY_ENCRYPTION_ALGORITHM;
        mKeyEncryptionKeyLength = EncDeHybrid.DEFAULT_KEY_ENCRYPTION_KEY_LENGTH;
        mPublicKey = null;
        mPrivateKey = null;
        mTransformation = EncDeHybrid.DEFAULT_TRANSFORMATION;
    }

    EncDeHybrid(String encAlgo, int encKeyLength, String keyEncAlgo, int keyEncKeyLength, String transformation)
    {
        mSecretKey = null;
        mEncryptionAlgorithm = encAlgo;
        mEncryptionKeyLength = encKeyLength;
        mKeyEncryptionAlgorithm = keyEncAlgo;
        mKeyEncryptionKeyLength = keyEncKeyLength;
        mTransformation = transformation;
    }

    public static BigInteger keyToNumber(byte[] byteArray)
    {
        return new BigInteger(1, byteArray);
    }

    public SecretKey getSecretKey()
    {
        return mSecretKey;
    }

    public byte[] getSecretKeyAsByteArray()
    {
        return mSecretKey.getEncoded();
    }

    // get base64 encoded version of the key
    public String getEncodedSecretKey()
    {
        String encodedKey = Base64.getEncoder().encodeToString(mSecretKey.getEncoded());
        return encodedKey;
    }

    // decode the base64 encoded string
    public SecretKey getDecodedSecretKey(String encodedKey, String algo)
    {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        SecretKey originalKey = null;

        if ( null == algo ) {
            originalKey = new SecretKeySpec(decodedKey,
                    0, decodedKey.length, mEncryptionAlgorithm);
        } else {
            originalKey = new SecretKeySpec(decodedKey,
                    0, decodedKey.length, algo);
        }

        return originalKey;
    }

    public PublicKey getPublicKey()
    {
        return mPublicKey;
    }

    public byte[] getPublicKeyAsByteArray()
    {
        return mPublicKey.getEncoded();
    }

    public String getEncodedPublicKey()
    {
        String encodedKey = Base64.getEncoder().encodeToString(mPublicKey.getEncoded());
        return encodedKey;
    }

    public PrivateKey getPrivateKey()
    {
        return mPrivateKey;
    }

    public byte[] getPrivateKeyAsByteArray()
    {
        return mPrivateKey.getEncoded();
    }

    private String getEncodedPrivateKey()
    {
        String encodedKey = Base64.getEncoder().encodeToString(mPrivateKey.getEncoded());
        return encodedKey;
    }

    // step 1 -- generate the symmetric key
    public void generateSymmetricKey()
    {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance(mEncryptionAlgorithm);
            generator.init(mEncryptionKeyLength);

            mSecretKey = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // step 2 -- encrypt the plain text
    public byte[] encryptText(String textToEncrypt)
    {
        byte[] byteCipherText = null;

        try {
            Cipher encCipher = Cipher.getInstance(mEncryptionAlgorithm);
            encCipher.init(Cipher.ENCRYPT_MODE, mSecretKey);
            byteCipherText = encCipher.doFinal(textToEncrypt.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return byteCipherText;
    }
    public void generateRSAKeys(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(mKeyEncryptionAlgorithm);
            kpg.initialize(mKeyEncryptionKeyLength);
            KeyPair keyPair = kpg.generateKeyPair();

            mPublicKey = keyPair.getPublic();
            mPrivateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // step 3 -- encrypt the secret key using key encryption algorithm
    public byte[] encryptSecretKey()
    {
        byte[] encryptedKey = null;
        try {
            Cipher cipher = Cipher.getInstance(mTransformation);
            cipher.init(Cipher.PUBLIC_KEY, mPublicKey);
            encryptedKey = cipher.doFinal(mSecretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return encryptedKey;
    }

    public byte[] encryptSecretKeyNoTransformation()
    {
        byte[] encryptedKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(mKeyEncryptionAlgorithm);
            kpg.initialize(mKeyEncryptionKeyLength);

            KeyPair keyPair = kpg.generateKeyPair();

            mPublicKey = keyPair.getPublic();
            mPrivateKey = keyPair.getPrivate();

            Cipher cipher = Cipher.getInstance(mTransformation);
            cipher.init(Cipher.PUBLIC_KEY, mPublicKey);

            encryptedKey = cipher.doFinal(mSecretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return encryptedKey;
    }

    // step 4 -- send across the encrypted text and the encrypted secret key



    // setp 5 -- decrypt secret key
    public byte[] decryptSecretKey(byte[] encryptedSecretKey)
    {
        byte[] decryptedKey = null;
        try {

            Cipher cipher = Cipher.getInstance(mTransformation);
            cipher.init(Cipher.PRIVATE_KEY, mPrivateKey);

            decryptedKey = cipher.doFinal(encryptedSecretKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return decryptedKey;
    }

    // step 6 -- Decrypt the cipher using decrypted symmetric key
    public String decryptText(byte[] decryptedKey, byte[] encryptedText)
    {
        String decryptedPlainText = null;

        try {
            SecretKey originalKey = new SecretKeySpec(decryptedKey , 0,
                    decryptedKey.length, mEncryptionAlgorithm);
            Cipher aesCipher2 = Cipher.getInstance(mEncryptionAlgorithm);
            aesCipher2.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] bytePlainText = aesCipher2.doFinal(encryptedText);
            decryptedPlainText = new String(bytePlainText);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return decryptedPlainText;
    }
}
