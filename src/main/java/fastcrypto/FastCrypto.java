package fastcrypto;

import fastcore.FastCore;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * FastCrypto: Hardware-accelerated Cryptography Engine for Java.
 * <p>
 * Provides AES-GCM (128 & 256-bit) via native CPU instructions (AES-NI)
 * using the Windows CNG (Cryptography Next Generation / BCrypt) kernel engine,
 * bypassing JCA provider overhead with zero-copy DirectByteBuffer and array support.
 */
public final class FastCrypto {

    private static final boolean NATIVE_LOADED;
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        boolean loaded = false;
        try {
            FastCore.loadLibrary("fastcrypto");
            loaded = true;
        } catch (Throwable t) {
            System.err.println("[FastCrypto] Warning: Could not load native library 'fastcrypto'. Falling back to Java engine: " + t.getMessage());
        }
        NATIVE_LOADED = loaded;
    }

    private FastCrypto() {}

    /**
     * Checks if native hardware acceleration (AES-NI / Windows CNG) is active.
     */
    public static boolean isNative() {
        return NATIVE_LOADED && nIsHardwareAccelerated();
    }

    /**
     * Generates cryptographically secure random bytes.
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Encrypts plaintext using AES-GCM with a 12-byte IV and 16-byte authentication tag.
     *
     * @param key 16-byte (AES-128) or 32-byte (AES-256) key
     * @param iv 12-byte initialization vector / nonce
     * @param plaintext raw data to encrypt
     * @param aad optional additional authenticated data (can be null)
     * @return ciphertext appended with 16-byte authentication tag
     */
    public static byte[] encryptAesGcm(byte[] key, byte[] iv, byte[] plaintext, byte[] aad) {
        validateAesGcmParams(key, iv, plaintext);
        if (NATIVE_LOADED) {
            byte[] result = nEncryptAesGcm(key, iv, plaintext, aad);
            if (result != null) return result;
        }
        return javaEncryptAesGcm(key, iv, plaintext, aad);
    }

    /**
     * Decrypts AES-GCM ciphertext (which contains the appended 16-byte tag).
     *
     * @param key 16-byte (AES-128) or 32-byte (AES-256) key
     * @param iv 12-byte initialization vector / nonce
     * @param ciphertextWithTag ciphertext appended with 16-byte tag
     * @param aad optional additional authenticated data (can be null)
     * @return decrypted plaintext
     * @throws SecurityException if authentication verification fails
     */
    public static byte[] decryptAesGcm(byte[] key, byte[] iv, byte[] ciphertextWithTag, byte[] aad) {
        validateAesGcmParams(key, iv, ciphertextWithTag);
        if (ciphertextWithTag.length < 16) {
            throw new IllegalArgumentException("Ciphertext too short to contain 16-byte authentication tag");
        }
        if (NATIVE_LOADED) {
            byte[] result = nDecryptAesGcm(key, iv, ciphertextWithTag, aad);
            if (result != null) return result;
            throw new SecurityException("AES-GCM authentication verification failed (native tag mismatch)");
        }
        return javaDecryptAesGcm(key, iv, ciphertextWithTag, aad);
    }

    /**
     * Zeroes out sensitive memory buffers to eliminate memory dump risks.
     */
    public static void wipe(byte[] secret) {
        if (secret == null) return;
        if (NATIVE_LOADED) {
            nWipe(secret);
        } else {
            Arrays.fill(secret, (byte) 0);
        }
    }

    private static void validateAesGcmParams(byte[] key, byte[] iv, byte[] data) {
        if (key == null || (key.length != 16 && key.length != 32)) {
            throw new IllegalArgumentException("AES key must be exactly 16 bytes (AES-128) or 32 bytes (AES-256)");
        }
        if (iv == null || iv.length != 12) {
            throw new IllegalArgumentException("AES-GCM standard IV must be exactly 12 bytes");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
    }

    // --- Pure Java Fallback Engine (using javax.crypto) ---
    private static byte[] javaEncryptAesGcm(byte[] key, byte[] iv, byte[] plaintext, byte[] aad) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(key, "AES");
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Java fallback AES-GCM encryption failed", e);
        }
    }

    private static byte[] javaDecryptAesGcm(byte[] key, byte[] iv, byte[] ciphertextWithTag, byte[] aad) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(key, "AES");
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(ciphertextWithTag);
        } catch (javax.crypto.AEADBadTagException e) {
            throw new SecurityException("AES-GCM authentication verification failed (bad tag)", e);
        } catch (Exception e) {
            throw new RuntimeException("Java fallback AES-GCM decryption failed", e);
        }
    }

    // --- Native Methods ---
    private static native boolean nIsHardwareAccelerated();
    private static native byte[] nEncryptAesGcm(byte[] key, byte[] iv, byte[] plaintext, byte[] aad);
    private static native byte[] nDecryptAesGcm(byte[] key, byte[] iv, byte[] ciphertextWithTag, byte[] aad);
    private static native void nWipe(byte[] secret);
}
