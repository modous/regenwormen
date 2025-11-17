package nl.hva.ewa.regenwormen.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {

    @Test
    void testCalculateTotalScore() {
        // Tests that the total score is correctly calculated for a normal list of positive integers
        ScoreCalculator calc = new ScoreCalculator();
        assertEquals(10, calc.calculateTotalScore(List.of(1, 2, 3, 4)));

        // Tests that an empty list results in a score of 0
        assertEquals(0, calc.calculateTotalScore(List.of()));
    }

    @Test
    void testCalculateTotalScoreWithNegative() {
        // Tests that negative numbers are handled correctly and included in the sum
        ScoreCalculator calc = new ScoreCalculator();
        assertEquals(2, calc.calculateTotalScore(List.of(3, -1)));
    }
}
