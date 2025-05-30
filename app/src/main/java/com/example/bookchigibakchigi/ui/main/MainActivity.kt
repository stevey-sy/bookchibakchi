package com.example.bookchigibakchigi.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMainBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.searchbook.SearchBookActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
//        copyDatabaseFile(this)
    }

//    fun copyDatabaseFile(context: Context) {
//        val dbName = "book_database" // .db 없음!
//        val dbFile = context.getDatabasePath(dbName)
//        val destFile = File(context.getExternalFilesDir(null), "$dbName.backup")
//
//        try {
//            FileInputStream(dbFile).use { input ->
//                FileOutputStream(destFile).use { output ->
//                    input.copyTo(output)
//                }
//            }
//            Log.d("DB_COPY", "DB copied to: ${destFile.absolutePath}")
//        } catch (e: Exception) {
//            Log.e("DB_COPY", "Error copying DB", e)
//        }
//    }

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

    private fun createSpannableString(text: String, color: Int): SpannableString {
        return SpannableString(text).apply {
            setSpan(ForegroundColorSpan(getColor(color)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun setupMenuItem(
        menuItem: MenuItem?,
        text: String,
        isSelected: Boolean = false
    ) {
        menuItem?.apply {
            title = createSpannableString(
                text = text,
                color = if (isSelected) android.R.color.black else android.R.color.darker_gray
            )
            if (isSelected) {
                icon = AppCompatResources.getDrawable(applicationContext, R.drawable.ic_check)
            }
        }
    }

    private fun setupFilterMenu(popup: PopupMenu) {
        when (mainViewModel.filterType.value) {
            is BookFilterType.Reading -> {
                setupMenuItem(popup.menu.findItem(R.id.filter_reading), "읽는 중", true)
                setupMenuItem(popup.menu.findItem(R.id.filter_finished), "완독")
                setupMenuItem(popup.menu.findItem(R.id.filter_all), "전체")
            }
            is BookFilterType.Finished -> {
                setupMenuItem(popup.menu.findItem(R.id.filter_reading), "읽는 중")
                setupMenuItem(popup.menu.findItem(R.id.filter_finished), "완독", true)
                setupMenuItem(popup.menu.findItem(R.id.filter_all), "전체")
            }
            is BookFilterType.All -> {
                setupMenuItem(popup.menu.findItem(R.id.filter_reading), "읽는 중")
                setupMenuItem(popup.menu.findItem(R.id.filter_finished), "완독")
                setupMenuItem(popup.menu.findItem(R.id.filter_all), "전체", true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this, SearchBookActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_filter -> {
                val wrapper = ContextThemeWrapper(this, R.style.menuItem)
                val popup = PopupMenu(wrapper, binding.toolbar, Gravity.END, 0, R.style.PopupStyle)
                popup.menuInflater.inflate(R.menu.menu_filter, popup.menu)
                
                setupFilterMenu(popup)
                
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
