package com.example.bookchigibakchigi.ui.card

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity
import com.example.bookchigibakchigi.databinding.ActivityCardBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.card.adapter.CardBackgroundAdapter
import com.example.bookchigibakchigi.ui.component.MovableEditText
import com.example.bookchigibakchigi.ui.shared.viewmodel.BookViewModel
import com.example.bookchigibakchigi.util.VibrationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CardActivity : BaseActivity() {

    private lateinit var binding: ActivityCardBinding
    private lateinit var adapter: CardBackgroundAdapter
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
        adjustBookContentPosition()
        setupToolbar(binding.toolbar, binding.main)
        initViewModel(intent)
        initBackgroundSelectView()
        initSnapHelper()
//        setupMovableEditText()
//        initFocusChangeListener()
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
            R.id.action_save -> {
                onSaveClicked()
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

        val copiedText = intent.getStringExtra("copiedText")
        if (copiedText != null) {
            binding.etBookContent.setText(copiedText)
        }

        val copiedPage = intent.getStringExtra("copiedPage")
        if (copiedPage != null) {

        }

        binding.viewModel = bookViewModel
        binding.lifecycleOwner = this

        // ✅ ViewModel에 데이터 저장
        book?.let {
            bookViewModel.setBook(it)  // LiveData 업데이트
            binding.etBookTitle.setText(book.title)
        }
    }

    private fun initClickListener() {
        binding.main.setOnClickListener {
//            binding.etBookContent.isMovable = false
//            binding.etBookContent.setBackgroundResource(R.drawable.background_edit_text_no_focus)
//            binding.etBookContent.clearFocus()
            binding.etBookContent.isMovable = false
            binding.flBookContent.isMovable = false
            binding.flBookContent.setBackgroundResource(R.drawable.background_edit_text_no_focus)
            binding.flBookContent.clearFocus()
            binding.etBookContent.clearFocus()

            binding.etBookTitle.isMovable = false
            binding.flBookTitle.isMovable = false
            binding.flBookTitle.setBackgroundResource(R.drawable.background_edit_text_no_focus)
            binding.etBookTitle.clearFocus()
            binding.flBookTitle.clearFocus()

            hideKeyboard()

            binding.colorPickerLayout.visibility = View.GONE
            binding.bgColorPickerLayout.visibility = View.GONE
        }

        binding.btnTextBackgroundColor.setOnClickListener {
            showColorPicker(binding.bgColorPickerLayout)
        }

        binding.btnTextColor.setOnClickListener {
            showColorPicker(binding.colorPickerLayout)
        }

        binding.btnMove.setOnClickListener {

            binding.etBookContent.isMovable = true
            binding.flBookContent.isMovable = true
            binding.flBookContent.setBackgroundResource(R.drawable.background_edit_text_has_focus)
            binding.etBookContent.clearFocus()

            binding.etBookTitle.isMovable = true
            binding.flBookTitle.isMovable = true
            binding.flBookTitle.setBackgroundResource(R.drawable.background_edit_text_has_focus)
            binding.etBookTitle.clearFocus()
            hideKeyboard()
        }

        initColorPickerListener()
        initBackgroundColorPickerListener()
    }

    private fun showColorPicker(layout: View) {
        if (layout.visibility == View.GONE) {
            layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // 리스너 제거 (한 번만 실행)
                    layout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // 화면의 전체 너비 가져오기
                    val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                    // colorPickerLayout 또는 bgColorPickerLayout의 너비 가져오기
                    val pickerWidth = layout.width.toFloat()
                    // 가운데 정렬할 X 좌표 계산
                    val newX = (screenWidth - pickerWidth) / 2

                    // customToolbar의 Y 좌표 가져오기
                    val toolbarY = binding.customToolbar.y

                    // colorPickerLayout 또는 bgColorPickerLayout의 높이 가져오기
                    val pickerHeight = layout.height.toFloat()
                    // 20dp를 px로 변환
                    val marginInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        20f, // 20dp
                        resources.displayMetrics
                    )

                    // 새로운 Y 위치 설정 (customToolbar 위 + margin 추가)
                    val newY = toolbarY - pickerHeight - marginInPx

                    layout.x = newX
                    layout.y = newY

                    // View 표시
                    layout.visibility = View.VISIBLE
                }
            })

            // 레이아웃 변경 후 높이를 정확히 측정할 수 있도록 `VISIBLE`로 설정
            layout.visibility = View.INVISIBLE
            layout.requestLayout() // 레이아웃 강제 업데이트
        } else {
            layout.visibility = View.GONE
        }
    }


    private fun adjustBookContentPosition() {
        binding.flBookTitle.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.flBookTitle.viewTreeObserver.removeOnGlobalLayoutListener(this) // 리스너 제거하여 불필요한 호출 방지

                val titleY = binding.flBookTitle.y // etBookTitle의 y 좌표
                val newY = titleY - binding.flBookContent.height - 20 // etBookTitle 위쪽으로 이동 (간격 20px 추가)

                binding.flBookContent.y = newY // etBookContent 위치 변경
                binding.etBookContent.requestFocus()

                // 마지막 자리로 커서 이동
                binding.etBookContent.setSelection(binding.etBookContent.text.length)
            }
        })
    }

    private fun initColorPickerListener() {
        val colorClickListener = View.OnClickListener { view ->
            val focusedView = currentFocus // 현재 포커스를 가진 View 가져오기

            if (focusedView is EditText) { // 포커스를 가진 View가 MovableEditText인지 확인
                val color = when (view.id) {
                    R.id.colorBlack -> resources.getColor(R.color.black, theme)
                    R.id.colorWhite -> resources.getColor(R.color.white, theme)
                    R.id.colorRed -> resources.getColor(R.color.red, theme)
                    R.id.colorBlue -> resources.getColor(R.color.blue, theme)
                    R.id.colorYellow -> resources.getColor(R.color.yellow, theme)
                    R.id.colorGray -> resources.getColor(R.color.gray, theme)
                    else -> return@OnClickListener
                }

                focusedView.setTextColor(color) // 포커스를 가진 EditText에 색상 적용
                binding.colorPickerLayout.visibility = View.GONE
            }
        }

        // 색상 버튼에 공통 리스너 적용
        binding.colorBlack.setOnClickListener(colorClickListener)
        binding.colorWhite.setOnClickListener(colorClickListener)
        binding.colorRed.setOnClickListener(colorClickListener)
        binding.colorGray.setOnClickListener(colorClickListener)
        binding.colorBlue.setOnClickListener(colorClickListener)
        binding.colorYellow.setOnClickListener(colorClickListener)
    }

    private fun initBackgroundColorPickerListener() {
        val bgColorClickListener = View.OnClickListener { view ->
            val focusedView = currentFocus // 현재 포커스를 가진 View 가져오기

            if (focusedView is EditText) { // 포커스를 가진 View가 MovableEditText인지 확인
                val bgColor = when (view.id) {
                    R.id.bgColorBlack -> resources.getColor(R.color.black, theme)
                    R.id.bgColorWhite -> resources.getColor(R.color.white, theme)
                    R.id.bgColorMint -> resources.getColor(R.color.mint, theme)
                    R.id.bgColorSky -> resources.getColor(R.color.sky, theme)
                    R.id.bgColorYellow -> resources.getColor(R.color.yellow_highlight, theme)
                    R.id.bgColorPurple -> resources.getColor(R.color.purple, theme)
                    else -> return@OnClickListener
                }

                focusedView.setBackgroundColor(bgColor) // 포커스를 가진 EditText에 배경색 적용
                binding.bgColorPickerLayout.visibility = View.GONE
            }
        }

        // 배경색 변경 버튼에 공통 리스너 적용
        binding.bgColorBlack.setOnClickListener(bgColorClickListener)
        binding.bgColorWhite.setOnClickListener(bgColorClickListener)
        binding.bgColorMint.setOnClickListener(bgColorClickListener)
        binding.bgColorSky.setOnClickListener(bgColorClickListener)
        binding.bgColorYellow.setOnClickListener(bgColorClickListener)
        binding.bgColorPurple.setOnClickListener(bgColorClickListener)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: binding.main // currentFocus가 null이면 루트 뷰 사용
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initBackgroundSelectView() {
        // 기본 배경 설정 (Glide 사용)

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
                        VibrationUtil.vibrate(this@CardActivity, 100) // ✅ 진동 효과 실행

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

    private fun onSaveClicked() {
        // binding.flCapture 영역을 Bitmap으로 캡처
        val captureView = binding.flCapture
        val bitmap = Bitmap.createBitmap(captureView.width, captureView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        captureView.draw(canvas)

        // 내부 저장소에 이미지 저장
        val savedFilePath = saveImageToInternalStorage(bitmap)
        if (savedFilePath != null) {
            Toast.makeText(this, "이미지가 내부 저장소에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            // DB에 데이터 저장
            savePhotoCardDataToDatabase(savedFilePath)
        } else {
            Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoCardDataToDatabase(filePath: String) {
        val imageFileName = filePath // ✅ 내부 저장소에 이미지 저장
        val book = bookViewModel.currentBook.value ?: return // ✅ 현재 선택된 책 정보 가져오기
        val content = binding.etBookContent.text.toString() // ✅ 사용자가 입력한 텍스트 내용 가져오기
        val textColor = binding.etBookContent.currentTextColor // ✅ 현재 텍스트 색상 가져오기
        val backgroundColor = binding.etBookContent.savedBackgroundColor
        val textSize = binding.etBookContent.textSize // ✅ 현재 텍스트 크기 가져오기

        val createdAt = System.currentTimeMillis() // ✅ 현재 시간 (timestamp)

        // ✅ 1. PhotoCardEntity 생성
        val photoCardEntity = PhotoCardEntity(
            imageFileName = imageFileName,
            isbn = book.isbn,
            createdAt = createdAt
        )

        // ✅ 2. CardTextEntity 리스트 생성
        val contentTextEntity = CardTextEntity(
            photoCardId = 0, // 🚨 먼저 저장 후 ID 업데이트 필요
            type = "text",
            content = content,
            textColor = textColor.toString(),
            textSize = textSize,
            textBackgroundColor = "#FFFFFF", // 기본 배경색 (예제)
            startX = binding.etBookContent.x, // X 좌표
            startY = binding.etBookContent.y, // Y 좌표
            font = "default"
        )

        val titleTextEntity = CardTextEntity(
            photoCardId = 0, // 🚨 먼저 저장 후 ID 업데이트 필요
            type = "text",
            content = content,
            textColor = textColor.toString(),
            textSize = textSize,
            textBackgroundColor = "#FFFFFF", // 기본 배경색 (예제)
            startX = binding.etBookContent.x, // X 좌표
            startY = binding.etBookContent.y, // Y 좌표
            font = "default"
        )

        // ✅ 3. 데이터베이스에 저장
//        lifecycleScope.launch(Dispatchers.IO) {
//            photoCardRepository.insertPhotoCardWithTexts(photoCardEntity, listOf(textEntity))
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@CardActivity, "포토카드가 저장되었습니다.", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    /**
     * 앱 내부 저장소에 Bitmap을 저장하는 함수
     * @param bitmap 저장할 이미지
     * @return 저장된 파일의 경로 (실패 시 null 반환)
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        return try {
            val isbn = bookViewModel.currentBook.value?.isbn
            val fileName = "${isbn}_${System.currentTimeMillis()}.png" // 파일 이름 (예: photo_1709000000000.png)
            val file = File(filesDir, fileName) // 내부 저장소에 저장할 파일 경로 설정

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // PNG 형식으로 저장
            outputStream.flush()
            outputStream.close()

            file.absolutePath // 저장된 파일 경로 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        // filename -> 책 isbn_currentTime.jpg
        val filename = "card_${System.currentTimeMillis()}.jpg"
        val resolver = contentResolver
        // Android Q 이상과 이하의 저장 위치를 구분
        val imageCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        return try {
            // 이미지 정보 등록 및 OutputStream 열기
            val imageUri = resolver.insert(imageCollection, contentValues)
            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                // 저장 완료 후 IS_PENDING 플래그 해제
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}