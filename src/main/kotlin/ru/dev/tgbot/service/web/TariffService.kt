package ru.dev.tgbot.service.web

import org.springframework.stereotype.Service
import ru.dev.tgbot.entity.Tariff
import ru.dev.tgbot.repository.TariffRepository

@Service
class TariffService(private val tariffRepository: TariffRepository) {

    // Метод для получения всех тарифов
    fun getAllTariffs(): List<Tariff> {
        return tariffRepository.findAll()
    }

    // Метод для поиска тарифа по callback
    fun findByCallback(callback: String): Tariff? {
        return tariffRepository.findByCallback(callback)
    }
}