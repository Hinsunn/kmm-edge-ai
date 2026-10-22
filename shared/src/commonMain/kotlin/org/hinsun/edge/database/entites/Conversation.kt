package org.hinsun.edge.database.entites

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.Uuid

//
// File        : Conversation.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Entity(
    tableName = "conversations",
    foreignKeys = [],
    indices = []
)
data class Conversation(
    @PrimaryKey var uuid: String = Uuid.random().toString(),
    var title: String,
    @ColumnInfo(name = "is_active") var isActive: Boolean = false,
    @ColumnInfo(name = "created_at") var createdAt: Long = Clock.System.now().epochSeconds,
    @ColumnInfo(name = "updated_at") var updatedAt: Long = Clock.System.now().epochSeconds,
)