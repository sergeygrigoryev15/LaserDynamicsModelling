package framework.entities.resonatorParts;

import com.google.gson.JsonObject;
import framework.interfaces.JsonFiles;

import java.io.FileReader;
import java.io.IOException;

class EvaluativeResonatorPart extends BaseResonatorPart implements JsonFiles {

    private double lifeTime;
    private double absorption;
    private double emission;
    private double ionsConcentration;
    private String name;

    EvaluativeResonatorPart(boolean isActiveMedia, String jsonFileName, double ionsConcentration){
        JsonObject jsonObject = null;
        String path = isActiveMedia ? AM_RESOURCES_DIR : SA_RESOURCES_DIR;
        try {
            jsonObject = (JsonObject) parser.parse(new FileReader(path.concat(jsonFileName).concat(".json")));

        } catch (IOException ex){
            logger.fatal(String.format("Can't find json file %s : ", jsonFileName) + ex);
        }
        assert jsonObject != null;
        setLifeTime(jsonObject.get("life time").getAsDouble())
            .setAbsorption(jsonObject.get("absorption").getAsDouble())
            .setEmission(jsonObject.get("emission").getAsDouble())
            .setIonsConcentration(ionsConcentration)
            .setName(jsonFileName);
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public EvaluativeResonatorPart setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
        return this;
    }

    public double getAbsorption() {
        return absorption;
    }

    public EvaluativeResonatorPart setAbsorption(double absorption) {
        this.absorption = absorption;
        return this;
    }

    public double getEmission() {
        return emission;
    }

    public EvaluativeResonatorPart setEmission(double emission) {
        this.emission = emission;
        return this;
    }

    public double getIonsConcentration() {
        return ionsConcentration;
    }

    public EvaluativeResonatorPart setIonsConcentration(double ionsConcentration) {
        this.ionsConcentration = ionsConcentration;
        return this;
    }

    public String getName() {
        return name;
    }

    public EvaluativeResonatorPart setName(String name) {
        this.name = name;
        return this;
    }
}
