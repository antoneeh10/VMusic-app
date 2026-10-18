/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val published_at: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: JSONArray
) {
    val tagName: String get() = tag_name
}

object VMusicReleaseRepository {
    private val client = HttpClient()
    private val mutex = Mutex()
    private var cachedStableRelease: ReleaseInfo? = null
    private const val GITHUB_RELEASES_API = "https://api.github.com/repos/antoneeh10/VMusic-app/releases"

    /**
     * Fetches the latest stable GitHub release using Semantic Versioning (x.x.x).
     * Implements Mutex for single scan / memory cache to prevent redundant HTTP requests.
     */
    suspend fun getLatestStableRelease(forceRefresh: Boolean = false): Result<ReleaseInfo> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                runCatching {
                    // 3. Single Scan Mechanism: return cached if available
                    if (cachedStableRelease != null && !forceRefresh) {
                        return@runCatching cachedStableRelease!!
                    }

                    val response = client.get(GITHUB_RELEASES_API).bodyAsText()
                    val jsonArray = JSONArray(response)
                    val releases = mutableListOf<GitHubRelease>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        releases.add(
                            GitHubRelease(
                                tag_name = obj.optString("tag_name"),
                                name = obj.optString("name", obj.optString("tag_name")),
                                body = obj.optString("body"),
                                published_at = obj.optString("published_at"),
                                draft = obj.optBoolean("draft", false),
                                prerelease = obj.optBoolean("prerelease", false),
                                assets = obj.optJSONArray("assets") ?: JSONArray()
                            )
                        )
                    }

                    // 1, 2. Version Format Validation (x.x.x) & API Filter using firstOrNull
                    val versionRegex = Regex("^v?\\d+\\.\\d+\\.\\d+$")
                    val stableRelease = releases.firstOrNull { release ->
                        !release.draft && !release.prerelease && release.tagName.matches(versionRegex)
                    } ?: throw Exception("No stable semantic version release found")

                    val cleanVersion = Updater.extractVersionNumber(stableRelease.tag_name)
                    val releaseInfo = ReleaseInfo(
                        tagName = stableRelease.tag_name,
                        versionName = cleanVersion,
                        description = stableRelease.body,
                        releaseDate = stableRelease.published_at,
                        assets = Updater.parseAssets(stableRelease.assets)
                    )

                    cachedStableRelease = releaseInfo
                    releaseInfo
                }
            }
        }
}
