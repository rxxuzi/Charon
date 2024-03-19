package net;

import java.io.IOException;
import java.net.*;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;

public class Spider {
    public final int port;

    public Spider(int port) {
        this.port = port;
    }

    public Spider() throws IOException {
        this.port = findAvailablePort();
    }

    public static int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    public static boolean checkPortAvailability(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Socket createSocket(String address) throws IOException {
        return new Socket(address, port);
    }

    public Socket createLocalhostSocket() throws IOException {
        return new Socket("localhost", port);
    }

    public ServerSocket createServerSocket(String address) throws IOException {
        InetAddress addr = InetAddress.getByName(address);
        return new ServerSocket(port, 50, addr);
    }

    public ServerSocket createLocalhostServerSocket() throws IOException {
        return new ServerSocket(port, 50, getLoopbackAddress());
    }

    public ServerSocket createGlobalUnicastSocket() throws IOException {
        InetAddress globalUnicastAddress = getGlobalUnicastAddress();
        if (globalUnicastAddress == null) {
            throw new IOException("Global unicast address not found.");
        }
        return new ServerSocket(port, 50, globalUnicastAddress);
    }

    public ServerSocket createLinkLocalServerSocket() throws IOException {
        InetAddress linkLocalAddress = getLinkLocalAddress();
        if (linkLocalAddress == null) {
            throw new IOException("Link-local address not found.");
        }
        return new ServerSocket(port, 50, linkLocalAddress);
    }

    public static InetAddress getGlobalUnicastAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;
            for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                if (addr instanceof Inet6Address && !addr.isLinkLocalAddress() && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return null;
    }

    public static InetAddress getLinkLocalAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;
            for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                if (addr.isLinkLocalAddress()) {
                    return addr;
                }
            }
        }
        return null;
    }

    public static InetAddress getLoopbackAddress() {
        return InetAddress.getLoopbackAddress();
    }

    public static boolean isAddressAssignedToLocalInterface(String address) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress.getHostAddress().equals(address)) {
                    return true;
                }
            }
        }
        return false;
    }
}
