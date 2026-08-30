package rix.fapi.nothingessentialremapper.gesture

/**
 * Pure state machine that turns raw key down/up timestamps into a [GestureType].
 *
 * The caller owns the clock: it must call [onKeyDown]/[onKeyUp] as events arrive, and
 * schedule a call to [onMultiPressTimeout] after [multiPressTimeoutMillis] of inactivity
 * following a key-up that returned null (i.e. "could still be the start of a multi-tap").
 */
class GestureClassifier(
    private val longPressThresholdMs: Long = 500L,
    private val multiPressTimeoutMs: Long = 350L
) {
    val multiPressTimeoutMillis: Long get() = multiPressTimeoutMs

    private var keyDownAt: Long = 0L
    private var tapCount: Int = 0

    fun onKeyDown(timestampMs: Long) {
        keyDownAt = timestampMs
    }

    /**
     * Returns the finalized gesture if this key-up resolves it immediately (long press, or the
     * maximum supported tap count was reached), or null if the caller must wait up to
     * [multiPressTimeoutMillis] for a possible next tap before calling [onMultiPressTimeout].
     */
    fun onKeyUp(timestampMs: Long): GestureType? {
        val heldFor = timestampMs - keyDownAt
        if (heldFor >= longPressThresholdMs) {
            tapCount = 0
            return GestureType.LONG_PRESS
        }

        tapCount += 1
        if (tapCount >= MAX_TAPS) {
            tapCount = 0
            return GestureType.TRIPLE_PRESS
        }
        return null
    }

    /** Called when [multiPressTimeoutMillis] elapses with no further key-down. */
    fun onMultiPressTimeout(): GestureType? {
        val result = when (tapCount) {
            1 -> GestureType.SINGLE_PRESS
            2 -> GestureType.DOUBLE_PRESS
            else -> null
        }
        tapCount = 0
        return result
    }

    private companion object {
        const val MAX_TAPS = 3
    }
}
