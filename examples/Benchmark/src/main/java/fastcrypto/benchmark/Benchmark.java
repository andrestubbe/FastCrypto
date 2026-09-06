package fastcrypto.benchmark;

import fastcrypto.FastCrypto;
import java.nio.charset.StandardCharsets;

public class Benchmark {
    private static final int WARMUP_ROUNDS = 5_000;
    private static final int MEASURE_ROUNDS = 50_000;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("       FastCrypto - Microsecond Latency & Throughput Benchmark ");
        System.out.println("===============================================================");
        System.out.println("CPU Hardware Accelerated: " + FastCrypto.isNative());
        System.out.println("Warmup iterations: " + WARMUP_ROUNDS + " | Benchmark iterations: " + MEASURE_ROUNDS);
        System.out.println();

        runBenchmarkForSize(64);       // 64 B (e.g. Tokens, Passwords, Keys)
        runBenchmarkForSize(1024);     // 1 KB (Network Packets, IPC blocks)
        runBenchmarkForSize(64 * 1024);// 64 KB (File chunks, frames)
    }

    private static void runBenchmarkForSize(int sizeBytes) {
        byte[] key = FastCrypto.randomBytes(32); // AES-256
        byte[] iv = FastCrypto.randomBytes(12);
        byte[] data = new byte[sizeBytes];
        for (int i = 0; i < sizeBytes; i++) data[i] = (byte) (i & 0xFF);

        // Warmup
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            byte[] ct = FastCrypto.encryptAesGcm(key, iv, data, null);
            FastCrypto.decryptAesGcm(key, iv, ct, null);
        }

        // Measure Encrypt
        long startEnc = System.nanoTime();
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            FastCrypto.encryptAesGcm(key, iv, data, null);
        }
        long encNanos = System.nanoTime() - startEnc;

        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, data, null);

        // Measure Decrypt
        long startDec = System.nanoTime();
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            FastCrypto.decryptAesGcm(key, iv, ciphertext, null);
        }
        long decNanos = System.nanoTime() - startDec;

        double avgEncUs = (encNanos / (double) MEASURE_ROUNDS) / 1000.0;
        double avgDecUs = (decNanos / (double) MEASURE_ROUNDS) / 1000.0;

        double mbProcessed = (sizeBytes * (double) MEASURE_ROUNDS) / (1024.0 * 1024.0);
        double encThroughput = mbProcessed / (encNanos / 1_000_000_000.0);
        double decThroughput = mbProcessed / (decNanos / 1_000_000_000.0);

        System.out.printf("--- Payload Size: %d bytes (%.1f KB) ---%n", sizeBytes, sizeBytes / 1024.0);
        System.out.printf("  AES-256-GCM Encrypt: %8.2f \u00b5s/op  | Throughput: %8.2f MB/s%n", avgEncUs, encThroughput);
        System.out.printf("  AES-256-GCM Decrypt: %8.2f \u00b5s/op  | Throughput: %8.2f MB/s%n", avgDecUs, decThroughput);
        System.out.println();
    }
}
