package com.example.bookchigibakchigi.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

abstract class BaseActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    protected fun addProgressBarToLayout(bindingRoot: ViewGroup) {
        // ProgressBar 생성
        progressBar = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                150, // 너비
                150  // 높이
            ).apply {
                gravity = Gravity.CENTER // 화면 중앙
            }
            // ProgressBar 색상 설정
            indeterminateTintList = getColorStateList(android.R.color.black)
            visibility = View.GONE // 기본 숨김
        }

        // ProgressBar를 Layout에 추가
        bindingRoot.addView(progressBar)
    }

    // Toolbar 설정
    protected fun initToolbar(toolbar: Toolbar, rootView: View) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    protected fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    protected fun hideProgressBar() {
        progressBar?.visibility = View.GONE
    }
}
