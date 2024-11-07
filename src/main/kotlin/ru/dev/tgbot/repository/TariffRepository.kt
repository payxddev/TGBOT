package ru.dev.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dev.tgbot.entity.Tariff

@Repository
interface TariffRepository : JpaRepository<Tariff, Long> {

    // Метод для поиска тарифа по его callback
    fun findByCallback(callback: String): Tariff?
}