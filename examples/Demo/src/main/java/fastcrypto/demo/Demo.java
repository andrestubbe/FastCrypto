package fastcrypto.demo;

import fastcrypto.FastCrypto;
import java.nio.charset.StandardCharsets;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("          FastCrypto - Live Demo                 ");
        System.out.println("=================================================");
        System.out.println("Hardware Acceleration Native Active: " + FastCrypto.isNative());
        System.out.println();

        // 1. Generate keys & IV
        byte[] key = FastCrypto.randomBytes(32); // AES-256
        byte[] iv = FastCrypto.randomBytes(12);  // 96-bit nonce
        byte[] aad = "UserContext:admin@fastjava".getBytes(StandardCharsets.UTF_8);

        String secretPayload = "FastCrypto: Sub-microsecond hardware-accelerated AES-NI authenticated encryption!";
        byte[] plaintext = secretPayload.getBytes(StandardCharsets.UTF_8);

        System.out.println("[+] Plaintext: \"" + secretPayload + "\"");
        System.out.println("[+] Plaintext length: " + plaintext.length + " bytes");

        // 2. Encrypt
        long t0 = System.nanoTime();
        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, plaintext, aad);
        long t1 = System.nanoTime();
        System.out.printf("[+] Encrypted in %.2f µs (Ciphertext + 16-byte GCM Tag: %d bytes)%n", (t1 - t0) / 1000.0, ciphertext.length);

        // 3. Decrypt
        long t2 = System.nanoTime();
        byte[] decrypted = FastCrypto.decryptAesGcm(key, iv, ciphertext, aad);
        long t3 = System.nanoTime();
        String restored = new String(decrypted, StandardCharsets.UTF_8);
        System.out.printf("[+] Decrypted in %.2f µs: \"%s\"%n", (t3 - t2) / 1000.0, restored);

        // 4. Memory Scrubbing
        FastCrypto.wipe(key);
        FastCrypto.wipe(plaintext);
        FastCrypto.wipe(decrypted);
        System.out.println("[+] Sensitive key and plaintext buffers wiped from RAM via SecureZeroMemory.");
        System.out.println();
        System.out.println("[SUCCESS] FastCrypto live demonstration complete!");
    }
}
