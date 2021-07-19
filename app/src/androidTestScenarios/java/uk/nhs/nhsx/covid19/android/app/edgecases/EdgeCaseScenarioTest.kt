package uk.nhs.nhsx.covid19.android.app.edgecases

import android.content.pm.ResolveInfo
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.nhs.nhsx.covid19.android.app.report.Reported
import uk.nhs.nhsx.covid19.android.app.report.Reporter
import uk.nhs.nhsx.covid19.android.app.report.config.TestConfiguration
import uk.nhs.nhsx.covid19.android.app.report.reporter
import uk.nhs.nhsx.covid19.android.app.status.StatusActivity
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.StatusRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.edgecases.EnableBluetoothRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.edgecases.EnableLocationRobot

@RunWith(Parameterized::class)
class EdgeCaseScenarioTest(override val configuration: TestConfiguration) : EspressoTest() {

    private val enableBluetoothRobot = EnableBluetoothRobot()
    private val enableLocationRobot = EnableLocationRobot()
    private val statusRobot = StatusRobot()

    @After
    fun tearDown() {
        testAppContext.packageManager.clear()
    }

    @Test
    fun openingHomeActivityWithBluetoothDisabled_shouldNavigateToEnableBluetoothActivity() {
        startTestActivity<StatusActivity>()

        testAppContext.setBluetoothEnabled(false)

        waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }
    }

    @Test
    fun pressingBackInEnableBluetoothActivity_shouldNotNavigate() {
        startTestActivity<StatusActivity>()

        testAppContext.setBluetoothEnabled(false)

        waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }

        testAppContext.device.pressBack()

        enableBluetoothRobot.checkActivityIsDisplayed()
    }

    @Test
    fun clickingEnableBluetoothButtonInEnableBluetoothActivity_whenCanResolveAndStartActivity_shouldNotShowError() {
        startTestActivity<StatusActivity>()

        testAppContext.setBluetoothEnabled(false)
        testAppContext.packageManager.resolutionsByAction[android.provider.Settings.ACTION_BLUETOOTH_SETTINGS] =
            ResolveInfo()

        waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }

        enableBluetoothRobot.clickAllowBluetoothButton()

        testAppContext.device.pressBack()

        waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }
    }

    @Test
    fun clickingEnableBluetoothButtonInEnableBluetoothActivity_whenCannotResolveActivity_shouldShowError() {
        startTestActivity<StatusActivity>()

        testAppContext.setBluetoothEnabled(false)

        waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }

        enableBluetoothRobot.clickAllowBluetoothButton()

        waitFor { enableBluetoothRobot.checkErrorIsDisplayed() }
    }

    @Test
    @Reported
    fun openingHomeActivityWithBluetoothDisabledThenEnableBluetooth_shouldNavigateBackToHomeActivity() =
        reporter(
            "Edge cases",
            "Enable bluetooth from app",
            "When the user opens the app with Bluetooth disabled, a screen is shown that urges them to start Bluetooth.",
            Reporter.Kind.FLOW
        ) {
            startTestActivity<StatusActivity>()

            testAppContext.setBluetoothEnabled(false)

            waitFor { enableBluetoothRobot.checkActivityIsDisplayed() }

            step(
                "Start",
                "The user is presented with a screen that shows that bluetooth has to be enabled to use the app."
            )

            testAppContext.setBluetoothEnabled(true)

            step(
                "Bluetooth enabled",
                "After successfully enabling bluetooth, the app navigates back to the home screen."
            )

            waitFor { statusRobot.checkActivityIsDisplayed() }
        }

    @Test
    fun openingHomeActivityWithLocationServicesDisabled_shouldNavigateToEnableLocationActivity() {
        startTestActivity<StatusActivity>()

        testAppContext.setLocationEnabled(false)

        waitFor { enableLocationRobot.checkActivityIsDisplayed() }
    }

    @Test
    fun pressingBackInEnableLocationActivity_shouldNotNavigate() {
        startTestActivity<StatusActivity>()

        testAppContext.setLocationEnabled(false)

        waitFor { enableLocationRobot.checkActivityIsDisplayed() }

        testAppContext.device.pressBack()

        enableLocationRobot.checkActivityIsDisplayed()
    }

    @Test
    fun openingHomeActivityWithLocationServicesDisabled_whenDeviceSupportsLocationlessScanning_shouldStayOnStatusActivity() {
        testAppContext.getExposureNotificationApi().setDeviceSupportsLocationlessScanning(true)
        startTestActivity<StatusActivity>()

        testAppContext.setLocationEnabled(false)

        statusRobot.checkActivityIsDisplayed()
    }

    @Test
    @Reported
    fun openingHomeActivityWithLocationServicesDisabledThenEnableLocationServices_shouldNavigateBackToHomeActivity() =
        reporter(
            "Edge cases",
            "Enable location services from app",
            "When the user opens the app with location services disabled, a screen is shown that urges them to start location services.",
            Reporter.Kind.FLOW
        ) {
            startTestActivity<StatusActivity>()

            testAppContext.setLocationEnabled(false)

            waitFor { enableLocationRobot.checkActivityIsDisplayed() }

            step(
                "Start",
                "The user is presented with a screen that shows that location services has to be enabled to use the app."
            )

            testAppContext.setLocationEnabled(true)

            step(
                "Location services enabled",
                "After successfully enabling location services, the app navigates back to the home screen."
            )

            waitFor { statusRobot.checkActivityIsDisplayed() }
        }
}
