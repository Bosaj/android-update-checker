package ma.bosaj.updateradar

import java.nio.file.Files
import ma.bosaj.updateradar.data.RemoteReleaseChecker
import ma.bosaj.updateradar.ui.components.UpdatePromptMode
import ma.bosaj.updateradar.ui.components.UpdatePromptState
import ma.bosaj.updateradar.util.ApkChecksumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAndSecurityTest {
    @Test
    fun parsesGitHubReleasesAndCachesByEtag() {
        RemoteReleaseChecker.clearCache()
        val sampleHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val json = """
            {
              "tag_name": "v1.4.0",
              "html_url": "https://github.com/Bosaj/android-update-checker/releases/tag/v1.4.0",
              "body": "### Changes\n- Added F-Droid & GitHub check\nSHA-256: $sampleHash",
              "assets": [
                {
                  "name": "update-radar-v1.4.0.apk",
                  "browser_download_url": "https://github.com/Bosaj/android-update-checker/releases/download/v1.4.0/update-radar-v1.4.0.apk",
                  "size": 4194304
                }
              ]
            }
        """.trimIndent()

        val key = "Bosaj/android-update-checker"
        val initialHeaders = RemoteReleaseChecker.buildRequestHeaders(key, authToken = "ghp_test123")
        assertFalse(initialHeaders.containsKey("If-None-Match"))
        assertEquals("Bearer ghp_test123", initialHeaders["Authorization"])

        val release = RemoteReleaseChecker.resolveGitHubResponse(key, 200, json, responseEtag = "W/\"etag-140\"")
        assertNotNull(release)
        assertEquals("1.4.0", release!!.normalizedVersion)
        assertEquals(sampleHash, release.sha256Hex)
        assertEquals(4194304L, release.apkSizeBytes)
        assertTrue(RemoteReleaseChecker.isRemoteNewer("1.2.0", release.tagName))
        assertFalse(RemoteReleaseChecker.isRemoteNewer("1.4.0", release.tagName))

        // Subsequent request sends If-None-Match and resolves 304 Not Modified from cache
        val cachedHeaders = RemoteReleaseChecker.buildRequestHeaders(key)
        assertEquals("W/\"etag-140\"", cachedHeaders["If-None-Match"])
        val from304 = RemoteReleaseChecker.resolveGitHubResponse(key, 304, null)
        assertEquals(release, from304)
    }

    @Test
    fun parsesFdroidPackageIndexJson() {
        val fdroidJson = """
            {
              "packageName": "ma.bosaj.updateradar",
              "suggestedVersionCode": 6,
              "packages": [
                { "versionName": "1.3.0", "versionCode": 6 },
                { "versionName": "1.2.0", "versionCode": 4 }
              ]
            }
        """.trimIndent()
        val parsed = RemoteReleaseChecker.parseFdroidPackageJson("ma.bosaj.updateradar", fdroidJson)
        assertNotNull(parsed)
        assertEquals("F-Droid", parsed!!.source)
        assertEquals("1.3.0", parsed.normalizedVersion)
        assertEquals(6L, parsed.versionCode)
        assertEquals("https://f-droid.org/repo/ma.bosaj.updateradar_6.apk", parsed.apkDownloadUrl)
    }

    @Test
    fun validatesApkSha256ChecksumBeforeInstallation() {
        val tempApk = Files.createTempFile("sample-release", ".apk").toFile()
        tempApk.writeText("dummy-apk-binary-payload-v1.4.0")
        val actualHash = ApkChecksumValidator.computeSha256(tempApk)
        assertEquals(64, actualHash.length)

        assertTrue(ApkChecksumValidator.verifyApkFile(tempApk, actualHash))
        assertTrue(ApkChecksumValidator.verifyApkFile(tempApk, "sha256:${actualHash.uppercase()}"))
        assertFalse(ApkChecksumValidator.verifyApkFile(tempApk, "0".repeat(64)))

        val sumsFile = """
            # Release SHA256SUMS
            $actualHash *update-radar-v1.4.0.apk
        """.trimIndent()
        assertEquals(actualHash, ApkChecksumValidator.extractFromSha256Sums(sumsFile, "update-radar-v1.4.0.apk"))
        assertNull(ApkChecksumValidator.extractFromSha256Sums(sumsFile, "other.apk"))
        tempApk.delete()
    }

    @Test
    fun computesMaterial3UpdateDialogProgressState() {
        val downloading = UpdatePromptState(
            appLabel = "Update Radar",
            currentVersion = "1.2.0",
            availableVersion = "1.4.0",
            mode = UpdatePromptMode.IMMEDIATE,
            bytesDownloaded = 2_097_152L,
            totalBytes = 4_194_304L,
            isDownloading = true,
            sha256Verified = true,
        )
        assertEquals(50, downloading.progressPercent)
        assertFalse(downloading.isReadyToInstall)

        val completed = downloading.copy(bytesDownloaded = 4_194_304L, isDownloading = false)
        assertEquals(100, completed.progressPercent)
        assertTrue(completed.isReadyToInstall)
    }
}
