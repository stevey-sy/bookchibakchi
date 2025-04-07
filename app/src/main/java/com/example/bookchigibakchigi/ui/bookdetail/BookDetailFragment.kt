package com.example.bookchigibakchigi.ui.bookdetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.bookchigibakchigi.ui.main.MainViewModel
import com.example.bookchigibakchigi.ui.main.MainViewUiState
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
                                    mainViewModel.updateCurrentBook(selectedBook)
                                    
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
            
            // 현재 상태를 MainViewUiState.BookList로 변경
            mainViewModel.uiState.value.let { state ->
                if (state is MainViewUiState.BookDetail) {
                    mainViewModel.updateUiState(MainViewUiState.MyLibrary(
                        books = state.books,
                        transitionName = arguments?.getString("transitionName").toString()
                    ))
                }
            }
            
            // 선택된 위치를 저장하고 뒤로 가기
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", currentPosition)
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        
        // RecordActivity에서 돌아왔을 때 저장된 위치를 복원
        val savedPosition = findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("selected_position")
        if (savedPosition != null && savedPosition >= 0) {
            // 현재 ViewPager의 위치와 저장된 위치가 다를 경우에만 위치를 변경
            if (binding.viewPager.currentItem != savedPosition) {
                binding.viewPager.setCurrentItem(savedPosition, false)
            }

            // RecordActivity에서 돌아왔을 때 currentBook 업데이트
            mainViewModel.uiState.value.let { state ->
                if (state is MainViewUiState.BookDetail && savedPosition < state.books.size) {
                    val selectedBook = state.books[savedPosition]
                    viewLifecycleOwner.lifecycleScope.launch {
                        mainViewModel.updateCurrentBook(selectedBook)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collectLatest { state ->
                    when (state) {
                        is MainViewUiState.BookDetail -> {
                            Log.d("TEST POSITION ", "observeViewModel: ")
                            state.initialPosition?.let { position ->
                                Log.d("TEST POSITION ", "state.initialPosition: ${state.initialPosition}")


                                var targetPosition = findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("selected_position")
                                // set 0 if targetPosition is null
                                if (targetPosition != null && position == 0) {
                                    targetPosition = position
                                }

                                adapter.setDataList(state.books)
                                val totalItems = state.books.size
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
                                    "sharedView_${state.currentBook.itemId}"
                            }
                        }

                        else -> {
                            // 다른 상태는 무시
                        }
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
            moveToMicrophoneActivity()
        }
        view.findViewById<LinearLayout>(R.id.llCamera).setOnClickListener {
            bottomSheetDialog.dismiss()
            showCameraDialog()
        }

        view.findViewById<LinearLayout>(R.id.llKeyboard).setOnClickListener {
            // 버튼 클릭 동작
        }
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
                val photoCards = state.photoCards
                adapter.updateList(photoCards)
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

    private fun moveToMicrophoneActivity() {
        val intent = Intent(requireContext(), MicrophoneActivity::class.java).apply {
            mainViewModel.uiState.value.let { state ->
                if (state is MainViewUiState.BookDetail) {
                    putExtra("currentBook", state.currentBook)
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
                    mainViewModel.uiState.value.let { state ->
                        if (state is MainViewUiState.BookDetail) {
                            putExtra("currentBook", state.currentBook)
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
                    mainViewModel.uiState.value.let { state ->
                        if (state is MainViewUiState.BookDetail) {
                            putExtra("currentBook", state.currentBook)
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
            mainViewModel.uiState.value.let { state ->
                if (state is MainViewUiState.BookDetail) {
                    val selectedBook = state.currentBook
                    selectedBook?.let { book ->
                        val intent = Intent(requireContext(), RecordActivity::class.java).apply {
                            putExtra("currentBook", book)
                        }

                        sharedView = binding.viewPager.findViewWithTag<View>("page_${binding.viewPager.currentItem}")?.findViewById(R.id.ivBook)
                        sharedView!!.transitionName = "sharedView_${state.currentBook.itemId}"

                        // 현재 ViewPager의 위치를 저장
                        val currentPosition = binding.viewPager.currentItem
                        findNavController().currentBackStackEntry?.savedStateHandle?.set("selected_position", currentPosition)

                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            sharedView!!,  // 시작점 (ViewPager의 ImageView)
                            sharedView!!.transitionName  // 동일한 transitionName 사용
                        )
                        startActivity(intent, options.toBundle())
                    }
                }
            }
        }

//        binding.btnMemo.setOnClickListener {
//            val selectedBook = mainViewModel.currentBook.value
//            selectedBook?.let { book ->
//                showBottomSheet()
//            }
//        }

        binding.llComments.setOnClickListener {
            showPhotoCardListDialog()
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