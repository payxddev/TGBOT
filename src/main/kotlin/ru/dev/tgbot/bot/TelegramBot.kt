package ru.dev.tgbot.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.dev.tgbot.config.BotConfig

@Component
class TelegramBot(private val botConfig: BotConfig) : TelegramWebhookBot() {

    override fun getBotToken(): String = botConfig.botToken

    override fun getBotUsername(): String = botConfig.botName

    override fun getBotPath(): String = "/webhook"

    override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*>? {
        // Здесь обработка идет через WebhookController
        return null
    }

}