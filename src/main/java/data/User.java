package data;

import com.google.gson.Gson;
import global.Config;
import net.Spider;
import security.Hex64;
import security.eula.EulaException;
import security.eula.EulaRSA;

import java.io.Serializable;
import java.net.SocketException;
import java.util.*;

public class User implements Serializable {
    public final String name;
    public final String id;

    // 公開鍵
    private transient final EulaRSA rsa;
    private final String publicKeyString;

    public transient boolean isOrigin = false;

    private final Map<String, String> ipv6 = new HashMap<>();

    public User(String name, String uuid) throws EulaException {
        this.name = name;
        this.id = uuid;
        init();
        setIPv6();
        rsa = new EulaRSA();
        publicKeyString = rsa.getPublicKeyString();
    }

    public User(String name) throws EulaException {
        this(name, UUID.randomUUID().toString());
    }

    private void init(){
        this.isOrigin = true;
    }

    private void setIPv6(){
        String GUA;
        String LLA;
        try {
            GUA = Objects.requireNonNull(Spider.getGlobalUnicastAddress()).getHostName();
            GUA = Hex64.enc(GUA);
        } catch (SocketException | NullPointerException e) {
            GUA = "?";
        }
        try {
            LLA = Objects.requireNonNull(Spider.getLinkLocalAddress()).getHostName();
            LLA = Hex64.enc(LLA);
        } catch (SocketException | NullPointerException e) {
            LLA = "?";
        }
        ipv6.put("LLA", LLA);
        ipv6.put("GUA", GUA);
    }

    public static User json2User(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return name + "," + id + "," + isOrigin;
    }
}
