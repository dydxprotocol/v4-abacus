package exchange.dydx.abacus.utils

import kotlin.test.Test
import kotlin.test.assertTrue

class RounderTests {

    @Test
    fun testQuickRound() {
        var result = Rounder.quickRound2(0.123456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 0.12 }

        result = Rounder.quickRound2(0.128456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 0.12 }

        result = Rounder.quickRound2(0.128456789, 0.01, Rounder.RoundingMode.NEAREST)
        assertTrue { result == 0.13 }

        result = Rounder.quickRound2(-0.123456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == -0.12 }

        result = Rounder.quickRound2(-0.128456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == -0.12 }

        result = Rounder.quickRound2(-0.128456789, 0.01, Rounder.RoundingMode.NEAREST)
        assertTrue { result == -0.13 }

        result = Rounder.quickRound2(123456789.0, 100.0, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 123456700.0 }

        result = Rounder.quickRound2(123456789.0, 100.0, Rounder.RoundingMode.NEAREST)
        assertTrue { result == 123456800.0 }
    }

    @Test
    fun testRound() {
        var result = Rounder.round(0.123456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 0.12 }

        result = Rounder.round(0.128456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 0.12 }

        result = Rounder.round(0.128456789, 0.01, Rounder.RoundingMode.NEAREST)
        assertTrue { result == 0.13 }

        result = Rounder.round(-0.123456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == -0.12 }

        result = Rounder.round(-0.128456789, 0.01, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == -0.12 }

        result = Rounder.round(-0.128456789, 0.01, Rounder.RoundingMode.NEAREST)
        assertTrue { result == -0.13 }

        result = Rounder.round(123456789.0, 100.0, Rounder.RoundingMode.TOWARDS_ZERO)
        assertTrue { result == 123456700.0 }

        result = Rounder.round(123456789.0, 100.0, Rounder.RoundingMode.NEAREST)
        assertTrue { result == 123456800.0 }
    }
}
