package com.richbars.moraisdabet.infrastructure.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.security.SecureRandom
import javax.net.ssl.*

@Service
open class HttpClientManager {

    private var httpClient: OkHttpClient? = null
    private var tlsClient: OkHttpClient? = null

    /** Inicializa a sessão HTTP ou HTTPS */
    suspend fun startSession(forceTLS: Boolean = false) = withContext(Dispatchers.IO) {
        if (forceTLS) {
            if (tlsClient == null) tlsClient = buildTlsClient()
        } else {
            if (httpClient == null) httpClient = OkHttpClient()
        }
    }

    /** Cria um cliente que ignora certificados SSL */
    private fun buildTlsClient(): OkHttpClient {
        // Trust manager que aceita todos (igual ao que tinha)
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        val sslSocketFactory = sslContext.socketFactory
        val trustManager = trustAllCerts[0] as X509TrustManager

        // ConnectionSpec com TLS modernos
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            // Opcional: configurar cipher suites (padrões OkHttp já são geralmente bons)
            .build()

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(15))
            .readTimeout(java.time.Duration.ofSeconds(30))
            .writeTimeout(java.time.Duration.ofSeconds(30))
            .build()
    }


    /** GET request com headers e query params */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        forceTLS: Boolean = false
    ): Response = withContext(Dispatchers.IO) {

        val fullUrl = buildUrlWithParams(url, queryParams)
        val client = getClient(forceTLS)

        val request = Request.Builder()
            .url(fullUrl)
            .headers(headers.toHeaders())
            .get()
            .build()

        val response = client.newCall(request).execute()
        return@withContext response
    }


    /** POST request com body, headers e query params */
    suspend fun post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
        queryParams: Map<String, String> = emptyMap(),
        forceTLS: Boolean = false
    ): Response = withContext(Dispatchers.IO) {

        val fullUrl = buildUrlWithParams(url, queryParams)
        val client = getClient(forceTLS)

        val requestBody = body.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(fullUrl)
            .headers(headers.toHeaders())
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        return@withContext response
    }


    /** Garante que o cliente correto (HTTP ou TLS) está pronto */
    private fun getClient(forceTLS: Boolean): OkHttpClient {
        return if (forceTLS) {
            tlsClient ?: buildTlsClient().also { tlsClient = it }
        } else {
            httpClient ?: OkHttpClient().also { httpClient = it }
        }
    }

    /** Monta a URL com parâmetros de query */
    private fun buildUrlWithParams(url: String, queryParams: Map<String, String>): String {
        if (queryParams.isEmpty()) return url

        val encodedParams = queryParams.map { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }.joinToString("&")

        return if (url.contains("?")) "$url&$encodedParams" else "$url?$encodedParams"
    }


    suspend fun webClientGet(
        url: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        timeoutSeconds: Long = 30
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fullUrl = buildUrlWithParams(url, queryParams)

            // Configura um HttpClient do Reactor (poderíamos customizar timeouts, etc.)
            val reactorHttpClient = HttpClient.create()
                .compress(true) // habilita compressão (gzip/br) se suportado
                .responseTimeout(java.time.Duration.ofSeconds(timeoutSeconds))

            val webClient = WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(reactorHttpClient))
                .baseUrl("") // usaremos a URL completa abaixo
                .build()

            // Monta a request adicionando headers
            val requestSpec = webClient.get()
                .uri(fullUrl)

            headers.forEach { (k, v) ->
                requestSpec.header(k, v)
            }

            // Executa e bloqueia na thread de IO (aceitável dentro de withContext(Dispatchers.IO))
            val bodyMono = requestSpec.retrieve()
                .bodyToMono(String::class.java)

            // await/block para obter resultado (estamos dentro de Dispatchers.IO)
            return@withContext bodyMono.block()
        } catch (e: Exception) {
            // Logue conforme seu logger (aqui apenas print para exemplo)
            println("webClientGet error for $url: ${e.message}")
            null
        }
    }

}
