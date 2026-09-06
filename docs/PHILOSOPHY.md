# The Philosophy of FastCrypto

> [!IMPORTANT]
> **"Kryptografie muss im Prozessor passieren – ohne JCA-Provider-Overhead, ohne Garbage Collection, ohne Speicherlecks."**

Standard Java cryptography via `javax.crypto` incurs significant latency penalties:
1. Provider lookup overhead on every cipher transformation.
2. Multiple internal buffer allocations causing GC thrashing during high-throughput I/O.
3. Inability to guarantee memory wiping because Java strings and arrays are managed by the garbage collector.

## Core Tenets

1. **Hardware-First Execution**
   Leverage modern processor extensions (AES-NI) directly at bare-metal speeds.

2. **Zero Overhead for Realtime Data**
   FastCrypto achieves 1.07 µs latency for small messages and up to 3.9 GB/s throughput on larger buffers, making it suitable for 60fps video frames, low-latency trading, and IPC.

3. **Defensive In-Memory Sanitization**
   Immediate zeroization of keys and plaintext prevents exposure in heap dumps.

4. **Seamless Integration**
   Integrates directly with `FastCore`, `FastMemory`, and `FastKeychain`.
