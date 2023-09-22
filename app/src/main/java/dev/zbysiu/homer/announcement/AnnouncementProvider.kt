package dev.zbysiu.homer.announcement

interface AnnouncementProvider {

    fun getActiveAnnouncement(): Announcement?

    fun markAnnouncementAsSeen(announcement: Announcement)
}