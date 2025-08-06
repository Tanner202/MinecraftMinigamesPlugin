package com.tanner.minigames.manager;

import com.tanner.minigames.Minigames;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private Minigames minigames;

    private List<File> files;

    public FileManager(Minigames minigames) {
        this.minigames = minigames;
        files = new ArrayList<>();
    }

    public void addFile(String path) {
        File file = new File(minigames.getDataFolder(), path);
        if (!file.exists()) {
            minigames.saveResource(path, false);
        }
        files.add(file);
    }

    public File getFile(Path path) {
        System.out.println(minigames.getDataFolder().toPath().resolve(path));
        for (File file : files) {
            System.out.println(file.getPath());
            if (file.toPath().equals(minigames.getDataFolder().toPath().resolve(path))) {
                return file;
            }
        }
        return null;
    }
}
