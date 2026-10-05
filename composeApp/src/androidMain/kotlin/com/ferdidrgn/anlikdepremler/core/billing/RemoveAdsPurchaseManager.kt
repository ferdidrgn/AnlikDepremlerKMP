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
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Must match the one-time in-app product id created in Google Play Console. */
const val REMOVE_ADS_PRODUCT_ID = "remove_ads_6_months"

/**
 * One-shot "remove ads for 6 months" purchase - same connect/query/launch/consume flow as
 * launchCoffeeDonationFlow, but on success extends PreferencesManager.adsFreeUntilMillis instead
 * of just thanking the user. Consumed immediately (not kept as an owned entitlement) so the same
 * product can be bought again later to stack another 6 months on top of the current period.
 */
fun launchRemoveAdsPurchaseFlow(context: Context, preferencesManager: PreferencesManager) {
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
                        consumeRemoveAdsPurchase(billingClient, context, preferencesManager, purchase)
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
                        .setProductId(REMOVE_ADS_PRODUCT_ID)
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

private fun consumeRemoveAdsPurchase(
    billingClient: BillingClient,
    context: Context,
    preferencesManager: PreferencesManager,
    purchase: Purchase
) {
    val consumeParams = ConsumeParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()

    billingClient.consumeAsync(consumeParams) { billingResult, _ ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            CoroutineScope(Dispatchers.Main).launch {
                preferencesManager.extendAdsFreeBySixMonths()
            }
            Toast.makeText(
                context,
                context.getString(R.string.ads_removed_thanks),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
