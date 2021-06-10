package uk.nhs.nhsx.covid19.android.app.status.testinghub

import androidx.test.platform.app.InstrumentationRegistry
import com.jeroenmols.featureflag.framework.FeatureFlag.DAILY_CONTACT_TESTING
import com.jeroenmols.featureflag.framework.TestSetting.USE_WEB_VIEW_FOR_INTERNAL_BROWSER
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.flow.functionalities.DailyContactTesting
import uk.nhs.nhsx.covid19.android.app.flow.functionalities.OrderTest
import uk.nhs.nhsx.covid19.android.app.remote.MockVirologyTestingApi
import uk.nhs.nhsx.covid19.android.app.report.notReported
import uk.nhs.nhsx.covid19.android.app.state.IsolationHelper
import uk.nhs.nhsx.covid19.android.app.state.asIsolation
import uk.nhs.nhsx.covid19.android.app.status.StatusActivity
import uk.nhs.nhsx.covid19.android.app.testhelpers.TestApplicationContext
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.BrowserRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LinkTestResultOnsetDateRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LinkTestResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LinkTestResultSymptomsRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.StatusRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.TestOrderingRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.TestResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.TestingHubRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.runWithFeatureEnabled
import java.time.LocalDate

class TestingHubScenarioTest : EspressoTest() {

    private val statusRobot = StatusRobot()
    private val testingHubRobot = TestingHubRobot()
    private val testOrderingRobot = TestOrderingRobot()
    private val orderTest = OrderTest(this)
    private val dailyContactTesting = DailyContactTesting()
    private val browserRobot = BrowserRobot()
    private val linkTestResultRobot = LinkTestResultRobot()
    private val linkTestResultSymptomsRobot = LinkTestResultSymptomsRobot()
    private val linkTestResultOnsetDateRobot = LinkTestResultOnsetDateRobot()
    private val testResultRobot = TestResultRobot(InstrumentationRegistry.getInstrumentation().targetContext)
    private val isolationHelper = IsolationHelper(testAppContext.clock)

    @Test
    fun activeIsolation_thenNavigateToBookATest_tapBack_shouldShowTestingHub() = notReported {
        testAppContext.setState(isolationHelper.contactCase().asIsolation())

        startTestActivity<StatusActivity>()

        statusRobot.checkActivityIsDisplayed()
        statusRobot.clickTestingHub()

        testingHubRobot.checkActivityIsDisplayed()
        testingHubRobot.clickBookTest()

        testOrderingRobot.checkActivityIsDisplayed()

        testAppContext.device.pressBack()

        testingHubRobot.checkActivityIsDisplayed()
    }

    @Test
    fun activeIsolation_thenNavigateToBookATest_bookATest_shouldShowStatusActivity() = notReported {
        testAppContext.setState(isolationHelper.contactCase().asIsolation())

        startTestActivity<StatusActivity>()

        statusRobot.checkActivityIsDisplayed()
        statusRobot.clickTestingHub()

        testingHubRobot.checkActivityIsDisplayed()
        testingHubRobot.clickBookTest()

        testOrderingRobot.checkActivityIsDisplayed()

        orderTest()

        statusRobot.checkActivityIsDisplayed()
    }

    @Test
    fun noActiveIsolation_thenNavigateToFindOutAboutTesting_closeBrowser_shouldShowStatusActivity() = notReported {
        runWithFeatureEnabled(USE_WEB_VIEW_FOR_INTERNAL_BROWSER) {
            testAppContext.setState(isolationHelper.neverInIsolation())

            startTestActivity<StatusActivity>()

            statusRobot.checkActivityIsDisplayed()
            statusRobot.clickTestingHub()

            testingHubRobot.checkActivityIsDisplayed()
            testingHubRobot.checkBookTestIsNotDisplayed()
            testingHubRobot.clickFindOutAboutTesting()

            waitFor { browserRobot.checkActivityIsDisplayed() }
            browserRobot.clickCloseButton()

            statusRobot.checkActivityIsDisplayed()
        }
    }

