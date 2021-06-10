package uk.nhs.nhsx.covid19.android.app.testordering.linktestresult

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jeroenmols.featureflag.framework.FeatureFlag
import com.jeroenmols.featureflag.framework.FeatureFlagTestHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.NegativeResultReceived
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.PositiveResultReceived
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.ResultReceived
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.VoidResultReceived
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEventProcessor
import uk.nhs.nhsx.covid19.android.app.analytics.TestOrderType.OUTSIDE_APP
import uk.nhs.nhsx.covid19.android.app.remote.data.DurationDays
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyCtaExchangeResponse
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_SELF_REPORTED
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.NEGATIVE
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.POSITIVE
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.VOID
import uk.nhs.nhsx.covid19.android.app.state.IsolationHelper
import uk.nhs.nhsx.covid19.android.app.state.IsolationState
import uk.nhs.nhsx.covid19.android.app.state.IsolationStateMachine
import uk.nhs.nhsx.covid19.android.app.state.OnTestResult
import uk.nhs.nhsx.covid19.android.app.state.asLogical
import uk.nhs.nhsx.covid19.android.app.testordering.AcknowledgedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.ReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantVirologyTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.Failure
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.Success
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.UnparsableTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.ValidationErrorType
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.ErrorState
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultError.BOTH_PROVIDED
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultError.INVALID
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultError.NEITHER_PROVIDED
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultError.NO_CONNECTION
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultError.UNEXPECTED
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultViewModel.LinkTestResultState
import uk.nhs.nhsx.covid19.android.app.testordering.unknownresult.ReceivedUnknownTestResultProvider
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class LinkTestResultViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val ctaTokenValidator = mockk<CtaTokenValidator>(relaxed = true)
    private val isolationStateMachine = mockk<IsolationStateMachine>(relaxed = true)
    private val linkTestResultOnsetDateNeededChecker = mockk<LinkTestResultOnsetDateNeededChecker>(relaxed = true)
    private val analyticsEventProcessor = mockk<AnalyticsEventProcessor>(relaxed = true)
    private val fixedClock = Clock.fixed(Instant.parse("2020-05-21T10:00:00Z"), ZoneOffset.UTC)
    private val receivedUnknownTestResultProvider = mockk<ReceivedUnknownTestResultProvider>(relaxUnitFun = true)
    private val isolationHelper = IsolationHelper(fixedClock)

    private val testSubject = LinkTestResultViewModel(
        ctaTokenValidator,
        isolationStateMachine,
        linkTestResultOnsetDateNeededChecker,
        analyticsEventProcessor,
        receivedUnknownTestResultProvider,
        fixedClock
    )

    private val viewStateObserver = mockk<Observer<LinkTestResultState>>(relaxed = true)
    private val confirmedDailyContactTestingNegativeObserver = mockk<Observer<Unit>>(relaxed = true)
    private val validationOnsetDateNeeded = mockk<Observer<ReceivedTestResult>>(relaxed = true)
    private val validationCompletedObserver = mockk<Observer<Unit>>(relaxed = true)

    @Before
    fun setUp() {
        testSubject.viewState().observeForever(viewStateObserver)
        testSubject.confirmedDailyContactTestingNegative().observeForever(confirmedDailyContactTestingNegativeObserver)
        testSubject.validationOnsetDateNeeded().observeForever(validationOnsetDateNeeded)
        testSubject.validationCompleted().observeForever(validationCompletedObserver)
    }

    @After
    fun tearDown() {
        FeatureFlagTestHelper.clearFeatureFlags()
    }

    @Test
    fun `display daily contact testing content when contact case only and feature flag enabled`() =
        runBlocking {
            enableShowDailyContactTesting()

            verify { isolationStateMachine.readLogicalState() }

            verify {
                viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = true))
            }
        }

    @Test
    fun `do not display daily contact testing content when not exclusively contact case isolation and feature flag enabled`() =
        runBlocking {
            FeatureFlagTestHelper.enableFeatureFlag(FeatureFlag.DAILY_CONTACT_TESTING)
            setIsolationState(indexAndContactCaseIsolation)

            testSubject.fetchInitialViewState()

            verify { isolationStateMachine.readLogicalState() }

            verify { viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = false)) }
        }

    @Test
    fun `do not display daily contact testing content when contact case only and feature flag disabled`() =
        runBlocking {
            FeatureFlagTestHelper.disableFeatureFlag(FeatureFlag.DAILY_CONTACT_TESTING)
            setIsolationState(contactCaseOnlyIsolation)

            testSubject.fetchInitialViewState()

            verify { isolationStateMachine.readLogicalState() }

            verify { viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = false)) }
        }

    @Test
    fun `continue button clicked with both negative DCT confirmation selected and CTA token entered should return input error state`() =
        runBlocking {
            enableShowDailyContactTesting()

            testSubject.ctaToken = "test"
            testSubject.onDailyContactTestingOptInChecked()
            testSubject.onContinueButtonClicked()

            verifyOrder {
                viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = true))
                viewStateObserver.onChanged(
                    LinkTestResultState(
                        showDailyContactTesting = true,
                        confirmedNegativeDailyContactTestingResult = true
                    )
                )
                viewStateObserver.onChanged(
                    LinkTestResultState(
                        showDailyContactTesting = true,
                        confirmedNegativeDailyContactTestingResult = true,
                        errorState = ErrorState(BOTH_PROVIDED)
                    )
                )
            }
        }

    @Test
    fun `continue button clicked with CTA token entered should return start validation`() =
        runBlocking {
            enableShowDailyContactTesting()

            setResult(POSITIVE, LAB_RESULT)

            testSubject.ctaToken = "test"
            testSubject.onContinueButtonClicked()

            verifyOrder {
                viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = true))
                viewStateObserver.onChanged(
                    LinkTestResultState(showDailyContactTesting = true, showValidationProgress = true)
                )
            }
            coVerify { ctaTokenValidator.validate("test") }
        }

    @Test
    fun `continue button clicked with only negative DCT confirmation provided should emit confirmation event`() =
        runBlocking {
            enableShowDailyContactTesting()

            testSubject.ctaToken = ""
            testSubject.onDailyContactTestingOptInChecked()
            testSubject.onContinueButtonClicked()

            verifyOrder {
                viewStateObserver.onChanged(LinkTestResultState(showDailyContactTesting = true))
                viewStateObserver.onChanged(
                    LinkTestResultState(
                        showDailyContactTesting = true,
                        confirmedNegativeDailyContactTestingResult = true
                    )
                )
            }
            verify { confirmedDailyContactTestingNegativeObserver.onChanged(null) }
        }

    @Test
    fun `continue button clicked with neither negative DCT confirmation nor CTA token entered should return input error state`() =
        runBlocking {
            enableShowDailyContactTesting()

            testSubject.ctaToken = ""
            testSubject.onContinueButtonClicked()

            verify {
                viewStateObserver.onChanged(
                    LinkTestResultState(
                        showDailyContactTesting = true,
                        errorState = ErrorState(NEITHER_PROVIDED)
                    )
                )
            }
        }

    @Test
    fun `continue button clicked with DCT not showing should start validation`() =
        runBlocking {
            setIsolationState(indexAndContactCaseIsolation)
            setResult(POSITIVE, LAB_RESULT)

            testSubject.ctaToken = "test"
            testSubject.fetchInitialViewState()
            testSubject.onContinueButtonClicked()

            verify { viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true)) }
            coVerify { ctaTokenValidator.validate(any()) }
        }

    @Test
    fun `successful cta token validation, onset date needed`() = runBlocking {
        disableShowDailyContactTesting()

        val testResultResponse = setResult(
            POSITIVE,
            RAPID_RESULT,
            diagnosisKeySubmissionSupported = true,
            requiresConfirmatoryTest = true,
            confirmatoryDayLimit = 2
        )

        val testResult = ReceivedTestResult(
            testResultResponse.diagnosisKeySubmissionToken,
            testResultResponse.testEndDate,
            testResultResponse.testResult,
            testResultResponse.testKit,
            testResultResponse.diagnosisKeySubmissionSupported,
            requiresConfirmatoryTest = testResultResponse.requiresConfirmatoryTest,
            confirmatoryDayLimit = testResultResponse.confirmatoryDayLimit
        )

        every { linkTestResultOnsetDateNeededChecker.isInterestedInAskingForSymptomsOnsetDay(testResult) } returns true

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        val event = OnTestResult(
            testResult = testResult,
            showNotification = false
        )

        verify { isolationStateMachine.processEvent(event) }

        verifyOrder {
            viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true))
            validationOnsetDateNeeded.onChanged(testResult)
        }
        verify(exactly = 0) { validationCompletedObserver.onChanged(any()) }
    }

    @Test
    fun `successful cta token validation, onset date not needed`() = runBlocking {
        disableShowDailyContactTesting()

        val testResultResponse = setResult(NEGATIVE, LAB_RESULT)
        val testResult = ReceivedTestResult(
            testResultResponse.diagnosisKeySubmissionToken,
            testResultResponse.testEndDate,
            testResultResponse.testResult,
            testResultResponse.testKit,
            testResultResponse.diagnosisKeySubmissionSupported,
            requiresConfirmatoryTest = testResultResponse.requiresConfirmatoryTest
        )

        every { linkTestResultOnsetDateNeededChecker.isInterestedInAskingForSymptomsOnsetDay(testResult) } returns false

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        val event = OnTestResult(
            testResult = testResult,
            showNotification = false
        )

        verify { isolationStateMachine.processEvent(event) }

        verifyOrder {
            viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true))
            validationCompletedObserver.onChanged(null)
        }
        verify(exactly = 0) { validationOnsetDateNeeded.onChanged(any()) }
    }

    @Test
    fun `cta token invalid`() = runBlocking {
        disableShowDailyContactTesting()

        coEvery { ctaTokenValidator.validate(any()) } returns Failure(ValidationErrorType.INVALID)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        verify(exactly = 0) { isolationStateMachine.processEvent(any()) }

        verifyOrder {
            viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true))
            viewStateObserver.onChanged(
                LinkTestResultState(
                    showValidationProgress = false,
                    errorState = ErrorState(INVALID)
                )
            )
        }
    }

    @Test
    fun `cta token validation without internet connection`() = runBlocking {
        disableShowDailyContactTesting()

        coEvery { ctaTokenValidator.validate(any()) } returns Failure(ValidationErrorType.NO_CONNECTION)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        verify(exactly = 0) { isolationStateMachine.processEvent(any()) }

        verifyOrder {
            viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true))
            viewStateObserver.onChanged(
                LinkTestResultState(
                    showValidationProgress = false,
                    errorState = ErrorState(NO_CONNECTION)
                )
            )
        }
    }

    @Test
    fun `cta token validation returns unknown test result`() = runBlocking {
        disableShowDailyContactTesting()

        coEvery { ctaTokenValidator.validate(any()) } returns UnparsableTestResult

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        verify(exactly = 0) { isolationStateMachine.processEvent(any()) }

        verifyOrder {
            receivedUnknownTestResultProvider setProperty "value" value true
            validationCompletedObserver.onChanged(any())
        }
    }

    @Test
    fun `cta token validation returns unexpected error`() = runBlocking {
        disableShowDailyContactTesting()

        coEvery { ctaTokenValidator.validate(any()) } returns Failure(ValidationErrorType.UNEXPECTED)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        verify(exactly = 0) { isolationStateMachine.processEvent(any()) }

        verifyOrder {
            viewStateObserver.onChanged(LinkTestResultState(showValidationProgress = true))
            viewStateObserver.onChanged(
                LinkTestResultState(
                    showValidationProgress = false,
                    errorState = ErrorState(UNEXPECTED)
                )
            )
        }
    }

    @Test
    fun `track analytics events on negative PCR result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(NEGATIVE, LAB_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(NegativeResultReceived)
            analyticsEventProcessor.track(ResultReceived(NEGATIVE, LAB_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on positive PCR result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(POSITIVE, LAB_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(PositiveResultReceived)
            analyticsEventProcessor.track(ResultReceived(POSITIVE, LAB_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on void PCR result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(VOID, LAB_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(VoidResultReceived)
            analyticsEventProcessor.track(ResultReceived(VOID, LAB_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on negative assisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(NEGATIVE, RAPID_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(NegativeResultReceived)
            analyticsEventProcessor.track(ResultReceived(NEGATIVE, RAPID_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on negative unassisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(NEGATIVE, RAPID_SELF_REPORTED)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(NegativeResultReceived)
            analyticsEventProcessor.track(
                ResultReceived(
                    NEGATIVE,
                    RAPID_SELF_REPORTED,
                    OUTSIDE_APP
                )
            )
        }
    }

    @Test
    fun `track analytics events on positive assisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(POSITIVE, RAPID_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(PositiveResultReceived)
            analyticsEventProcessor.track(ResultReceived(POSITIVE, RAPID_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on positive unassisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(POSITIVE, RAPID_SELF_REPORTED)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(PositiveResultReceived)
            analyticsEventProcessor.track(
                ResultReceived(
                    POSITIVE,
                    RAPID_SELF_REPORTED,
                    OUTSIDE_APP
                )
            )
        }
    }

    @Test
    fun `track analytics events on void assisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(VOID, RAPID_RESULT)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(VoidResultReceived)
            analyticsEventProcessor.track(ResultReceived(VOID, RAPID_RESULT, OUTSIDE_APP))
        }
    }

    @Test
    fun `track analytics events on void unassisted LFD result`() = runBlocking {
        disableShowDailyContactTesting()

        setResult(VOID, RAPID_SELF_REPORTED)

        testSubject.ctaToken = "ctaToken"
        testSubject.onContinueButtonClicked()

        coVerifyAll {
            analyticsEventProcessor.track(VoidResultReceived)
            analyticsEventProcessor.track(ResultReceived(VOID, RAPID_SELF_REPORTED, OUTSIDE_APP))
        }
    }

    private fun setResult(
        result: VirologyTestResult,
        testKitType: VirologyTestKitType,
        diagnosisKeySubmissionToken: String = "submissionToken",
        testEndDate: Instant = Instant.now(),
        diagnosisKeySubmissionSupported: Boolean = true,
        requiresConfirmatoryTest: Boolean = false,
        confirmatoryDayLimit: Int? = null
    ): VirologyCtaExchangeResponse {
        val testResultResponse =
            VirologyCtaExchangeResponse(
                diagnosisKeySubmissionToken,
                testEndDate,
                result,
                testKitType,
                diagnosisKeySubmissionSupported,
                requiresConfirmatoryTest,
                confirmatoryDayLimit
            )
        coEvery { ctaTokenValidator.validate(any()) } returns Success(testResultResponse)
        return testResultResponse
    }

    private fun enableShowDailyContactTesting() {
        FeatureFlagTestHelper.enableFeatureFlag(FeatureFlag.DAILY_CONTACT_TESTING)
        setIsolationState(contactCaseOnlyIsolation)

        testSubject.fetchInitialViewState()
    }

    private fun disableShowDailyContactTesting() {
        FeatureFlagTestHelper.disableFeatureFlag(FeatureFlag.DAILY_CONTACT_TESTING)
        setIsolationState(indexAndContactCaseIsolation)

        testSubject.fetchInitialViewState()
    }

    private fun setIsolationState(isolationState: IsolationState) {
        every { isolationStateMachine.readState() } returns isolationState
        every { isolationStateMachine.readLogicalState() } returns isolationState.asLogical()
    }

    private val contactCaseOnlyIsolation = IsolationState(
        isolationConfiguration = DurationDays(),
        contactCase = isolationHelper.contactCase()
    )

    private val indexAndContactCaseIsolation = IsolationState(
        isolationConfiguration = DurationDays(),
        contactCase = isolationHelper.contactCase(),
        indexInfo = isolationHelper.positiveTest(
            AcknowledgedTestResult(
                testEndDate = LocalDate.now(fixedClock),
                acknowledgedDate = LocalDate.now(fixedClock),
                testResult = RelevantVirologyTestResult.POSITIVE,
                testKitType = LAB_RESULT,
                requiresConfirmatoryTest = false,
                confirmedDate = null
            )
        )
    )
}
