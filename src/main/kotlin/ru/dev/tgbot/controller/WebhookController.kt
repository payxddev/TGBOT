package ru.dev.tgbot.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.dev.tgbot.service.bot.TelegramBotService


@RestController
@RequestMapping("/webhook")
class WebhookController(private val telegramBotService: TelegramBotService) {

    @PostMapping
    fun onUpdateReceived(@RequestBody update: Update): SendMessage {
        return telegramBotService.handleUpdate(update)
    }
}