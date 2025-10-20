package app;


import java.io.*;
import java.nio.charset.StandardCharsets;


public class FileUtils {
    public static String readFile(String path) throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static void writeFile(String path, String content) throws IOException {
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), StandardCharsets.UTF_8))) {
            out.write(content);
        }
    }
}