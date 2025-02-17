package com.sas.sasnettystarter.netty;

@FunctionalInterface
public interface TiFunction<T, U, P extends ProjectInterface, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u, P p);
}