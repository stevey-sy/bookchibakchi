package com.example.bookchigibakchigi.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMainBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.dialog.TwoButtonsDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var myLibraryNavHostFragment: Fragment
    private lateinit var communityNavHostFragment: Fragment
    private var currentMenuResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initFragments()
        initNavigation()
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                TwoButtonsDialog(
                    context = this@MainActivity,
                    title = "알림",
                    msg = "앱을 종료하시겠습니까?",
                    btnText1 = "취소",
                    btnText2 = "종료",
                    onBtn1Click = {
                        // 취소 버튼 클릭 시 아무것도 하지 않음
                    },
                    onBtn2Click = {
                        // 종료 버튼 클릭 시 앱 종료
                        finish()
                    }
                ).show()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
        }
    }

    private fun initFragments() {
        communityNavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_community)!!
        myLibraryNavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_my_library)!!

        // 처음에는 MyLibrary만 보여주기
        supportFragmentManager.beginTransaction()
            .hide(communityNavHostFragment)
            .show(myLibraryNavHostFragment)
            .commit()

    }

    private fun initNavigation() {
        val navView = binding.navView

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_my_library -> {
                    supportFragmentManager.beginTransaction()
                        .show(myLibraryNavHostFragment)
                        .hide(communityNavHostFragment)
                        .commit()
                    updateToolbarTitle(
                        title = "오독오독",
                        fontResId = R.font.dashi,
                        textSizeSp = 30f,
                        menuResId = R.menu.menu_my_library
                    )
                    true
                }
                R.id.navigation_community -> {
                    supportFragmentManager.beginTransaction()
                        .hide(myLibraryNavHostFragment)
                        .show(communityNavHostFragment)
                        .commit()

                    updateToolbarTitle(
                        title = "오독오독",
                        fontResId = R.font.dashi,
                        textSizeSp = 30f,
                        menuResId = null
                    )
                    true
                }
                else -> false
            }
        }
    }

    fun updateToolbarTitle(
        title: String,
        fontResId: Int,
        textSizeSp: Float,
        menuResId: Int? = null
    ) {
        // 기존 타이틀 제거
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 안 쓰겠다는 선언

        // 기존 타이틀 뷰 제거 (tag로 식별)
        val existing = binding.toolbar.findViewWithTag<TextView>("customTitle")
        existing?.let { binding.toolbar.removeView(it) }

        // 새로운 커스텀 TextView 추가
        val titleView = TextView(this).apply {
            text = title
            typeface = ResourcesCompat.getFont(context, fontResId)
            textSize = textSizeSp
            setTextColor(getColor(android.R.color.black))
            tag = "customTitle"
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
        }

        binding.toolbar.addView(titleView)

        // 메뉴 설정
        if (menuResId != null) {
            currentMenuResId = menuResId
            invalidateOptionsMenu()
        } else {
            currentMenuResId = null
            binding.toolbar.menu.clear()
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

//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_my_library, menu)
//        return true
//    }
override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//    menu?.clear()
    currentMenuResId?.let {
        menuInflater.inflate(it, menu)
        Log.d("ToolbarMenuCheck", "menu size: ${menu?.size()}")
    }
    return true
}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
//                navController.navigate(R.id.navigation_search_book)
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
