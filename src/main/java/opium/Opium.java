package opium;

import security.Hash;
import security.eula.EulaException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import static global.Message.colorize;

/**
 * <h1>Opium</h1>
 * The Opium class is designed to facilitate detailed and secure communication of file-related information over networks,
 * surpassing the limitations of merely serializing File objects for transmission.
 * <p>
 * This class encapsulates not only the file itself
 * but also includes additional attributes such as sender and receiver identifiers, the file's path, its transmission status,
 * and a hash for integrity verification. Furthermore, it contains the actual file data as a byte array,
 * enabling the direct transmission of file contents alongside its metadata.
 * <p>
 * The creation of Opium stemmed from the need to transmit files with a richer context,
 * ensuring that all pertinent information is preserved during the serialization process,
 * thereby enhancing the robustness and effectiveness of file transfer operations in distributed systems.
 *
 * @author rxxuzi
 * @see Serializable
 */
public class Opium implements Serializable {
    public File file;
    public String name;
    public String path;
    public String to;
    public String from;
    public long size;
    public boolean send;
    private boolean isDir;
    public String hash;
    private byte[] data;

    /**
     * Initializes the Opium object with specified sender, receiver, file path, and transmission flag.
     * It also prepares the file for transmission by calculating its hash and loading its data into memory.
     *
     * @param from Sender identifier.
     * @param to Receiver identifier.
     * @param path Path to the file or directory.
     * @param send Flag indicating whether the file should be sent.
     * @throws OpiumException If the file does not exist or hashing fails.
     */
    public Opium(String from, String to, String path, boolean send) throws OpiumException {
        File f = new File(path);
        if (!f.exists()) {
            throw new OpiumException("File not found");
        }
        this.file = f;
        this.name = file.getName();
        this.path = path;
        this.to = to;
        this.from = from;
        this.send = send;
        init();
    }

    /**
     * Overloaded constructor that sets the send flag to false by default.
     *
     * @param from Sender identifier.
     * @param to Receiver identifier.
     * @param path Path to the file or directory.
     * @throws OpiumException If the file does not exist or hashing fails.
     */
    public Opium(String from, String to, String path) throws OpiumException {
        this(from, to, path, false);
    }

    /**
     * An alternative constructor using a File object.
     *
     * @param from The sender's identifier.
     * @param to The recipient's identifier.
     * @param file The file object.
     * @param send Indicates whether the file should be sent.
     * @throws OpiumException If hashing fails.
     */
    public Opium(String from, String to, File file, boolean send) throws OpiumException {
        this.file = file;
        this.name = file.getName();
        this.path = file.getPath();
        this.to = to;
        this.from = from;
        this.send = send;
        init();
    }

    /**
     * Initializes the object by calculating the hash of the file and loading its data into memory.
     *
     * @throws OpiumException If hashing fails or file data cannot be read.
     */
    private void init() throws OpiumException {
        try {
            this.hash = Hash.fileHash(path);
        } catch (EulaException e) {
            this.hash = "unknown";
            throw new OpiumException(e.getMessage(), e);
        }
        load();

        this.isDir = file.isDirectory();
        this.size = file.length();
    }

    /**
     * Loads the file data into memory.
     *
     * @throws OpiumException If reading the file data fails.
     */
    private void load() throws OpiumException {
        try {
            data = Files.readAllBytes(this.file.toPath());
        } catch (IOException e) {
            throw new OpiumException("Failed to read file data.", e);
        }
    }

    /**
     * Saves the loaded file data to the specified path.
     *
     * @param targetPath The path where the file should be saved.
     * @return true if the file is successfully saved, false otherwise.
     */
    public boolean save(String targetPath) {
        if (!isDir){
            try (FileOutputStream fos = new FileOutputStream(targetPath)) {
                fos.write(data);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false; // ディレクトリは保存できない。
        }
    }

    /**
     * Saves the loaded file data to a default path derived from the file's name.
     *
     * @return true if the file is successfully saved, false otherwise.
     */
    public boolean save() {
        return save(this.name);
    }

    /**
     * Provides a string representation of this Opium object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "@" + from + " -> @" + to + ": \"" + name + "\"";
    }

    /**
     * Provides a detailed string representation of the Opium object, including sender, receiver, file name, path, size, and hash.
     *
     * @return A detailed string representation of the object.
     */
    public String detail(){
        String s0 = colorize(5, "@" + from + " -> @" + to);
        String s1 = colorize(5, "File: ") + name;
        String s2 = colorize(5, "Path: ") + path;
        String s3 = colorize(5, "Size: ") + size;
        String s4 = colorize(5, "Hash: ") + hash;

        return s0 + "\n" + s1 + "\n" + s2 + "\n" + s3 + "\n" + s4;
    }
}
