package framework;

public class Constants {

    private static final String propertiesFileName = "stage.properties";
    private static final PropertiesResourceManager props = new PropertiesResourceManager(propertiesFileName);

    public static final double resonatorPointsNumber = Double.valueOf(props.getProperty("resonatorPointsNumber"));
    public static final double time = Double.valueOf(props.getProperty("time"));

    //TODO add dt and dx evaluation

    public static final double speedOfLight = Double.valueOf(props.getProperty("speedOfLight"));
    public static final double planksconst = Double.valueOf(props.getProperty("planksconst"));
    public static final double internalLoses = Double.valueOf(props.getProperty("internalLoses"));
    public static final double refractionIndex = Double.valueOf(props.getProperty("refractionIndex"));
    public static final double luminiscention = Double.valueOf(props.getProperty("luminiscention"));
    public static final double pumpPower = Double.valueOf(props.getProperty("pumpPower"));
    public static final double pumpWaveLength = Double.valueOf(props.getProperty("pumpWaveLength"));
    public static final double pumpCrossSection = Double.valueOf(props.getProperty("pumpCrossSection"));
    public static final double leftMirror = Double.valueOf(props.getProperty("leftMirror"));
    public static final double rightMirror = Double.valueOf(props.getProperty("rightMirror"));

}
