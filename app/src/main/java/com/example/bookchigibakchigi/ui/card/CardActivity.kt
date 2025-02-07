package com.example.bookchigibakchigi.ui.card

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityCardBinding
import com.example.bookchigibakchigi.databinding.ActivityMemoBinding
import com.example.bookchigibakchigi.ui.BaseActivity

class CardActivity : BaseActivity() {

    private lateinit var binding: ActivityCardBinding
    private lateinit var adapter: CardBackgroundAdapter
    // 실제 데이터 리스트
    private val actualImages = listOf(
        R.drawable.img_blue_sky,
        R.drawable.img_light_blue_sky,
        R.drawable.img_night_sky,
        R.drawable.img_pink_sky,
        R.drawable.img_forest,
        R.drawable.img_white_wall,
        R.drawable.img_gray_wall,
        R.drawable.img_blue_wall
    )

    // 가짜 데이터 추가한 리스트 (앞뒤에 더미 아이템 추가)
    private val paddedImages = listOf(R.drawable.img_dummy, R.drawable.img_dummy) +
            actualImages + listOf(R.drawable.img_dummy, R.drawable.img_dummy)

    private var lastSelectedPosition = -1 // 마지막으로 선택된 아이템 위치


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar(binding.toolbar, binding.main)

        // 기본 배경 설정 (Glide 사용)
        Glide.with(this)
            .load(R.drawable.img_blue_sky)
            .into(binding.ivBackground)

        // RecyclerView 설정
        adapter = CardBackgroundAdapter(paddedImages) { selectedImage ->
            Glide.with(this)
                .load(selectedImage)
                .into(binding.ivBackground)
        }

        binding.rvBackground.apply {
            adapter = this@CardActivity.adapter
            layoutManager = LinearLayoutManager(this@CardActivity, LinearLayoutManager.HORIZONTAL, false).apply {
                initialPrefetchItemCount = 3
            }
            setHasFixedSize(true)
            setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                setMaxRecycledViews(0, 10)
            })
        }

//        // pagerSnapHelper 적용 → 한 칸씩 이동
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvBackground)

//        val snapHelper = CustomSnapHelper(this)
//        snapHelper.attachToRecyclerView(binding.rvBackground)

        // 스크롤이 멈추면 선택된 아이템 감지 + 진동 효과 추가
        binding.rvBackground.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager.getPosition(snapView)

                    if (position != lastSelectedPosition) { // 새로운 아이템이 선택되었을 때만 실행
                        lastSelectedPosition = position
                        vibrate() // ✅ 진동 효과 실행

                        // 선택된 이미지 변경
                        Glide.with(this@CardActivity)
                            .load(paddedImages[position])
                            .into(binding.ivBackground)
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_card, menu) // 메뉴 파일 연결
        return true
    }

    // 메뉴 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {// API 레벨 31 (Android 12) 이상에서는 VibratorManager 사용
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 레벨 26 (Android 8.0) 이상에서는 Vibrator 사용
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // API 레벨 26 미만에서는 Vibrator 사용 (deprecated 방식)
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(50)
        }
    }
}