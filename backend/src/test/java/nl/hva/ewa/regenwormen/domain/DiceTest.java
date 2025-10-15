package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DiceTest {

    @Test
    void roll_setsDiceFace_andPointsBetween1And6() {
        // Arrange
        Dice dice = new Dice(); // constructor rolt al automatisch

        // Act
        DiceFace result = dice.getDiceState();
        int points = dice.getPoints();

        // Assert
        assertThat(result).isNotNull();
        assertThat(points)
                .as("Dice points should be between 1 and 6")
                .isBetween(1, 6);
    }

    @Test
    void take_marksDiceAsTaken_andPreventsReroll() {
        // Arrange
        Dice dice = new Dice();

        // Act
        dice.take();

        // Assert
        assertThat(dice.isTaken()).isTrue();

        // Act & Assert (reroll mag niet meer)
        assertThatThrownBy(dice::roll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already taken");

        // Act & Assert (nogmaals take mag niet)
        assertThatThrownBy(dice::take)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void roll_assignsNewFaceEachTime_whenNotTaken() {
        // Arrange
        Dice dice = new Dice();
        DiceFace first = dice.getDiceState();

        // Act
        dice.roll();
        DiceFace second = dice.getDiceState();

        // Assert
        assertThat(second)
                .as("New roll should assign a (potentially) different face")
                .isNotNull();
        // het kan toevallig gelijk zijn, maar in 1 test mag dat
    }

    @Test
    void constructor_rollsAutomatically() {
        // Arrange & Act
        Dice dice = new Dice();

        // Assert
        assertThat(dice.getDiceState())
                .as("Constructor should auto-roll a face")
                .isNotNull();
    }
}
