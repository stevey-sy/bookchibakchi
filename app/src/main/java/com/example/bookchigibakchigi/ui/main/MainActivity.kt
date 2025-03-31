package com.example.bookchigibakchigi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMainBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.searchbook.SearchBookActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    val mainViewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar)
        // 백버튼 제거
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)


        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_library, // Add this line
                R.id.navigation_book_detail,
                R.id.navigation_pick_book,
                R.id.navigation_community,
                R.id.navigation_setting,
            )
        )

        // BottomNavigationView와 NavController 연결
        navView.setupWithNavController(navController)

        // ActionBar와 NavController 연결
        setupActionBarWithNavController(navController, appBarConfiguration)

        // NavController의 목적지가 변경될 때마다 Toolbar의 타이틀을 업데이트
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarForDestination(destination.id)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_my_library, menu)
        return true
    }

    private fun updateToolbarForDestination(destinationId: Int) {
        // Toolbar 메뉴 초기화
        binding.toolbar.menu.clear()

        when (destinationId) {
            R.id.navigation_book_detail -> {
                binding.toolbar.title = getString(R.string.app_name)
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            R.id.navigation_my_library -> {
                 binding.toolbar.title = getString(R.string.app_name)
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            R.id.navigation_pick_book -> {
                binding.toolbar.title = getString(R.string.title_pick_book)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            R.id.navigation_community -> {
                binding.toolbar.title = getString(R.string.title_community)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            R.id.navigation_setting -> {
                binding.toolbar.title = getString(R.string.title_setting)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            else -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeButtonEnabled(true)
            }
        }

                binding.toolbar.getChildAt(0)?.let { view ->
            if (view is TextView) {
                view.typeface = ResourcesCompat.getFont(this, R.font.dashi)
                view.textSize = 32f
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                moveToBookSearchActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun moveToBookSearchActivity() {
        val intent = Intent(this, SearchBookActivity::class.java)
        startActivity(intent)
    }
}
