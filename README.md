# FastCrypto ⚡

Hardware-accelerated AES-GCM (128/256-bit) authenticated encryption for Java via native CPU instructions (AES-NI / Windows CNG).

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)
[![Release](https://img.shields.io/github/v/release/andrestubbe/FastCrypto)](https://github.com/andrestubbe/FastCrypto/releases)

---

## ⚡ Overview

**FastCrypto** provides ultra-low latency, hardware-accelerated cryptographic primitives for Java. By interfacing directly with native Windows BCrypt / CNG cryptographic hardware drivers via `FastCore`, FastCrypto bypasses the heavy JCA/JCE provider machinery, minimizes allocation overhead, and executes AES-GCM at hardware-native CPU speeds.

### Key Highlights
- **Sub-microsecond per-operation latency**: ~1.07 – 1.41 µs per 64-byte payload.
- **Hardware-Saturated Throughput**: Up to **~3,980 MB/s (3.9 GB/s)** on bulk payloads.
- **Zero-Heap Leakage**: Instant RAM scrubbing via native `SecureZeroMemory` to neutralize heap dump memory extraction.
- **Full Fallback Guarantee**: Gracefully and transparently falls back to standard `javax.crypto` if native drivers are unavailable.

---

## 📊 Verified Benchmarks

*Benchmark executed on Windows 11 x64, Intel/AMD processor with AES-NI hardware acceleration enabled (50,000 iterations per test):*

| Payload Size | Operation | Average Latency | Throughput | Hardware Acceleration |
|:---|:---|:---:|:---:|:---:|
| **64 Bytes** *(Tokens, Passwords)* | AES-256-GCM Encrypt | **1.41 µs** | 43.1 MB/s | Native AES-NI |
| **64 Bytes** *(Tokens, Passwords)* | AES-256-GCM Decrypt | **1.07 µs** | 56.8 MB/s | Native AES-NI |
| **1 KB** *(Network Packets, IPC)* | AES-256-GCM Encrypt | **1.53 µs** | 640.1 MB/s | Native AES-NI |
| **1 KB** *(Network Packets, IPC)* | AES-256-GCM Decrypt | **1.30 µs** | 751.4 MB/s | Native AES-NI |
| **64 KB** *(Bulk Blocks / Frames)* | AES-256-GCM Encrypt | **15.69 µs** | **3,983.2 MB/s (~4.0 GB/s)** | Native AES-NI |
| **64 KB** *(Bulk Blocks / Frames)* | AES-256-GCM Decrypt | **17.36 µs** | **3,600.2 MB/s (~3.6 GB/s)** | Native AES-NI |

---

## 🚀 Quickstart

### 1. Maven / JitPack Dependency

Add the JitPack repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add FastCrypto:
```xml
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastCrypto</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Basic Usage

```java
import fastcrypto.FastCrypto;
import java.nio.charset.StandardCharsets;

public class SecurityService {
    public static void main(String[] args) {
        // 1. Generate keys and IV
        byte[] key = FastCrypto.randomBytes(32); // AES-256
        byte[] iv = FastCrypto.randomBytes(12);  // 96-bit standard nonce
        byte[] aad = "tenant-id:42".getBytes(StandardCharsets.UTF_8);

        byte[] plaintext = "Sensitive user license key payload".getBytes(StandardCharsets.UTF_8);

        // 2. Encrypt (Hardware-accelerated AES-NI)
        byte[] ciphertext = FastCrypto.encryptAesGcm(key, iv, plaintext, aad);

        // 3. Decrypt & Verify 16-byte GCM Tag
        byte[] decrypted = FastCrypto.decryptAesGcm(key, iv, ciphertext, aad);

        // 4. Zero out sensitive memory
        FastCrypto.wipe(key);
        FastCrypto.wipe(plaintext);
        FastCrypto.wipe(decrypted);
    }
}
```

---

## 🏃 Running Examples & Benchmarks

```bash
# Run interactive live demonstration
run-demo.bat

# Run the benchmark suite
run-benchmark.bat
```

---

## 📜 Documentation

- [API Reference](docs/REFERENCE.md)
- [Design Philosophy](docs/PHILOSOPHY.md)
- [Roadmap](docs/ROADMAP.md)
- [Changelog](docs/CHANGELOG.md)

---

## 📄 License

MIT © 2026 Andre Stubbe
