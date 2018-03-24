package moe.feng.yeelight.model;

import com.google.gson.annotations.Expose;
import moe.feng.yeelight.GsonUtils;
import moe.feng.yeelight.YeelightAPI;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Method implements Serializable {

    public static final String GET_PROP = "get_prop";
    public static final String SET_BRIGHTNESS = "set_bright";
    public static final String SET_POWER = "set_power";
    public static final String TOGGLE = "toggle";
    public static final String SET_DEFAULT = "set_default";
    public static final String SET_HSV = "set_hsv";
    public static final String SET_RGB = "set_rgb";
    public static final String START_COLOR_FLOW = "start_cf";
    public static final String STOP_COLOR_FLOW = "stop_cf";
    public static final String SET_ADJUST = "set_adjust";
    public static final String SET_SCENE = "set_scene";
    public static final String CRON_ADD = "cron_add";
    public static final String CRON_GET = "cron_get";
    public static final String CRON_DEL = "cron_del";

    public static final class Effect {

        public static final String SUDDEN = "sudden";
        public static final String SMOOTH = "smooth";

    }

    public static final class Mode {

        public static final int LAST = 0;
        public static final int COLOR_TEMPERATURE = 1;
        public static final int RGB = 2;
        public static final int HSV = 3;
        public static final int COLOR_FLOW = 4;
        public static final int NIGHT_LIGHT = 5;

    }

    public static final class ColorFlowAction {

        public static final int RECOVER_LAST_STATE = 0;
        public static final int STAY_FINAL_STATE = 1;
        public static final int TURN_OFF = 2;

    }

    public static final class AdjustAction {

        public static final String INCREASE = "increase";
        public static final String DECREASE = "decrease";
        public static final String CIRCLE = "circle";

    }

    public static final class AdjustProperty {

        public static final String BRIGHTNESS = "bright";
        public static final String COLOR_TEMPERATURE = "ct";
        public static final String COLOR = "color";

    }

    private Bulb target;
    private @Expose int id = 1000;
    private @Expose String method;
    private @Expose List params;

    private Method() {

    }

    public int getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public List getParams() {
        return params;
    }

    public String toJsonString() {
        return GsonUtils.toJson(this);
    }

    public Response call() throws IOException {
        YeelightAPI.openSocket(target);

        BufferedReader reader = YeelightAPI.getSocketReader(target);
        DataOutputStream out = YeelightAPI.getSocketOutputStream(target);

        out.writeBytes(toJsonString() + "\r\n");
        out.flush();

        for (int retryTime = 0; retryTime < 10; retryTime++) {
            Response result = Response.fromJson(reader.readLine());
            if (result.getId() == 1000) {
                return result;
            }
        }

        return null;
    }

    public static class Builder implements Serializable {

        private Bulb target;
        private String method;
        private List params = new ArrayList<>();

        public Builder() {

        }

        public Bulb getTarget() {
            return target;
        }

        public Builder setTarget(Bulb target) {
            this.target = target;
            return this;
        }

        public String getMethod() {
            return method;
        }

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public List<Object> getParams() {
            return params;
        }

        public <T> Builder setParams(List<T> params) {
            this.params = params;
            return this;
        }

        public Builder setParams(Object... params) {
            return setParams(Arrays.asList(params));
        }

        public Builder addParam(Object param) {
            this.params.add(param);
            return this;
        }

        public Method build() {
            Method result = new Method();
            result.target = target;
            result.method = method;
            result.params = params;
            return result;
        }

    }

    public static class Response implements Serializable {

        private @Expose int id;
        private @Expose List<String> result;
        private @Expose Map<String, String> error;

        public int getId() {
            return id;
        }

        public List<String> getResult() {
            return result;
        }

        @Override
        public String toString() {
            return GsonUtils.toJson(this);
        }

        public boolean isOk() {
            return !isFailed() && result != null && !result.isEmpty();
        }

        public boolean isFailed() {
            return error != null && !error.isEmpty();
        }

        public static Response fromJson(String json) {
            return GsonUtils.fromJson(json, Response.class);
        }
    }

}
