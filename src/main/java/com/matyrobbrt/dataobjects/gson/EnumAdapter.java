package com.matyrobbrt.dataobjects.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class EnumAdapter<T extends Enum<T>> extends TypeAdapter<T> {
    private final Class<T> clazz;
    private final boolean ignoreCase;

    public EnumAdapter(Class<T> clazz, boolean ignoreCase) {
        this.clazz = clazz;
        this.ignoreCase = ignoreCase;
    }

    public EnumAdapter(Class<T> clazz) {
        this(clazz, true);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public T read(JsonReader in) throws IOException {
        final var name = in.nextString();
        for (final var value : clazz.getEnumConstants())
            if (isSame(value.toString(), name))
                return value;
        return null;
    }

    private boolean isSame(String one, String two) {
        if (ignoreCase)
            return one.equalsIgnoreCase(two);
        else
            return one.equals(two);
    }
}
