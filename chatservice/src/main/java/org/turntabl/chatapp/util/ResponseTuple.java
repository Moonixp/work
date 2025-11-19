package org.turntabl.chatapp.util;

import lombok.Data;

@Data
public class ResponseTuple<T, V> {
    private final T data;
    private final V code;

    @Override
    public String toString() {
        return "response code: " + code + "\ndata: \n" + data;
    }
}
