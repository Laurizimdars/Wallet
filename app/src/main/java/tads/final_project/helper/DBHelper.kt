package tads.final_project

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import tads.final_project.entity.Card
import tads.final_project.entity.Category
import tads.final_project.entity.Transaction

class DBHelper(context: Context) : SQLiteOpenHelper(context, "banco.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createCardTable = """
            CREATE TABLE IF NOT EXISTS card (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                number TEXT NOT NULL,
                cvv TEXT NOT NULL,
                expiration_date TEXT NOT NULL
            )
        """
        db.execSQL(createCardTable)

        val createCategoryTable = """
            CREATE TABLE IF NOT EXISTS category (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """
        db.execSQL(createCategoryTable)

        val createTransactionTable = """
            CREATE TABLE IF NOT EXISTS [transaction] (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount FLOAT NOT NULL,
                card_id INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                is_paid BOOLEAN NOT NULL,
                date TEXT NOT NULL,
                FOREIGN KEY(card_id) REFERENCES card(id),
                FOREIGN KEY(category_id) REFERENCES category(id)
            )
        """
        db.execSQL(createTransactionTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS [transaction]")
        db.execSQL("DROP TABLE IF EXISTS category")
        db.execSQL("DROP TABLE IF EXISTS card")
        onCreate(db)
    }

    // Card functions
    fun cardInsert(card: Card): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("number", card.number)
            put("cvv", card.cvv)
            put("expiration_date", card.expiration_date)
        }
        return db.insert("card", null, values)
    }

    fun cardUpdate(card: Card): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("number", card.number)
            put("cvv", card.cvv)
            put("expiration_date", card.expiration_date)
        }
        return db.update("card", values, "id = ?", arrayOf(card.id.toString()))
    }

    fun cardDelete(id: Int): Int {
        val db = writableDatabase
        return db.delete("card", "id = ?", arrayOf(id.toString()))
    }

    fun getCardById(cardId: Int): Card? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM card WHERE id = ?", arrayOf(cardId.toString()))

        var card: Card? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val number = cursor.getString(cursor.getColumnIndexOrThrow("number"))
            val cvv = cursor.getString(cursor.getColumnIndexOrThrow("cvv"))
            val expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("expiration_date"))
            card = Card(id, number, cvv, expirationDate)
        }
        cursor.close()
        return card
    }

    fun cardsSelectAll(): ArrayList<Card> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM card", null)

        val cards = ArrayList<Card>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val numberIndex = cursor.getColumnIndex("number")
            val cvvIndex = cursor.getColumnIndex("cvv")
            val expirationDateIndex = cursor.getColumnIndex("expiration_date")

            if (idIndex != -1 && numberIndex != -1 && cvvIndex != -1 && expirationDateIndex != -1) {
                val id = cursor.getInt(idIndex)
                val number = cursor.getString(numberIndex)
                val cvv = cursor.getString(cvvIndex)
                val expirationDate = cursor.getString(expirationDateIndex)
                cards.add(Card(id, number, cvv, expirationDate))
            }
        }
        cursor.close()
        return cards
    }

    fun canDeleteCard(cardId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM [transaction] WHERE card_id = ?", arrayOf(cardId.toString()))
        var canDelete = true
        if (cursor.moveToFirst()) {
            canDelete = cursor.getInt(0) == 0
        }
        cursor.close()
        return canDelete
    }


    // Category functions
    fun categoryInsert(category: Category): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", category.name)
        }
        return db.insert("category", null, values)
    }

    fun categoryUpdate(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", category.name)
        }
        return db.update("category", values, "id = ?", arrayOf(category.id.toString()))
    }

    fun categoryDelete(id: Int): Int {
        val db = writableDatabase
        return db.delete("category", "id = ?", arrayOf(id.toString()))
    }

    fun getCategoryById(categoryId: Int): Category? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM category WHERE id = ?", arrayOf(categoryId.toString()))

        var category: Category? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            category = Category(id, name)
        }
        cursor.close()
        return category
    }

    fun categoriesSelectAll(): ArrayList<Category> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM category", null)

        val categories = ArrayList<Category>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val nameIndex = cursor.getColumnIndex("name")

            if (idIndex != -1 && nameIndex != -1) {
                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                categories.add(Category(id, name))
            }
        }
        cursor.close()
        return categories
    }

    fun canDeleteCategory(categoryId: Int): Boolean {
        val db: SQLiteDatabase = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM [transaction] WHERE category_id = ?",
            arrayOf(categoryId.toString())
        )
        var inUse = false
        if (cursor.moveToFirst()) {
            inUse = cursor.getInt(0) > 0
        }
        cursor.close()
        return inUse
    }

    // Transaction functions
    fun transactionInsert(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", transaction.amount)
            put("card_id", transaction.card_id)
            put("category_id", transaction.category_id)
            put("is_paid", transaction.is_paid)
            put("date", transaction.date)
        }
        return db.insert("[transaction]", null, values)
    }

    fun transactionUpdate(transaction: Transaction): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", transaction.amount)
            put("card_id", transaction.card_id)
            put("category_id", transaction.category_id)
            put("is_paid", transaction.is_paid)
            put("date", transaction.date)
        }
        return db.update("[transaction]", values, "id = ?", arrayOf(transaction.id.toString()))
    }

    fun transactionsSelectAll(): ArrayList<Transaction> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM [transaction]", null)

        val transactions = ArrayList<Transaction>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val amountIndex = cursor.getColumnIndex("amount")
            val cardIdIndex = cursor.getColumnIndex("card_id")
            val categoryIdIndex = cursor.getColumnIndex("category_id")
            val isPaidIndex = cursor.getColumnIndex("is_paid")
            val dateIndex = cursor.getColumnIndex("date")

            if (idIndex != -1 && amountIndex != -1 && cardIdIndex != -1 &&
                categoryIdIndex != -1 && isPaidIndex != -1 && dateIndex != -1) {
                val id = cursor.getInt(idIndex)
                val amount = cursor.getFloat(amountIndex)
                val cardId = cursor.getInt(cardIdIndex)
                val categoryId = cursor.getInt(categoryIdIndex)
                val isPaid = cursor.getInt(isPaidIndex) > 0
                val date = cursor.getString(dateIndex)
                transactions.add(Transaction(id, amount, cardId, categoryId, isPaid, date))
            }
        }
        cursor.close()
        return transactions
    }

    fun getTransactionSums(): Pair<Float, Float> {
        val db = readableDatabase
        var sumPaid = 0.0f
        var sumUnpaid = 0.0f

        val cursor: Cursor = db.rawQuery(
            "SELECT is_paid, SUM(amount) as total FROM [transaction] GROUP BY is_paid",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val isPaid = cursor.getInt(cursor.getColumnIndexOrThrow("is_paid")) == 1
                val total = cursor.getFloat(cursor.getColumnIndexOrThrow("total"))
                if (isPaid) {
                    sumPaid = total
                } else {
                    sumUnpaid = total
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return Pair(sumPaid, sumUnpaid)
    }
}