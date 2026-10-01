package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.smartfactory.ai.data.local.database.entity.AgentAuditEntity

@Dao
interface AuditDao {
    @Insert
    suspend fun insertAuditLog(audit: AgentAuditEntity)
}
