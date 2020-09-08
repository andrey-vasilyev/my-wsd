package io.vasianda.wsd

import android.content.SharedPreferences
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import java.math.BigDecimal
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

interface BalanceService {
    suspend fun getBalance(login: String, password: CharArray): Result<BigDecimal, String>
}

@Singleton
class RemoteBalanceService @Inject constructor() : BalanceService {

    private val url: String = "https://cabinet.nch-spb.com"

    private val client = HttpClient(Android) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpCookies) {
            storage = MyCookiesStorage(AcceptAllCookiesStorage())
        }
        install(HttpRedirect) { }
        followRedirects = true
        BrowserUserAgent()
    }

    override suspend fun getBalance(login: String, password: CharArray): Result<BigDecimal, String> {
        @Suppress("BlockingMethodInNonBlockingContext")
        val payload = "login=${URLEncoder.encode(login, "UTF-8")}&password=${URLEncoder.encode(String(password), "UTF-8")}&submit=%D0%92%D1%85%D0%BE%D0%B4"
        password.fill('0')
        val html = try {
            val post = client.post<HttpResponse>("${url}/onyma/") {
                contentType(ContentType.Application.FormUrlEncoded)
                body = payload
            }
            if (post.status != HttpStatusCode.Found) {
                return Err("Login failed")
            }
            client.get<HttpResponse>("${url}${post.headers["Location"]}")
        } catch (e: Exception) {
            return Err("Connection error: ${e.localizedMessage}")
        }
        val found = Jsoup.parse(html.readText(), url).select(".balance-value")
        if (found.size == 0) {
            return Err("Login or html parsing failed")
        }
        return Ok(BigDecimal(found[0].text()))
    }

    //workaround KTOR-917
    private class MyCookiesStorage(private val cookiesStorage: CookiesStorage) : CookiesStorage by cookiesStorage {
        override suspend fun get(requestUrl: Url): List<Cookie> =
            cookiesStorage.get(requestUrl).map { it.copy(encoding = CookieEncoding.RAW) }
    }
}