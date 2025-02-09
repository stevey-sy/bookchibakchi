package com.example.bookchigibakchigi.ui.card

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
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


        // 화면 너비 가져오기
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        // 아이템 너비 계산 (화면 너비를 5등분)
        val itemWidth = screenWidth / 6

        // 🔹 llAim 동적 설정
        val aimLayoutParams = FrameLayout.LayoutParams(itemWidth, itemWidth).apply {
            gravity = android.view.Gravity.CENTER // 중앙에 고정
        }
        binding.llAim.layoutParams = aimLayoutParams
        binding.llAim.setBackgroundResource(R.drawable.background_item_selected)

        // RecyclerView 설정
        adapter = CardBackgroundAdapter(paddedImages, itemWidth) { selectedImage ->
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

        // PagerSnapHelper 적용
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvBackground)

        // llAim의 렌더링 완료 후 RecyclerView 초기 위치 설정
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // llAim의 크기와 위치가 설정된 후 실행
                binding.llAim.viewTreeObserver.removeOnGlobalLayoutListener(this) // 여기서 'this'는 리스너 객체를 참조

                // llAim의 시작점
                val llAimStart = binding.llAim.left
                Log.d("DEBUG", "llAimStart: $llAimStart") // llAim의 시작점 로그 출력

                // RecyclerView 세 번째 아이템의 시작점
                val layoutManager = binding.rvBackground.layoutManager as LinearLayoutManager
                val snapView = layoutManager.findViewByPosition(2) // 세 번째 아이템 뷰

                // SnapView(세 번째 아이템)가 존재할 경우 위치 조정
                snapView?.let {
                    val rvItemStart = it.left
                    Log.d("DEBUG", "rvBackground third item start: $rvItemStart") // 세 번째 아이템의 시작점 로그 출력

                    // 오프셋 계산: llAimStart와 rvItemStart의 차이만큼 조정
                    val offset = llAimStart - rvItemStart
                    Log.d("DEBUG", "Offset to adjust: $offset") // 오프셋 로그 출력

                    // 스크롤 이동: 세 번째 아이템을 llAim에 정확히 맞춤
                    layoutManager.scrollToPositionWithOffset(0, offset)

                    // SnapHelper로 중앙 위치 강제 스냅
                    snapHelper.findSnapView(binding.rvBackground.layoutManager)?.let { snapView ->
                        val position = layoutManager.getPosition(snapView)
                        if (position != lastSelectedPosition) {
                            lastSelectedPosition = position
                            Glide.with(this@CardActivity).load(paddedImages[position]).into(binding.ivBackground)
                        }
                    }
                }
            }
        }

        // ViewTreeObserver에 리스너 추가
        binding.llAim.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

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