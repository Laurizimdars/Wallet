package tads.final_project.entity

class Category(
    val id: Int = 0,
    var name: String = ""
) {
    override fun toString(): String {
        return "Categoria ID: $id\nNome: $name"
    }
}