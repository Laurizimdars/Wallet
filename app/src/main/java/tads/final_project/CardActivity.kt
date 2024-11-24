package tads.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import tads.final_project.databinding.ActivityCardBinding
import tads.final_project.entity.Card

class CardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardBinding
    private lateinit var adapter: ArrayAdapter<Card>
    private var pos: Int = -1
    private val MY_SCAN_REQUEST_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DBHelper(this)
        val cardList = db.cardsSelectAll()

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            cardList
        )
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, i, _ ->
            binding.textId.text = "ID: ${cardList[i].id}"
            binding.editCardNumber.setText(cardList[i].number)
            binding.editExpirationDate.setText(cardList[i].expiration_date)
            binding.editCvv.setText(cardList[i].cvv)
            pos = i
        }

        binding.buttonInsert.setOnClickListener {
            val number = binding.editCardNumber.text.toString()
            val expirationDate = binding.editExpirationDate.text.toString()
            val cvv = binding.editCvv.text.toString()
            if (isValidCard(number, expirationDate, cvv)) {
                val card = Card(number = number, expiration_date = expirationDate, cvv = cvv)
                val result = db.cardInsert(card)
                if (result > 0) {
                    Toast.makeText(applicationContext, "Sucesso! Id: $result", Toast.LENGTH_SHORT).show()
                    cardList.add(Card(result.toInt(), number, expirationDate, cvv))
                    adapter.notifyDataSetChanged()
                    binding.textId.text = "ID: "
                    binding.editCardNumber.setText("")
                    binding.editExpirationDate.setText("")
                    binding.editCvv.setText("")
                } else {
                    Toast.makeText(applicationContext, "Falha na inserção!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonDelete.setOnClickListener {
            if (pos >= 0) {
                val cardId = cardList[pos].id
                if (db.canDeleteCard(cardId)) {
                    db.cardDelete(cardId)
                    cardList.removeAt(pos)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext, "Cartão excluído com sucesso.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Não é possível excluir o cartão. Existem transações associadas.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonScan.setOnClickListener {
            onScanPress()
        }

        val tabLayout: TabLayout = findViewById(R.id.tbLayout)
        tabLayout.selectTab(tabLayout.getTabAt(2))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val intent: Intent
                when (tab?.position) {
                    0 -> {
                        intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        intent = Intent(applicationContext, TransactionActivity::class.java)
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

    private fun isValidCard(number: String, expirationDate: String, cvv: String): Boolean {
        if (number.isEmpty() || number.length < 15) {
            Toast.makeText(applicationContext, "Número do cartão inválido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidExpirationDate(expirationDate)) {
            Toast.makeText(applicationContext, "Data de expiração inválida.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvv.isEmpty() || cvv.length < 3 || cvv.length > 4) {
            Toast.makeText(applicationContext, "CVV inválido.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isValidExpirationDate(expirationDate: String): Boolean {
        val regex = Regex("^(0?[1-9]|1[0-2])/\\d{4}$")
        return regex.matches(expirationDate)
    }


    private fun onScanPress() {
        val scanIntent = Intent(this, CardIOActivity::class.java)

        // Customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false) // default: false

        startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Toast.makeText(this, "Scan was canceled.", Toast.LENGTH_SHORT).show()


        if (requestCode == MY_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                val scanResult = data.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)
                scanResult?.let {
                    binding.editCardNumber.setText(it.cardNumber)
                    if (it.isExpiryValid) {
                        binding.editExpirationDate.setText("${it.expiryMonth}/${it.expiryYear}")
                    }
                    if (it.cvv != null) {
                        binding.editCvv.setText(it.cvv)
                    }
                }
            }
        }
    }
}