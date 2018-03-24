import io.reactivex.schedulers.Schedulers;
import moe.feng.yeelight.RxYeelightAPI;

public class RxTest {

    public static void main(String[] args) {
        RxYeelightAPI.discoverBulbs()
                .subscribeOn(Schedulers.io())
                .subscribe(bulb -> {
                    System.out.println("Discovering: Bulb " + bulb.toJsonString());
                });
        RxYeelightAPI.onNewBulb()
                .subscribeOn(Schedulers.io())
                .subscribe(newBulb -> {
                    System.out.println("New bulb: " + newBulb.toJsonString());
                    RxYeelightAPI.call(newBulb.createToggleMethod())
                            .subscribe(System.out::println);
                });
        while (true);
    }

}
