package opium;

import data.Zip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <h1>OpiumZip</h1>
 * Provides functionality to asynchronously save and compress a list of {@link Opium} objects into a zip file.
 * <p>
 * This class extends the {@link Zip} class to leverage zip file creation capabilities.
 * It manages a list of {@link Opium} objects, providing methods to asynchronously save these objects to a temporary directory
 * and then compress them into a single zip file. The temporary files are managed within a dedicated directory to ensure
 * a clean workspace and facilitate cleanup after compression.
 * </p>
 * <p>
 * Usage involves creating an instance with a list of {@link Opium} objects, initiating the save process with {@link #save(String)},
 * and optionally handling completion with the returned {@link CompletableFuture}.
 * </p>
 *
 * @author rxxuzi
 */
public class OpiumZip extends Zip {
    private static final String tmpPath = ".opium/";
    private static final File tmpDir = new File(tmpPath);
    private final List<Opium> list = new ArrayList<>();

    /**
     * Constructs an {@link OpiumZip} instance with a specified list of {@link Opium} objects.
     * Ensures the temporary directory exists or creates it if not.
     *
     * @param opiumList the list of {@link Opium} objects to be saved and compressed
     * @throws RuntimeException if the temporary directory cannot be created
     */
    public OpiumZip(List<Opium> opiumList){
        list.addAll(opiumList);
        if (!tmpDir.exists()){
            if (!tmpDir.mkdir()) throw new RuntimeException("Failed to create temporary directory.");
        }
    }

    /**
     * Initializes the asynchronous save process for each {@link Opium} object in the list.
     * Saves each object to the temporary directory in parallel.
     *
     * @return a {@link CompletableFuture} representing the completion of all save operations
     */
    public CompletableFuture<Void> initAsync() {
        List<CompletableFuture<Void>> futures = list.stream()
                .map(opium -> CompletableFuture.runAsync(() -> opium.save2dir(tmpPath)))
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Asynchronously saves files from the Opium list to a temporary directory and compresses them into a zip file.
     * <br>
     * This method initiates the asynchronous saving and compression process.
     * The zip file will be named according to the {@code zipName} parameter.
     * <p>
     * <h3>Usage Example:</h3>
     * <pre>
     *   {@snippet lang='java':
     *      OpiumZip zip = new OpiumZip(opiumList);
     *      CompletableFuture<Void> future = zip.saveAsync("example.zip");
     *      future.thenRun(() -> System.out.println("Compression completed."))
     *       .exceptionally(e -> {
     *           System.err.println("An error occurred: " + e.getMessage());
     *           return null;
     *       });
     *   }
     * </pre>
     *
     * @param zipName the name of the zip file to be created, including the .zip extension
     * @return a CompletableFuture that completes when the save and compression process is finished
     */
    public CompletableFuture<Void> save(String zipName){
        // initAsyncが完了した後、非同期でzipメソッドを呼び出す
        return initAsync().thenRunAsync(() -> {
            try {
                zip(tmpPath, zipName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to compress to zip: " + zipName, e);
            }
        });
    }

    /**
     * Deletes the temporary directory used for saving {@link Opium} objects.
     * This method can be used to clean up the temporary files once compression is completed.
     *
     * @return true if the directory was successfully deleted, false otherwise
     */
    public static boolean deleteTmpDir(){
        return tmpDir.delete();
    }

}
