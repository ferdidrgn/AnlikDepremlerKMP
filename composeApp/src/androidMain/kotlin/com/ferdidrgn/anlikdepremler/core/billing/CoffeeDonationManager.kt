package com.ferdidrgn.anlikdepremler.core.billing

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.ferdidrgn.anlikdepremler.R

private const val DONATION_SMALL = "donation_small"

/**
 * One-shot "buy me a coffee" in-app purchase flow: connects a fresh
 * [BillingClient], queries the product, launches the purchase UI, and
 * consumes the purchase immediately so it can be bought again later.
 */
fun launchCoffeeDonationFlow(context: Context, productId: String = DONATION_SMALL) {
    val activity = context as? Activity ?: return
    lateinit var billingClient: BillingClient

    val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        consumeCoffeePurchase(billingClient, context, purchase)
                    }
                }
            }
        }
        .enablePendingPurchases(pendingPurchasesParams)
        .build()

    billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {}
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                billingClient.queryProductDetailsAsync(params) { result, productDetailsResult ->
                    val list = productDetailsResult.productDetailsList
                    if (result.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()) {
                        val productDetails = list.first()
                        val flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )
                            )
                            .build()
                        billingClient.launchBillingFlow(activity, flowParams)
                    }
                }
            }
        }
    })
}

private fun consumeCoffeePurchase(
    billingClient: BillingClient,
    context: Context,
    purchase: Purchase
) {
    val consumeParams = ConsumeParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()

    billingClient.consumeAsync(consumeParams) { billingResult, _ ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Toast.makeText(
                context,
                context.getString(R.string.thanks_for_coffee),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
