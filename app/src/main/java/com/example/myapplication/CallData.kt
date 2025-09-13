package com.example.myapplication

import java.time.Instant

object CallData {
    var callList = mutableListOf(
        CallClass(R.drawable.not_call,"Алина (5)", Instant.parse("2025-08-22T22:16:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Максим Анатольевич", Instant.parse("2025-08-22T19:22:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Рамиль", Instant.parse("2025-08-22T16:30:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Василий", Instant.parse("2025-08-22T13:48:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-22T11:05:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Валера Воротников", Instant.parse("2025-08-21T18:52:00+03:00").toEpochMilli()),
        CallClass(R.drawable.up_call,"Алина", Instant.parse("2025-08-21T14:35:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Алина", Instant.parse("2025-08-21T11:07:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Сергей Иванович", Instant.parse("2025-08-21T09:18:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Игорь КамАЗ (2)", Instant.parse("2025-08-20T15:32:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-20T11:10:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Дима", Instant.parse("2025-08-19T18:52:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Максим Анатольевич (2)", Instant.parse("2025-08-19T14:35:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Костя", Instant.parse("2025-08-19T11:07:00+03:00").toEpochMilli()),
        CallClass(R.drawable.up_call,"Саша Алкаш", Instant.parse("2025-08-19T09:18:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Максим Анатольевич", Instant.parse("2025-08-18T17:45:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-18T14:20:00+03:00").toEpochMilli()),
        CallClass(R.drawable.up_call,"Мищук", Instant.parse("2025-08-18T11:10:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Костя", Instant.parse("2025-08-17T19:15:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Игорь КамАЗ", Instant.parse("2025-08-17T16:40:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Алина", Instant.parse("2025-08-17T13:05:00+03:00").toEpochMilli()),

        CallClass(R.drawable.up_call,"Саша Памятник", Instant.parse("2025-08-16T18:30:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Валера Воротников", Instant.parse("2025-08-16T15:55:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-16T12:25:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Дима", Instant.parse("2025-08-15T20:10:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Максим Анатольевич", Instant.parse("2025-08-15T17:30:00+03:00").toEpochMilli()),
        CallClass(R.drawable.up_call,"Тарачков", Instant.parse("2025-08-15T14:00:00+03:00").toEpochMilli()),

        CallClass(R.drawable.not_call,"Алина", Instant.parse("2025-08-14T19:45:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Рамиль", Instant.parse("2025-08-14T16:15:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Василий", Instant.parse("2025-08-14T13:40:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Саша Алкаш", Instant.parse("2025-08-13T18:05:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-13T14:50:00+03:00").toEpochMilli()),
        CallClass(R.drawable.up_call,"Илья Александрович", Instant.parse("2025-08-13T11:30:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Игорь КамАЗ (2)", Instant.parse("2025-08-12T19:20:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Костя", Instant.parse("2025-08-12T15:10:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Алина", Instant.parse("2025-08-12T12:00:00+03:00").toEpochMilli()),

        CallClass(R.drawable.up_call,"Саша", Instant.parse("2025-08-11T17:55:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Валера Воротников (2)", Instant.parse("2025-08-11T14:25:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Максим Анатольевич", Instant.parse("2025-08-11T10:45:00+03:00").toEpochMilli()),

        CallClass(R.drawable.not_call,"Неизвестный номер", Instant.parse("2025-08-10T18:40:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Дима", Instant.parse("2025-08-10T15:05:00+03:00").toEpochMilli()),

        CallClass(R.drawable.down_call,"Рамиль", Instant.parse("2025-08-09T19:30:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Василий", Instant.parse("2025-08-09T16:20:00+03:00").toEpochMilli()),
        CallClass(R.drawable.not_call,"Алина", Instant.parse("2025-08-09T13:15:00+03:00").toEpochMilli()),
        CallClass(R.drawable.down_call,"Саша Алкаш", Instant.parse("2025-08-09T09:40:00+03:00").toEpochMilli()),
    )
}