package nl.hva.ewa.regenwormen.domain.Enum;

public enum DiceFace {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SPECIAL(5);

    private final int points;

    DiceFace(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    // Helper om een int-waarde om te zetten naar een enum
    public static DiceFace fromInt(int eyes) {
        return switch (eyes) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            case 5 -> FIVE;
            case 6 -> SPECIAL;
            default -> throw new IllegalArgumentException("Invalid dice eyes: " + eyes);
        };
    }
}
