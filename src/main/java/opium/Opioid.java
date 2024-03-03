package opium;

import server.CharonServer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * <h1>Opioid</h1>
 * The Opioid class manages a collection of Opium objects, facilitating operations such as saving, adding, and removing these objects.
 * It serves as a centralized manager for handling Opium instances, offering static methods to manipulate the collection globally.
 * This class was designed to enhance file communication by managing detailed information encapsulated within Opium objects
 * rather than limiting interactions to simple file serialization. It supports bulk operations on the collection,
 * including optimized saving mechanisms and filtering based on sender or receiver criteria.
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *     <li>Saving individual or all Opium objects to their respective paths.</li>
 *     <li>Adding or removing Opium objects from the global collection.</li>
 *     <li>Filtering Opium objects based on their sender or receiver attributes.</li>
 *     <li>Optimizing the collection by removing objects not associated with current users.</li>
 *     <li>Sorting the collection of Opium objects by file size or file name to organize or prioritize the data efficiently.</li>
 * </ul>
 */

public class Opioid {

    public static List<Opium> list = new ArrayList<>();
    private static List<Opium> copylist = new ArrayList<>();

    public static boolean saveAll(){
        AtomicBoolean fail = new AtomicBoolean(false);
        IntStream.range(0, list.size()).parallel().forEach(i -> {
            Opium opium = list.get(i);
            if (!opium.save(opium.name)) {
                fail.set(true);
            }
        });
        return !fail.get(); // 成功したら true を返す。失敗したら false を返す。
    }

    public static boolean save(int index){
        Opium opium = list.get(index);
        return opium.save(); // 成功したら true を返す。失敗したら false を返す。
    }

    public static void add(Opium opium){
        list.add(opium);
    }

    public static void add(Opium... opium){
        list.addAll(Arrays.asList(opium));
    }

    public static void remove(Opium opium){
        list.remove(opium);
    }

    public static void remove(int index){
        if (index < list.size() && index >= 0){
            remove(list.get(index));
        }
    }

    public static void remove(Opium... opium){
        list.removeAll(Arrays.asList(opium));
    }

    public static void removeOpium(List<Opium> list){
        Opioid.list.removeAll(list);
    }

    public static void clear(){
        list.clear();
    }

    public static void optimaizor(){
        // 最適化するメソッド
        // 存在しないユーザーのファイルを削除する
        Set<String> user = CharonServer.clientMap.keySet();
        list.removeIf(o -> !user.contains(o.from) || !user.contains(o.to));
    }

    public static List<Opium> filterFromUser(String from){
        return list.stream().filter(o -> o.from.equals(from)).toList();
    }

    public static List<Opium> filterToUser(String to){
        return list.stream().filter(o -> o.to.equals(to)).toList();
    }

    public static void sortByFileSize(){
        // ファイルサイズでソートする
        list.sort(Comparator.comparingLong(o -> o.size));
    }

    public static void sortByFileName(){
        // ファイル名でソートする
        list.sort(Comparator.comparing(o -> o.name));
    }

    public static void sortByTime(){
        // 時間でソートする.
        list.sort(Comparator.comparing(o -> o.time));
    }

    public static void show(){
        int size = list.size();
        for (int i = 0 ; i < size ; i ++ ){
            Opium opium = Opioid.list.get(i);
            System.out.printf("%3d %s \n" ,i , " : " + opium.toString());
        }
    }

    public static boolean sort(String opt){
        if (opt.startsWith("-n")){
            sortByFileName();
        } else if (opt.startsWith("-s")) {
            sortByFileSize();
        } else if (opt.startsWith("-t")) {
            sortByTime();
        } else {
            return false;
        }
        return true;
    }

    public static boolean zip(String zipName){
        AtomicBoolean success = new AtomicBoolean(true);
        OpiumZip zip = new OpiumZip(list);
        CompletableFuture<Void> future = zip.save(zipName);
        future.thenRun(() -> System.out.println("Compression completed."))
                .exceptionally(e -> {
                    success.set(false);
                    System.err.println("Failed");
                    return null; // Voidメソッドでの返り値はnull
                    });

        return success.get();
    }
}
