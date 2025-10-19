package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;

import java.util.Random;

public class Dice {
    private DiceFace diceState ;
    private int points;
    private boolean taken = false;

    public static final Random RANDOM = new Random();

    public Dice () {
        this.roll();
    }

    public DiceFace getDiceState() {return diceState;}
    public int getPoints(){return points;}
    public boolean isTaken(){return taken;}

    public void roll() {
        if(taken){throw new IllegalStateException("Dice is already taken");}
        int eyes = RANDOM.nextInt(6) + 1;
        this.diceState = DiceFace.fromInt(eyes);
        this.points = diceState.getPoints();
    }

    public void take(){
        if (taken) throw new IllegalStateException("Dice is already taken");
        this.taken = true;
    }


}
