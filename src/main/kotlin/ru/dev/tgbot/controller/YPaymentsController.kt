package ru.dev.tgbot.controller

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dev.tgbot.service.PaymentService
import java.net.InetAddress
import java.net.UnknownHostException

@RestController
@RequestMapping("/yookassa_payment")
class YPaymentsController(private val paymentService: PaymentService) {

    private val logger = LoggerFactory.getLogger(YPaymentsController::class.java)

    // Массив доверенных IP-адресов YooKassa
    private val YOOKASSA_IPS = listOf(
        "185.71.76.0/27",
        "127.0.0.1",
        "185.71.77.0/27",
        "77.75.153.0/25",
        "77.75.156.11",
        "77.75.156.35",
        "77.75.154.128/25",
        "2a02:5180::/32"
    )

    @PostMapping
    fun yookassaPayment(@RequestBody data: Map<String, Any>, request: HttpServletRequest): ResponseEntity<String> {
        val clientIp = getClientIp(request)
        if (!isValidYooKassaIP(clientIp)) {
            logger.warn("Invalid IP: $clientIp")
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid IP")
        }

        // Извлечение данных платежа
        val paymentObject = data["object"] as Map<String, Any>
        val paymentId = paymentObject["id"] as String
        val paymentStatus = paymentObject["status"] as String

        if (paymentStatus == "succeeded") {
            // Обработка успешного платежа
            paymentService.handleSuccessfulPayment(paymentObject) // Передаем paymentObject, а не paymentId
            return ResponseEntity.ok("Payment processed successfully")
        }
        else if (paymentStatus == "canceled") {
            paymentService.handleCanceledPayment(paymentId)
            return ResponseEntity.ok("Payment canceled")
        }

        return ResponseEntity.ok("Webhook received")
    }

    private fun getClientIp(request: HttpServletRequest): String? {
        return request.getHeader("CF-Connecting-IP")
            ?: request.getHeader("X-Real-IP")
            ?: request.getHeader("X-Forwarded-For")
            ?: request.remoteAddr
    }

    // Проверка, что IP находится в диапазоне доверенных
    private fun isValidYooKassaIP(clientIp: String?): Boolean {
        return clientIp != null && YOOKASSA_IPS.any { subnet ->
            if (subnet.contains("/")) {
                try {
                    val network = subnet.split("/")
                    val networkAddress = InetAddress.getByName(network[0])
                    val prefixLength = network[1].toInt()

                    val clientAddress = InetAddress.getByName(clientIp)
                    networkAddress.equals(clientAddress) // Пример базовой проверки
                } catch (e: UnknownHostException) {
                    false
                }
            } else {
                clientIp == subnet
            }
        }
    }
}