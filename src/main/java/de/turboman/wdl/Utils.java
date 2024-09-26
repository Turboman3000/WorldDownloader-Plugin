package de.turboman.wdl;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static ArrayList<String> parseDirectoryRecursive(String path) {
        final ArrayList<String> files = new ArrayList<>();

        for (var directory : Stream.of(Objects.requireNonNull(new File(path).list())).filter(file -> new File(path + "/" + file).isDirectory()).collect(Collectors.toSet()))
            files.addAll(parseDirectoryRecursive(path + "/" + directory));

        files.addAll(Stream.of(Objects.requireNonNull(new File(path).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File -> File.getAbsoluteFile().toPath().toString())
                .toList());

        return files;
    }
}
