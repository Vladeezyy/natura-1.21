package com.progwml6.natura.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A loader-neutral config description (the ModConfigSpec builder calls the config was written with): sections,
 * options with comments, defaults and ranges. Each loader binds the options to its own config file; an option not bound
 * yet (or before the file is read) gives its default.
 */
public final class ConfigSpec {
    private final List<Value<?>> values;

    private ConfigSpec(List<Value<?>> values) {
        this.values = List.copyOf(values);
    }

    public List<Value<?>> values() {
        return this.values;
    }

    public abstract static sealed class Value<T> implements Supplier<T> permits BoolValue, IntValue {
        private final List<String> path;
        private final List<String> comment;
        private final T defaultValue;
        private volatile Supplier<T> source;

        Value(List<String> path, List<String> comment, T defaultValue) {
            this.path = List.copyOf(path);
            this.comment = List.copyOf(comment);
            this.defaultValue = defaultValue;
        }

        /** The section names, then the option name. */
        public List<String> path() {
            return this.path;
        }

        public List<String> comment() {
            return this.comment;
        }

        public T defaultValue() {
            return this.defaultValue;
        }

        /** Binds the option to the loader's config value. */
        public void bind(Supplier<T> source) {
            this.source = source;
        }

        @Override
        public T get() {
            Supplier<T> source = this.source;
            return source == null ? this.defaultValue : source.get();
        }
    }

    public static final class BoolValue extends Value<Boolean> {
        BoolValue(List<String> path, List<String> comment, boolean defaultValue) {
            super(path, comment, defaultValue);
        }
    }

    public static final class IntValue extends Value<Integer> {
        private final int min;
        private final int max;

        IntValue(List<String> path, List<String> comment, int defaultValue, int min, int max) {
            super(path, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        public int min() {
            return this.min;
        }

        public int max() {
            return this.max;
        }
    }

    public static final class Builder {
        private final List<String> sections = new ArrayList<>();
        private final List<Value<?>> values = new ArrayList<>();
        private List<String> comment = List.of();

        public Builder push(String section) {
            this.sections.add(section);
            return this;
        }

        public Builder pop() {
            this.sections.remove(this.sections.size() - 1);
            return this;
        }

        public Builder comment(String... lines) {
            this.comment = List.of(lines);
            return this;
        }

        public BoolValue define(String name, boolean defaultValue) {
            return this.add(new BoolValue(this.path(name), this.takeComment(), defaultValue));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return this.add(new IntValue(this.path(name), this.takeComment(), defaultValue, min, max));
        }

        public ConfigSpec build() {
            return new ConfigSpec(this.values);
        }

        private List<String> path(String name) {
            List<String> path = new ArrayList<>(this.sections);
            path.add(name);
            return path;
        }

        private List<String> takeComment() {
            List<String> comment = this.comment;
            this.comment = List.of();
            return comment;
        }

        private <V extends Value<?>> V add(V value) {
            this.values.add(value);
            return value;
        }
    }
}
