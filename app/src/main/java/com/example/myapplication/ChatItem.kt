package com.example.myapplication.model.com.example.myapplication

import com.example.myapplication.Message
import com.example.myapplication.MessageStatus
import com.example.myapplication.Person

data class ChatItem(
    val personId: Int,
    val fullName: String,
    val avatarRes: Int,
    val lastMessageText: String?,
    val lastMessageTime: Long,
    val lastStatus: MessageStatus?, // SENT/DELIVERED/READ или null
    val unreadCount: Int,           // число непрочитанных входящих
    val isLastOutgoing: Boolean     // последнее сообщение в чате — моё?
)
fun buildChatItems(
    allMessages: List<Message>,
    allPersons: List<Person>
): List<ChatItem> {
    // получаем только id пользователей, у которых есть сообщения
    val personIdsWithMessages = allMessages.map { it.idPerson }.toSet()

    return personIdsWithMessages.mapNotNull { personId ->
        val person = allPersons.find { it.idPerson == personId } ?: return@mapNotNull null
        val personMessages = allMessages.filter { it.idPerson == person.idPerson }
        val lastMsg = personMessages.maxByOrNull { it.dataTime } ?: return@mapNotNull null

        ChatItem(
            personId        = person.idPerson,
            fullName        = person.fullName,
            avatarRes       = person.img,
            lastMessageText = lastMsg.textMessage,
            lastMessageTime = lastMsg.dataTime,
            lastStatus      = lastMsg.status,
            unreadCount     = personMessages.count {!it.toFrom && it.status == MessageStatus.SENT },
            isLastOutgoing  = lastMsg.toFrom
        )
    }
        .sortedByDescending { it.lastMessageTime }
}
