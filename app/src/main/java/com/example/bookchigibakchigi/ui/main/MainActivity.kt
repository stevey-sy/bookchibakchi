package com.example.bookchigibakchigi.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMainBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val navController by lazy { findNavController(R.id.nav_host_fragment_activity_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
        }
    }

    private fun setupNavigation() {
        val navView = binding.navView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_library,
                R.id.navigation_book_detail,
                R.id.navigation_pick_book,
                R.id.navigation_community,
                R.id.navigation_setting
            )
        )

        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarForDestination(destination.id)
        }
    }

    private fun updateToolbarForDestination(destinationId: Int) {
        binding.toolbar.menu.clear()
        
        val (title, showBackButton) = when (destinationId) {
            R.id.navigation_book_detail, R.id.navigation_my_library -> {
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
                getString(R.string.app_name) to false
            }
            R.id.navigation_pick_book -> getString(R.string.title_pick_book) to false
            R.id.navigation_community -> getString(R.string.title_community) to false
            R.id.navigation_setting -> getString(R.string.title_setting) to false
            else -> null to true
        }

        binding.toolbar.title = title
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showBackButton)
            setHomeButtonEnabled(showBackButton)
        }

        binding.toolbar.getChildAt(0)?.let { view ->
            if (view is TextView) {
                view.typeface = ResourcesCompat.getFont(this, R.font.dashi)
                view.textSize = 32f
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_my_library, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                navController.navigate(R.id.navigation_search_book)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
