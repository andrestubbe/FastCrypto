package fastcrypto;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class FastCryptoTest {

    @Test
    public void testAesGcm128EncryptionDecryption() {
        byte[] key = FastCrypto.randomBytes(16); // AES-128
        byte[] iv = FastCrypto.randomBytes(12);  // standard 96-bit IV
        byte[] plaintext = "Hello FastJava! Ultra-fast AES-GCM encryption with AES-NI.".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "FastCrypto-Header".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, plaintext, aad);
        assertNotNull(ciphertext);
        assertEquals(plaintext.length + 16, ciphertext.length, "Ciphertext should be plaintext length + 16-byte tag");

        byte[] decrypted = FastCrypto.decryptAesGcm(key, iv, ciphertext, aad);
        assertNotNull(decrypted);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testAesGcm256EncryptionDecryption() {
        byte[] key = FastCrypto.randomBytes(32); // AES-256
        byte[] iv = FastCrypto.randomBytes(12);
        byte[] plaintext = "Super Secret License Token Data - 256-bit AES GCM".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, plaintext, null);
        assertNotNull(ciphertext);
        assertEquals(plaintext.length + 16, ciphertext.length);

        byte[] decrypted = FastCrypto.decryptAesGcm(key, iv, ciphertext, null);
        assertNotNull(decrypted);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testTamperedCiphertextFails() {
        byte[] key = FastCrypto.randomBytes(32);
        byte[] iv = FastCrypto.randomBytes(12);
        byte[] plaintext = "Original authentic message".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, plaintext, null);

        // Tamper with 1 byte of ciphertext
        ciphertext[0] ^= 0x5A;

        assertThrows(SecurityException.class, () -> {
            FastCrypto.decryptAesGcm(key, iv, ciphertext, null);
        });
    }

    @Test
    public void testMemoryWiping() {
        byte[] secret = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        FastCrypto.wipe(secret);
        for (byte b : secret) {
            assertEquals(0, b);
        }
    }
}
