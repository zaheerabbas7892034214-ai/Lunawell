package com.zaheer.lunawell.presentation.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.ProductDetails
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaywallUiState(
    val productDetails: ProductDetails? = null,
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val priceText: String = "₹999/year"
)

class PaywallViewModel(
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadProductDetails()
        observeProStatus()
    }

    private fun loadProductDetails() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                billingManager.startConnection()
                
                val productDetails = billingManager.queryProductDetails()
                
                if (productDetails != null) {
                    val price = productDetails.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull()
                        ?.formattedPrice ?: "₹999/year"
                    
                    _uiState.value = _uiState.value.copy(
                        productDetails = productDetails,
                        priceText = price,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unable to load subscription details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load subscription"
                )
            }
        }
    }

    private fun observeProStatus() {
        viewModelScope.launch {
            combine(
                billingManager.isPro,
                preferencesManager.getIsProActive()
            ) { isPro, isProPref ->
                isPro || isProPref
            }.collectLatest { isPro ->
                _uiState.value = _uiState.value.copy(isPro = isPro)
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val productDetails = _uiState.value.productDetails
        if (productDetails != null) {
            billingManager.launchBillingFlow(activity, productDetails)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "Product details not available. Please try again."
            )
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val hasActivePurchase = billingManager.queryPurchases()
                
                if (hasActivePurchase) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Purchase restored successfully!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No active purchases found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to restore purchases"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
