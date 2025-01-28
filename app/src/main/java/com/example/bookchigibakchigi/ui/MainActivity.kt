package com.example.bookchigibakchigi.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMainBinding
import com.example.bookchigibakchigi.ui.bookdetail.BookDetailFragment
import com.example.bookchigibakchigi.ui.searchbook.SearchBookActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
                R.id.navigation_record,
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

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val currentDestination = navController.currentDestination?.id

        if (currentDestination == R.id.navigation_book_detail) {
            // BookDetailFragment를 찾아서 업데이트 트리거
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
            val fragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
            if (fragment is BookDetailFragment) {
                fragment.refreshContent() // Fragment의 refresh 메서드 호출
            }
        }
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
                binding.toolbar.title = getString(R.string.b_archive)
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
//                binding.toolbar.menu.findItem(R.id.action_grid).isVisible = true
//                binding.toolbar.menu.findItem(R.id.action_horizontal).isVisible = false
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
            }
            R.id.navigation_my_library -> {
                 binding.toolbar.title = getString(R.string.b_archive)
                binding.toolbar.inflateMenu(R.menu.menu_my_library)
//                binding.toolbar.menu.findItem(R.id.action_horizontal).isVisible = true
//                binding.toolbar.menu.findItem(R.id.action_grid).isVisible = false
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
            R.id.navigation_record -> {
                binding.toolbar.title = getString(R.string.title_record)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                moveToBookSearchActivity()
                true
            }
            R.id.action_grid -> {
                val navController = findNavController(R.id.nav_host_fragment_activity_main)
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.navigation_book_detail) {
                    navController.navigate(R.id.navigation_my_library)
                } else {
                    Toast.makeText(this, "Grid", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_horizontal -> {
                val navController = findNavController(R.id.nav_host_fragment_activity_main)
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.navigation_my_library) {
                    navController.navigate(R.id.navigation_book_detail)
                } else {
                    Toast.makeText(this, "Horizontal", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun moveToBookSearchActivity() {
        // SearchBookActivity를 시작
        val intent = Intent(this, SearchBookActivity::class.java)
        startActivity(intent)
    }
}
