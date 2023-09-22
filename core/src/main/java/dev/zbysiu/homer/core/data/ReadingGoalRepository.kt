package dev.zbysiu.homer.core.data

import dev.zbysiu.homer.core.book.ReadingGoal
import io.reactivex.Completable
import io.reactivex.Single

interface ReadingGoalRepository {

    fun retrievePagesPerMonthReadingGoal(): Single<ReadingGoal.PagesPerMonthReadingGoal>

    fun storePagesPerMonthReadingGoal(goal: Int): Completable

    fun resetPagesPerMonthReadingGoal(): Completable

    fun retrieveBookPerMonthReadingGoal(): Single<ReadingGoal.BooksPerMonthReadingGoal>

    fun storeBooksPerMonthReadingGoal(goal: Int): Completable

    fun resetBooksPerMonthReadingGoal(): Completable
}