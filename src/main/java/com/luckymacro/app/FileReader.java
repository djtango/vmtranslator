package com.luckymacro.app;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FileReader {
    public static String read(String filename) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Path f = new File(filename).toPath();
            Files.copy(f, baos);
            String out = baos.toString(StandardCharsets.UTF_8.name());
            return out;
        }
        catch (IOException e) {
            return "file read failed";
        }
    }

}
