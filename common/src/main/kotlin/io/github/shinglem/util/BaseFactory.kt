package io.github.shinglem.util

interface BaseFactory<T> {

    fun setInstance(ins : T)
    fun getInstance(): T
}