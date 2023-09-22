package dev.zbysiu.homer.announcement

import android.content.SharedPreferences
import dev.zbysiu.homer.R

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement: Announcement = Announcement(
        key = "suggestions_announcement",
        titleRes = R.string.announcement_suggestion_title,
        descriptionRes = R.string.announcement_suggestion_description,
        illustration = Announcement.Illustration.ImageIllustration(R.drawable.ic_suggestions),
        action = null
    )

    override fun getActiveAnnouncement(): Announcement? {

        val isSeen = isActiveAnnouncementSeen()
        return if (!isSeen) {
            activeAnnouncement
        } else {
            null
        }
    }

    private fun isActiveAnnouncementSeen(): Boolean {
        return sharedPreferences.getBoolean(activeAnnouncement.key, false)
    }

    override fun markAnnouncementAsSeen(announcement: Announcement) {
        sharedPreferences.edit()
            .putBoolean(announcement.key, true)
            .apply()
    }
}