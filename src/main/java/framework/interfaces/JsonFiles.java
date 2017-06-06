package framework.interfaces;

import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface JsonFiles {
    String RESOURCES_DIR = System.getProperty("user.dir").concat("/src/main/resources/");
    String OUTPUTS_DIR = System.getProperty("user.dir").concat("/src/main/outputs/");
    String AM_RESOURCES_DIR = RESOURCES_DIR.concat("activeMedias/");
    String SA_RESOURCES_DIR = RESOURCES_DIR.concat("saturableAbsorbers/");

    Logger logger = LogManager.getLogger();
    JsonParser parser = new JsonParser();
}
