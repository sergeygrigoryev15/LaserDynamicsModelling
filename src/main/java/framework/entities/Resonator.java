package framework.entities;

import framework.entities.resonatorParts.BaseResonatorPart;
import framework.enums.ResonatorElementType;

import java.util.HashMap;

public class Resonator {

    private double length;
    private HashMap<ResonatorElementType, BaseResonatorPart> parts;

    public double getLength() {
        return length;
    }

    public Resonator setLength(final double length) {
        this.length = length;
        return this;
    }

    public HashMap<ResonatorElementType, BaseResonatorPart> getParts() {
        return parts;
    }

    public Resonator setParts(final HashMap<ResonatorElementType, BaseResonatorPart> parts) {
        this.parts = parts;
        return this;
    }
}
