package com.ali.checkout.hero

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ali.checkout.hero.ui.theme.CheckoutHeroTheme
import com.google.gson.Gson
import com.samsung.android.sdk.samsungpay.v2.*;
import com.samsung.android.sdk.samsungpay.v2.SpaySdk.ServiceType
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager.CustomSheetTransactionInfoListener
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CheckoutHeroTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    SamsungPayButton(baseContext)
                }
            }
        }
    }
}

@Composable
fun SamsungPayButton(context: Context) {
    Button(onClick = { initiateSamsungPay(context) }) {
        Text(text = "Pay with Samsung Pay")
    }
}

fun samsungPayPartnerInfo(): PartnerInfo {
    val bundle = Bundle().apply {
        putString(SamsungPay.PARTNER_SERVICE_TYPE, ServiceType.INAPP_PAYMENT.toString())
    }

    return PartnerInfo("1b7da11c1d1945d1a21e3b", bundle)
}

fun makeCustomSheetPaymentInfo(): CustomSheetPaymentInfo {
    val brandList = mutableListOf(
        SpaySdk.Brand.VISA,
        SpaySdk.Brand.MASTERCARD
    )

    val amountControl = AmountBoxControl("amount-control", "SAR").apply {
        setAmountTotal(1.00, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)
    }

    val customSheet = CustomSheet().apply {
        addControl(amountControl)
    }

    return CustomSheetPaymentInfo.Builder()
        .setMerchantId("ecd2fa3d-3568-44bb-bdf9-3724858a4c32")
        .setOrderNumber("123456789012")
        .setMerchantName("Checkout Hero")
        .setAllowedCardBrands(brandList)
        .setCardHolderNameEnabled(true)
        .setRecurringEnabled(false)
        .setCustomSheet(customSheet)
        .build()
}


fun initiateSamsungPay(context: Context) {
    val paymentManager = PaymentManager(context, samsungPayPartnerInfo())

    paymentManager.startInAppPayWithCustomSheet(makeCustomSheetPaymentInfo(), object : CustomSheetTransactionInfoListener {
        override fun onCardInfoUpdated(cardInfo: CardInfo, sheet: CustomSheet) {
            val amountControl = sheet.getSheetControl("amount-control") as AmountBoxControl
            amountControl.setAmountTotal(1.00, AmountConstants.FORMAT_TOTAL_PRICE_ONLY)

            sheet.updateControl(amountControl)
            paymentManager.updateSheet(sheet)
        }

        override fun onSuccess(response: CustomSheetPaymentInfo, paymentCredential: String, extraPaymentData: Bundle) {
            try {
                val DPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_DPAN, "")
                val FPAN = response.cardInfo.cardMetaData.getString(SpaySdk.EXTRA_LAST4_FPAN, "")

                Toast.makeText(context, "DPAN: " + DPAN + "FPAN: " + FPAN, Toast.LENGTH_LONG).show()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }

            Toast.makeText(context, "Transaction : onSuccess", Toast.LENGTH_LONG).show()

            authorizePayment(paymentCredential)
        }

        override fun onFailure(errorCode: Int, errorData: Bundle) {
            Toast.makeText(context, "Transaction : onFailure : $errorCode", Toast.LENGTH_LONG).show()
        }

    })
}

fun authorizePayment(paymentCredentials: String) {
    val token = Gson().fromJson(paymentCredentials, PaymentCredentialJson::class.java).data

    val paymentRequest = object {
        val amount = 200_00
        val description = "Samsung Pay Demo"
        val currency = "SAR"
        val publishable_api_key = "pk_test_fBqKwEKsyjrbrwp7SenGED9zgCey8EVjuusJ4zcZ"
        val source = object {
            val type = "samsungpay"
            val token = token!!.data
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        val client = URL("https://apimig.moyasar.com/v1/payments").openConnection() as HttpURLConnection
        println(client.postJson(paymentRequest))
    }
}
