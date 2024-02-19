package global;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static global.Message.*;


public class Cmd {

    public static void ls(){
        ls("./");
    }

    public static void ls(String filepath){
        String path = filepath;
        if (filepath.isEmpty()) path = "./";
        // check if path is a file (not a directory)
        if (new File(path).isFile()) {
            System.out.println("File not found.");
            return;
        }
        if (new File(path).isFile()) return;
        File[] files = new File(path).listFiles();
        if (files == null) return;

        System.out.println("List directory contents : ");
        notice("%19s %8s %19s", "LastWriteTime", "Length", "Name");
        for(File f : files){
            String lastWriteTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(f.lastModified()));
            String name = f.getName();
            if (name.length() > 16) name = name.substring(0, 16) + "...";
            if (f.isDirectory()) name += "/";

            long length = f.length();

            System.out.printf("%s  %8s  %19s\n", lastWriteTime, fileSize(length), name);
        }
    }

    public static String fileSize(long length){
        if (length < 1024) return length + "  B";
        if (length < 1024 * 1024) return (length / 1024) + " KB";
        if  (length < 1024 * 1024 * 1024) return (length / (1024 * 1024)) + " MB";
        return (length / (1024 * 1024 * 1024)) + " GB";
    }

    public static String pwd(){
        return new File(".").getAbsolutePath();
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

    public static void mkdir(String dirname){
        File file = new File(dirname);
        if (file.mkdir()) {
            System.out.println("Directory created successfully.");
        } else {
            System.out.println("Failed to create directory.");
        }
    }

    public static void rmdir(String dirname){
        File file = new File(dirname);
        if (!file.exists()) {
            System.out.println("Directory not found.");
            return;
        }

        if (file.delete()) {
            System.out.println("Directory deleted successfully.");
        } else {
            System.out.println("Failed to delete directory.");
        }
    }
}
