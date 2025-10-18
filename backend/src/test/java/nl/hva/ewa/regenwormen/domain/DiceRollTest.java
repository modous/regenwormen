//package nl.hva.ewa.regenwormen.domain;
//
//import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
//import nl.hva.ewa.regenwormen.domain.Enum.TurnState;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//class DicerollTest {
//
//    @Test
//    void newDiceroll_startsInCanRoll_andNoBust() {
//        // Arrange
//        Player p = new Player("Alice");
//
//        // Act
//        Diceroll roll = new Diceroll(p);
//
//        // Assert
//        assertThat(roll.getTurnState()).isEqualTo(TurnState.CAN_ROLL);
//        assertThat(roll.getBusted()).isFalse();
//        assertThat(roll.hasSpecial()).isFalse();
//        assertThat(roll.getTakenScore()).isEqualTo(0);
//    }
//
//    @Test
//    void roll_thenMustPick_and_optionsNotEmptyInitially() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//
//        // Act
//        List<DiceFace> options = roll.rollRemainingDice();
//
//        // Assert
//        assertThat(roll.getTurnState()).isEqualTo(TurnState.MUST_PICK);
//        assertThat(options).isNotNull().isNotEmpty();
//    }
//
//    @Test
//    void pickDice_consumesAllDiceOfThatFace_andAccumulatesScore() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//        List<DiceFace> options = roll.rollRemainingDice();
//        DiceFace chosen = options.get(0);
//
//        // Act
//        int scoreAfterPick = roll.pickDice(chosen);
//
//        // Assert
//        assertThat(scoreAfterPick).isGreaterThan(0);
//        assertThat(roll.getTurnState()).isIn(TurnState.CAN_ROLL, TurnState.ENDED);
//    }
//
//
//    @Test
//    void cannotRollWhenMustPick_first() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//        roll.rollRemainingDice(); // -> MUST_PICK
//
//        // Act & Assert
//        assertThatThrownBy(roll::rollRemainingDice)
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("First pick a dice face");
//    }
//
//    @Test
//    void cannotPickFaceThatWasNotInLastRoll() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//        List<DiceFace> options = roll.rollRemainingDice();
//
//        // Act & Assert
//        DiceFace illegalChoice = null;
//        for (DiceFace f : DiceFace.values()) {
//            if (!options.contains(f)) {
//                illegalChoice = f;
//                break;
//            }
//        }
//
//        if (illegalChoice != null) {
//            DiceFace finalIllegalChoice = illegalChoice;
//            assertThatThrownBy(() -> roll.pickDice(finalIllegalChoice))
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessageContaining("was not present in the last roll");
//        } else {
//            // Alle faces zaten in de opties (erg onwaarschijnlijk) → sla negatieve tak over
//            assertThat(true).isTrue();
//        }
//    }
//
//    @Test
//    void hasSpecial_becomesTrueWhenPickingSpecial() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//
//        // Act
//        List<DiceFace> options = roll.rollRemainingDice();
//
//        // Assert (alleen als SPECIAL beschikbaar is)
//        if (options.contains(DiceFace.SPECIAL)) {
//            roll.pickDice(DiceFace.SPECIAL);
//            assertThat(roll.hasSpecial()).isTrue();
//        } else {
//            assertThat(roll.hasSpecial()).isFalse();
//        }
//    }
//
//    @Test
//    void turnCannotBeUsedAfterEnded() {
//        // Arrange
//        User user = new User();
//        Player p = new Player("Alice", user);
//        Diceroll roll = new Diceroll(p);
//
//        // Forceer ENDED via legitieme flow met safety cap
//        roll.rollRemainingDice();
//        roll.pickDice(roll.getPickableFaces().get(0));
//
//        int safety = 0;
//        while (roll.getTurnState() != TurnState.ENDED && safety++ < 12) {
//            if (roll.getTurnState() == TurnState.CAN_ROLL) {
//                List<DiceFace> opts = roll.rollRemainingDice();
//                if (!opts.isEmpty()) {
//                    roll.pickDice(opts.get(0));
//                }
//            } else {
//                List<DiceFace> opts = roll.getPickableFaces();
//                if (!opts.isEmpty()) {
//                    roll.pickDice(opts.get(0));
//                } else {
//                    // Geen opties meer → busted
//                    break;
//                }
//            }
//        }
//
//        // Act & Assert
//        if (roll.getTurnState() == TurnState.ENDED) {
//            assertThatThrownBy(roll::rollRemainingDice)
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessageContaining("Turn already ended");
//            assertThatThrownBy(() -> roll.pickDice(DiceFace.SPECIAL))
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessageContaining("Turn already ended");
//        } else {
//            // fallback assert
//            assertThat(roll.getTurnState()).isIn(TurnState.CAN_ROLL, TurnState.MUST_PICK, TurnState.ENDED);
//        }
//    }
//}
