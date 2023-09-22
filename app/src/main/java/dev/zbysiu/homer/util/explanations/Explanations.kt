package dev.zbysiu.homer.util.explanations

interface Explanations {

    fun suggestion(): Explanation.Suggestion

    fun wishlist(): Explanation.Wishlist

    fun markSeen(explanation: Explanation)

    fun update(explanation: Explanation)
}
