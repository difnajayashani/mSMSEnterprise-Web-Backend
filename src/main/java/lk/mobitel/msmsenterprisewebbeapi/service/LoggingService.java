package lk.mobitel.msmsenterprisewebbeapi.service;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class LoggingService {
    public void logLoginActivity(String sourceIP, String sessionId, Integer customer, String username, String index, Object sessionCreation) {
        // Set the time zone
        ZoneId zoneId = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(zoneId);

        // Create the log entry
        String logEntry = String.format("{\"Time\":\"%s\",\"ClientIP\":\"%s\",\"Session\":\"%s\",\"AccountNo\":\"%s\",\"User\":\"%s\",\"Function\":\"%s\",\"Log\":\"%s\"}",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now), sourceIP, sessionId, customer, username, index, sessionCreation.toString());

        // Create the log file path
        String logFileName = "/logs/mSMSEnterpriseWebBEAPI/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".json";

        // Check if the log file exists, if not, create it
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Write log entry to the log file
        try (FileWriter fileWriter = new FileWriter(logFileName, true)) {
            fileWriter.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logActivity(String sourceIP, String sessionId, Integer customer, String userName, String index, String log) {
        // Set the time zone
        ZoneId zoneId = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(zoneId);

        // Create the log entry
        String logEntry = String.format("{\"Time\":\"%s\",\"ClientIP\":\"%s\",\"Session\":\"%s\",\"AccountNo\":\"%s\",\"User\":\"%s\",\"Function\":\"%s\",\"Log\":\"%s\"}",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now), sourceIP, sessionId, customer, userName, index, log);

        // Create the log file path
        String logFileName = "/logs/mSMSEnterpriseWebBEAPI/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".json";

        // Check if the log file exists, if not, create it
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Write log entry to the log file
        try (FileWriter fileWriter = new FileWriter(logFileName, true)) {
            fileWriter.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
