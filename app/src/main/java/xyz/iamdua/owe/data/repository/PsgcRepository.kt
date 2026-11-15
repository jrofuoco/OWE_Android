package xyz.iamdua.owe.data.repository

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PsgcItem(
    val code: String,
    val name: String
)

class PsgcRepository(context: Context) {

    companion object {
        private const val BASE_URL = "https://psgc.gitlab.io/api"
        val NCR_PROVINCE = PsgcItem(
            code = "NCR_130000000",
            name = "Metro Manila (NCR)"
        )
    }

    private val cacheDir = File(context.applicationContext.filesDir, "psgc_cache").apply {
        if (!exists()) mkdirs()
    }

    private val client = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 10_000
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private var memoryProvinces: List<PsgcItem>? = null
    private val memoryMunicipalities = mutableMapOf<String, List<PsgcItem>>()
    private val memoryBarangays = mutableMapOf<String, List<PsgcItem>>()

    private fun readDiskCache(fileName: String): List<PsgcItem>? {
        return runCatching {
            val file = File(cacheDir, fileName)
            if (!file.exists()) return null
            json.decodeFromString<List<PsgcItem>>(file.readText())
        }.getOrNull()
    }

    private fun writeDiskCache(fileName: String, items: List<PsgcItem>) {
        runCatching {
            File(cacheDir, fileName).writeText(json.encodeToString(items))
        }
    }

    suspend fun getProvinces(): Result<List<PsgcItem>> = withContext(Dispatchers.IO) {
        memoryProvinces?.let { return@withContext Result.success(it) }
        readDiskCache("provinces.json")?.let {
            memoryProvinces = it
            return@withContext Result.success(it)
        }
        runCatching {
            val raw = client.get("$BASE_URL/provinces.json").bodyAsText()
            val parsed = json.decodeFromString<List<PsgcItem>>(raw)
                .sortedBy { it.name }
            val result = listOf(NCR_PROVINCE) + parsed
            memoryProvinces = result
            writeDiskCache("provinces.json", result)
            result
        }
    }

    suspend fun getCitiesMunicipalities(provinceCode: String): Result<List<PsgcItem>> =
        withContext(Dispatchers.IO) {
            memoryMunicipalities[provinceCode]?.let { return@withContext Result.success(it) }
            val cacheFile = "muni_$provinceCode.json"
            readDiskCache(cacheFile)?.let {
                memoryMunicipalities[provinceCode] = it
                return@withContext Result.success(it)
            }
            runCatching {
                val url = if (provinceCode == NCR_PROVINCE.code) {
                    "$BASE_URL/regions/130000000/cities-municipalities.json"
                } else {
                    "$BASE_URL/provinces/$provinceCode/cities-municipalities.json"
                }
                val raw = client.get(url).bodyAsText()
                val result = json.decodeFromString<List<PsgcItem>>(raw)
                    .sortedBy { it.name }
                memoryMunicipalities[provinceCode] = result
                writeDiskCache(cacheFile, result)
                result
            }
        }

    suspend fun getBarangays(cityOrMunicipalityCode: String): Result<List<PsgcItem>> =
        withContext(Dispatchers.IO) {
            memoryBarangays[cityOrMunicipalityCode]?.let { return@withContext Result.success(it) }
            val cacheFile = "brgy_$cityOrMunicipalityCode.json"
            readDiskCache(cacheFile)?.let {
                memoryBarangays[cityOrMunicipalityCode] = it
                return@withContext Result.success(it)
            }
            runCatching {
                val raw = client.get("$BASE_URL/cities-municipalities/$cityOrMunicipalityCode/barangays.json")
                    .bodyAsText()
                val result = json.decodeFromString<List<PsgcItem>>(raw)
                    .sortedBy { it.name }
                memoryBarangays[cityOrMunicipalityCode] = result
                writeDiskCache(cacheFile, result)
                result
            }
        }
}
