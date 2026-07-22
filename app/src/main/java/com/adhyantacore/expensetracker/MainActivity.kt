package com.adhyantacore.expensetracker

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.adhyantacore.expensetracker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()


    }
    private fun setupBottomNavigation() {

        binding.navDashboard.setOnClickListener {
            navigateTo(R.id.dashboardFragment)
        }

        binding.navTransactions.setOnClickListener {
            navigateTo(R.id.transactionFragment)
        }

        binding.navAdd.setOnClickListener {
            navigateTo(R.id.addExpenseFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {

                R.id.dashboardFragment -> selectDashboard()

                R.id.transactionFragment -> selectTransactions()

                R.id.addExpenseFragment -> selectAdd()
            }
        }
    }

    private fun selectDashboard() {

        binding.navDashboard.setBackgroundResource(R.drawable.bg_bottom_nav_active_pill)
        binding.navTransactions.background = null
        binding.navAdd.background = null

        binding.imgDashboard.setColorFilter(getColor(R.color.white))
        binding.txtDashboard.setTextColor(getColor(R.color.white))

        binding.imgTransactions.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtTransactions.setTextColor(getColor(R.color.bottom_nav_inactive))

        binding.imgAdd.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtAdd.setTextColor(getColor(R.color.bottom_nav_inactive))

    }

    private fun selectTransactions() {

        binding.navTransactions.setBackgroundResource(R.drawable.bg_bottom_nav_active_pill)
        binding.navDashboard.background = null
        binding.navAdd.background = null

        binding.imgDashboard.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtDashboard.setTextColor(getColor(R.color.bottom_nav_inactive))

        binding.imgTransactions.setColorFilter(getColor(R.color.white))
        binding.txtTransactions.setTextColor(getColor(R.color.white))

        binding.imgAdd.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtAdd.setTextColor(getColor(R.color.bottom_nav_inactive))



    }

    private fun selectAdd() {

        binding.navAdd.setBackgroundResource(R.drawable.bg_bottom_nav_active_pill)
        binding.navDashboard.background = null
        binding.navTransactions.background = null

        binding.imgDashboard.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtDashboard.setTextColor(getColor(R.color.bottom_nav_inactive))

        binding.imgTransactions.setColorFilter(getColor(R.color.bottom_nav_inactive))
        binding.txtTransactions.setTextColor(getColor(R.color.bottom_nav_inactive))

        binding.imgAdd.setColorFilter(getColor(R.color.white))
        binding.txtAdd.setTextColor(getColor(R.color.white))

    }

    private fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(destinationId)
        }
    }
}