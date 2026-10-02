package com.example.data.remote

import android.util.Log
import com.example.model.AdrRecord
import com.example.model.Decision
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreSyncService {

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
        } catch (t: Throwable) {
            Log.i("FirestoreSyncService", "Firestore initialization deferred until project connected: ${t.message}")
        }
    }

    suspend fun syncDecision(decision: Decision): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val docData = hashMapOf(
                "id" to decision.id,
                "title" to decision.title,
                "rawProblemStatement" to decision.rawProblemStatement,
                "status" to decision.status.name,
                "tier" to decision.tier.name,
                "currentRevisionNumber" to decision.currentRevisionNumber,
                "revisionHash" to decision.revisionHash,
                "updatedAt" to decision.updatedAt,
                "schemaVersion" to "7.0"
            )
            db.collection("decisions").document(decision.id).set(docData)
            true
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Failed to sync decision to Firestore: ${e.message}")
            false
        }
    }

    suspend fun syncAdr(adr: AdrRecord): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val docData = hashMapOf(
                "id" to adr.id,
                "decisionId" to adr.decisionId,
                "adrNumber" to adr.adrNumber,
                "title" to adr.title,
                "status" to adr.status,
                "chosenOption" to adr.chosenOption,
                "revisionHash" to adr.revisionHash,
                "packetHash" to adr.packetHash,
                "approvedBy" to adr.approvedBy,
                "approvalTimestamp" to adr.approvalTimestamp,
                "renderedMarkdown" to adr.renderedMarkdown,
                "schemaVersion" to "7.0"
            )
            db.collection("adrs").document(adr.id).set(docData)
            true
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Failed to sync ADR to Firestore: ${e.message}")
            false
        }
    }
}
