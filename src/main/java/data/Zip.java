package data;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class Zip {

    protected static void zip(String targetPath, String zipName) throws IOException {
        File target = new File(targetPath);
        File zip = new File(zipName);

        try ( ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            compress(zos, target);
        } catch (IOException e) {
            throw new IOException("Failed to zip file.", e);
        }
    }

    private static void compress(ZipOutputStream zos, File file) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        if(file.isDirectory()){
            zos.putNextEntry(new ZipEntry(file.getName() + "/"));
            File[] files = file.listFiles();
            if (files != null){
                for (File f : files) {
                    compress(zos, f);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(file.getPath()));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while ((len = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            bis.close();
            zos.closeEntry();
        }
    }
}
