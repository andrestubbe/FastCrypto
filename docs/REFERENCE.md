# FastCrypto Reference

## 1. Cryptographic Architecture

FastCrypto bypasses standard Java JCA/JCE provider latency penalties by calling native OS kernel crypto primitives (`BCryptEncrypt` / `BCryptDecrypt`) directly via `FastCore`.

### Algorithms
*   **AES-GCM (Galois/Counter Mode)**:
    - Key sizes: 128-bit (16 bytes) and 256-bit (32 bytes).
    - Nonce / IV: 96-bit (12 bytes) recommended standard.
    - Tag: 128-bit (16 bytes) authentication tag automatically authenticated and appended.
    - Additional Authenticated Data (AAD): Supported for header / contextual integrity verification.
*   **Memory Sanitization (`SecureZeroMemory`)**:
    - Avoids compiler dead-store elimination.
    - Overwrites key and plaintext buffers in place.

---

## 2. API Contract

### AES-GCM
```java
// Encrypt with AES-GCM (appends 16-byte authentication tag to returned array)
byte[] FastCrypto.encryptAesGcm(byte[] key, byte[] iv, byte[] plaintext, byte[] aad);

// Decrypt with AES-GCM (verifies 16-byte tag, throws SecurityException on mismatch)
byte[] FastCrypto.decryptAesGcm(byte[] key, byte[] iv, byte[] ciphertextWithTag, byte[] aad);
```

### Random Generation & Sanitization
```java
// Secure random byte generation
byte[] FastCrypto.randomBytes(int length);

// Memory wiping
void FastCrypto.wipe(byte[] secret);
```

---

## 3. Platform Support
| Platform | Driver Backend | Hardware Instructions | Status |
|:---|:---|:---|:---:|
| Windows 10/11 x64 | Windows CNG (`bcrypt.dll`) | AES-NI, AVX |  Fully Supported |
| Windows Server 2016+ | Windows CNG (`bcrypt.dll`) | AES-NI |  Fully Supported |
| Other Platforms | Standard `javax.crypto` Fallback | JIT/Compiler optimized |  Automatic Fallback |

---
**Part of the FastJava Ecosystem**  *Making the JVM faster.*
