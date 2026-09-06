#include <jni.h>
#include <windows.h>
#include <bcrypt.h>
#include <stdio.h>

#pragma comment(lib, "bcrypt.lib")

static BCRYPT_ALG_HANDLE hAesGcmAlg = NULL;

static void ensureAlgorithmHandle() {
    if (hAesGcmAlg == NULL) {
        NTSTATUS status = BCryptOpenAlgorithmProvider(
            &hAesGcmAlg,
            BCRYPT_AES_ALGORITHM,
            MS_PRIMITIVE_PROVIDER,
            0
        );
        if (BCRYPT_SUCCESS(status)) {
            // Set chaining mode to GCM
            BCryptSetProperty(
                hAesGcmAlg,
                BCRYPT_CHAINING_MODE,
                (PUCHAR)BCRYPT_CHAIN_MODE_GCM,
                sizeof(BCRYPT_CHAIN_MODE_GCM),
                0
            );
        }
    }
}

extern "C" {

JNIEXPORT jboolean JNICALL Java_fastcrypto_FastCrypto_nIsHardwareAccelerated(JNIEnv *env, jclass cls) {
    ensureAlgorithmHandle();
    return (hAesGcmAlg != NULL) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jbyteArray JNICALL Java_fastcrypto_FastCrypto_nEncryptAesGcm(
    JNIEnv *env, jclass cls, jbyteArray jKey, jbyteArray jIv, jbyteArray jPlaintext, jbyteArray jAad)
{
    ensureAlgorithmHandle();
    if (!hAesGcmAlg) return NULL;

    jsize keyLen = env->GetArrayLength(jKey);
    jsize ivLen = env->GetArrayLength(jIv);
    jsize ptLen = env->GetArrayLength(jPlaintext);

    jbyte* keyBytes = env->GetByteArrayElements(jKey, NULL);
    jbyte* ivBytes = env->GetByteArrayElements(jIv, NULL);
    jbyte* ptBytes = env->GetByteArrayElements(jPlaintext, NULL);

    jsize aadLen = (jAad != NULL) ? env->GetArrayLength(jAad) : 0;
    jbyte* aadBytes = (jAad != NULL) ? env->GetByteArrayElements(jAad, NULL) : NULL;

    BCRYPT_KEY_HANDLE hKey = NULL;
    NTSTATUS status = BCryptGenerateSymmetricKey(
        hAesGcmAlg,
        &hKey,
        NULL, 0,
        (PUCHAR)keyBytes,
        (ULONG)keyLen,
        0
    );

    jbyteArray jResult = NULL;

    if (BCRYPT_SUCCESS(status)) {
        // Prepare GCM Auth Info
        BCRYPT_AUTHENTICATED_CIPHER_MODE_INFO authInfo;
        BCRYPT_INIT_AUTH_MODE_INFO(authInfo);

        // Copy IV because BCrypt may mutate it
        UCHAR ivCopy[16];
        memcpy(ivCopy, ivBytes, (size_t)ivLen);

        UCHAR tag[16];
        authInfo.pbNonce = ivCopy;
        authInfo.cbNonce = (ULONG)ivLen;
        authInfo.pbAuthData = (PUCHAR)aadBytes;
        authInfo.cbAuthData = (ULONG)aadLen;
        authInfo.pbTag = tag;
        authInfo.cbTag = sizeof(tag);

        // Ciphertext size = plaintext size + tag (16 bytes)
        ULONG cbCiphertext = (ULONG)ptLen;
        ULONG cbResult = 0;

        PUCHAR pCiphertext = (PUCHAR)malloc(cbCiphertext > 0 ? cbCiphertext : 1);

        status = BCryptEncrypt(
            hKey,
            (PUCHAR)ptBytes,
            (ULONG)ptLen,
            &authInfo,
            NULL, 0,
            pCiphertext,
            cbCiphertext,
            &cbResult,
            0
        );

        if (BCRYPT_SUCCESS(status)) {
            jsize totalLen = (jsize)(cbResult + sizeof(tag));
            jResult = env->NewByteArray(totalLen);
            if (jResult != NULL) {
                if (cbResult > 0) {
                    env->SetByteArrayRegion(jResult, 0, (jsize)cbResult, (jbyte*)pCiphertext);
                }
                env->SetByteArrayRegion(jResult, (jsize)cbResult, (jsize)sizeof(tag), (jbyte*)tag);
            }
        }

        if (pCiphertext) {
            SecureZeroMemory(pCiphertext, cbCiphertext > 0 ? cbCiphertext : 1);
            free(pCiphertext);
        }
        BCryptDestroyKey(hKey);
    }

    env->ReleaseByteArrayElements(jKey, keyBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(jIv, ivBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(jPlaintext, ptBytes, JNI_ABORT);
    if (jAad != NULL) {
        env->ReleaseByteArrayElements(jAad, aadBytes, JNI_ABORT);
    }

    return jResult;
}

JNIEXPORT jbyteArray JNICALL Java_fastcrypto_FastCrypto_nDecryptAesGcm(
    JNIEnv *env, jclass cls, jbyteArray jKey, jbyteArray jIv, jbyteArray jCiphertextWithTag, jbyteArray jAad)
{
    ensureAlgorithmHandle();
    if (!hAesGcmAlg) return NULL;

    jsize keyLen = env->GetArrayLength(jKey);
    jsize ivLen = env->GetArrayLength(jIv);
    jsize totalCtLen = env->GetArrayLength(jCiphertextWithTag);

    if (totalCtLen < 16) return NULL;

    jsize ctLen = totalCtLen - 16;

    jbyte* keyBytes = env->GetByteArrayElements(jKey, NULL);
    jbyte* ivBytes = env->GetByteArrayElements(jIv, NULL);
    jbyte* ctBytes = env->GetByteArrayElements(jCiphertextWithTag, NULL);

    jsize aadLen = (jAad != NULL) ? env->GetArrayLength(jAad) : 0;
    jbyte* aadBytes = (jAad != NULL) ? env->GetByteArrayElements(jAad, NULL) : NULL;

    BCRYPT_KEY_HANDLE hKey = NULL;
    NTSTATUS status = BCryptGenerateSymmetricKey(
        hAesGcmAlg,
        &hKey,
        NULL, 0,
        (PUCHAR)keyBytes,
        (ULONG)keyLen,
        0
    );

    jbyteArray jResult = NULL;

    if (BCRYPT_SUCCESS(status)) {
        BCRYPT_AUTHENTICATED_CIPHER_MODE_INFO authInfo;
        BCRYPT_INIT_AUTH_MODE_INFO(authInfo);

        UCHAR ivCopy[16];
        memcpy(ivCopy, ivBytes, (size_t)ivLen);

        UCHAR tag[16];
        memcpy(tag, (const void*)(ctBytes + ctLen), 16);

        authInfo.pbNonce = ivCopy;
        authInfo.cbNonce = (ULONG)ivLen;
        authInfo.pbAuthData = (PUCHAR)aadBytes;
        authInfo.cbAuthData = (ULONG)aadLen;
        authInfo.pbTag = tag;
        authInfo.cbTag = sizeof(tag);

        PUCHAR pPlaintext = (PUCHAR)malloc(ctLen > 0 ? (size_t)ctLen : 1);
        ULONG cbResult = 0;

        status = BCryptDecrypt(
            hKey,
            (PUCHAR)ctBytes,
            (ULONG)ctLen,
            &authInfo,
            NULL, 0,
            pPlaintext,
            (ULONG)ctLen,
            &cbResult,
            0
        );

        if (BCRYPT_SUCCESS(status)) {
            jResult = env->NewByteArray((jsize)cbResult);
            if (jResult != NULL && cbResult > 0) {
                env->SetByteArrayRegion(jResult, 0, (jsize)cbResult, (jbyte*)pPlaintext);
            }
        }

        if (pPlaintext) {
            SecureZeroMemory(pPlaintext, ctLen > 0 ? (size_t)ctLen : 1);
            free(pPlaintext);
        }
        BCryptDestroyKey(hKey);
    }

    env->ReleaseByteArrayElements(jKey, keyBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(jIv, ivBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(jCiphertextWithTag, ctBytes, JNI_ABORT);
    if (jAad != NULL) {
        env->ReleaseByteArrayElements(jAad, aadBytes, JNI_ABORT);
    }

    return jResult;
}

JNIEXPORT void JNICALL Java_fastcrypto_FastCrypto_nWipe(JNIEnv *env, jclass cls, jbyteArray jSecret) {
    if (jSecret == NULL) return;
    jsize len = env->GetArrayLength(jSecret);
    if (len > 0) {
        void* ptr = env->GetPrimitiveArrayCritical(jSecret, NULL);
        if (ptr != NULL) {
            SecureZeroMemory(ptr, (size_t)len);
            env->ReleasePrimitiveArrayCritical(jSecret, ptr, 0);
        }
    }
}

}
