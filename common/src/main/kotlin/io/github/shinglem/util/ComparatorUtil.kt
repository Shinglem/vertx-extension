package io.github.shinglem.util

import java.math.BigDecimal

val STRING_COMPARATOR = Comparator<String> { x: String, y: String ->
    val fileA: String = x
    val fileB: String = y
    val arr1 = fileA.toCharArray()
    val arr2 = fileB.toCharArray()
    var i = 0
    var j = 0
    while (i < arr1.size && j < arr2.size) {
        if (Character.isDigit(arr1[i]) && Character.isDigit(arr2[j])) {
            var s1 = ""
            var s2 = ""
            while (i < arr1.size && Character.isDigit(arr1[i])) {
                s1 += arr1[i]
                i++
            }
            while (j < arr2.size && Character.isDigit(arr2[j])) {
                s2 += arr2[j]
                j++
            }

            return@Comparator BigDecimal(s1).compareTo(BigDecimal(s2))
        } else {
            if (arr1[i] > arr2[j]) {
                return@Comparator 1
            }
            if (arr1[i] < arr2[j]) {
                return@Comparator -1
            }
            i++
            j++
        }
    }
    if (arr1.size == arr2.size) {
        return@Comparator 0
    } else {
        return@Comparator if (arr1.size > arr2.size) 1 else -1
    }
}