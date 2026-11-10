package com.xlays.c00lgui.modules;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A single configurable setting attached to a module.
 * Types are kept small on purpose: BOOL, INT, DOUBLE.
 *
 * The setting does not store its value directly — it reads/writes
 * through getter/setter lambdas that call into the owning module's
 * static fields. This keeps the module classes unchanged.
 */
public final class ModuleSetting {

    public enum Type { BOOL, INT, DOUBLE }

    private final String module;
    private final String name;
    private final Type type;
    private final double min;
    private final double max;
    private final Supplier<Object> getter;
    private final Consumer<Object> setter;
    private final String description;

    public ModuleSetting(String module,
                         String name,
                         Type type,
                         double min,
                         double max,
                         Supplier<Object> getter,
                         Consumer<Object> setter,
                         String description) {
        this.module = module;
        this.name = name;
        this.type = type;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
        this.description = description == null ? "" : description;
    }

    public String getModule() { return module; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public String getDescription() { return description; }

    public Object getValue() {
        return getter.get();
    }

    public void setValue(Object value) {
        if (value == null) return;
        switch (type) {
            case BOOL -> {
                if (value instanceof Boolean b) setter.accept(b);
                else if (value instanceof Number n) setter.accept(n.doubleValue() != 0.0);
            }
            case INT -> {
                double v = clamp(toDouble(value), min, max);
                setter.accept((int) Math.round(v));
            }
            case DOUBLE -> {
                double v = clamp(toDouble(value), min, max);
                setter.accept(v);
            }
        }
    }

    public void toggleBool() {
        if (type != Type.BOOL) return;
        Object v = getValue();
        boolean current = v instanceof Boolean b && b;
        setter.accept(!current);
    }

    /** Adjust by delta. BOOL settings flip. Values are clamped to [min, max]. */
    public void adjust(double delta) {
        if (type == Type.BOOL) {
            toggleBool();
            return;
        }
        double v = clamp(toDouble(getValue()) + delta, min, max);
        if (type == Type.INT) setter.accept((int) Math.round(v));
        else setter.accept(v);
    }

    /** Human-readable current value, used by the settings panel. */
    public String displayValue() {
        Object v = getValue();
        if (v == null) return "-";
        return switch (type) {
            case BOOL -> (v instanceof Boolean b && b) ? "ON" : "OFF";
            case INT -> String.valueOf(((Number) v).intValue());
            case DOUBLE -> String.format("%.2f", ((Number) v).doubleValue());
        };
    }

    private static double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof Boolean b) return b ? 1 : 0;
        return 0;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}