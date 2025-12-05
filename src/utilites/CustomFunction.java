package utilites;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class CustomFunction {

    public static Config loadConfig(String filePath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            String clean = json.replace("\n", "").replace("\r", "").replace("\t", "").trim();

            Config cfg = new Config();

            String baseURL   = extractValue(clean, "\"baseURL\"");
            String userName  = extractValue(clean, "\"userName\"");
            String passWord  = extractValue(clean, "\"passWord\"");
            String firstName = extractValue(clean, "\"firstName\"");
            String middle    = extractValue(clean, "\"middleName\"");
            String lastName  = extractValue(clean, "\"lastName\"");

            String fromDate  = extractValue(clean, "\"fromDate\"");
            String toDate    = extractValue(clean, "\"toDate\"");
            String empName   = extractValue(clean, "\"employeeName\"");
            String status    = extractValue(clean, "\"status\"");
            String leaveType = extractValue(clean, "\"leaveType\"");
            String subUnit   = extractValue(clean, "\"subUnit\"");
            boolean resetFilters = extractBoolean(clean, "\"resetFilters\"");

            cfg.setBaseURL(baseURL);
            cfg.setAuth(userName, passWord);
            cfg.setDefaults(firstName, middle, lastName);
            cfg.setLeaveSearch(fromDate, toDate, empName, status, leaveType, subUnit,resetFilters);
 
         

            String recruitmentSection = extractSection(clean, "\"recruitment\"");
            if (!recruitmentSection.isEmpty()) {
                String cFirst   = extractValue(recruitmentSection, "\"candidateFirstName\"");
                String cMiddle  = extractValue(recruitmentSection, "\"candidateMiddleName\"");
                String cLast    = extractValue(recruitmentSection, "\"candidateLastName\"");
                String vacancy  = extractValue(recruitmentSection, "\"vacancy\"");
                String email    = extractValue(recruitmentSection, "\"email\"");
                String contact  = extractValue(recruitmentSection, "\"contactNumber\"");
                String resume   = extractValue(recruitmentSection, "\"resumePath\"");
                String keywords = extractValue(recruitmentSection, "\"keywords\"");
                String date     = extractValue(recruitmentSection, "\"dateOfApplication\"");
                String notes    = extractValue(recruitmentSection, "\"notes\"");
                boolean consent = extractBoolean(recruitmentSection, "\"consent\"");

                cfg.setRecruitment(cFirst, cMiddle, cLast, vacancy, email, contact, resume, keywords, date, notes, consent);
            }

            return cfg;

        } catch (IOException e) {
            throw new RuntimeException("Error reading config file: " + filePath, e);
        }
    }

    private static String extractValue(String cleanJson, String key) {
        int keyIndex = cleanJson.indexOf(key);
        if (keyIndex == -1) return "";
        int colonIndex = cleanJson.indexOf(":", keyIndex);
        int firstQuote = cleanJson.indexOf("\"", colonIndex + 1);
        int secondQuote = cleanJson.indexOf("\"", firstQuote + 1);
        return cleanJson.substring(firstQuote + 1, secondQuote);
    }

    private static String extractSection(String cleanJson, String key) {
        int keyIndex = cleanJson.indexOf(key);
        if (keyIndex == -1) return "";
        int colonIndex = cleanJson.indexOf(":", keyIndex);
        int braceStart = cleanJson.indexOf("{", colonIndex);

        int depth = 0;
        for (int i = braceStart; i < cleanJson.length(); i++) {
            if (cleanJson.charAt(i) == '{') depth++;
            else if (cleanJson.charAt(i) == '}') {
                depth--;
                if (depth == 0) return cleanJson.substring(braceStart, i + 1);
            }
        }
        return "";
    }

    private static boolean extractBoolean(String jsonSection, String key) {
        int keyIndex = jsonSection.indexOf(key);
        if (keyIndex == -1) return false;
        int colonIndex = jsonSection.indexOf(":", keyIndex);
        int start = colonIndex + 1;

        while (Character.isWhitespace(jsonSection.charAt(start))) start++;

        int end = start;
        while (end < jsonSection.length() && jsonSection.charAt(end) != ',' && jsonSection.charAt(end) != '}')
            end++;

        return "true".equalsIgnoreCase(jsonSection.substring(start, end).trim());
    }

    public static String generateRandomEmployeeId() {
        int id = new Random().nextInt(9000) + 1000;
        return String.valueOf(id);
    }

    public static void appendToFile(String line, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path, true)) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }
    
    public static void writeTextFile(String path, String content) {
        try {
            File file = new File(path);

            // Ensure parent directory exists
            //C:\Project\artifacts\TestCases\LoginTests\TC_LOG_001_validLogin\Actual\baseline.txt-->path
            //C:\Project\artifacts\TestCases\LoginTests\TC_LOG_001_validLogin\Actual\--->Parent file

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // Overwrite the file with the new content
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(content);
                writer.write(System.lineSeparator());
            }

        } catch (Exception e) {
            System.err.println("Error writing file: " + path);
            e.printStackTrace();
        }
    }

}
