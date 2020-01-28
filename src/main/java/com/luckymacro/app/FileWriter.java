package com.luckymacro.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FileWriter {
    public static void write(String filename, String contents) {
        byte[] buf = contents.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        try {
            Path f = new File(filename).toPath();
            Files.copy(bais, f);
        }
        catch (IOException e) {
            System.out.println( "writing to file failed..." );
            e.printStackTrace();
        }

    }
}
