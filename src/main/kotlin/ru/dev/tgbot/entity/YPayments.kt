package ru.dev.tgbot.entity

import jakarta.persistence.*
import java.math.BigInteger

@Entity
@Table(name = "y_payments")
data class YPayments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(name = "tg_id")
    var tg_id: BigInteger?,
    @Column(name = "tg_username")
    var tg_username: String?,
    @Column(name = "payment_id")
    var paymentId: String?,
    @Column(name = "chat_id")
    var chat_id: BigInteger?,
    @Column(name = "callback")
    var callback: String?

) {
    constructor() : this(
        id = null,
        tg_id = null,
        paymentId = null,
        tg_username = null,
        chat_id = null,
        callback = null,
    )
}