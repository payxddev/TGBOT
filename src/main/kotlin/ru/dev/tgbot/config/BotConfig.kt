package ru.dev.tgbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class BotConfig(
    @Value("\${telegram.bot.token}") val botToken: String,
    @Value("\${telegram.bot.name}") val botName: String,
    @Value("\${telegram.bot.owner}") val botOwner: String,
    @Value("\${telegram.bot.customerEmail}") val customerEmail: String,
    @Value("\${telegram.bot.shopId}") val shopId: Int,
    @Value("\${telegram.bot.yookassaToken}") val yookassaToken: String,
){
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}