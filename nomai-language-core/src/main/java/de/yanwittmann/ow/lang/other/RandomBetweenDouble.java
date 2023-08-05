package de.yanwittmann.ow.lang.other;

import java.util.Random;

public class RandomBetweenDouble {
    protected final double min;
    protected final double max;

    public RandomBetweenDouble(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double next() {
        return next(new Random());
    }

    public double next(Random random) {
        return min + (max - min) * random.nextDouble();
    }
}
