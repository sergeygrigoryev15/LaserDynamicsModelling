package framework;

public class Constants {

    private static final String propertiesFileName = "stage.properties";
    private static final PropertiesResourceManager props = new PropertiesResourceManager(propertiesFileName);

    public static final float speedOfLight = Float.valueOf(props.getProperty("speedOfLight"));
    public static final float planksconst = Float.valueOf(props.getProperty("planksconst"));

}
