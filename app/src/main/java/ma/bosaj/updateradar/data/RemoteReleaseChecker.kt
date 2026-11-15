package ma.bosaj.updateradar.data

import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

data class RemoteRelease(
    val source: String,
    val tagName: String,
    val normalizedVersion: String,
    val versionCode: Long?,
    val releaseNotes: String,
    val htmlUrl: String,
    val apkDownloadUrl: String?,
    val apkSizeBytes: Long,
    val sha256Hex: String?,
    val etag: String? = null,
)

/**
 * GitHub Releases API and F-Droid package index parser with HTTP ETag caching
 * and semantic version comparison (#4, #8).
 */
object RemoteReleaseChecker {
    private val etagCache = ConcurrentHashMap<String, Pair<String, RemoteRelease>>()
    private val SHA256_INLINE_RE = Regex("""(?i)(?:sha-?256\s*[:=]\s*)([0-9a-f]{64})""")

    fun normalizeVersion(rawTag: String): String =
        rawTag.trim().removePrefix("v").removePrefix("V").substringBefore("+").trim()

    /** Compares two dotted semantic version strings (e.g. "1.2.0" vs "v1.3.0-rc1"). */
    fun isRemoteNewer(installedVersion: String, remoteTag: String): Boolean {
        val currentParts = parseNumericParts(installedVersion)
        val remoteParts = parseNumericParts(remoteTag)
        val maxLen = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val r = remoteParts.getOrElse(i) { 0 }
            if (r != c) return r > c
        }
        return false
    }

    private fun parseNumericParts(version: String): List<Int> =
        normalizeVersion(version)
            .substringBefore("-")
            .split(".")
            .mapNotNull { it.trim().toIntOrNull() }

    /** Builds HTTP request headers including If-None-Match ETag and optional GitHub Bearer token. */
    fun buildRequestHeaders(cacheKey: String, authToken: String? = null): Map<String, String> {
        val headers = linkedMapOf(
            "Accept" to "application/vnd.github+json",
            "User-Agent" to "UpdateRadar-Android",
        )
        etagCache[cacheKey]?.first?.let { etag ->
            if (etag.isNotBlank()) headers["If-None-Match"] = etag
        }
        if (!authToken.isNullOrBlank()) {
            headers["Authorization"] = "Bearer ${authToken.trim()}"
        }
        return headers
    }

    /**
     * Resolves an HTTP response from GitHub Releases API (`/repos/{owner}/{repo}/releases/latest`).
     * Returns the cached release on HTTP 304 Not Modified, or parses & caches on HTTP 200.
     */
    fun resolveGitHubResponse(
        cacheKey: String,
        statusCode: Int,
        responseBody: String?,
        responseEtag: String? = null,
    ): RemoteRelease? {
        if (statusCode == 304) {
            return etagCache[cacheKey]?.second
        }
        if (statusCode != 200 || responseBody.isNullOrBlank()) return null
        val parsed = parseGitHubReleaseJson(responseBody, responseEtag)
        if (!responseEtag.isNullOrBlank()) {
            etagCache[cacheKey] = responseEtag to parsed
        }
        return parsed
    }

    fun parseGitHubReleaseJson(jsonText: String, etag: String? = null): RemoteRelease {
        val obj = JSONObject(jsonText)
        val tag = obj.optString("tag_name", "")
        val body = obj.optString("body", "")
        val htmlUrl = obj.optString("html_url", "")
        var apkUrl: String? = null
        var apkSize = 0L

        val assets = obj.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name", "")
                val url = asset.optString("browser_download_url", "")
                if (name.endsWith(".apk", ignoreCase = true) && url.isNotBlank()) {
                    apkUrl = url
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }
        }

        val sha256 = SHA256_INLINE_RE.find(body)?.groupValues?.getOrNull(1)?.lowercase()
        return RemoteRelease(
            source = "GitHub",
            tagName = tag,
            normalizedVersion = normalizeVersion(tag),
            versionCode = null,
            releaseNotes = body,
            htmlUrl = htmlUrl,
            apkDownloadUrl = apkUrl,
            apkSizeBytes = apkSize,
            sha256Hex = sha256,
            etag = etag,
        )
    }

    /** Parses F-Droid Package API JSON (`https://f-droid.org/api/v1/packages/<pkg>`). */
    fun parseFdroidPackageJson(packageName: String, jsonText: String): RemoteRelease? {
        val obj = JSONObject(jsonText)
        val suggestedCode = obj.optLong("suggestedVersionCode", -1L)
        val packages = obj.optJSONArray("packages") ?: return null
        var chosenVersionName = ""
        var chosenCode = suggestedCode

        for (i in 0 until packages.length()) {
            val pkgObj = packages.optJSONObject(i) ?: continue
            val code = pkgObj.optLong("versionCode", -1L)
            val vName = pkgObj.optString("versionName", "")
            if (code == suggestedCode || chosenVersionName.isEmpty()) {
                chosenCode = code
                chosenVersionName = vName
                if (code == suggestedCode) break
            }
        }
        if (chosenCode <= 0L && chosenVersionName.isEmpty()) return null

        val apkUrl = if (chosenCode > 0L) "https://f-droid.org/repo/${packageName}_$chosenCode.apk" else null
        return RemoteRelease(
            source = "F-Droid",
            tagName = chosenVersionName,
            normalizedVersion = normalizeVersion(chosenVersionName),
            versionCode = if (chosenCode > 0L) chosenCode else null,
            releaseNotes = "",
            htmlUrl = "https://f-droid.org/packages/$packageName/",
            apkDownloadUrl = apkUrl,
            apkSizeBytes = 0L,
            sha256Hex = null,
        )
    }

    fun clearCache() {
        etagCache.clear()
    }
}
