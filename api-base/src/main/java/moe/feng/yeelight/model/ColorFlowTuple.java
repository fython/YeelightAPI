package moe.feng.yeelight.model;

import java.io.Serializable;
import java.util.List;

public class ColorFlowTuple implements Serializable {

    private int duration = 50;
    private int mode = Mode.COLOR_TEMPERATURE;
    private int value = 1000;
    private int brightness = 50;

    public int getDuration() {
        return duration;
    }

    public int getMode() {
        return mode;
    }

    public int getValue() {
        return value;
    }

    public int getBrightness() {
        return brightness;
    }

    @Override
    public String toString() {
        return String.format("%1$d, %2$d, %3$d, %4$d", duration, mode, value, brightness);
    }

    public static String parseListToString(List<ColorFlowTuple> list) {
        StringBuilder sb = new StringBuilder();
        for (ColorFlowTuple e : list) {
            sb.append(e).append(", ");
        }
        return sb.toString().substring(0, sb.length() - 2);
    }

    public static final class Mode {

        public static final int COLOR = 1;
        public static final int COLOR_TEMPERATURE = 2;
        public static final int SLEEP = 7;

    }

    public static class Builder {

        private int duration = 50;
        private int mode = Mode.COLOR_TEMPERATURE;
        private int value = 1000;
        private int brightness = 50;

        public Builder() {

        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setMode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder setValue(int value) {
            this.value = value;
            return this;
        }

        public Builder setBrightness(int brightness) {
            this.brightness = brightness;
            return this;
        }

        public Builder colorTemperature(int ct) {
            return setMode(Mode.COLOR_TEMPERATURE).setValue(ct);
        }

        public Builder color(int color) {
            return setMode(Mode.COLOR).setValue(color);
        }

        public Builder sleep() {
            return setMode(Mode.SLEEP);
        }

        public ColorFlowTuple build() {
            ColorFlowTuple tuple = new ColorFlowTuple();
            tuple.duration = duration;
            tuple.mode = mode;
            tuple.value = value;
            tuple.brightness = brightness;
            return tuple;
        }
    }

}
