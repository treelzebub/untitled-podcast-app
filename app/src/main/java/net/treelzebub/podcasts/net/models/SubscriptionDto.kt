package net.treelzebub.podcasts.net.models

data class SubscriptionDto(
    val id: String,
    val rssLink: String
)

data class SubscriptionUpdateRequest(
    val sub: SubscriptionDto
)

data class SubscriptionUpdateResponse(
    val code: Int,
    val error: ErrorType = ErrorType.None
) {
    enum class ErrorType {
        None, BrokenLink, Unknown
    }
}