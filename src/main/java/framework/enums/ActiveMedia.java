package framework.enums;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

public enum ActiveMedia {
    RUBY("Ruby");

    private final String value;

    ActiveMedia(final String value) {
        this.value = value;
    }

    @Contract(pure = true)
    public String toString() {
        return this.value;
    }

    public static ActiveMedia getNameByValue(final String value) {
        return Arrays.stream(ActiveMedia.values()).filter(type -> value.equals(type.toString())).findFirst().get();
    }
}
