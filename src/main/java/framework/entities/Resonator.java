package framework.entities;

import framework.entities.resonatorParts.BaseResonatorPart;

public class Resonator {

    private double length;
    private BaseResonatorPart[] parts;

    public double getLength() {
        return length;
    }

    public Resonator setLength(final double length) {
        this.length = length;
        return this;
    }

    public BaseResonatorPart[] getParts() {
        return parts;
    }

    public Resonator setParts(final BaseResonatorPart[] parts) {
        this.parts = parts;
        return this;
    }
}
