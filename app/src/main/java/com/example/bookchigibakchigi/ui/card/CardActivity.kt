package com.example.bookchigibakchigi.ui.card

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityCardBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.card.adapter.CardBackgroundAdapter

class CardActivity : BaseActivity() {

    private lateinit var binding: ActivityCardBinding
    private val viewModel : CardActivityViewModel by viewModels()
    private lateinit var adapter: CardBackgroundAdapter
    var isDragging = false
    var dX = 0f
    var dY = 0f
    // 실제 데이터 리스트
    private val actualImages = listOf(
        R.drawable.img_light_blue_sky,
        R.drawable.img_blue_sky,
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
        initViewModel(intent)
        initBackgroundSelectView()
        initSnapHelper()
        //        setupMovableEditText()
        initFocusChangeListener()
        initClickListener()
        initCustomToolbar()
//        initEditTextTouchListener()
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

    private fun initViewModel(intent: Intent) {
        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // ✅ ViewModel에 데이터 저장
        book?.let {
            viewModel.setCurrentBook(it)  // LiveData 업데이트
            viewModel.setBookInfo(book.title)
        }
    }

    private fun initClickListener() {
        binding.main.setOnClickListener {
            binding.etBookTitle.clearFocus()
            hideKeyboard()
        }

        binding.btnTextColor.setOnClickListener {

        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: binding.main // currentFocus가 null이면 루트 뷰 사용
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initBackgroundSelectView() {
        // 기본 배경 설정 (Glide 사용)
//        Glide.with(this)
//            .load(R.drawable.img_blue_sky)
//            .into(binding.ivBackground)

        // 화면 너비 가져오기
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        // 아이템 너비 계산 (화면 너비를 5등분)
        val itemWidth = screenWidth / 6

        // llAim 동적 설정
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
    }

    private fun initEditTextTouchListener() {
        binding.etBookTitle.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치가 테두리 영역에서 발생했는지 확인
                    if (isTouchOnBorder(view, event)) {
                        isDragging = true // 드래그 시작 플래그 설정
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        true // 이동 이벤트 처리
                    } else {
                        isDragging = false
                        false // 기본 동작 (텍스트 수정 등) 처리
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        // 드래그 동작
                        view.animate()
                            .x(event.rawX + dX)
                            .y(event.rawY + dY)
                            .setDuration(0)
                            .start()
                        true
                    } else {
                        false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    isDragging = false // 드래그 종료
                    view.performClick() // 접근성을 위한 performClick 호출
                    true
                }

                else -> false
            }
        }
    }

    // 테두리 영역에 터치가 발생했는지 확인하는 함수
    private fun isTouchOnBorder(view: View, event: MotionEvent): Boolean {
        val borderWidth = 20 // 테두리 영역의 두께 (px 단위, dp로 계산 가능)
        val x = event.x
        val y = event.y
        val width = view.width
        val height = view.height

        // 터치가 뷰의 테두리 내에서 발생했는지 확인
        return x < borderWidth || x > width - borderWidth || y < borderWidth || y > height - borderWidth
    }

    private fun initSnapHelper() {
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

                        binding.etBookTitle.bringToFront();
                    }
                }
            }
        })
    }

    private fun initCustomToolbar() {
//        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
//            val rect = Rect()
//            binding.root.getWindowVisibleDisplayFrame(rect)
//
//            val screenHeight = binding.root.rootView.height
//            val keypadHeight = screenHeight - rect.bottom
//
//            if (keypadHeight > screenHeight * 0.15) { // 키보드가 올라왔을 때
//                binding.customToolbar.visibility = View.VISIBLE
//                binding.customToolbar.y = (rect.bottom - binding.customToolbar.height).toFloat()
//                binding.customToolbar.animate()
//                    .translationY((rect.bottom - binding.customToolbar.height).toFloat()) // 키보드 바로 위로 이동
//                    .setDuration(200)
//                    .start()
//            } else { // 키보드가 내려갔을 때
//                binding.customToolbar.animate()
//                    .translationY(screenHeight.toFloat()) // 화면 아래로 숨김
//                    .setDuration(200)
//                    .withEndAction {
//                        binding.customToolbar.visibility = View.GONE
//                    }
//                    .start()
//            }
//        }
        val rootView = window.decorView.findViewById<View>(android.R.id.content)

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) { // 키보드가 올라왔는지 확인
                Log.d("KeyboardHeight", "키보드 높이: $keypadHeight px")
//                binding.customToolbar.y = (keypadHeight + binding.customToolbar.height).toFloat()
                binding.customToolbar.visibility = View.VISIBLE
                binding.customToolbar.animate()
                    .translationY((-keypadHeight + binding.customToolbar.height).toFloat()) // 키보드 바로 위로 이동
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .setDuration(600)
                    .start()
            } else {
                binding.customToolbar.visibility = View.GONE
                binding.customToolbar.animate()
                    .translationY((keypadHeight + binding.customToolbar.height).toFloat()) // 키보드 바로 위로 이동
                    .setDuration(200)
                    .start()
            }
        }

    }

    private fun initFocusChangeListener() {
        binding.etBookTitle.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // 포커스가 있을 때 배경 변경
                binding.etBookTitle.setBackgroundResource(R.drawable.background_edit_text_has_focus)
                binding.customToolbar.visibility = View.VISIBLE
            } else {
                // 포커스가 없을 때 배경 원래대로 변경
                binding.etBookTitle.setBackgroundResource(R.drawable.background_edit_text_no_focus)
                binding.customToolbar.visibility = View.GONE
            }
        }
    }

    private fun setupMovableEditText() {
        var dX = 0f
        var dY = 0f

        binding.etBookTitle.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치 시작 시 X, Y 오프셋 계산
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 터치 이동 시 EditText의 위치 변경
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // performClick 호출로 클릭 이벤트 처리
                    view.performClick()
                    true
                }

                else -> false
            }
        }

        // performClick을 오버라이드하여 접근성 이벤트를 처리
        binding.etBookTitle.setOnClickListener {
            // 필요하다면 클릭 이벤트 로직을 여기에 작성
            if (binding.customToolbar.visibility == View.GONE) {
                binding.customToolbar.visibility = View.VISIBLE
            }
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