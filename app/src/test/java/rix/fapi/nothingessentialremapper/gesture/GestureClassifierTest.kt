package rix.fapi.nothingessentialremapper.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GestureClassifierTest {

    private val classifier = GestureClassifier(longPressThresholdMs = 500L, multiPressTimeoutMs = 300L)

    @Test
    fun `short tap followed by timeout is single press`() {
        classifier.onKeyDown(0L)
        assertNull(classifier.onKeyUp(100L))
        assertEquals(GestureType.SINGLE_PRESS, classifier.onMultiPressTimeout())
    }

    @Test
    fun `two short taps followed by timeout is double press`() {
        classifier.onKeyDown(0L)
        assertNull(classifier.onKeyUp(100L))
        classifier.onKeyDown(200L)
        assertNull(classifier.onKeyUp(280L))
        assertEquals(GestureType.DOUBLE_PRESS, classifier.onMultiPressTimeout())
    }

    @Test
    fun `three short taps resolve immediately as triple press`() {
        classifier.onKeyDown(0L)
        assertNull(classifier.onKeyUp(80L))
        classifier.onKeyDown(150L)
        assertNull(classifier.onKeyUp(220L))
        classifier.onKeyDown(300L)
        assertEquals(GestureType.TRIPLE_PRESS, classifier.onKeyUp(360L))
    }

    @Test
    fun `holding past the threshold is a long press`() {
        classifier.onKeyDown(0L)
        assertEquals(GestureType.LONG_PRESS, classifier.onKeyUp(600L))
    }

    @Test
    fun `state resets after a finalized gesture`() {
        classifier.onKeyDown(0L)
        classifier.onKeyUp(600L)
        classifier.onKeyDown(1000L)
        assertNull(classifier.onKeyUp(1080L))
        assertEquals(GestureType.SINGLE_PRESS, classifier.onMultiPressTimeout())
    }
}
