package org.polyfrost.polyplus.client.cosmetics

import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.PolyPlusConfig
import org.polyfrost.polyplus.client.network.http.postBodyAuthorized
import org.polyfrost.polyplus.client.network.http.getBodyAuthorized
import org.polyfrost.polyplus.client.network.http.responses.CreateCheckoutRequest
import org.polyfrost.polyplus.client.network.http.responses.CreateCheckoutResponse
import org.polyfrost.polyplus.client.network.http.responses.TransactionInfo
import org.polyfrost.polyplus.client.network.http.responses.TransactionsResponse
import org.polyfrost.polyplus.client.utils.ClientPlatform

object BillingService {
    private val LOGGER = LogManager.getLogger()

    suspend fun createCheckout(priceIds: List<String>): Result<CreateCheckoutResponse> {
        val prices = priceIds.filter { it.isNotBlank() }
        if (prices.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nothing to check out"))
        }
        val player = ClientPlatform.localPlayerUuid().toString()
        return PolyPlusClient.HTTP.postBodyAuthorized<CreateCheckoutResponse>(
            "${PolyPlusConfig.apiUrl}/stripe/create",
        ) {
            contentType(ContentType.Application.Json)
            setBody(CreateCheckoutRequest(player = player, prices = prices))
        }.onFailure { LOGGER.error("Failed to create Stripe checkout", it); org.polyfrost.polyplus.client.PolyPlusSentry.capture(it) }
    }

    suspend fun checkoutAndOpen(priceIds: List<String>): Result<String> =
        createCheckout(priceIds).map { response ->
            ClientPlatform.openUri(response.url)
            response.url
        }

    suspend fun fetchTransactions(): Result<List<TransactionInfo>> =
        PolyPlusClient.HTTP
            .getBodyAuthorized<TransactionsResponse>("${PolyPlusConfig.apiUrl}/transactions/player")
            .map { it.transactions.sortedByDescending { tx -> tx.id } }
            .onFailure { LOGGER.error("Failed to fetch transactions", it); org.polyfrost.polyplus.client.PolyPlusSentry.capture(it) }
}
