package ru.dev.tgbot.entity

import jakarta.persistence.*

@Entity
@Table(name = "tariffs")
data class Tariff(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? ,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val price: Long,

    @Column(nullable = false)
    val callback: String,

    @Column(nullable = false)
    val months: Int
) {
    constructor() : this(
        id = null,
        title = "",
        price = 0,
        callback = "",
        months = 0,
    )
}