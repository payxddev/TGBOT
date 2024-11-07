package ru.dev.tgbot.service.bot

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class KeyboardService {
    fun createMainMenuKeyboard(): ReplyKeyboardMarkup {
        val keyboardMarkup = ReplyKeyboardMarkup()
        keyboardMarkup.resizeKeyboard = true
        // Первая строка с одной кнопкой "Присоединиться"
        val keyboardRow1 = KeyboardRow().apply {
            add(KeyboardButton("""Присоедениться 🚀""".trimIndent()))
        }
        // Вторая строка с двумя кнопками "Моя подписка" и "Частые вопросы"
        val keyboardRow2 = KeyboardRow().apply {
            add(KeyboardButton("""Моя подписка 👤""".trimIndent()))
            add(KeyboardButton("""Нужна помощь ? 🆘""".trimIndent()))
        }
        // Добавляем строки клавиатуры в список
        keyboardMarkup.keyboard = mutableListOf(keyboardRow1, keyboardRow2)

        return keyboardMarkup
    }

}