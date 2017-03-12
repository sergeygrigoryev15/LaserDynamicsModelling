package framework.enums;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

public enum SaturableAbsorber {
    NDYAG("NdYag");

    private final String value;

    SaturableAbsorber(String value) {
        this.value = value;
    }

    @Contract(pure = true)
    public String toString(){
        return this.value;
    }

    public static SaturableAbsorber getNameByValue(String value){
        return Arrays.stream(SaturableAbsorber.values()).filter(type->value.equals(type.toString())).findFirst().get();
    }
}
