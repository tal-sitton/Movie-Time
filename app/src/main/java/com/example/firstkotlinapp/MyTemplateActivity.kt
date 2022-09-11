package com.example.firstkotlinapp

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

open class MyTemplateActivity : AppCompatActivity() {

    override fun onBackPressed() {
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_manu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                MainActivity.resetToDefault()
                MainActivity.filter()
                recreate()
                true
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }
}
