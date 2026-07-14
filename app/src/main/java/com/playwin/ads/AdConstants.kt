package com.playwin.ads

import com.myplaywin.app.BuildConfig

object AdConstants {
    // AdMob App ID: ca-app-pub-9239068235254084~5723386922
    
    // Production Unit IDs
    private const val PROD_BANNER = "ca-app-pub-9239068235254084/1438998304"
    private const val PROD_NATIVE = "ca-app-pub-9239068235254084/8826533738"
    private const val PROD_REWARDED = "ca-app-pub-9239068235254084/4695717036"

    // standard Google AdMob Test Unit IDs
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    val BANNER_AD_UNIT_ID: String = PROD_BANNER

    val NATIVE_AD_UNIT_ID: String = PROD_NATIVE

    val REWARDED_AD_UNIT_ID: String = PROD_REWARDED
}
