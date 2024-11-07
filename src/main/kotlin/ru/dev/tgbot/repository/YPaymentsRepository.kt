package ru.dev.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.dev.tgbot.entity.YPayments

@Repository
interface YPaymentsRepository: JpaRepository<YPayments, Long>{
    fun findByPaymentId(paymentId: String) : YPayments?
}
