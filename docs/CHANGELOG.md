# Changelog

All notable changes to **FastCrypto** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.1] - 2026-09-07

### Fixed
- Added `jitpack.io` repository declaration in `pom.xml` to allow resolution of internal FastJava dependencies (`FastCore`) on JitPack CI.
- Updated documentation and JitPack release tags.

## [0.1.0] - 2026-09-06

### Added
- Native hardware-accelerated AES-GCM (128-bit and 256-bit) encryption and decryption via Windows CNG / AES-NI.
- Native `SecureZeroMemory` wiping (`FastCrypto.wipe`).
- Pure Java fallback engine using `javax.crypto` when native library is absent.
- Full unit test suite covering AES-128, AES-256, authentication tag tampering, and memory wiping.
- Interactive `run-demo.bat` and comprehensive microsecond `run-benchmark.bat`.
- Complete documentation suite (`README.md`, `docs/REFERENCE.md`, `docs/PHILOSOPHY.md`, `docs/ROADMAP.md`).
