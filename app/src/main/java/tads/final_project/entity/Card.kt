package tads.final_project.entity

class Card(
    val id: Int = 0,
    var number: String = "",
    var cvv: String = "",
    var expiration_date: String = ""
) {
    override fun toString(): String {
        return "Card ID: $id\nNúmero: $number\nData de Expiração: $expiration_date\nCVV: $cvv"
    }
}