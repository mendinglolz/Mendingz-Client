package com.mendingz.client.module;

public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public static class Bool extends Setting {
        public boolean value;

        public Bool(String name, boolean value) {
            super(name);
            this.value = value;
        }
    }

    public static class Num extends Setting {
        public double value;
        public final double min, max, step;

        public Num(String name, double value, double min, double max, double step) {
            super(name);
            this.value = value;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public double ratio() {
            return (value - min) / (max - min);
        }

        public void setFromRatio(double t) {
            double raw = min + Math.max(0, Math.min(1, t)) * (max - min);
            value = Math.max(min, Math.min(max, Math.round(raw / step) * step));
        }

        public String display() {
            if (step >= 1) return String.valueOf((int) value);
            if (step >= 0.1) return String.format("%.1f", value);
            return String.format("%.2f", value);
        }
    }
}
