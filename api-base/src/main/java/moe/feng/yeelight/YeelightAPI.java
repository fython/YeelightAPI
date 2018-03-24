package moe.feng.yeelight;

import moe.feng.yeelight.model.Bulb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

public final class YeelightAPI {

    static Map<Bulb, Socket> sSocketPool = new HashMap<>();
    static Map<Bulb, BufferedReader> sSocketReader = new HashMap<>();
    static Map<Bulb, DataOutputStream> sSocketOutputStream = new HashMap<>();

    static final String DATA_DISCOVER = "M-SEARCH * HTTP/1.1\r\n"
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

    public static Socket openSocket(Bulb bulb) throws IOException {
        if (!sSocketPool.containsKey(bulb)) {
            System.out.println("Cannot find " + bulb.location + " \'s socket. Create a new one.");
            Socket socket = new Socket(bulb.getAddress(), bulb.getPort());
            socket.setTcpNoDelay(true);
            sSocketReader.put(bulb, new BufferedReader(new InputStreamReader(socket.getInputStream())));
            sSocketOutputStream.put(bulb, new DataOutputStream(socket.getOutputStream()));
            sSocketPool.put(bulb, socket);
        }
        return sSocketPool.get(bulb);
    }

    public static BufferedReader getSocketReader(Bulb bulb) {
        return sSocketReader.get(bulb);
    }

    public static DataOutputStream getSocketOutputStream(Bulb bulb) {
        return sSocketOutputStream.get(bulb);
    }

    public static void closeSocket(Bulb bulb) {
        try {
            Socket s = sSocketPool.remove(bulb);
            sSocketReader.remove(bulb);
            sSocketOutputStream.remove(bulb);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
