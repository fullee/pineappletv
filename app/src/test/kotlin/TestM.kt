import java.io.File

fun main() {
    val file = File("C://")
    file.listFiles().forEach { println("${it.name}") }
}