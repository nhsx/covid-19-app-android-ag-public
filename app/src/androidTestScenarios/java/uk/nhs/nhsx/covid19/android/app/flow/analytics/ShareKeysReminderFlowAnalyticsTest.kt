package uk.nhs.nhsx.covid19.android.app.flow.analytics

import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.flow.analytics.ShareKeysReminderFlowAnalyticsTest.KeySharingReminderTestFlow.CONSENT_AND_SUCCESS
import uk.nhs.nhsx.covid19.android.app.flow.analytics.ShareKeysReminderFlowAnalyticsTest.KeySharingReminderTestFlow.CONSENT_BUT_FAILURE
import uk.nhs.nhsx.covid19.android.app.flow.analytics.ShareKeysReminderFlowAnalyticsTest.KeySharingReminderTestFlow.NO_CONSENT
import uk.nhs.nhsx.covid19.android.app.flow.functionalities.PollingTestResult
import uk.nhs.nhsx.covid19.android.app.flow.functionalities.SelfDiagnosis
import uk.nhs.nhsx.covid19.android.app.flow.functionalities.ShareKeysReminder
import uk.nhs.nhsx.covid19.android.app.remote.data.Metrics
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT

class ShareKeysReminderFlowAnalyticsTest : AnalyticsTest() {

    private var selfDiagnosis = SelfDiagnosis(this)
    private var pollingTestResult = PollingTestResult(testAppContext)
    private var shareKeysReminder = ShareKeysReminder(this.testAppContext)

    private enum class KeySharingReminderTestFlow {
        NO_CONSENT,
        CONSENT_BUT_FAILURE,
        CONSENT_AND_SUCCESS
    }

    @Test
    fun selfDiagnosis_receivePositivePCR_declineKeySharingInInitialFlow_DeclineKeySharingInReminderFlow() {
        receivePositiveTestResultAfterSelfDiagnosisAndDeclineSharingKeysInitially(NO_CONSENT)
    }

    @Test
    fun selfDiagnosis_receivePositivePCR_declineKeySharingInInitialFlow_ConsentToKeySharingInReminderFlow_Failure() {
        receivePositiveTestResultAfterSelfDiagnosisAndDeclineSharingKeysInitially(CONSENT_BUT_FAILURE)
    }

    @Test
    fun selfDiagnosis_receivePositivePCR_declineKeySharingInInitialFlow_ConsentToKeySharingInReminderFlow_Success() {
        receivePositiveTestResultAfterSelfDiagnosisAndDeclineSharingKeysInitially(CONSENT_AND_SUCCESS)
    }

    private fun receivePositiveTestResultAfterSelfDiagnosisAndDeclineSharingKeysInitially(reminderFlow: KeySharingReminderTestFlow) {
        // Current date: 2nd Jan -> Analytics packet for: 1st Jan
        // Starting state: App running normally, not in isolation
        assertAnalyticsPacketIsNormal()

        // Complete questionnaire with risky symptoms and order test on 2nd Jan
        // Symptom onset date: Don't remember
        // Isolation end date: 10th Jan
        selfDiagnosis.selfDiagnosePositiveAndOrderTest(receiveResultImmediately = false)

        // Current date: 3rd Jan -> Analytics packet for: 2nd Jan
        assertOnFields {
            // Now in isolation due to self-diagnosis
            assertEquals(1, Metrics::completedQuestionnaireAndStartedIsolation)
            assertEquals(1, Metrics::startedIsolation)
            assertEquals(1, Metrics::launchedTestOrdering)
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::isIsolatingBackgroundTick)
            assertPresent(Metrics::isIsolatingForSelfDiagnosedBackgroundTick)
        }

        pollingTestResult.receiveAndAcknowledgePositiveTestResultAndDeclineKeySharing(
            LAB_RESULT,
            this::advanceToNextBackgroundTaskExecution
        )

        // Current date: 4th Jan -> Analytics packet for: 3rd Jan
        assertOnFields {
            // Still in isolation, for both self-diagnosis and positive test result
            assertEquals(1, Metrics::receivedPositiveTestResult)
            assertEquals(1, Metrics::receivedPositiveTestResultViaPolling)
            assertPresent(Metrics::isIsolatingBackgroundTick)
            assertPresent(Metrics::isIsolatingForTestedPositiveBackgroundTick)
            assertPresent(Metrics::isIsolatingForSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::hasTestedPositiveBackgroundTick)
            assertEquals(1, Metrics::askedToShareExposureKeysInTheInitialFlow)
        }

        // Current date: 5th Jan -> Analytics packet for: 4rd Jan
        assertOnFields {
            // Still in isolation, for both self-diagnosis and positive test result and with exposureKeyReminderNotification
            assertPresent(Metrics::isIsolatingBackgroundTick)
            assertPresent(Metrics::isIsolatingForTestedPositiveBackgroundTick)
            assertPresent(Metrics::isIsolatingForSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::hasTestedPositiveBackgroundTick)
            assertEquals(1, Metrics::totalShareExposureKeysReminderNotifications)
        }

        when (reminderFlow) {
            NO_CONSENT -> shareKeysReminder(shouldConsentToShareKeys = false, keySharingFinishesSuccessfully = false)
            CONSENT_BUT_FAILURE -> shareKeysReminder(shouldConsentToShareKeys = true, keySharingFinishesSuccessfully = false)
            CONSENT_AND_SUCCESS -> shareKeysReminder(shouldConsentToShareKeys = true, keySharingFinishesSuccessfully = true)
        }

        // Current date: 6th Jan -> Analytics packet for: 5rd Jan
        assertOnFields {
            // Still in isolation
            assertPresent(Metrics::isIsolatingBackgroundTick)
            assertPresent(Metrics::isIsolatingForTestedPositiveBackgroundTick)
            assertPresent(Metrics::isIsolatingForSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::hasTestedPositiveBackgroundTick)
            when (reminderFlow) {
                NO_CONSENT -> { }
                CONSENT_BUT_FAILURE -> {
                    assertEquals(1, Metrics::consentedToShareExposureKeysInReminderScreen)
                }
                CONSENT_AND_SUCCESS -> {
                    assertEquals(1, Metrics::consentedToShareExposureKeysInReminderScreen)
                    assertEquals(1, Metrics::successfullySharedExposureKeys)
                }
            }
        }

        // Dates: 7th-11th Jan -> Analytics packets for: 6th-10th Jan
        assertOnFieldsForDateRange(7..11) {
            // Still in isolation
            assertPresent(Metrics::isIsolatingBackgroundTick)
            assertPresent(Metrics::isIsolatingForTestedPositiveBackgroundTick)
            assertPresent(Metrics::isIsolatingForSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::hasTestedPositiveBackgroundTick)
        }

        // Dates: 12th-25th Jan -> Analytics packets for: 11th-24rd Jan
        assertOnFieldsForDateRange(12..25) {
            // Isolation is over, but isolation reason still stored for 14 days
            assertPresent(Metrics::hasSelfDiagnosedBackgroundTick)
            assertPresent(Metrics::hasSelfDiagnosedPositiveBackgroundTick)
            assertPresent(Metrics::hasTestedPositiveBackgroundTick)
        }

        // Current date: 25th Jan -> Analytics packet for: 24th Jan
        // Previous isolation reason no longer stored
        assertAnalyticsPacketIsNormal()
    }
}
