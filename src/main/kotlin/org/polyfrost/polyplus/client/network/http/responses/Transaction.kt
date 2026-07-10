package org.polyfrost.polyplus.client.network.http.responses

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

@Serializable(with = TransactionProvider.Serializer::class)
enum class TransactionProvider {
    Stripe,
    Ingame,
    AdminGrant,
    Unknown;

    val serializedName: String
        get() = when (this) {
            Stripe -> "stripe"
            Ingame -> "ingame"
            AdminGrant -> "admin_grant"
            Unknown -> "unknown"
        }

    val displayName: String
        get() = when (this) {
            Stripe -> "Stripe"
            Ingame -> "In-game"
            AdminGrant -> "Admin grant"
            Unknown -> "Unknown"
        }

    companion object {
        fun fromSerializedName(name: String): TransactionProvider? =
            entries.firstOrNull { it != Unknown && it.serializedName == name }
    }

    internal object Serializer : KSerializer<TransactionProvider> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("TransactionProvider", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: TransactionProvider) {
            encoder.encodeString(value.serializedName)
        }

        override fun deserialize(decoder: Decoder): TransactionProvider =
            fromSerializedName(decoder.decodeString()) ?: Unknown
    }
}

@Serializable(with = TransactionStatus.Serializer::class)
enum class TransactionStatus {
    Pending,
    Completed,
    Failed,
    Refunded,
    Unknown;

    val serializedName: String
        get() = when (this) {
            Pending -> "pending"
            Completed -> "completed"
            Failed -> "failed"
            Refunded -> "refunded"
            Unknown -> "unknown"
        }

    val displayName: String
        get() = when (this) {
            Pending -> "Pending"
            Completed -> "Completed"
            Failed -> "Failed"
            Refunded -> "Refunded"
            Unknown -> "Unknown"
        }

    companion object {
        fun fromSerializedName(name: String): TransactionStatus? =
            entries.firstOrNull { it != Unknown && it.serializedName == name }
    }

    internal object Serializer : KSerializer<TransactionStatus> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("TransactionStatus", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: TransactionStatus) {
            encoder.encodeString(value.serializedName)
        }

        override fun deserialize(decoder: Decoder): TransactionStatus =
            fromSerializedName(decoder.decodeString()) ?: Unknown
    }
}

@Serializable
data class TransactionInfo(
    val id: Int,
    val provider: TransactionProvider,
    val status: TransactionStatus,
    val amount: Float? = null,
    val buyer: String? = null,
    @SerialName("discount_rate") val discountRate: Int? = null,
    @SerialName("stripe_payment_id") val stripePaymentId: String? = null,
    @SerialName("raw_metadata") val rawMetadata: JsonElement? = null,
)

@Serializable
data class TransactionsResponse(
    val transactions: List<TransactionInfo> = emptyList(),
)
