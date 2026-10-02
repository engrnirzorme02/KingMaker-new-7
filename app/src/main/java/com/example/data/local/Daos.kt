package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions ORDER BY updatedAt DESC")
    fun getAllDecisions(): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE id = :id LIMIT 1")
    suspend fun getDecisionById(id: String): DecisionEntity?

    @Query("SELECT * FROM decisions WHERE id = :id LIMIT 1")
    fun getDecisionFlow(id: String): Flow<DecisionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(decision: DecisionEntity)

    @Query("DELETE FROM decisions WHERE id = :id")
    suspend fun deleteDecision(id: String)
}

@Dao
interface AdrDao {
    @Query("SELECT * FROM adrs ORDER BY adrNumber DESC")
    fun getAllAdrs(): Flow<List<AdrEntity>>

    @Query("SELECT * FROM adrs WHERE id = :id LIMIT 1")
    suspend fun getAdrById(id: String): AdrEntity?

    @Query("SELECT * FROM adrs WHERE decisionId = :decisionId LIMIT 1")
    suspend fun getAdrByDecisionId(decisionId: String): AdrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdr(adr: AdrEntity)
}

@Dao
interface BlueprintDao {
    @Query("SELECT * FROM blueprint_edges")
    fun getAllEdges(): Flow<List<BlueprintEdgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: BlueprintEdgeEntity)

    @Query("DELETE FROM blueprint_edges WHERE id = :edgeId")
    suspend fun deleteEdge(edgeId: String)
}

@Dao
interface OutcomeDao {
    @Query("SELECT * FROM outcome_observations ORDER BY observationDate DESC")
    fun getAllOutcomes(): Flow<List<OutcomeObservationEntity>>

    @Query("SELECT * FROM outcome_observations WHERE decisionId = :decisionId")
    fun getOutcomesForDecision(decisionId: String): Flow<List<OutcomeObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutcome(outcome: OutcomeObservationEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
