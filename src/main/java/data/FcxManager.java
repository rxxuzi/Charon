package data;

import server.CharonServer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static server.CharonServer.fcxList;

public class FcxManager {
    public static boolean saveAllFcx(List<Fcx> list){
        AtomicBoolean fail = new AtomicBoolean(false);
        IntStream.range(0, list.size()).parallel().forEach(i -> {
            Fcx fcx = list.get(i);
            if (!fcx.save(fcx.path)) {
                fail.set(true);
            }
        });
        return !fail.get(); // 成功したら true を返す。失敗したら false を返す。
    }

    public static void addFcx(Fcx fcx){
        fcxList.add(fcx);
    }

    public static void addFcx(Fcx... fcxx){
        fcxList.addAll(Arrays.asList(fcxx));
    }
    public static void removeFcx(Fcx fcx){
        fcxList.remove(fcx);
    }

    public static void removeFcx(Fcx... fcxx){
        fcxList.removeAll(Arrays.asList(fcxx));
    }

    public static void removeFcx(List<Fcx> list){
        fcxList.removeAll(list);
    }

    public static void clearFcx(){
        fcxList.clear();
    }

    public static void optimaizor(){
        Set<String> user = CharonServer.clientMap.keySet();
        fcxList.removeIf(fcx -> !user.contains(fcx.from) || !user.contains(fcx.to));
    }

    public static List<Fcx> filterFromUser(String from){
        return fcxList.stream().filter(fcx -> fcx.from.equals(from)).toList();
    }

    public static List<Fcx> filterToUser(String to){
        return fcxList.stream().filter(fcx -> fcx.to.equals(to)).toList();
    }

}
