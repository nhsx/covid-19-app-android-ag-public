package uk.nhs.nhsx.covid19.android.app.status.localmessage

import uk.nhs.nhsx.covid19.android.app.remote.data.NotificationMessage
import uk.nhs.nhsx.covid19.android.app.status.GetFirstMessageOfTypeNotification
import javax.inject.Inject

class GetLocalMessageFromStorage @Inject constructor(
    private val localMessagesProvider: LocalMessagesProvider,
    private val getFirstMessageOfTypeNotification: GetFirstMessageOfTypeNotification
) {
    suspend operator fun invoke(): NotificationMessage? {
        val localMessages = localMessagesProvider.localMessages
        return getFirstMessageOfTypeNotification(localMessages)?.message?.translations?.translateOrNull()
    }
}
