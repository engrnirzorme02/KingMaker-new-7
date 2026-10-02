package com.example.domain

import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implements RFC 8785 JSON Canonicalization Scheme (JCS) representation
 * and SHA-256 hashing for immutable revisions, review packets, and approvals.
 */
object JcsHasher {

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder()
        for (b in hashBytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    /**
     * Canonicalizes a JSON string per RFC 8785:
     * - Object keys sorted by UTF-16 code units (lexicographical order)
     * - Whitespace removed
     * - Predictable scalar representations
     */
    fun canonicalize(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("{")) {
                canonicalizeObject(JSONObject(trimmed))
            } else if (trimmed.startsWith("[")) {
                canonicalizeArray(JSONArray(trimmed))
            } else {
                trimmed
            }
        } catch (e: Exception) {
            jsonString.trim()
        }
    }

    private fun canonicalizeObject(obj: JSONObject): String {
        val keys = mutableListOf<String>()
        val it = obj.keys()
        while (it.hasNext()) {
            keys.add(it.next())
        }
        keys.sort()

        val sb = StringBuilder("{")
        for (i in keys.indices) {
            val key = keys[i]
            if (i > 0) sb.append(",")
            sb.append(JSONObject.quote(key))
            sb.append(":")
            val value = obj.get(key)
            sb.append(canonicalizeValue(value))
        }
        sb.append("}")
        return sb.toString()
    }

    private fun canonicalizeArray(arr: JSONArray): String {
        val sb = StringBuilder("[")
        for (i in 0 until arr.length()) {
            if (i > 0) sb.append(",")
            val value = arr.get(i)
            sb.append(canonicalizeValue(value))
        }
        sb.append("]")
        return sb.toString()
    }

    private fun canonicalizeValue(value: Any?): String {
        return when (value) {
            null, JSONObject.NULL -> "null"
            is JSONObject -> canonicalizeObject(value)
            is JSONArray -> canonicalizeArray(value)
            is String -> JSONObject.quote(value)
            is Number, is Boolean -> value.toString()
            else -> JSONObject.quote(value.toString())
        }
    }

    fun computeRevisionHash(
        decisionId: String,
        revisionNumber: Int,
        title: String,
        rawProblem: String,
        claimsJson: String,
        optionsJson: String
    ): String {
        val json = JSONObject().apply {
            put("decisionId", decisionId)
            put("revisionNumber", revisionNumber)
            put("title", title)
            put("rawProblem", rawProblem)
            put("claims", claimsJson)
            put("options", optionsJson)
            put("schemaVersion", "7.0")
        }
        val canonical = canonicalize(json.toString())
        return sha256Hex(canonical)
    }

    fun computePacketHash(
        revisionHash: String,
        recommendedOptionId: String,
        qualityScore: Float
    ): String {
        val json = JSONObject().apply {
            put("revisionHash", revisionHash)
            put("recommendedOptionId", recommendedOptionId)
            put("qualityScore", String.format("%.4f", qualityScore))
            put("schemaVersion", "7.0")
        }
        val canonical = canonicalize(json.toString())
        return sha256Hex(canonical)
    }
}
