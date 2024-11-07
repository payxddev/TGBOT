package ru.dev.tgbot.service

import com.google.gson.JsonObject
import okio.IOException
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import ru.deelter.yookassa.YooKassa
import ru.deelter.yookassa.data.impl.*
import ru.deelter.yookassa.data.impl.requests.PaymentCreateData
import ru.dev.tgbot.config.BotConfig
import ru.dev.tgbot.entity.YPayments
import ru.dev.tgbot.repository.YPaymentsRepository
import ru.dev.tgbot.service.bot.TelegramBotService
import java.math.BigInteger

@Service
class PaymentService(
    private val botConfig: BotConfig,
    private val yPaymentsRepository: YPaymentsRepository,
    private val vpnService: VpnService,  // Сервис для создания учетной записи VPN
    @Lazy private val telegramBotService: TelegramBotService
) {

    private val yookassa: YooKassa = YooKassa.create(
        botConfig.shopId,
        botConfig.yookassaToken
    )

    fun createCustomer(email: String?): Customer {
        return Customer.builder()
            .email(email)
            .build()
    }

    fun createReceiptItem(description: String, price: Double): ReceiptItem {
        return ReceiptItem.builder()
            .description(description)
            .amount(Amount.from(price, Currency.RUB))
            .quantity(1)
            .vatCode(1)
            .build()
    }

    fun createReceipt(description: String, price: Double, email: String?): Receipt {
        return Receipt.builder()
            .items(createReceiptItem(description, price))
            .customer(createCustomer(email))
            .build()
    }

    @Throws(IOException::class)
    fun createPayment(description: String, price: Double, receipt: Receipt, tgId: BigInteger, chatId: BigInteger,tgUsername: String, callback: String): Payment {
        val metadata = JsonObject().apply {
            addProperty("tg_id", tgId.toString())
            addProperty("tg_username", tgUsername)// Преобразуем BigInteger в строку для корректного хранения в JSON
            addProperty("chat_id", chatId.toString())
            addProperty("callback", callback)
        }
        val payment = yookassa.createPayment(
            PaymentCreateData.builder()
                .amount(Amount.from(price, Currency.RUB))
                .description(description)
                .redirect("https://t.me/${botConfig.botName}")  // Этот URL можно заменить на актуальный
                .capture(true)
                .receipt(receipt)
                .metadata(metadata)
                .build()
        )

        // Сохраняем информацию о платеже
        savePayment(tgId,tgUsername, chatId, payment.id.toString(), callback)
        return payment
    }

    private fun savePayment(tgId: BigInteger, tgUsername: String, chatId: BigInteger, paymentId: String, callback: String) {
        val yPayment = YPayments(
            id = null,
            tg_id = tgId,
            paymentId = paymentId,
            tg_username = tgUsername,
            chat_id = chatId,
            callback = callback
        )
        yPaymentsRepository.save(yPayment)
    }

    fun handleSuccessfulPayment(paymentData: Map<String, Any>) {
        val tgUsername = (paymentData["metadata"] as Map<String, Any>)["tg_username"] as String
        val chatId = (paymentData["metadata"] as Map<String, Any>)["chat_id"] as String
        val callback = (paymentData["metadata"] as Map<String, Any>)["callback"] as String
        val paymentId = paymentData["id"] as String

        val payment = yPaymentsRepository.findByPaymentId(paymentId)

        payment?.let {
            // Успешная оплата, запускаем процесс создания учетной записи для VPN
            vpnService.createVpnAccount(tgUsername, callback)
            val username = tgUsername // Преобразуем tgId в строку для использования в качестве имени пользователя
            telegramBotService.sendSuccessNotification(chatId, username)

            // Обновляем статус платежа (можно добавить поле в YPayments для этого)
            it.paymentId = paymentId
            yPaymentsRepository.save(it)

        }
    }

    fun handleCanceledPayment(paymentId: String) {
        val payment = yPaymentsRepository.findByPaymentId(paymentId)
        payment?.let {
            yPaymentsRepository.delete(it)
        }
    }
}