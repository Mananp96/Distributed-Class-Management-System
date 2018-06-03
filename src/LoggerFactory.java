import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.IOException;

public class LoggerFactory {

    private static final String LogDirectory = System.getProperty("user.dir") + "\\Logs";

    static {
        File directory = new File(LogDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }


    private static Logger getInstance(String loggerName) throws IOException {
        Logger logger = Logger.getLogger(loggerName);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %m%n");
        FileAppender fileAppender = new FileAppender(layout, LogDirectory + "\\" + loggerName + ".txt");


        logger.removeAllAppenders();
        logger.addAppender(fileAppender);

        return logger;

    }

    public static void Log(String managerID, String message) {

        try {
            Logger logger = LoggerFactory.getInstance(managerID);

            logger.info(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void LogServer(String message) {

        try {
            Logger logger = LoggerFactory.getInstance("Server");

            logger.info(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
