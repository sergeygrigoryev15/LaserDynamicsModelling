package framework.enums;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

public enum SaturableAbsorber {
    NDYAG("NdYag");

    private final String value;

    SaturableAbsorber(final String value) {
        this.value = value;
    }

    @Contract(pure = true)
    public String toString() {
        return this.value;
    }

    public static SaturableAbsorber getNameByValue(final String value) {
        return Arrays.stream(SaturableAbsorber.values()).filter(type -> value.equals(type.toString())).findFirst().get();
    }
}
