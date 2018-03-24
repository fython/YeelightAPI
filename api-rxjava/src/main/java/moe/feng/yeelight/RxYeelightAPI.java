package moe.feng.yeelight;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import moe.feng.yeelight.model.Bulb;
import moe.feng.yeelight.model.Method;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RxYeelightAPI {

    private static List<Bulb> sBulbs = new ArrayList<>();

    public static Flowable<Bulb> discoverBulbs() {
        return discoverBulbs(10000);
    }

    public static Flowable<Bulb> discoverBulbs(int timeout) {
        return Flowable.create(emitter -> {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(2000);

            byte[] data = YeelightAPI.DATA_DISCOVER.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    data, data.length, Inet4Address.getByName("239.255.255.250"), 1982);
            socket.send(packet);

            packet.setData(new byte[65507]);
            packet.setLength(65507);

            long startTime = System.currentTimeMillis();

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
                    emitter.onNext(bulb);
                } catch (SocketTimeoutException ignored) {

                }
                if (timeout > 0 && System.currentTimeMillis() - startTime >= timeout) {
                    break;
                }
            }

            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    public static Flowable<Bulb> onNewBulb() {
        return discoverBulbs(-1)
                .filter(bulb -> !sBulbs.contains(bulb))
                .map(bulb -> {
                    sBulbs.add(bulb);
                    return bulb;
                });
    }

    public static void clearBulbCache() {
        sBulbs.clear();
    }

    public static Single<Method.Response> call(Method method) {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(method.call());
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

}
