import moe.feng.yeelight.YeelightAPI;
import moe.feng.yeelight.model.Bulb;
import moe.feng.yeelight.model.ColorFlowTuple;
import moe.feng.yeelight.model.Method;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        try {
            List<Bulb> bulbs;
            while ((bulbs = YeelightAPI.discoverBulbs()).isEmpty()) {
                System.out.println("Cannot find bulbs.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Bulb bulb = bulbs.get(0);
            System.out.println(bulb.callGetProperties());
            System.out.println(bulb.createStartColorFlowMethod(
                    0,
                    Method.ColorFlowAction.RECOVER_LAST_STATE,
                    Arrays.asList(
                            new ColorFlowTuple.Builder()
                                    .setBrightness(1)
                                    .setDuration(1000)
                                    .colorTemperature(2700)
                                    .build(),
                            new ColorFlowTuple.Builder()
                                    .setBrightness(100)
                                    .setDuration(500)
                                    .colorTemperature(8000)
                                    .build(),
                            new ColorFlowTuple.Builder()
                                    .setBrightness(50)
                                    .setDuration(500)
                                    .colorTemperature(2700)
                                    .build()
                    )).call());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
