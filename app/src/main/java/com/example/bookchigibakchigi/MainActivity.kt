package com.example.bookchigibakchigi

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchigibakchigi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // AppBarConfiguration 설정
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_library, R.id.navigation_pick_book, R.id.navigation_community, R.id.navigation_record, R.id.navigation_setting
            )
        )

        // BottomNavigationView와 NavController 연결
        navView.setupWithNavController(navController)

        // ActionBar와 NavController 연결
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 초기 타이틀 설정
        val currentDestination = navController.currentDestination
        currentDestination?.let {
            updateToolbarTitle(it.id)
        }

        // NavController의 목적지가 변경될 때마다 Toolbar의 타이틀을 업데이트
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarTitle(destination.id)
        }
    }

    // 목적지에 따라 Toolbar 타이틀을 업데이트하는 함수
    private fun updateToolbarTitle(destinationId: Int) {
        when (destinationId) {
            R.id.navigation_my_library -> binding.toolbar.title = getString(R.string.title_my_library)
            R.id.navigation_pick_book -> binding.toolbar.title = getString(R.string.title_pick_book)
            R.id.navigation_community -> binding.toolbar.title = getString(R.string.title_community)
            R.id.navigation_record -> binding.toolbar.title = getString(R.string.title_record)
            R.id.navigation_setting -> binding.toolbar.title = getString(R.string.title_setting)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}