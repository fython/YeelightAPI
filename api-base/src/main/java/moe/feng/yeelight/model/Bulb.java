package moe.feng.yeelight.model;

import moe.feng.yeelight.GsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Bulb implements Serializable {

    public String location;
    public String server;
    public String id;
    public String model;
    public String fwVersion;
    public List<String> supportMethods = new ArrayList<>();
    public boolean isPowerOn;
    public int bright;
    public int colorMode;
    public int temperature;

    public String getAddress() {
        return location.substring(location.lastIndexOf("/") + 1, location.lastIndexOf(":"));
    }

    public int getPort() {
        return Integer.valueOf(location.substring(location.lastIndexOf(":") + 1));
    }

    public boolean isRGBSupport() {
        return supportMethods.contains("set_rgb");
    }

    public boolean isHSVSupport() {
        return supportMethods.contains("set_hsv");
    }

    public boolean isColorFlowSupport() {
        return supportMethods.contains("start_cf");
    }

    public boolean isColorTemperatureSupport() {
        return supportMethods.contains("set_ct_abx");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Bulb)) {
            return false;
        } else {
            return Objects.equals(location, ((Bulb) obj).location);
        }
    }

    public String toJsonString() {
        return GsonUtils.toJson(this);
    }

    public Method.Builder createMethod(String method) {
        return new Method.Builder().setTarget(this).setMethod(method);
    }

    public Method createSetPowerMethod(boolean state, String effect, int duration) {
        if (!Method.Effect.SMOOTH.equals(effect) && !Method.Effect.SUDDEN.equals(effect)) {
            throw new IllegalArgumentException();
        }
        return createMethod(Method.SET_POWER)
                .setParams(state ? "on": "off", effect, duration)
                .build();
    }

    public Method createSetPowerMethod(boolean state, String effect, int duration, int mode) {
        if (!Method.Effect.SMOOTH.equals(effect) && !Method.Effect.SUDDEN.equals(effect)) {
            throw new IllegalArgumentException();
        }
        if (mode < Method.Mode.LAST || mode > Method.Mode.NIGHT_LIGHT) {
            throw new IllegalArgumentException();
        }
        return createMethod(Method.SET_POWER)
                .setParams(state ? "on": "off", effect, duration, mode)
                .build();
    }

    public Method createSetBrightMethod(int brightness, String effect, int duration) {
        if (brightness < 1 || brightness > 100) {
            throw new IllegalArgumentException();
        }
        if (!Method.Effect.SMOOTH.equals(effect) && !Method.Effect.SUDDEN.equals(effect)) {
            throw new IllegalArgumentException();
        }
        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        return createMethod(Method.SET_BRIGHTNESS)
                .setParams(brightness, effect, duration)
                .build();
    }

    public Method createSetHSVMethod(int hue, int sat, String effect, int duration) {
        if (hue < 0 || hue > 359) {
            throw new IllegalArgumentException();
        }
        if (sat < 0 || sat > 100) {
            throw new IllegalArgumentException();
        }
        if (!Method.Effect.SMOOTH.equals(effect) && !Method.Effect.SUDDEN.equals(effect)) {
            throw new IllegalArgumentException();
        }
        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        return createMethod(Method.SET_HSV)
                .setParams(hue, sat, effect, duration)
                .build();
    }

    public Method createSetRGBMethod(int rgb, String effect, int duration) {
        if (rgb < 0 || rgb > 0xFFFFFF) {
            throw new IllegalArgumentException();
        }
        if (!Method.Effect.SMOOTH.equals(effect) && !Method.Effect.SUDDEN.equals(effect)) {
            throw new IllegalArgumentException();
        }
        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        return createMethod(Method.SET_RGB)
                .setParams(rgb, effect, duration)
                .build();
    }

    public Method createStartColorFlowMethod(int count, int action, List<ColorFlowTuple> colorFlowTuples) {
        return createMethod(Method.START_COLOR_FLOW)
                .setParams(count, action, ColorFlowTuple.parseListToString(colorFlowTuples))
                .build();
    }

    public Method createStopColorFlowMethod() {
        return createMethod(Method.STOP_COLOR_FLOW).build();
    }

    public Method createSetAdjustMethod(String action, String prop) {
        return createMethod(Method.SET_ADJUST)
                .setParams(action, prop)
                .build();
    }

    public Method createSetColorSceneMethod(int color, int brightness) {
        return createMethod(Method.SET_SCENE)
                .setParams("color", color, brightness)
                .build();
    }

    public Method createSetHSVSceneMethod(int hue, int sat, int brightness) {
        return createMethod(Method.SET_SCENE)
                .setParams("hsv", hue, sat, brightness)
                .build();
    }

    public Method createSetColorTemperatureSceneMethod(int temperature, int brightness) {
        return createMethod(Method.SET_SCENE)
                .setParams("ct", temperature, brightness)
                .build();
    }

    public Method createSetColorFlowSceneMethod(int count, int action, List<ColorFlowTuple> colorFlowTuples) {
        return createMethod(Method.SET_SCENE)
                .setParams("cf", count, action, ColorFlowTuple.parseListToString(colorFlowTuples))
                .build();
    }

    public Method createAutoDelayOffSceneMethod(int brightness, int delay) {
        return createMethod(Method.SET_SCENE)
                .setParams("auto_delay_off", brightness, delay)
                .build();
    }

    public Method createCronAddMethod(int type, int minutes) {
        return createMethod(Method.CRON_ADD)
                .setParams(type, minutes)
                .build();
    }

    public Method createCronGetMethod(int type) {
        return createMethod(Method.CRON_GET).setParams(type).build();
    }

    public Method createCronDelMethod(int type) {
        return createMethod(Method.CRON_DEL).setParams(type).build();
    }

    public Method createToggleMethod() {
        return createMethod(Method.TOGGLE).build();
    }

    public Method createSetDefaultMethod() {
        return createMethod(Method.SET_DEFAULT).build();
    }

    public Method createGetPropMethod(String... propNames) {
        return createMethod(Method.GET_PROP)
                .setParams(propNames)
                .build();
    }

    public Map<String, String> callGetProperties(String... propNames) throws IOException {
       Method.Response result = createGetPropMethod(propNames).call();
       if (result.getResult().size() != propNames.length) {
           throw new IllegalStateException("Result size is not equal to props\' which we request.");
       }
       Map<String, String> map = new HashMap<>();
       for (int i = 0; i < propNames.length; i++) {
           map.put(propNames[i], result.getResult().get(i));
       }
       return map;
    }

    public String callGetProperty(String propNames) throws IOException {
        Method.Response result = createGetPropMethod(propNames).call();
        if (result.getResult().size() != 1) {
            throw new IllegalStateException("Result size is not equal to props\' which we request.");
        }
        return result.getResult().get(0);
    }

    public static Bulb parseRawData(Map<String, String> map) {
        Bulb bulb = new Bulb();
        bulb.location = map.get("Location");
        bulb.server = map.get("Server");
        bulb.id = map.get("id");
        bulb.model = map.get("model");
        bulb.isPowerOn = !"on".equals(map.get("power"));
        bulb.fwVersion = map.get("fw_ver");
        bulb.supportMethods = Arrays.asList(map.get("support").split(" "));
        bulb.bright = Integer.valueOf(map.get("bright"));
        bulb.colorMode = Integer.valueOf(map.get("color_mode"));
        bulb.temperature = Integer.valueOf(map.get("ct"));
        return bulb;
    }

    public static Bulb fromJson(String json) {
        return GsonUtils.fromJson(json, Bulb.class);
    }

}
