package data;

import java.io.*;

public class Fcx implements Serializable {
    public File file;
    public String name;
    public String path;
    public String type;
    public String to;
    public String from;
    public boolean send;

    public Fcx(String from, String to, String path, boolean send) throws IOException {
        File f = new File(path);
        if (!f.exists()) throw new IOException("File not found");
        this.type = f.isDirectory() ? "dir" : "file";
        this.file = new File(path);
        this.name = file.getName();
        this.path = path;
        this.to = to;
        this.from = from;
        this.send = send;
    }

    public Fcx(String from, String to, String path) throws IOException {
        this(from, to, path, false);
    }

    public Fcx(String from, String to, File file, boolean send) {
        this.file = file;
        this.name = file.getName();
        this.path = file.getPath();
        this.to = to;
        this.from = from;
        this.type = file.isDirectory() ? "dir" : "file";
        this.send = send;
    }

    public boolean save(String path) {
        File saveFile = new File(path);
        if(!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        boolean flag = true;
        try (FileInputStream fis = new FileInputStream(this.file); // f は元のファイル
             FileOutputStream fos = new FileOutputStream(saveFile)) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            flag = false;
        }
        return flag;
    }

    public boolean save(Fcx fcx) {
        return save(fcx.path);
    }

    public boolean save() {
        return save(this.path);
    }

    @Override
    public String toString() {
        return "@" + from + " -> @" + to + ": \"" + name + "\"";
    }
}
