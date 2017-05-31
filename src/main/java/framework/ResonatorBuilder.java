package framework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import framework.entities.Resonator;
import framework.entities.resonatorParts.ActiveMedia;
import framework.entities.resonatorParts.BaseResonatorPart;
import framework.entities.resonatorParts.SaturableAbsorber;
import framework.entities.resonatorParts.Vacuum;
import framework.enums.ResonatorElementType;
import framework.interfaces.JsonFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.stream.IntStream;

public class ResonatorBuilder implements JsonFiles {

    private static JsonObject jsonObject;
    private static final String fileName = "Resonator.json";
    private static final File source = new File(RESOURCES_DIR.concat(fileName));

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

    private HashMap<ResonatorElementType, BaseResonatorPart> getResonatorParts() {
        HashMap<ResonatorElementType, BaseResonatorPart> parts = new HashMap<>();
        IntStream.range(0, getNumberOfResonatorElements()).forEach(i -> {
            JsonObject tmpObject = getResonatorElements().get(i).getAsJsonObject();
            ResonatorElementType type = ResonatorElementType.getNameByValue(tmpObject.get("type").getAsString());
            BaseResonatorPart part;
            switch (type) {
                case ACTIVE_MEDIA:
                    part = new ActiveMedia(tmpObject.get("name").getAsString(), tmpObject.get("ions concentration").getAsDouble());
                    part.setLength(tmpObject.get("length").getAsDouble());
                    parts.put(ResonatorElementType.ACTIVE_MEDIA, part);
                    break;
                case VACUUM:
                    part = new Vacuum();
                    part.setLength(tmpObject.get("length").getAsDouble());
                    parts.put(ResonatorElementType.VACUUM, part);
                    break;
                case SATURABLE_ABSORBER:
                    part = new SaturableAbsorber(tmpObject.get("name").getAsString(), tmpObject.get("ions concentration").getAsDouble());
                    part.setLength(tmpObject.get("length").getAsDouble());
                    parts.put(ResonatorElementType.SATURABLE_ABSORBER, part);
                    break;
            }
        });
        return parts;
    }

    public Resonator buildResonator() {
        return new Resonator()
                .setLength(getResonatorLength())
                .setParts(getResonatorParts());
    }
}
