package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val rawProblemStatement: String,
    val status: String,
    val tier: String,
    val currentRevisionNumber: Int,
    val revisionHash: String,
    val createdAt: Long,
    val updatedAt: Long,
    val claimsJson: String,
    val questionsJson: String,
    val optionsJson: String,
    val criteriaJson: String,
    val perspectivesJson: String,
    val critiquesJson: String,
    val qualityVectorJson: String,
    val recommendedOptionId: String?,
    val decisiveRationale: String,
    val whatCouldChangeThis: String,
    val adrId: String?,
    val dependenciesJson: String
)

@Entity(tableName = "adrs")
data class AdrEntity(
    @PrimaryKey val id: String,
    val decisionId: String,
    val adrNumber: Int,
    val title: String,
    val status: String,
    val context: String,
    val decisionOutcome: String,
    val chosenOption: String,
    val consequences: String,
    val revisionHash: String,
    val packetHash: String,
    val approvedBy: String,
    val approvalTimestamp: Long,
    val renderedMarkdown: String
)

@Entity(tableName = "blueprint_edges")
data class BlueprintEdgeEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val targetId: String,
    val relationType: String
)

@Entity(tableName = "outcome_observations")
data class OutcomeObservationEntity(
    @PrimaryKey val id: String,
    val decisionId: String,
    val adrNumber: Int,
    val expectedMetric: String,
    val observedMetric: String,
    val divergenceClass: String,
    val observationDate: Long,
    val reviewNotes: String,
    val warrantsSupersedingRevision: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val modelName: String,
    val thinkingProcess: String?,
    val sourcesJson: String
)
