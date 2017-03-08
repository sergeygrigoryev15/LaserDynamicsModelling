package framework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import framework.entities.Resonator;
import framework.entities.resonatorParts.ActiveMedia;
import framework.entities.resonatorParts.BaseResonatorPart;
import framework.entities.resonatorParts.SaturableAbsorber;
import framework.entities.resonatorParts.Vacuum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ResonatorBuilder {

    private static final String RESOURCES_DIR = System.getProperty("user.dir").concat("/src/main/resources/");

    private static Logger logger = LogManager.getLogger();
    private static JsonParser parser = new JsonParser();
    private static JsonObject jsonObject;
    private static final String fileName = "Resonator.json";
    private static final File source = new File(RESOURCES_DIR.concat(fileName));

    private enum ResonatorElementType {
        ACTIVE_MEDIA("active media"),
        VACUUM("vacuum"),
        SATURABLE_ABSORBER("saturable absorber");

        private final String value;

        ResonatorElementType(String value) {
            this.value = value;
        }

        @Contract(pure = true)
        public String toString(){
            return this.value;
        }

        public static ResonatorElementType getNameByValue(String value){
            return Arrays.stream(ResonatorElementType.values()).filter(type->value.equals(type.toString())).findFirst().get();
        }
    }

    public ResonatorBuilder() {
        try {
            jsonObject = (JsonObject) parser.parse(new FileReader(source));
        } catch (FileNotFoundException ex) {
            jsonObject = null;
            logger.fatal("JSON file can't be found: " + ex);
        }
    }

    private int getNumberOfResonatorElements() {
        return getResonatorElements().size();
    }

    private JsonArray getResonatorElements() {
        return jsonObject.get("parts").getAsJsonArray();
    }

    private double getResonatorLength() {
        return jsonObject.get("length").getAsDouble();
    }

    private BaseResonatorPart[] getResonatorParts() {
        BaseResonatorPart[] parts = new BaseResonatorPart[getNumberOfResonatorElements()];
        IntStream.range(0, getNumberOfResonatorElements()).forEach(i -> {
            JsonObject tmpObject = getResonatorElements().get(i).getAsJsonObject();
            ResonatorElementType type = ResonatorElementType.getNameByValue(tmpObject.get("part_name").getAsString());
            BaseResonatorPart part;
            switch (type) {
                case ACTIVE_MEDIA:
                    part = new ActiveMedia();
                    break;
                case VACUUM:
                    part = new Vacuum();
                    break;
                case SATURABLE_ABSORBER:
                    part = new SaturableAbsorber();
                    break;
                default:
                    part = new BaseResonatorPart();
            }
            part.setLength(tmpObject.get("length").getAsDouble());
            parts[i] = part;
        });
        return parts;
    }

    public Resonator buildResonator(){
        return new Resonator()
                .setLength(getResonatorLength())
                .setParts(getResonatorParts());
    }
}
