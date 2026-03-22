package com.ode.ftps;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CryptoChunkCodec {
    public static final int CHUNK_SIZE_BYTES = 1024;
    public static final String ENCRYPTION_ALGORITHM_LABEL = "aes-256-gcm";

    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final String KDF_ALGO = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int KEY_LEN_BYTES = 32;
    private static final int SALT_LEN_BYTES = 16;
    private static final int IV_LEN_BYTES = 12;
    private static final int TAG_LEN_BYTES = 16;
    private static final byte[] MAGIC = "ENC1".getBytes(StandardCharsets.US_ASCII);

    private CryptoChunkCodec() {
    }

    public static byte[] construirPayloadEncriptado(String txtContent, String passphrase)
        throws IOException, GeneralSecurityException {
        byte[] plainBuffer = txtContent.getBytes(StandardCharsets.UTF_8);
        byte[] compressed = gzip(plainBuffer);

        byte[] salt = randomBytes(SALT_LEN_BYTES);
        byte[] iv = randomBytes(IV_LEN_BYTES);
        byte[] key = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LEN_BYTES * 8, iv));
        byte[] cipherPlusTag = cipher.doFinal(compressed);

        int cipherLen = cipherPlusTag.length - TAG_LEN_BYTES;
        byte[] encrypted = Arrays.copyOfRange(cipherPlusTag, 0, cipherLen);
        byte[] authTag = Arrays.copyOfRange(cipherPlusTag, cipherLen, cipherPlusTag.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MAGIC);
        out.write(salt);
        out.write(iv);
        out.write(authTag);
        out.write(encrypted);
        return out.toByteArray();
    }

    public static String desencriptarPayload(byte[] payload, String passphrase)
        throws GeneralSecurityException, IOException {
        int minLen = MAGIC.length + SALT_LEN_BYTES + IV_LEN_BYTES + TAG_LEN_BYTES;
        if (payload.length < minLen) {
            throw new IllegalArgumentException("Payload invalido: muy pequeno");
        }

        byte[] magic = Arrays.copyOfRange(payload, 0, MAGIC.length);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IllegalArgumentException("Payload invalido: cabecera ENC1 no encontrada");
        }

        int offset = MAGIC.length;
        byte[] salt = Arrays.copyOfRange(payload, offset, offset + SALT_LEN_BYTES);
        offset += SALT_LEN_BYTES;
        byte[] iv = Arrays.copyOfRange(payload, offset, offset + IV_LEN_BYTES);
        offset += IV_LEN_BYTES;
        byte[] authTag = Arrays.copyOfRange(payload, offset, offset + TAG_LEN_BYTES);
        offset += TAG_LEN_BYTES;
        byte[] encrypted = Arrays.copyOfRange(payload, offset, payload.length);

        byte[] key = deriveKey(passphrase, salt);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LEN_BYTES * 8, iv));

        byte[] cipherPlusTag = new byte[encrypted.length + authTag.length];
        System.arraycopy(encrypted, 0, cipherPlusTag, 0, encrypted.length);
        System.arraycopy(authTag, 0, cipherPlusTag, encrypted.length, authTag.length);

        byte[] decompressed = cipher.doFinal(cipherPlusTag);
        byte[] plain = gunzip(decompressed);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static byte[] deriveKey(String passphrase, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LEN_BYTES * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGO);
        return factory.generateSecret(keySpec).getEncoded();
    }

    private static byte[] randomBytes(int size) {
        byte[] b = new byte[size];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {
            gzipOut.write(data);
        }
        return out.toByteArray();
    }

    private static byte[] gunzip(byte[] data) throws IOException {
        try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = gzipIn.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }
}
