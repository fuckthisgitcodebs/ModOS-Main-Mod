package com.mod.os.recents.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Fts4
import java.time.Instant

@Entity(
    tableName = "clips",
    indices = [
        Index(value = ["sourcePackage", "timestamp"], orders = [Index.Order.DESC, Index.Order.DESC]),
        Index(value = ["isArchived", "timestamp"], orders = [Index.Order.ASC, Index.Order.DESC]),
        Index(value = ["isArchived", "clipType", "timestamp"], orders = [Index.Order.ASC, Index.Order.ASC, Index.Order.DESC]),
        Index(value = ["hash"], unique = true),
        Index(value = ["isArchived", "timestamp", "contentPreview"])
    ]
)
data class ClipEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant = Instant.now(),
    val sourcePackage: String,
    val appLabel: String? = null,
    val contentPreview: String,
    val fullContent: String,
    val mimeType: String? = null,
    val hash: String,
    val clipType: ClipType,
    val isSensitive: Boolean = false,
    val isArchived: Boolean = false,
    val tags: String? = null
)

@Fts4(
    contentEntity = ClipEntry::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
@Entity(tableName = "clips_fts")
data class ClipFts(
    val contentPreview: String,
    val fullContent: String,
    val tags: String?
)
