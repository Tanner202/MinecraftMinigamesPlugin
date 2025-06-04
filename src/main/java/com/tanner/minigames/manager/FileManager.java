package com.tanner.minigames.manager;

import com.tanner.minigames.Minigames;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private Minigames minigames;

    private List<File> files;

    public FileManager(Minigames minigames) {
        this.minigames = minigames;
        files = new ArrayList<>();
    }

    public void addFile(File file) {
        files.add(file);
    }

    public File getFile(String path) {
        System.out.println(minigames.getDataFolder() + "/" + path);
        for (File file : files) {
            System.out.println(file.getPath());
            if (file.getPath().equals(minigames.getDataFolder() + "/" + path)) {
                return file;
            }
        }
        return null;
    }
}
