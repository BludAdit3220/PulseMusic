/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.listentogether

import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Codec for encoding and decoding messages using kotlinx.serialization ProtoBuf
 */
@OptIn(ExperimentalSerializationApi::class)
class MessageCodec(
    var compressionEnabled: Boolean = false
) {
    companion object {
        private const val TAG = "MessageCodec"
        private const val COMPRESSION_THRESHOLD = 100 // Only compress if > 100 bytes
    }
    
    /**
     * Encode a message
     */
    fun encode(msgType: String, payload: Any?): ByteArray {
        var payloadBytes = byteArrayOf()
        var compressed = false
        
        if (payload != null) {
            payloadBytes = when (payload) {
                is CreateRoomPayload -> ProtoBuf.encodeToByteArray(payload)
                is JoinRoomPayload -> ProtoBuf.encodeToByteArray(payload)
                is ApproveJoinPayload -> ProtoBuf.encodeToByteArray(payload)
                is RejectJoinPayload -> ProtoBuf.encodeToByteArray(payload)
                is PlaybackActionPayload -> ProtoBuf.encodeToByteArray(payload)
                is BufferReadyPayload -> ProtoBuf.encodeToByteArray(payload)
                is KickUserPayload -> ProtoBuf.encodeToByteArray(payload)
                is TransferHostPayload -> ProtoBuf.encodeToByteArray(payload)
                is ChatPayload -> ProtoBuf.encodeToByteArray(payload)
                is SuggestTrackPayload -> ProtoBuf.encodeToByteArray(payload)
                is ApproveSuggestionPayload -> ProtoBuf.encodeToByteArray(payload)
                is RejectSuggestionPayload -> ProtoBuf.encodeToByteArray(payload)
                is ReconnectPayload -> ProtoBuf.encodeToByteArray(payload)
                else -> throw IllegalArgumentException("Unsupported payload type: ${payload::class.simpleName}")
            }
            
            // Compress if enabled and payload is large enough
            if (compressionEnabled && payloadBytes.size > COMPRESSION_THRESHOLD) {
                val compressedBytes = compressData(payloadBytes)
                if (compressedBytes.size < payloadBytes.size) {
                    payloadBytes = compressedBytes
                    compressed = true
                }
            }
        }
        
        val envelope = Envelope(
            type = msgType,
            payload = payloadBytes,
            compressed = compressed
        )
        
        return ProtoBuf.encodeToByteArray(envelope)
    }
    
    /**
     * Decode a message
     */
    fun decode(data: ByteArray): Pair<String, ByteArray> {
        val envelope = ProtoBuf.decodeFromByteArray<Envelope>(data)
        
        var payloadBytes = envelope.payload
        
        if (envelope.compressed) {
            payloadBytes = decompressData(payloadBytes) ?: payloadBytes
        }
        
        return Pair(envelope.type, payloadBytes)
    }
    
    /**
     * Compress data using GZIP
     */
    private fun compressData(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(data)
        }
        return outputStream.toByteArray()
    }
    
    /**
     * Decompress GZIP data
     */
    private fun decompressData(data: ByteArray): ByteArray? {
        return try {
            val inputStream = ByteArrayInputStream(data)
            GZIPInputStream(inputStream).use { gzip ->
                gzip.readBytes()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to decompress data")
            null
        }
    }
    
    /**
     * Decode payload to Kotlin objects
     */
    fun decodePayload(msgType: String, payloadBytes: ByteArray): Any? {
        if (payloadBytes.isEmpty()) return null
        
        return when (msgType) {
            MessageTypes.ROOM_CREATED -> ProtoBuf.decodeFromByteArray<RoomCreatedPayload>(payloadBytes)
            MessageTypes.JOIN_REQUEST -> ProtoBuf.decodeFromByteArray<JoinRequestPayload>(payloadBytes)
            MessageTypes.JOIN_APPROVED -> ProtoBuf.decodeFromByteArray<JoinApprovedPayload>(payloadBytes)
            MessageTypes.JOIN_REJECTED -> ProtoBuf.decodeFromByteArray<JoinRejectedPayload>(payloadBytes)
            MessageTypes.USER_JOINED -> ProtoBuf.decodeFromByteArray<UserJoinedPayload>(payloadBytes)
            MessageTypes.USER_LEFT -> ProtoBuf.decodeFromByteArray<UserLeftPayload>(payloadBytes)
            MessageTypes.SYNC_PLAYBACK -> ProtoBuf.decodeFromByteArray<PlaybackActionPayload>(payloadBytes)
            MessageTypes.BUFFER_WAIT -> ProtoBuf.decodeFromByteArray<BufferWaitPayload>(payloadBytes)
            MessageTypes.BUFFER_COMPLETE -> ProtoBuf.decodeFromByteArray<BufferCompletePayload>(payloadBytes)
            MessageTypes.ERROR -> ProtoBuf.decodeFromByteArray<ErrorPayload>(payloadBytes)
            MessageTypes.HOST_CHANGED -> ProtoBuf.decodeFromByteArray<HostChangedPayload>(payloadBytes)
            MessageTypes.KICKED -> ProtoBuf.decodeFromByteArray<KickedPayload>(payloadBytes)
            MessageTypes.SYNC_STATE -> ProtoBuf.decodeFromByteArray<SyncStatePayload>(payloadBytes)
            MessageTypes.RECONNECTED -> ProtoBuf.decodeFromByteArray<ReconnectedPayload>(payloadBytes)
            MessageTypes.USER_RECONNECTED -> ProtoBuf.decodeFromByteArray<UserReconnectedPayload>(payloadBytes)
            MessageTypes.USER_DISCONNECTED -> ProtoBuf.decodeFromByteArray<UserDisconnectedPayload>(payloadBytes)
            MessageTypes.SUGGESTION_RECEIVED -> ProtoBuf.decodeFromByteArray<SuggestionReceivedPayload>(payloadBytes)
            MessageTypes.SUGGESTION_APPROVED -> ProtoBuf.decodeFromByteArray<SuggestionApprovedPayload>(payloadBytes)
            MessageTypes.SUGGESTION_REJECTED -> ProtoBuf.decodeFromByteArray<SuggestionRejectedPayload>(payloadBytes)
            else -> null
        }
    }
}

@kotlinx.serialization.Serializable
data class Envelope(
    val type: String,
    val payload: ByteArray,
    val compressed: Boolean = false
)
