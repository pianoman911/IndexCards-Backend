package de.pianoman911.indexcards.util;

import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CipherUtils {

    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static byte[] encryptAES(String text, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
            return cipher.doFinal(text.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptAES(byte[] data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
            return new String(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateKey() {
        byte[] key = new byte[16];
        new SecureRandom("TollerSeed".getBytes()).nextBytes(key);
        return key;
    }

    public static String byteToString(byte[] data, boolean compress) {
        return B64_ENCODER.encodeToString(compress ? compressToBytes(data) : data);
    }

    public static byte[] stringToByte(String data, boolean decompress) {
        return decompress ? decompressBytes(B64_DECODER.decode(data)) : B64_DECODER.decode(data);
    }

    public static byte @NotNull [] compressToBytes(byte @NotNull [] bytes) {
        byte[] compressed = EMPTY_BYTE_ARRAY;

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(bytes);
            } catch (Throwable ignored) {
                return compressed;
            }

            compressed = output.toByteArray();
        } catch (Throwable ignored) {
            return compressed;
        }

        return compressed;
    }

    public static byte @NotNull [] decompressBytes(byte @NotNull [] bytes) {
        byte[] decompressed = EMPTY_BYTE_ARRAY;

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             GZIPInputStream gzip = new GZIPInputStream(input)) {
            decompressed = gzip.readAllBytes();
        } catch (Throwable ignored) {
            return decompressed;
        }

        return decompressed;
    }
}
