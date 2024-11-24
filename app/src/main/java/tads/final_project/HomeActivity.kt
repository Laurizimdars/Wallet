package tads.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tabLayout: TabLayout = findViewById(R.id.tbLayout)
        tabLayout.selectTab(tabLayout.getTabAt(0))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val intent: Intent
                when (tab?.position) {
                    1 -> {
                        intent = Intent(applicationContext, TransactionActivity::class.java)
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

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        val dbHelper = DBHelper(this)
        val (sumPaid, sumUnpaid) = dbHelper.getTransactionSums()

        val paidTextView: TextView = findViewById(R.id.text_paid_sum)
        val unpaidTextView: TextView = findViewById(R.id.text_unpaid_sum)

        paidTextView.text = "Total Pago: $sumPaid R$"
        unpaidTextView.text = "Total Não pago: $sumUnpaid R$"
    }
}