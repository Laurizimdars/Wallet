package tads.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import tads.final_project.databinding.ActivityCategoryBinding
import tads.final_project.entity.Category

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: ArrayAdapter<Category>
    private var pos: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.main)

        val db = DBHelper(this)
        val categoryList = db.categoriesSelectAll()

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categoryList
        )
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, i, _ ->
            binding.textId.text = "ID: ${categoryList[i].id}"
            binding.editCategoryName.setText(categoryList[i].name)
            pos = i
        }

        binding.buttonInsert.setOnClickListener {
            val name = binding.editCategoryName.text.toString()
            if (isValidCategoryName(name)) {
                val category = Category(name = name)
                val result = db.categoryInsert(category)
                if (result > 0) {
                    Toast.makeText(applicationContext, "Categoria inserida! Id: $result", Toast.LENGTH_SHORT).show()
                    categoryList.add(Category(result.toInt(), name))
                    adapter.notifyDataSetChanged()
                    binding.textId.text = "ID: "
                    binding.editCategoryName.setText("")
                } else {
                    Toast.makeText(applicationContext, "Falha na inserção!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonUpdate.setOnClickListener {
            if (pos >= 0) {
                val name = binding.editCategoryName.text.toString()
                if (isValidCategoryName(name)) {
                    val category = categoryList[pos]
                    category.name = name
                    val result = db.categoryUpdate(category)
                    if (result > 0) {
                        Toast.makeText(applicationContext, "Categoria atualizada!", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                        pos = -1
                        binding.textId.text = "ID: "
                        binding.editCategoryName.setText("")
                    } else {
                        Toast.makeText(applicationContext, "Falha na atualização!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.buttonDelete.setOnClickListener {
            if (pos >= 0) {
                val category = categoryList[pos]
                if (db.canDeleteCategory(category.id)) {
                    Toast.makeText(applicationContext, "Categoria em uso, não pode ser deletada!", Toast.LENGTH_SHORT).show()
                } else {
                    val result = db.categoryDelete(category.id)
                    if (result > 0) {
                        Toast.makeText(applicationContext, "Categoria deletada!", Toast.LENGTH_SHORT).show()
                        categoryList.removeAt(pos)
                        adapter.notifyDataSetChanged()
                        pos = -1
                        binding.textId.text = "ID: "
                        binding.editCategoryName.setText("")
                    } else {
                        Toast.makeText(applicationContext, "Falha na exclusão!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val tabLayout: TabLayout = findViewById(R.id.tbLayout)
        tabLayout.selectTab(tabLayout.getTabAt(3))

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
                    2 -> {
                        intent = Intent(applicationContext, CardActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun isValidCategoryName(name: String): Boolean {
        return if (name.length > 3) {
            true
        } else {
            Toast.makeText(applicationContext, "O nome da categoria deve ter mais de 3 caracteres.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}