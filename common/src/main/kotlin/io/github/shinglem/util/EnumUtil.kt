package io.github.shinglem.util



public inline fun <reified T : Enum<T>> enumOrdinalOf(od : Int): T {

   return enumValues<T>().find { it.ordinal == od } ?: enumValues<T>().lastOrNull() ?: throw Exception("do not has this enum in no.$od")
}

//public inline fun <reified T : Enum<T>> enumByName(name : String): T {
//   return enumValues<T>().find { it.name == name } ?: throw Exception("do not has this enum")
//}