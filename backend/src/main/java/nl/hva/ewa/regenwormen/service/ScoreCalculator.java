package nl.hva.ewa.regenwormen.service;

import java.util.List;

public class ScoreCalculator {
    public int calculateTotalScore(List<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }
}
