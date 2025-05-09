package com.example.bookchigibakchigi.ui.bookdetail

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentBookDetailBinding
import com.example.bookchigibakchigi.ui.crop.CropActivity
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter
import com.example.bookchigibakchigi.ui.bookdetail.adapter.PhotoCardAdapter
import com.example.bookchigibakchigi.ui.microphone.MicrophoneActivity
import com.example.bookchigibakchigi.ui.record.RecordActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.ui.addmemo.AddMemoActivity
import com.example.bookchigibakchigi.ui.bookdetail.adapter.MemoListAdapter
import com.example.bookchigibakchigi.ui.card.CardActivity
import com.example.bookchigibakchigi.ui.main.MainActivity
import com.example.bookchigibakchigi.ui.main.MainViewModel
import com.example.bookchigibakchigi.ui.main.MainViewUiState
import com.example.bookchigibakchigi.util.BindingAdapters.setProgressTranslation
import com.example.bookchigibakchigi.util.PermissionUtil
import kotlinx.coroutines.flow.collectLatest
import kotlin.collections.set
import com.example.bookchigibakchigi.util.FileUtil

@AndroidEntryPoint
class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: BookViewPagerAdapter
    private var sharedView: View? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var capturedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTransitions()
        initLaunchers()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            updateToolbarTitle(
                title = "오독오독",
                fontResId = R.font.dashi,
                textSizeSp = 30f,
                menuResId = null
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        initBinding()
        initViewPager()
        initClickListeners()
        prepareSharedElementTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()  // 애니메이션 시작 전까지 정지.
        initBackPressedCallback() // 시스템 Back 버튼 동작 커스터마이징
        observeViewModel() // ViewModel의 상태 관찰하여 ViewPager 데이터 설정
    }

    private fun initBinding() {
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun prepareSharedElementTransition() {
        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // ViewPager에서 현재 선택된 페이지의 Transition Name 가져오기
                    val currentPosition = binding.viewPager.currentItem
                    // ViewPager의 현재 페이지 View를 찾음
                    val currentPage = binding.viewPager.findViewWithTag<View>("page_$currentPosition")
                    //val imageView = currentPage?.findViewById<View>(R.id.ivBook)
                    val imageView = currentPage?.findViewById<View>(R.id.cardView)

                    if (imageView != null) {
                        // names 리스트의 첫 번째 Transition Name에 매핑
                        sharedElements[names[0]] = imageView
                    }
                }
            }
        )
    }

    private fun initViewPager() {
        adapter = BookViewPagerAdapter(
            arguments?.getString("transitionName") ?: "",
            onItemClick = { bookEntity, position, sharedView ->
                // 아이템 클릭 이벤트 처리
                //                Log.d("TEST TEST","클릭된 아이템: ${bookEntity.itemId}, Position: $position")
            },
            onImageLoaded = {
                startPostponedEnterTransition()
            },
        )
        binding.viewPager.adapter = adapter

        binding.viewPager.apply {
            clipToPadding = false
            clipChildren = false
            setPageTransformer(PreviewPageTransformer())
            // 페이지 간격 설정
            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
            setPadding(pageMarginPx, 0, pageMarginPx, 0)
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
            override fun onPageScrollStateChanged(state: Int) {
                // 스크롤이 멈춘 상태에서만 실행 (즉, UI 업데이트 완료 후)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val position = binding.viewPager.currentItem
                    mainViewModel.uiState.value.let { state ->
                        when (state) {
                            is MainViewUiState.BookDetail -> {
                                val selectedBook = state.books[position]
                                binding.tvBookTitle.isSelected = true

                                // 코루틴을 사용하여 updateCurrentBook 함수의 완료를 기다림
                                viewLifecycleOwner.lifecycleScope.launch {
//                                    mainViewModel.updateCurrentBook(selectedBook)

                                    mainViewModel.setSelectedBook(selectedBook.itemId)
                                    // updateCurrentBook 함수가 완료된 후에 다음 작업을 수행
                                    sharedView = binding.viewPager.findViewWithTag<View>("page_$position")?.findViewById(R.id.cardView)
                                    binding.viewPager.post {
                                        val transitionName = "sharedView_${selectedBook.itemId}" // Transition Name 생성
                                        findNavController().previousBackStackEntry?.savedStateHandle?.set("current_transition_name", transitionName)
                                        findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", position)
                                    }
                                }
                            }
                            else -> {
                                // 다른 상태는 무시
                            }
                        }
                    }
                }
            }
        })

        val initialPosition = arguments?.getInt("selected_position") ?: 0
        binding.viewPager.setCurrentItem(initialPosition, false)

    }

    private fun initBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentPosition = binding.viewPager.currentItem
            
            // 현재 상태를 MainViewUiState.MyLibrary로 변경
            mainViewModel.uiState.value.let { state ->
                if (state is MainViewUiState.BookDetail) {
                    mainViewModel.updateUiState(MainViewUiState.MyLibrary(
                        books = state.books,
                        transitionName = arguments?.getString("transitionName") ?: ""
                    ))
                }
            }
            
            // 선택된 위치를 저장하고 뒤로 가기
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", currentPosition)
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 현재 상태를 한 번만 확인하고 처리
        val currentState = mainViewModel.uiState.value
        Log.d("TEST POSITION ", "observeViewModel: $currentState")
        if (currentState is MainViewUiState.BookDetail) {
            Log.d("TEST POSITION ", "observeViewModel: ")
            currentState.initialPosition?.let { position ->
                Log.d("TEST POSITION ", "state.initialPosition: ${currentState.initialPosition}")

                val targetPosition = position

                currentState.books.let {
                    adapter.setDataList(currentState.books)
                    val totalItems = currentState.books.size
                    val hasEnoughItemsForPreview = targetPosition in 1 until totalItems - 1
                    val isNearEnd = targetPosition!! >= totalItems - 3 // 마지막 3개 이내인지 확인

                    binding.viewPager.offscreenPageLimit = when {
                        isNearEnd -> 2
                        hasEnoughItemsForPreview -> 3
                        else -> 1
                    }

                    binding.viewPager.setCurrentItem(targetPosition, false)
                    val currentPage =
                        binding.viewPager.findViewWithTag<View>("page_$targetPosition")
                    currentPage?.findViewById<View>(R.id.cardView)?.transitionName =
                        "sharedView_${currentState.currentBook.itemId}"

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.selectedBook.collectLatest { selectedBook ->
                    selectedBook?.let {
                        binding.tvProgressPercentage.text = selectedBook.getPercentageStr()
                        animProgressBar(selectedBook.progressPercentage, binding.progressBarForeground)
                        animPercentage(selectedBook.progressPercentage, binding.llProgressPercentage)
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        val photoFile = FileUtil.createImageFile(requireContext())
        capturedImageUri = FileUtil.getFileUri(requireContext(), photoFile)
        takePictureLauncher.launch(capturedImageUri)
    }

    private fun showCameraDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_select_photo_type, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
        view.findViewById<LinearLayout>(R.id.llCamera).setOnClickListener {
            bottomSheetDialog.dismiss()
            onCameraBtnClicked()
        }

        view.findViewById<LinearLayout>(R.id.llGallery).setOnClickListener {
            // 버튼 클릭 동작
            bottomSheetDialog.dismiss()
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_select_memo_create_type, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
        // 추가 로직 (예: 버튼 클릭 이벤트)
        view.findViewById<LinearLayout>(R.id.llMicrophone).setOnClickListener {
            bottomSheetDialog.dismiss()
            moveToActivity(MicrophoneActivity::class.java)
        }
        view.findViewById<LinearLayout>(R.id.llCamera).setOnClickListener {
            bottomSheetDialog.dismiss()
            showCameraDialog()
        }

        view.findViewById<LinearLayout>(R.id.llKeyboard).setOnClickListener {
            // 버튼 클릭 동작
            bottomSheetDialog.dismiss()
             moveToActivity(AddMemoActivity::class.java)
//            moveToActivity(CardActivity::class.java)
        }
    }

    private fun showMemoListDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_memo_list, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPhotoCards)
        val closeBtn = view.findViewById<ImageView>(R.id.ivClose)
        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // linearLayoutManager for recyclerView
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = linearLayoutManager

        val adapter = MemoListAdapter(
            onModifyClicked = { memoId ->
                // 메모 수정 로직
                val intent = Intent(requireContext(), AddMemoActivity::class.java).apply {
                    mainViewModel.selectedBook.value?.let { book ->
                        putExtra("isEditMode", true)
                        putExtra("bookId", book.itemId)
                        putExtra("memoId", memoId)
                    }
                }
                startActivity(intent)
            },
            onDeleteClicked = { memoId ->
                // 메모 삭제 로직
                mainViewModel.selectedBook.value?.let { book ->
                    mainViewModel.deleteMemoById(memoId)
                }
            }
        )
        recyclerView.adapter = adapter

        // selectedBook의 Flow를 구독하여 메모 리스트 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.selectedBook.collect { selectedBook ->
                selectedBook?.memoList?.let { memoList ->
                    adapter.submitList(memoList)
                    if(memoList.isEmpty()) bottomSheetDialog.dismiss()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun showPhotoCardListDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_photo_card_list, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPhotoCards)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = staggeredGridLayoutManager

        val adapter = PhotoCardAdapter(emptyList()) // 초기 리스트 비움
        recyclerView.adapter = adapter

        // ViewModel의 photoCardList 데이터를 가져와서 어댑터에 설정

        mainViewModel.uiState.value.let { state ->
            if (state is MainViewUiState.BookDetail) {
//                val photoCards = state.photoCards
//                adapter.updateList(photoCards)
            }
        }
        bottomSheetDialog.show()
    }

    private fun onCameraBtnClicked() {
        if (checkPermissions()) {
            // 권한이 이미 허용된 경우 카메라 실행
            launchCamera()
        } else {
            // 권한 요청
            requestPermissions()
        }
    }

    private fun moveToActivity(activityClass: Class<*>) {
        val intent = Intent(requireContext(), activityClass).apply {
            mainViewModel.selectedBook.value.let { book ->
                book?.let {
                    putExtra("bookId", book.itemId)
                }
            }
        }
        startActivity(intent)
    }

    private fun initTransitions() {
        val transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.shared_element_transition)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }

    private fun initLaunchers() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val cameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] ?: false
            if (cameraPermissionGranted) {
                // 권한이 모두 허용된 경우 카메라 호출
                launchCamera()
            } else {
                // 권한이 거부된 경우
                Toast.makeText(requireContext(), "카메라 및 저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // ActivityResultLauncher 초기화
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val intent = Intent(requireContext(), CropActivity::class.java).apply {
                    putExtra("IMAGE_URI", capturedImageUri.toString()) // Uri를 String으로 변환하여 전달
                    mainViewModel.selectedBook.value.let { book ->
                        book?.let {
                            putExtra("bookId", book.itemId)
                        }
                    }
                }

                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val intent = Intent(requireContext(), CropActivity::class.java).apply {
                    putExtra("IMAGE_URI", uri.toString())
                    mainViewModel.selectedBook.value.let { book ->
                        book?.let {
                            putExtra("bookId", book.itemId)
                        }
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initClickListeners() {
        binding.btnReading.setOnClickListener {
                mainViewModel.selectedBook.value.let { book ->
                    book?.let {
                        val intent = Intent(requireContext(), RecordActivity::class.java).apply {
                            putExtra("bookId", book.itemId)
                        }
                        startActivity(intent)
                    }
                }
        }

        binding.btnMemo.setOnClickListener {
            val selectedBook = mainViewModel.selectedBook.value
            selectedBook?.let { book ->
                showBottomSheet()
            }
        }

        binding.llComments.setOnClickListener {
            mainViewModel.selectedBook.value.let { book ->
                book?.let {
                    if(book.memoList.isNotEmpty()) showMemoListDialog()
                }
            }
        }
    }

    private fun animProgressBar(percentage: Int, progressView: View) {
        // post로 부모의 layout width가 측정된 뒤 실행
        progressView.post {
            val parentWidth = (progressView.parent as ViewGroup).width
            val targetWidth = (parentWidth * percentage) / 100
             val currentWidth = progressView.layoutParams.width
//            val currentWidth = 0

            val animator = ValueAnimator.ofInt(currentWidth, targetWidth).apply {
                duration = 800
                addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Int
                    val params = progressView.layoutParams
                    params.width = animatedValue
                    progressView.layoutParams = params
                }
            }
            animator.start()
        }
    }

    private fun animPercentage(percentage: Int, percentView: View) {
        percentView.post {
            val parentView = percentView.parent as? ViewGroup
            val parentWidth = parentView?.width ?: 0  // 부모 width 가져오기
            var viewWidth = percentView.width // 현재 뷰의 width

            Log.d("animPercentage", "parentWidth: ${parentWidth}")
            Log.d("animPercentage", "viewWidth: ${viewWidth}")

            // ✅ 부모 width에 대한 비율로 translationX 계산
            val baseTranslationX = (parentWidth * (percentage / 100f)).coerceAtMost(parentWidth.toFloat())

            // ✅ 텍스트뷰의 width를 고려하여 중앙 정렬 (뷰 width의 절반만큼 빼기)
            val targetTranslationX = (baseTranslationX - viewWidth / 2)

            // 현재 translationX 값 가져오기
             val currentTranslationX = percentView.translationX
//            val currentTranslationX = 0f


            // ValueAnimator로 애니메이션 실행
            val animator = ValueAnimator.ofFloat(currentTranslationX, targetTranslationX)
            animator.duration = 800 // 애니메이션 지속 시간 (300ms)
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                percentView.translationX = animatedValue // 애니메이션 값 적용
            }
            animator.start()
        }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = android.Manifest.permission.CAMERA
        val readMediaPermission = android.Manifest.permission.READ_MEDIA_IMAGES

        return (requireContext().checkSelfPermission(cameraPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        permissionLauncher.launch(PermissionUtil.getRequiredPermissions())
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}