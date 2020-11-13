package exp

import org.junit.Test

class AnnotionTest {

    @Test
    fun test() {
        A::class.annotations.also {
            println(it)
        }

        A::class.java.annotations
            .also {
            println(it.toList())
        }


        B::class.annotations.also {
            println(it)
        }

        B::class.java.annotations
            .also {
                println(it.toList())
            }
    }

}

annotation class Base
@Base
annotation class T1

@T1
class A{

}
class B