    @Test
    fun navigateToEnterTestResultViaTestingHub_enterValidToken_receivedApiResult_shouldShowStatusActivity() =
        notReported {
            testAppContext.setLocalAuthority(TestApplicationContext.ENGLISH_LOCAL_AUTHORITY)

            startTestActivity<StatusActivity>()

            statusRobot.checkActivityIsDisplayed()
            statusRobot.clickTestingHub()

            testingHubRobot.checkActivityIsDisplayed()
            testingHubRobot.clickEnterTestResult()

            linkTestResultRobot.checkActivityIsDisplayed()
            // LFD token because we want to skip the symptom onset flow
            linkTestResultRobot.enterCtaToken(MockVirologyTestingApi.POSITIVE_LFD_TOKEN_INDICATIVE_NO_KEY_SUBMISSION)
            linkTestResultRobot.clickContinue()

            waitFor {
                testResultRobot.checkActivityDisplaysPositiveWillBeInIsolationAndOrderTest()
            }
            testResultRobot.clickCloseButton()

            statusRobot.checkActivityIsDisplayed()
        }

    @Test
    fun navigateToEnterTestResultViaTestingHub_requiresSymptomOnsetDate_completeFlow_shouldShowStatusActivity() =
        notReported {
            testAppContext.setLocalAuthority(TestApplicationContext.ENGLISH_LOCAL_AUTHORITY)

            startTestActivity<StatusActivity>()

            statusRobot.checkActivityIsDisplayed()
            statusRobot.clickTestingHub()

            testingHubRobot.checkActivityIsDisplayed()
            testingHubRobot.clickEnterTestResult()

            linkTestResultRobot.checkActivityIsDisplayed()
            // PCR token because we want to enter the symptom onset flow
            linkTestResultRobot.enterCtaToken(MockVirologyTestingApi.POSITIVE_PCR_TOKEN_NO_KEY_SUBMISSION)
            linkTestResultRobot.clickContinue()

            waitFor { linkTestResultSymptomsRobot.checkActivityIsDisplayed() }
            linkTestResultSymptomsRobot.clickYes()

            linkTestResultOnsetDateRobot.checkActivityIsDisplayed()
            linkTestResultOnsetDateRobot.clickSelectDate()
            val onsetDate = LocalDate.now(testAppContext.clock).minusDays(3)
            linkTestResultOnsetDateRobot.selectDayOfMonth(onsetDate.dayOfMonth)
            linkTestResultOnsetDateRobot.clickContinueButton()

            waitFor {
                testResultRobot.checkActivityDisplaysPositiveWillBeInIsolation()
            }
            testResultRobot.clickIsolationActionButton()

            statusRobot.checkActivityIsDisplayed()
        }

    @Test
    fun navigateToEnterTestResultViaTestingHub_receiveErrorFromApi_shouldShowError_tapBack_shouldShowTestingHub() =
        notReported {
            testAppContext.setLocalAuthority(TestApplicationContext.ENGLISH_LOCAL_AUTHORITY)

            startTestActivity<StatusActivity>()

            statusRobot.checkActivityIsDisplayed()
            statusRobot.clickTestingHub()

            testingHubRobot.checkActivityIsDisplayed()
            testingHubRobot.clickEnterTestResult()

            linkTestResultRobot.checkActivityIsDisplayed()

            // Force failure response and tap return to verify appropriate navigation behaviour
            testAppContext.virologyTestingApi.testResultForCtaTokenStatusCode = 500

            linkTestResultRobot.enterCtaToken(MockVirologyTestingApi.POSITIVE_PCR_TOKEN)
            linkTestResultRobot.clickContinue()

            waitFor { linkTestResultRobot.checkValidationErrorUnexpectedIsDisplayed() }

            testAppContext.device.pressBack()

            testingHubRobot.checkActivityIsDisplayed()
        }

    @Test
    fun userParticipatesInDailyContactTesting_completesFlow_shouldShowStatusActivity() = notReported {
        runWithFeatureEnabled(DAILY_CONTACT_TESTING) {
            testAppContext.setState(isolationHelper.contactCase().asIsolation())

            startTestActivity<StatusActivity>()

            dailyContactTesting.optIn()

            statusRobot.checkActivityIsDisplayed()
        }
    }
}
