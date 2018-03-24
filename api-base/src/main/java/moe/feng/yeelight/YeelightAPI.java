package moe.feng.yeelight;

import moe.feng.yeelight.model.Bulb;
import moe.feng.yeelight.model.Method;

import java.io.IOException;
import java.net.*;
import java.util.*;

public final class YeelightAPI {

    private static Map<Bulb, Socket> sSocketPool = new HashMap<>();

    private static final String DATA_DISCOVER = "M-SEARCH * HTTP/1.1\r\n"
            + "ST:wifi_bulb\r\n"
            + "MAN:\"ssdp:discover\"\r\n";

    public static List<Bulb> discoverBulbs() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(2000);

        byte[] data = DATA_DISCOVER.getBytes();

        DatagramPacket packet = new DatagramPacket(
                data, data.length, Inet4Address.getByName("239.255.255.250"), 1982);
        socket.send(packet);

        packet.setData(new byte[65507]);
        packet.setLength(65507);

        List<Bulb> result = new ArrayList<>();

        while (true) {
            try {
                socket.receive(packet);

                String text = new String(packet.getData());
                Map<String, String> map = new HashMap<>();
                for (String line : text.split("\n")) {
                    if (line.contains(":")) {
                        map.put(line.substring(0, line.indexOf(":")).trim(),
                                line.substring(line.indexOf(":") + 1).trim());
                    }
                }

                Bulb bulb = Bulb.parseRawData(map);
                if (!result.contains(bulb)) {
                    result.add(bulb);
                }
            } catch (SocketTimeoutException e) {
                break;
            }
        }

        return result;
    }

    public static Method.Response call(Bulb target, String method, List<Object> params) throws IOException {
        return new Method.Builder()
                .setTarget(target)
                .setMethod(method)
                .setParams(params)
                .build()
                .call();
    }

    public static Socket openSocket(Bulb bulb) throws IOException {
        if (!sSocketPool.containsKey(bulb)) {
            System.out.println("Cannot find " + bulb.location + " \'s socket. Create a new one.");
            Socket socket = new Socket(bulb.getAddress(), bulb.getPort());
            socket.setTcpNoDelay(true);
            sSocketPool.put(bulb, socket);
        }
        return sSocketPool.get(bulb);
    }

}
