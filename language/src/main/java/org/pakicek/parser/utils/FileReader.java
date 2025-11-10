package org.pakicek.parser.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReader {
    public static String readFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("File not found: " + filePath);
            }
            
            BufferedReader reader = Files.newBufferedReader(path);
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            reader.close();
            return content.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
    
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
}