package com.luckymacro.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryIteratorException;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;

public class DirectoryReader {

    private static Path toPath(String s) {
        return new File(s).toPath();
    }

    public static boolean isDirectory(String dirname) {
        Path p = toPath(dirname);
        return Files.isDirectory(p);
    }
    public static ArrayList<String> listFiles(String dirname) {
        ArrayList<String> result = new ArrayList();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(toPath(dirname), "*.vm")) {
            for (Path entry: ds) {
                result.add(entry.toString());
            }
        } catch (DirectoryIteratorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;

    }
}
