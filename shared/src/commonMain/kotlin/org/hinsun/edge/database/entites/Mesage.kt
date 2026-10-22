package org.hinsun.edge.database.entites

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.Uuid

//
// File        : Mesage.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

enum class MessageRole {
    HUMAN,
    SYSTEM,
}

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["uuid"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["conversation_id"], unique = false)
    ]
)
data class Message(
    @PrimaryKey var uuid: String = Uuid.random().toString(),

    var content: String,
    var role: MessageRole = MessageRole.HUMAN,

    @ColumnInfo(name = "usage_credits") var usageCredits: Int = 0,
    @ColumnInfo(name = "usage_tokens") var usageTokens: Int = 0,

    // References
    @ColumnInfo(name = "conversation_id") val conversationId: String,

    @ColumnInfo(name = "created_at") var createdAt: Long = Clock.System.now().epochSeconds,
    @ColumnInfo(name = "updated_at") var updatedAt: Long = Clock.System.now().epochSeconds,
)
