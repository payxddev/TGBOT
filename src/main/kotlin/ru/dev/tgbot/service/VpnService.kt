package ru.dev.tgbot.service

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.slf4j.LoggerFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import java.math.BigInteger
import java.time.Instant

@Service
class VpnService(private val restTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(VpnService::class.java)

    // Конфигурации VPN панели
    private val vpnApiUrl: String = "https://devdark.ru"
    private val username: String = "nemeuslion"
    private val password: String = "Zergimba69"

    private lateinit var token: String

    init {
        token = getToken().toString()
    }

    // Метод для получения токена
    final fun getToken(): Any {
        val data: MultiValueMap<String, String> = LinkedMultiValueMap()
        data.add("username", username)
        data.add("password", password)
        val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
            "$vpnApiUrl/api/admin/token", data, Map::class.java
        )
        return response.body?.get("access_token") ?: throw Exception("No access token returned")
    }

    // Метод для проверки существования пользователя
    fun checkIfUserExists(username: String): Boolean {
        return try {
            val headers = HttpHeaders().apply { set("Authorization", "Bearer $token") }
            val response = restTemplate.exchange(
                "$vpnApiUrl/api/user/$username",
                HttpMethod.GET,
                HttpEntity(null, headers),
                Map::class.java
            )
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // Метод для добавления пользователя
    fun addUser(data: Map<String, Any>): Map<*, *>? {
        val headers = HttpHeaders().apply { set("Authorization", "Bearer $token") }
        val response = restTemplate.exchange(
            "$vpnApiUrl/api/user",
            HttpMethod.POST,
            HttpEntity(data, headers),
            Map::class.java
        )
        return response.body
    }

    // Метод для модификации пользователя
    fun modifyUser(username: String, data: Map<String, Any>): Map<*, *>? {
        val headers = HttpHeaders().apply { set("Authorization", "Bearer $token") }
        val response = restTemplate.exchange(
            "$vpnApiUrl/api/user/$username",
            HttpMethod.PUT,
            HttpEntity(data, headers),
            Map::class.java
        )
        return response.body
    }
    fun createVpnAccount(tgUsername: String, callback: String) {
        val username = tgUsername // Преобразуем tgId в строку для использования в качестве имени пользователя
        val userExists = checkIfUserExists(username)
        val userData: MutableMap<String, Any>

        // Извлечение количества месяцев из строки callback
        val months = extractMonthsFromCallback(callback)

        if (userExists) {
            // Если пользователь существует, получаем его данные
            userData = getUserData(username)!!
            userData["status"] = "active"
            userData["expire"] = when (val expire = userData["expire"]) {
                is Number -> {
                    val expireTime = expire.toLong()
                    if (expireTime < Instant.now().epochSecond) {
                        getSubscriptionEndDate(months) // Передаем количество месяцев
                    } else {
                        expireTime + getSubscriptionEndDate(months, additional = true)
                    }
                }
                else -> getSubscriptionEndDate(months)  // Если нет даты, создаем новую подписку
            }
            modifyUser(username, userData)
        } else {
            // Если пользователь не существует, создаем нового
            userData = mutableMapOf(
                "username" to username,
                "proxies" to getProxies(),
                "inbounds" to getInbounds(),
                "expire" to getSubscriptionEndDate(months),
                "data_limit" to 0,
                "data_limit_reset_strategy" to "no_reset"
            )
            addUser(userData)
        }
    }
    private fun extractMonthsFromCallback(callback: String): Int {
        return callback.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
    }
    // Получение данных пользователя
    fun getUserData(username: String): MutableMap<String, Any>? {
        val headers = HttpHeaders().apply { set("Authorization", "Bearer $token") }
        return try {
            val response = restTemplate.exchange(
                "$vpnApiUrl/api/user/$username",
                HttpMethod.GET,
                HttpEntity(null, headers),
                Map::class.java
            )
            response.body as MutableMap<String, Any>
        } catch (ex: HttpClientErrorException.NotFound) {
            // Логируем ошибку, если пользователь не найден
            logger.warn("Пользователь не найден: $username")
            null
        } catch (ex: Exception) {
            // Логируем другие возможные ошибки
            logger.error("Ошибка при получении данных пользователя: ${ex.message}")
            null
        }
    }

    // Получение конфигурации прокси и inbounds
    fun getProxies(): Map<String, Any> {
        return mapOf(
            "vless" to mapOf("flow" to "xtls-rprx-vision"),
        )
    }

    fun getInbounds(): Map<String, List<String>> {
        return mapOf(
            "vless" to listOf("VLESS TCP REALITY"),
        )
    }

    // Метод для получения времени окончания подписки
    fun getSubscriptionEndDate(months: Int, additional: Boolean = false): Long {
        val currentTime = Instant.now().epochSecond
        return (if (additional) 0 else currentTime) + 60 * 60 * 24 * 30 * months
    }
}
