package global;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Cmd {
    public static void ls(){
        File[] files = new File("./").listFiles();
        if (files == null) return;

        System.out.println("List directory contents : ");
        System.out.println("LastWriteTime          Length    Name");
        for(File f : files){
            String lastWriteTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(f.lastModified()));
            String name = f.getName();
            if (name.length() > 16) name = name.substring(0, 16) + "...";
            if (f.isDirectory()) name += "/";
            long length = f.length();
            System.out.printf("%s  %8d    %s\n",  lastWriteTime, length, name);
        }
    }

    public static void cat(String filename){
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("File not found.");
                return;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public static void rm(String filename){
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found.");
            return;
        }

        if (file.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete file.");
        }
    }
}
