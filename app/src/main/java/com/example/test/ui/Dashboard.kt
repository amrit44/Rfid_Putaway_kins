package com.example.test.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.RFIDActivity
import com.example.test.databinding.ActivityDashboardBinding

class Dashboard : AppCompatActivity() {
    private val context: Context = this@Dashboard
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardput.setOnClickListener {
            startActivity(Intent(context, RFIDActivity::class.java))
        }
        binding.cardLf.setOnClickListener {
            startActivity(Intent(context, lostandfound::class.java))
        }
        binding.cardPutreturn.setOnClickListener {
            startActivity(Intent(context, Putawaydispatchreturn::class.java))
        }
    }

}