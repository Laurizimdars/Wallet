package tads.final_project

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import tads.final_project.databinding.ActivityTransactionBinding
import tads.final_project.entity.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var adapter: TransactionAdapter
    private var transactionList: MutableList<Transaction> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = DBHelper(this)
        val cardList = db.cardsSelectAll()
        val categoryList = db.categoriesSelectAll()

        setupSpinner(binding.spinnerCard, cardList.map { it.number })
        setupSpinner(binding.spinnerCategory, categoryList.map { it.name })

        transactionList = db.transactionsSelectAll().toMutableList()
        adapter = TransactionAdapter(this, transactionList)
        binding.listView.adapter = adapter

        binding.buttonSave.setOnClickListener {
            if (cardList.isEmpty() || categoryList.isEmpty()) {
                Toast.makeText(applicationContext, "É necessário cadastrar pelo menos um cartão e uma categoria.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCardPosition = binding.spinnerCard.selectedItemPosition
            val selectedCategoryPosition = binding.spinnerCategory.selectedItemPosition

            val amount = binding.editAmount.text.toString().toFloatOrNull()
            val date = binding.editDate.text.toString()

            if (selectedCardPosition < 0 || selectedCategoryPosition < 0) {
                Toast.makeText(applicationContext, "Selecione um cartão e uma categoria.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null || amount <= 0) {
                Toast.makeText(applicationContext, "Insira um valor válido maior que zero.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDate(date)) {
                Toast.makeText(applicationContext, "Insira uma data válida no formato dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                amount = amount,
                card_id = cardList[selectedCardPosition].id,
                category_id = categoryList[selectedCategoryPosition].id,
                date = date,
                is_paid = false
            )

            val result = db.transactionInsert(transaction)
            if (result > 0) {
                Toast.makeText(applicationContext, "Transação salva!", Toast.LENGTH_SHORT).show()
                transactionList.add(Transaction(result.toInt(), amount, cardList[selectedCardPosition].id, categoryList[selectedCategoryPosition].id, false, date))
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(applicationContext, "Falha ao salvar transação!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val transaction = transactionList[position]
            transaction.is_paid = !transaction.is_paid
            db.transactionUpdate(transaction)
            adapter.notifyDataSetChanged()
            Toast.makeText(applicationContext, "Status de pagamento alterado!", Toast.LENGTH_SHORT).show()
        }

        val tabLayout: TabLayout = findViewById(R.id.tbLayout)
        tabLayout.selectTab(tabLayout.getTabAt(1))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val intent: Intent
                when (tab?.position) {
                    0 -> {
                        intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        intent = Intent(applicationContext, CardActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        intent = Intent(applicationContext, CategoryActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private class TransactionAdapter(context: Context, transactions: List<Transaction>) :
        ArrayAdapter<Transaction>(context, android.R.layout.simple_list_item_1, transactions) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val transaction = getItem(position)
            if (transaction != null) {
                view.text = transaction.toString(context)
            }

            // Set text color based on is_paid status
            view.setTextColor(if (transaction?.is_paid == true) Color.GREEN else Color.RED)

            return view
        }
    }
}