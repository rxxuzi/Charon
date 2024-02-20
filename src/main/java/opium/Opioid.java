package opium;

import server.CharonServer;

import java.util.*;
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

    public static List<Opium> opiumList = new ArrayList<>();
    private static List<Opium> copylist = new ArrayList<>();

    public static boolean saveAll(){
        AtomicBoolean fail = new AtomicBoolean(false);
        IntStream.range(0, opiumList.size()).parallel().forEach(i -> {
            Opium opium = opiumList.get(i);
            if (!opium.save(opium.name)) {
                fail.set(true);
            }
        });
        return !fail.get(); // 成功したら true を返す。失敗したら false を返す。
    }

    public static boolean save(int index){
        Opium opium = opiumList.get(index);
        return opium.save(); // 成功したら true を返す。失敗したら false を返す。
    }

    public static void add(Opium opium){
        opiumList.add(opium);
    }

    public static void add(Opium... opium){
        opiumList.addAll(Arrays.asList(opium));
    }
    public static void remove(Opium opium){
        opiumList.remove(opium);
    }

    public static void remove(Opium... opium){
        opiumList.removeAll(Arrays.asList(opium));
    }

    public static void removeOpium(List<Opium> list){
        opiumList.removeAll(list);
    }

    public static void clear(){
        opiumList.clear();
    }

    public static void optimaizor(){
        // 最適化するメソッド
        // 存在しないユーザーのファイルを削除する
        Set<String> user = CharonServer.clientMap.keySet();
        opiumList.removeIf(o -> !user.contains(o.from) || !user.contains(o.to));
    }

    public static List<Opium> filterFromUser(String from){
        return opiumList.stream().filter(o -> o.from.equals(from)).toList();
    }

    public static List<Opium> filterToUser(String to){
        return opiumList.stream().filter(o -> o.to.equals(to)).toList();
    }

    public static void sortByFileSize(){
        // ファイルサイズでソートする
        opiumList.sort(Comparator.comparingLong(o -> o.size));
    }

    public static void sortByFileName(){
        // ファイル名でソートする
        opiumList.sort(Comparator.comparing(o -> o.name));
    }
}
