package framework.enums;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

public enum ResonatorElementType {
    ACTIVE_MEDIA("active media"),
    VACUUM("vacuum"),
    SATURABLE_ABSORBER("saturable absorber");

    private final String value;

    ResonatorElementType(final String value) {
        this.value = value;
    }

    @Contract(pure = true)
    public String toString() {
        return this.value;
    }

    public static ResonatorElementType getNameByValue(final String value) {
        return Arrays.stream(ResonatorElementType.values()).filter(type -> value.equals(type.toString())).findFirst().get();
    }
}
