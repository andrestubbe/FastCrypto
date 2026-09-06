# FastCrypto Roadmap 

**Vision:** Ultra-fast, zero-overhead cryptographic engine for modern JVM applications.

##  v0.1.0: Hardware AES-NI (Current)
- [x] AES-GCM 128/256-bit native engine via Windows CNG.
- [x] Sub-microsecond latency and ~4.0 GB/s throughput.
- [x] Native `SecureZeroMemory` wiping.
- [x] Standalone Demo and Benchmark suites.
- [x] Transparent pure-Java fallback.

##  v0.2.0: Non-AES Fast Cipher Suites
- [ ] ChaCha20-Poly1305 native SIMD implementation (AVX2/AVX-512).
- [ ] DirectByteBuffer / `FastMemory` zero-copy stream API.

##  v0.5.0: Asymmetric & License Signatures
- [ ] Ed25519 high-speed signature verification for offline software license keys.
- [ ] Curve25519 ECDH key agreement.

##  v1.0.0: Enterprise Production Hardening
- [ ] Cross-platform Linux (OpenSSL/libcrypto) and macOS (CommonCrypto) parity.
- [ ] FIPS 140-3 compliance guidelines.
