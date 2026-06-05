package pat.project.tlsandssl.lab

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request

data class CertificateInfo(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validUntil: String,
    val publicKeyAlgorithm: String,
    val signatureAlgorithm: String,
    val serialNumber: String,
    val fingerprintSha256: String,
    val publicKeyPin: String,
    val validity: String
)

data class InspectionResult(
    val url: String,
    val host: String,
    val statusCode: Int,
    val tlsVersion: String,
    val cipherSuite: String,
    val certificates: List<CertificateInfo>
)

enum class FailureKind { INVALID_URL, CERTIFICATE, HOSTNAME, NEGOTIATION, TIMEOUT, NETWORK, UNKNOWN }

data class LabFailure(val kind: FailureKind, val title: String, val detail: String)

sealed interface LabResult<out T> {
    data class Success<T>(val value: T) : LabResult<T>
    data class Failure(val error: LabFailure) : LabResult<Nothing>
}

object UrlPolicy {
    fun validate(raw: String): LabFailure? {
        val uri = runCatching { URI(raw.trim()) }.getOrNull()
            ?: return LabFailure(FailureKind.INVALID_URL, "Некорректный URL", "Введите полный HTTPS URL.")
        if (!uri.scheme.equals("https", ignoreCase = true)) {
            return LabFailure(FailureKind.INVALID_URL, "Разрешён только HTTPS", "Cleartext HTTP исключён из безопасной лаборатории.")
        }
        if (uri.host.isNullOrBlank()) {
            return LabFailure(FailureKind.INVALID_URL, "Не найден хост", "Например: https://example.com")
        }
        return null
    }
}

class TlsInspector(
    private val baseClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    suspend fun inspect(rawUrl: String): LabResult<InspectionResult> = withContext(Dispatchers.IO) {
        UrlPolicy.validate(rawUrl)?.let { return@withContext LabResult.Failure(it) }
        execute(baseClient, rawUrl.trim())
    }

    suspend fun verifyPin(result: InspectionResult, matching: Boolean): LabResult<String> = withContext(Dispatchers.IO) {
        val observed = result.certificates.firstOrNull()?.publicKeyPin
            ?: return@withContext LabResult.Failure(LabFailure(FailureKind.CERTIFICATE, "Нет сертификата", "Сначала выполните инспекцию."))
        val pin = if (matching) observed else "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        val client = baseClient.newBuilder()
            .certificatePinner(CertificatePinner.Builder().add(result.host, pin).build())
            .build()
        when (val response = execute(client, result.url)) {
            is LabResult.Success -> LabResult.Success(if (matching) "Pin совпал: соединение разрешено." else "Неожиданно: неверный pin принят.")
            is LabResult.Failure -> if (!matching && response.error.kind == FailureKind.CERTIFICATE) {
                LabResult.Success("Ожидаемый отказ: неверный pin заблокировал соединение.")
            } else response
        }
    }

    private fun execute(client: OkHttpClient, url: String): LabResult<InspectionResult> = try {
        client.newCall(Request.Builder().url(url).head().build()).execute().use { response ->
            val handshake = response.handshake
                ?: return LabResult.Failure(LabFailure(FailureKind.NEGOTIATION, "Нет TLS handshake", "Сервер не вернул TLS metadata."))
            LabResult.Success(
                InspectionResult(
                    url = url,
                    host = response.request.url.host,
                    statusCode = response.code,
                    tlsVersion = handshake.tlsVersion.javaName,
                    cipherSuite = handshake.cipherSuite.javaName,
                    certificates = handshake.peerCertificates.mapNotNull { it.toInfoOrNull() }
                )
            )
        }
    } catch (error: Throwable) {
        LabResult.Failure(error.toLabFailure())
    }
}

fun Certificate.toInfoOrNull(): CertificateInfo? {
    val certificate = this as? X509Certificate ?: return null
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val validity = try {
        certificate.checkValidity()
        "Действителен"
    } catch (_: CertificateExpiredException) {
        "Истёк"
    } catch (_: CertificateNotYetValidException) {
        "Ещё не действует"
    }
    val fingerprint = MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
        .joinToString(":") { "%02X".format(it) }
    return CertificateInfo(
        subject = certificate.subjectX500Principal.name,
        issuer = certificate.issuerX500Principal.name,
        validFrom = format.format(certificate.notBefore),
        validUntil = format.format(certificate.notAfter),
        publicKeyAlgorithm = certificate.publicKey.algorithm,
        signatureAlgorithm = certificate.sigAlgName,
        serialNumber = certificate.serialNumber.toString(16).uppercase(),
        fingerprintSha256 = fingerprint,
        publicKeyPin = CertificatePinner.pin(certificate),
        validity = validity
    )
}

fun Throwable.toLabFailure(): LabFailure = when (this) {
    is SocketTimeoutException -> LabFailure(FailureKind.TIMEOUT, "Таймаут", "Сервер не ответил за ограниченное время.")
    is SSLPeerUnverifiedException -> LabFailure(
        FailureKind.CERTIFICATE,
        "Сертификат или pin отклонён",
        message ?: "Peer identity не прошла проверку."
    )
    is SSLHandshakeException -> {
        val detail = message.orEmpty()
        val kind = if ("hostname" in detail.lowercase()) FailureKind.HOSTNAME else FailureKind.CERTIFICATE
        LabFailure(kind, "TLS-проверка отклонена", detail.ifBlank { "Сертификат, имя хоста или параметры TLS не прошли проверку." })
    }
    is IOException -> LabFailure(FailureKind.NETWORK, "Ошибка сети", message ?: "Проверьте подключение и адрес.")
    else -> LabFailure(FailureKind.UNKNOWN, "Не удалось выполнить запрос", message ?: javaClass.simpleName)
}
