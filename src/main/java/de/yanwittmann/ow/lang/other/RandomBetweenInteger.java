package de.yanwittmann.ow.lang.other;

import java.util.Random;

public class RandomBetweenInteger {
    protected final int min;
    protected final int max;

    public RandomBetweenInteger(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int next() {
        return next(new Random());
    }

    public int next(Random random) {
        return random.nextInt(max - min) + min;
    }
}
