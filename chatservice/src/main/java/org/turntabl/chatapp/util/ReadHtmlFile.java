package org.turntabl.chatapp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class ReadHtmlFile {
    /**
     * Read a File in /src/main/resources/static
     * 
     * @param filename the file to be read, should be relative to the path stated
     *                 above
     * @return The file data
     */
    public String readFile(String filename) {
        File file = new File("src/main/resources/static/" + filename);
        try {
            Scanner scanner = new Scanner(file);
            var builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            scanner.close();
            return builder.toString();
        } catch (FileNotFoundException ex) {
            return "Page Not available: " + ex.getMessage();
        }
    }
}
