package com.ali.checkout.hero

import com.google.gson.annotations.SerializedName

class PaymentCredentialJson {
    @SerializedName("3DS")
    var data: CryptoDataJson? = null

    @SerializedName("payment_card_brand")
    var paymentCardBrand: String? = null

    @SerializedName("payment_currency_type")
    var paymentCurrencyType: String? = null

    @SerializedName("payment_last4_dpan")
    var paymentLast4Dpan: String? = null

    @SerializedName("payment_last4_fpan")
    var paymentLast4Fpan: String? = null

    @SerializedName("combo_debit_credit")
    var comboDebitCredit: String? = null

    @SerializedName("payment_shipping_method")
    var paymentShippingMethod: String? = null
}
