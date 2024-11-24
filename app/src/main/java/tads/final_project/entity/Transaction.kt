package tads.final_project.entity

import android.content.Context
import tads.final_project.DBHelper

class Transaction(
    val id: Int = 0,
    var amount: Float = 0.0f,
    var card_id: Int = 0,  // Foreign Key
    var category_id: Int = 0,  // Foreign Key
    var is_paid: Boolean = false,
    var date: String = ""
) {
    fun toString(context: Context): String {
        val dbHelper = DBHelper(context)
        val card = dbHelper.getCardById(card_id)
        val category = dbHelper.getCategoryById(category_id)

        val cardNumber = card?.number ?: "Unknown"
        val categoryName = category?.name ?: "Unknown"

        return "ID: $id | Valor: $amount | Pago: ${if (is_paid) "Sim" else "Não"}\n" +
                "Data: $date | Categoria: $categoryName\n" +
                "Cartão: $cardNumber"
    }
}