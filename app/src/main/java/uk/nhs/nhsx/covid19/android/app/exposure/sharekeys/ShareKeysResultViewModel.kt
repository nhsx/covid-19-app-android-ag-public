package uk.nhs.nhsx.covid19.android.app.exposure.sharekeys

import androidx.lifecycle.ViewModel
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.SuccessfullySharedExposureKeys
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEventProcessor
import javax.inject.Inject

class ShareKeysResultViewModel @Inject constructor(
    private val analyticsEventProcessor: AnalyticsEventProcessor
) : ViewModel() {

    private var hasTrackedSuccessfulKeySharingAlready = false

    fun onCreate() {
        if (!hasTrackedSuccessfulKeySharingAlready) {
            analyticsEventProcessor.track(SuccessfullySharedExposureKeys)
            hasTrackedSuccessfulKeySharingAlready = true
        }
    }
}
