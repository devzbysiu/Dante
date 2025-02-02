package dev.zbysiu.homer.core.data.local

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class DanteRealmMigration : RealmMigration {

    private enum class Migrations {
        BASE,
        DATES,
        RATING_LANG,
        PAGES_NOTES,
        NAME_REFACTORING,
        SUMMARY_AND_LABELS,
        LABEL_OBJECT,
        LABEL_BOOK_ID,
        PAGES_RECORD,
        PAGES_PRIMARY_KEY
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var versionCounter = oldVersion
        val schema = realm.schema

        if (versionCounter == Migrations.BASE.v()) {
            migrateDates(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.DATES.v()) {
            migrateRatingAndLanguage(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.RATING_LANG.v()) {
            migrateBookPageCountAndNotes(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.PAGES_NOTES.v()) {
            migrateNameRefactoring(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.NAME_REFACTORING.v()) {
            migrateSummaryAndLabels(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.SUMMARY_AND_LABELS.v()) {
            migrateToLabelObjects(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.LABEL_OBJECT.v()) {
            migrateToLabelBookIds(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.LABEL_BOOK_ID.v()) {
            migrateToPagesRecord(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.PAGES_RECORD.v()) {
            migrateToPagesPrimaryKey(schema)
        }
    }

    private fun migrateDates(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("startDate", Long::class.java)
                ?.addField("endDate", Long::class.java)
                ?.addField("wishlistDate", Long::class.java)
    }

    private fun migrateRatingAndLanguage(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("rating", Int::class.java)
                ?.addField("language", String::class.java)
    }

    private fun migrateBookPageCountAndNotes(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("currentPage", Int::class.java)
                ?.addField("notes", String::class.java)
    }

    private fun migrateNameRefactoring(schema: RealmSchema) {
        schema.rename("Book", "RealmBook")
        schema.rename("BookConfig", "RealmBookConfig")
    }

    private fun migrateSummaryAndLabels(schema: RealmSchema) {
        schema.get("RealmBook")
                ?.addField("summary", String::class.java)
                ?.addRealmListField("labels", String::class.java)
    }

    private fun migrateToLabelObjects(schema: RealmSchema) {

        val labelSchema = schema.create("RealmBookLabel")
                .addField("title", String::class.java)
                .addField("hexColor", String::class.java)

        schema.get("RealmBook")
                ?.removeField("labels")
                ?.addRealmListField("labels", labelSchema)
    }

    private fun migrateToLabelBookIds(schema: RealmSchema) {

        schema.get("RealmBookLabel")
            ?.addField("bookId", Long::class.java)
    }

    private fun migrateToPagesRecord(schema: RealmSchema) {

        schema.create("RealmPageRecord")
                .addField("bookId", Long::class.java)
                .addField("fromPage", Int::class.java)
                .addField("toPage", Int::class.java)
                .addField("timestamp", Long::class.java)
    }

    private fun migrateToPagesPrimaryKey(schema: RealmSchema) {
        schema.get("RealmPageRecord")
                ?.addField("recordId", String::class.java)
                ?.transform { dynamicObject ->

                    val primaryId = with(dynamicObject) {
                        val bookId = getLong("bookId")
                        val timestamp = getLong("timestamp")
                        "$bookId-$timestamp"
                    }
                    dynamicObject.set("recordId", primaryId)
                }
                ?.addPrimaryKey("recordId")
    }

    companion object {

        private fun Migrations.v(): Long = this.ordinal.toLong()

        val migrationVersion = Migrations.PAGES_PRIMARY_KEY.v()
    }
}
