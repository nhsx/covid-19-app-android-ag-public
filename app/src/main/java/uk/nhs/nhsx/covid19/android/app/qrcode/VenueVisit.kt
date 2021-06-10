package uk.nhs.nhsx.covid19.android.app.qrcode

import android.content.Context
import com.squareup.moshi.JsonClass
import uk.nhs.nhsx.covid19.android.app.util.toLocalDate
import uk.nhs.nhsx.covid19.android.app.util.uiFormat
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@JsonClass(generateAdapter = true)
data class VenueVisit(
    val id: String = UUID.randomUUID().toString(),
    val venue: Venue,
    val from: Instant,
    val to: Instant,
    val wasInRiskyList: Boolean = false
)

fun VenueVisit.uiDate(context: Context, zoneId: ZoneId = ZoneId.systemDefault()): String {

    // Subtract 1 second in order to show time of 23:59 instead of 00:00
    val uiTo = to.minusSeconds(1L)

    val dateFrom = from.toLocalDate(zoneId).uiFormat(context)
    val dateTo = uiTo.toLocalDate(zoneId).uiFormat(context)

    val timeFrom = from.atZone(zoneId).toLocalTime().uiFormat()
    val timeTo = uiTo.atZone(zoneId).toLocalTime().uiFormat()

    val timeSpan = "$timeFrom - $timeTo"

    return if (dateTo == dateFrom) {
        "$dateFrom $timeSpan"
    } else {
        "$dateFrom $timeSpan $dateTo"
    }
}
