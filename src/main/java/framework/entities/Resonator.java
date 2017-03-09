package framework.entities;

import framework.entities.resonatorParts.BaseResonatorPart;

public class Resonator {

    public static final float speedOfLight = 3.0e8f;
    private double length;
    private BaseResonatorPart[] parts;

    public double getLength() {
        return length;
    }

    public Resonator setLength(double length) {
        this.length = length;
        return this;
    }

    public BaseResonatorPart[] getParts() {
        return parts;
    }

    public Resonator setParts(BaseResonatorPart[] parts) {
        this.parts = parts;
        return this;
    }
}
