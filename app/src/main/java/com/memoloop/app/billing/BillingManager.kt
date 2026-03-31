package com.memoloop.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BillingManager(context: Context) {

    companion object {
        val PRODUCT_IDS = listOf("donate_coffee", "donate_lunch", "donate_big")
    }

    private var billingClient: BillingClient? = null
    private var productDetailsList: List<ProductDetails> = emptyList()
    private var onPurchaseComplete: ((Boolean) -> Unit)? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                consumePurchase(purchase)
            }
        } else {
            onPurchaseComplete?.invoke(false)
        }
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    fun connect(onReady: (List<ProductDetails>) -> Unit) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts(onReady)
                } else {
                    onReady(emptyList())
                }
            }

            override fun onBillingServiceDisconnected() {
                // Will retry on next user action
            }
        })
    }

    private fun queryProducts(onReady: (List<ProductDetails>) -> Unit) {
        val productList = PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Sort by product ID to maintain consistent order
                productDetailsList = detailsList.sortedBy { PRODUCT_IDS.indexOf(it.productId) }
            }
            onReady(productDetailsList)
        }
    }

    fun launchPurchase(
        activity: Activity,
        productDetails: ProductDetails,
        onComplete: (Boolean) -> Unit
    ) {
        onPurchaseComplete = onComplete

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient?.launchBillingFlow(activity, flowParams)
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
            onPurchaseComplete?.invoke(
                billingResult.responseCode == BillingClient.BillingResponseCode.OK
            )
        }
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
    }
}
