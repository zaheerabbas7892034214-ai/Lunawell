package com.zaheer.lunawell.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.zaheer.lunawell.data.repository.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingManager(
    private val context: Context,
    private val entitlementRepository: EntitlementRepository
) : BillingClientStateListener, PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    
    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val SUBSCRIPTION_PRODUCT_ID = "lunawell_pro_subscription"
        private const val BASE_PLAN_ID = "yearly_base"
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingResponseCode.OK) {
            coroutineScope.launch {
                queryPurchases()
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        // Retry connection
        startConnection()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            coroutineScope.launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            // User cancelled the purchase
        }
    }

    suspend fun queryProductDetails(): ProductDetails? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUBSCRIPTION_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    continuation.resume(productDetailsList.first())
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == BASE_PLAN_ID }
            ?.offerToken
            ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    suspend fun queryPurchases(): Boolean = withContext(Dispatchers.IO) {
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val purchasesResult = billingClient.queryPurchasesAsync(params)
            
            if (purchasesResult.billingResult.responseCode == BillingResponseCode.OK) {
                val purchases = purchasesResult.purchasesList
                
                if (purchases.isNotEmpty()) {
                    val activePurchase = purchases.firstOrNull { purchase ->
                        purchase.products.contains(SUBSCRIPTION_PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    }

                    if (activePurchase != null) {
                        handlePurchase(activePurchase)
                        return@withContext true
                    }
                }
                
                // No active subscription
                entitlementRepository.saveEntitlement(
                    isProActive = false,
                    expiryTimestamp = null,
                    purchaseToken = null
                )
                _isPro.value = false
                return@withContext false
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase.purchaseToken)
            }
            
            // Update entitlement
            entitlementRepository.saveEntitlement(
                isProActive = true,
                expiryTimestamp = null, // Could be extracted from purchase if available
                purchaseToken = purchase.purchaseToken
            )
            _isPro.value = true
        }
    }

    suspend fun acknowledgePurchase(purchaseToken: String) = withContext(Dispatchers.IO) {
        try {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
            result.responseCode == BillingResponseCode.OK
        } catch (e: Exception) {
            false
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
