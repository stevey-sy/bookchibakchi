package com.example.bookchigibakchigi.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.widget.PopupMenu
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

        initToolbar()
        initNavigation()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
        }
    }

    private fun initNavigation() {
        val navView = binding.navView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_library,
                R.id.navigation_pick_book
            )
        )

        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        navView.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_my_library, true)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build()

            when (item.itemId) {
                R.id.navigation_my_library -> {
                    navController.navigate(R.id.navigation_my_library, null, navOptions)
                    true
                }
                R.id.navigation_pick_book -> {
                    navController.navigate(R.id.navigation_community, null, navOptions)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarForDestination(destination.id)
        }
    }

    private fun updateToolbarForDestination(destinationId: Int) {
        binding.toolbar.menu.clear()
        when (destinationId) {
            R.id.navigation_my_library -> {
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
            }
        }

        binding.toolbar.title = getString(R.string.app_name)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
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
            R.id.action_filter -> {
                val wrapper = android.view.ContextThemeWrapper(this, R.style.menuItem)
//                val popup = PopupMenu(wrapper, binding.toolbar)
                val popup = PopupMenu(wrapper, binding.toolbar, Gravity.END,0, R.style.PopupStyle)
                popup.menuInflater.inflate(R.menu.menu_filter, popup.menu)
                
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.filter_reading -> {
                            mainViewModel.updateFilterType(BookFilterType.Reading)
                            true
                        }
                        R.id.filter_finished -> {
                            mainViewModel.updateFilterType(BookFilterType.Finished)
                            true
                        }
                        R.id.filter_all -> {
                            mainViewModel.updateFilterType(BookFilterType.All)
                            true
                        }
                        else -> false
                    }
                }
                
                popup.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
