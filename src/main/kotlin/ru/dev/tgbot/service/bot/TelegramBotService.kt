package ru.dev.tgbot.service.bot


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo
import ru.dev.tgbot.bot.TelegramBot

import ru.dev.tgbot.config.BotConfig
import ru.dev.tgbot.service.PaymentService
import ru.dev.tgbot.service.VpnService
import ru.dev.tgbot.service.web.TariffService
import java.math.BigInteger


@Service
class TelegramBotService(
    private val keyboardService: KeyboardService,
    private val tariffService: TariffService,
    private val paymentService: PaymentService,
    private val botConfig: BotConfig,
    private val vpnService: VpnService
) {
    @Autowired
    private lateinit var telegramBot: TelegramBot
    fun handleUpdate(update: Update): SendMessage {
        if (update.hasCallbackQuery()) {
            return handleCallback(update)
        }
        val chatId = update.message.chatId.toString()
        val sender = update.message.from?.firstName + " (" + update.message.from?.userName + ")"
        val messageText = update.message.text

        val responseMessage = SendMessage(chatId, "")


        when (messageText) {
            "Присоедениться 🚀".trimIndent() -> {
                val tariffs = tariffService.getAllTariffs()
                val inlineKeyboardMarkup = InlineKeyboardMarkup()
                val keyboardButtons = tariffs.map { tariff ->
                    val button = InlineKeyboardButton(tariff.title)
                    button.callbackData = tariff.callback
                    listOf(button)
                }
                inlineKeyboardMarkup.keyboard = keyboardButtons
                responseMessage.text = "Выберите тариф:"
                responseMessage.replyMarkup = inlineKeyboardMarkup
            }
            "Моя подписка 👤".trimIndent() -> {
                val username = update.message.from.userName  // Используем Telegram ID как имя пользователя
                val userData = vpnService.getUserData(username)

                if (userData != null) {
                    val subscriptionUrl = userData["subscription_url"] as? String ?: "https://google.com"
                    responseMessage.text = "Ваша страница подписки ⬇\uFE0F"
                    responseMessage.replyMarkup = getSubscriptionKeyboard(subscriptionUrl)
                } else {
                    responseMessage.text = "Пользователь с таким идентификатором не найден. Пожалуйста, проверьте данные и попробуйте снова."
                }
            }
            "Нужна помощь ? 🆘".trimIndent() -> {
                val channelLink = "https://t.me/nsk_vpn" // Это ссылка на ваш канал из конфигурации

                responseMessage.text = """
        Перейдите по <a href="$channelLink">ссылке</a> и задайте нам вопрос. Мы всегда рады помочь 🤗
    """.trimIndent()

                responseMessage.enableHtml(true)  // Включаем поддержку HTML для ссылки

            }
            "/start" -> {
                responseMessage.text = "Здравствуйте, $sender \uD83D\uDC4B\uD83C\uDFFB\n\nВыберите действие ⬇️"
                responseMessage.replyMarkup = keyboardService.createMainMenuKeyboard()
            }
            else -> {
                responseMessage.text = "Я не понимаю, выберите действие из клавиатуры."
                responseMessage.replyMarkup = keyboardService.createMainMenuKeyboard()
            }
        }

        return responseMessage
    }

    fun handleCallback(update: Update): SendMessage {
            val callbackQuery = update.callbackQuery
            val chatId = callbackQuery.message.chatId.toString()
            val callbackData = callbackQuery.data
            val tgid = callbackQuery.from?.id?.toBigInteger()?: BigInteger.ZERO
            val tgUsername = callbackQuery.from?.userName ?: ""
            val responseMessage = SendMessage(chatId, "")

        val tariff = tariffService.findByCallback(callbackData)

        if (tariff != null) {
            val receipt = paymentService.createReceipt("f\"Подписка на VPN сервис: " + tariff.title, tariff.price.toDouble(), botConfig.customerEmail)
            val payment = paymentService.createPayment(
                description = "Подписка на VPN сервис: ${tariff.title}",
                price = tariff.price.toDouble(),
                receipt = receipt,
                tgId = tgid,
                tgUsername = tgUsername,// Это переменные с реальными данными
                chatId = chatId.toBigInteger(),
                callback = tariff.callback
            )
            responseMessage.text = """
                Вы выбрали тариф: ${tariff.title}.
                Цена: ${tariff.price} руб./ ${tariff.months} мес.
            """.trimIndent()

            val inlineKeyboardMarkup = InlineKeyboardMarkup()
            val button = InlineKeyboardButton("Оплатить").apply {
                webApp = WebAppInfo(payment.confirmation.url)
            }
            inlineKeyboardMarkup.keyboard = listOf(listOf(button))

            responseMessage.replyMarkup = inlineKeyboardMarkup
        } else {
            responseMessage.text = "Неизвестный тариф."
        }

        return responseMessage
    }
    fun getSubscriptionKeyboard(subscriptionUrl: String): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val button = InlineKeyboardButton("Перейти 🔗").apply {
            webApp = WebAppInfo(subscriptionUrl)
        }
        inlineKeyboardMarkup.keyboard = listOf(listOf(button))
        return inlineKeyboardMarkup
    }
    fun sendSuccessNotification(chatId: String, tgUsername: String) {
        val userData = vpnService.getUserData(tgUsername)
        val subscriptionUrl = userData?.get("subscription_url") as? String ?: "https://google.com" // URL по умолчанию

        val message = """
        ✅ Ваш платеж успешно обработан!
        Ваш VPN аккаунт создан и готов к использованию.
        Если у вас возникнут вопросы, пожалуйста, свяжитесь с поддержкой.
        Ваша страница подписки ⬇️
    """.trimIndent()

        val sendMessage = SendMessage(chatId, message)
        sendMessage.replyMarkup = getSubscriptionKeyboard(subscriptionUrl) // Добавляем клавиатуру

        try {
            telegramBot.execute(sendMessage) // Отправляем сообщение
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}