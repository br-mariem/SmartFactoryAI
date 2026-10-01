package com.smartfactory.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Journal "Boîte Noire" de l'Agent. Il trace toutes les actions
 * que l'IA a voulu prendre pour des raisons de sécurité industrielle.
 */
@Entity(tableName = "agent_audit_trail")
data class AgentAuditEntity(
    @PrimaryKey(autoGenerate = true) val auditId: Int = 0,
    val timestamp: Long,
    val machineId: String,
    val aiReasoning: String,
    val proposedAction: String,
    val status: String // Ex: "VALIDÉ_PAR_HUMAIN" ou "REFUSÉ"
)
