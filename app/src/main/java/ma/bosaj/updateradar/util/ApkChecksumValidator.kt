package ma.bosaj.updateradar.util

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 * Cryptographic SHA-256 APK integrity verifier prior to launching PackageInstaller (#11).
 */
object ApkChecksumValidator {
    private const val BUFFER_SIZE = 64 * 1024
    private val HEX_CHARS = "0123456789abcdef".toCharArray()
    private val SHA256_HEX_RE = Regex("^[0-9a-fA-F]{64}$")

    fun computeSha256(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val read = stream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return toHex(digest.digest())
    }

    fun computeSha256(file: File): String =
        file.inputStream().buffered().use { computeSha256(it) }

    /**
     * Verifies [file] against [expectedSha256Hex] using constant-time byte comparison.
     */
    fun verifyApkFile(file: File, expectedSha256Hex: String): Boolean {
        val cleaned = expectedSha256Hex.trim().removePrefix("sha256:").trim().lowercase()
        if (!SHA256_HEX_RE.matches(cleaned) || !file.isFile) return false
        val actual = computeSha256(file)
        return MessageDigest.isEqual(
            actual.toByteArray(Charsets.US_ASCII),
            cleaned.toByteArray(Charsets.US_ASCII),
        )
    }

    /**
     * Extracts the expected SHA-256 hex digest for [apkFileName] from a GNU coreutils
     * `SHA256SUMS` text file (`<64-hex>  <filename>` or `<64-hex> *<filename>`).
     */
    fun extractFromSha256Sums(sumsContent: String, apkFileName: String): String? {
        for (line in sumsContent.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            val parts = trimmed.split(Regex("""\s+"""), limit = 2)
            if (parts.size == 2) {
                val hash = parts[0].trim().lowercase()
                val name = parts[1].trim().removePrefix("*").substringAfterLast('/')
                if (name.equals(apkFileName, ignoreCase = true) && SHA256_HEX_RE.matches(hash)) {
                    return hash
                }
            } else if (parts.size == 1 && SHA256_HEX_RE.matches(parts[0])) {
                return parts[0].lowercase()
            }
        }
        return null
    }

    private fun toHex(bytes: ByteArray): String {
        val out = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            out[i * 2] = HEX_CHARS[v ushr 4]
            out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(out)
    }
}
