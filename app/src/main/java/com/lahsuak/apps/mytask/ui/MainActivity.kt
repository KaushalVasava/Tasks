package com.lahsuak.apps.mytask.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.util.Constants.SHARE_FORMAT
import com.lahsuak.apps.mytask.data.util.Constants.THEME_DEFAULT
import com.lahsuak.apps.mytask.data.util.Constants.THEME_KEY
import com.lahsuak.apps.mytask.data.util.Util
import com.lahsuak.apps.mytask.data.util.Util.getUserLoginStatus
import com.lahsuak.apps.mytask.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var shareTxt: String? = null
        var isWidgetClick = false
        //var selectedTheme = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MyTask)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sp.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()

        AppCompatDelegate.setDefaultNightMode(selectedTheme)

        //shared text received from other apps
        if (intent?.action == Intent.ACTION_SEND) {
            if (SHARE_FORMAT == intent.type) {
                shareTxt = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }

        //this is for transparent status bar and navigation bar
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            true
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setOnClickListener {
            Toast.makeText(this, "Click toolbar", Toast.LENGTH_SHORT).show()
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.my_container) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.main_nav_graph)
        val lastSignIN = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignIN != null) {
            graph.setStartDestination(R.id.taskFragment)
        } else
            graph.setStartDestination(R.id.loginFragment)
        // if (getUserLoginStatus(this))
        //   graph.setStartDestination(R.id.taskFragment)

        navHostFragment.navController.graph = graph
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)//,appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        //Pass argument appBarConfiguration in navigateUp() method
        // for hamburger icon respond to click events
        //navConfiguration
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}