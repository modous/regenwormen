package nl.hva.ewa.regenwormen.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {

    @Test
    void testCalculateTotalScore() {
        ScoreCalculator calc = new ScoreCalculator();
        assertEquals(10, calc.calculateTotalScore(List.of(1, 2, 3, 4)));
        assertEquals(0, calc.calculateTotalScore(List.of()));
    }

    @Test
    void testCalculateTotalScoreWithNegative() {
        ScoreCalculator calc = new ScoreCalculator();
        assertEquals(2, calc.calculateTotalScore(List.of(3, -1)));
    }
}
