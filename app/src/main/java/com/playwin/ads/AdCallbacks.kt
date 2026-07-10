package com.playwin.ads

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem

interface AdCallbacks {
    interface Rewarded {
        fun onAdLoaded()
        fun onAdFailedToLoad(errorCode: Int, errorMessage: String)
        fun onAdShowed()
        fun onAdFailedToShow(error: AdError)
        fun onAdDismissed()
        fun onUserEarnedReward(rewardItem: RewardItem)
    }

    interface Banner {
        fun onAdLoaded()
        fun onAdFailedToLoad(errorCode: Int, errorMessage: String)
        fun onAdClicked()
        fun onAdImpression()
    }

    interface Native {
        fun onAdLoaded()
        fun onAdFailedToLoad(errorCode: Int, errorMessage: String)
        fun onAdClicked()
        fun onAdImpression()
    }
}
