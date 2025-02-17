package com.example.bookchigibakchigi.ui.bookdetail

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentBookDetailBinding
import com.example.bookchigibakchigi.ui.crop.CropActivity
import com.example.bookchigibakchigi.ui.MainActivityViewModel
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter
import com.example.bookchigibakchigi.ui.record.RecordActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter: BookViewPagerAdapter
    private var sharedView: View? = null
    private var currentItemId = 0
    private val CAMERA_REQUEST_CODE = 1001
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var capturedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transition 설정
        val transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.shared_element_transition)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val cameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] ?: false
//            val storagePermissionGranted =
//                permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false

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
                    putExtra("currentBook", viewModel.currentBook.value)
                }

                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)

        // 전달된 itemId를 Bundle에서 가져오기
        currentItemId = arguments?.getInt("itemId")!!
        val transitionName = arguments?.getString("transitionName") ?: ""

        // ViewModel 바인딩
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = BookViewPagerAdapter(
            transitionName,
            onItemClick = { bookEntity, position, sharedView ->
                // 아이템 클릭 이벤트 처리
                Log.d("TEST TEST","클릭된 아이템: ${bookEntity.itemId}, Position: $position")
            },
            onImageLoaded = {
                startPostponedEnterTransition()
            }
        )
        binding.viewPager.adapter = adapter


        // ViewModel 데이터 관찰 및 초기 화면 설정
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList) // Adapter에 데이터 설정

            // ✅ bookList에서 itemId와 일치하는 책 찾기
            val targetBookItem = bookList.find { it.itemId == currentItemId }

            // 찾은 책을 로그로 출력 (디버깅용)
            Log.d("TEST TEST", "찾은 책: ${targetBookItem?.title}, itemId: ${targetBookItem?.itemId}")

            // targetBookItem을 활용한 로직 추가 가능
            targetBookItem?.let { book ->
                val position = bookList.indexOf(book)

                viewModel.setCurrentBook(book.itemId) // 선택된 책을 ViewModel에 반영


                val totalItems = bookList.size // 전체 아이템 개수
                val hasEnoughItemsForPreview = position in 1 until totalItems - 1
                val isNearEnd = position >= totalItems - 3 // ✅ 마지막 3개 이내인지 확인

                // ✅ offscreenPageLimit을 동적으로 조정
                binding.viewPager.offscreenPageLimit = when {
                    isNearEnd -> 2
                    hasEnoughItemsForPreview -> 3
                    else -> 1
                }

                binding.viewPager.setCurrentItem(position, false)
                val currentPage = binding.viewPager.findViewWithTag<View>("page_$position")
                currentPage?.findViewById<View>(R.id.ivBook)?.transitionName =
                    "sharedView_${book.itemId}"
            }
        }

        binding.btnRecord.setOnClickListener {
            val selectedBook = viewModel.currentBook.value
            selectedBook?.let { book ->
                val intent = Intent(requireContext(), RecordActivity::class.java).apply {
                    putExtra("currentBook", book)
                }

                sharedView = binding.viewPager.findViewWithTag<View>("page_${binding.viewPager.currentItem}")?.findViewById(R.id.ivBook)
                sharedView!!.transitionName = "sharedView_${viewModel.currentBook.value?.itemId}"

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    sharedView!!,  // 시작점 (ViewPager의 ImageView)
                    sharedView!!.transitionName  // 동일한 transitionName 사용
                )
                startActivity(intent, options.toBundle())
            }
        }

        binding.btnMemo.setOnClickListener {
            val selectedBook = viewModel.currentBook.value
            selectedBook?.let { book ->
//                val intent = Intent(requireContext(), MemoActivity::class.java).apply {
//                    putExtra("currentBook", book)
//                }

//                sharedView = binding.viewPager.findViewWithTag<View>("page_${binding.viewPager.currentItem}")?.findViewById(R.id.ivBook)
//                sharedView!!.transitionName = "sharedView_${viewModel.currentBook.value?.itemId}"
//
//                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    requireActivity(),
//                    sharedView!!,  // 시작점 (ViewPager의 ImageView)
//                    sharedView!!.transitionName  // 동일한 transitionName 사용
//                )
//                startActivity(intent, options.toBundle())

//                val intent = Intent(requireContext(), CardActivity::class.java).apply {
//                    putExtra("currentBook", book)
//                }
//
//                startActivity(intent)
                showBottomSheet()
            }

        }

        prepareSharedElementTransition()

        return binding.root
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = android.Manifest.permission.CAMERA
        val readMediaPermission = android.Manifest.permission.READ_MEDIA_IMAGES

        return (requireContext().checkSelfPermission(cameraPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
        permissionLauncher.launch(permissions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        // 시스템 Back 버튼 동작 커스터마이징
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentPosition = binding.viewPager.currentItem
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", currentPosition)
            findNavController().popBackStack()
        }

        binding.viewPager.clipToPadding = false
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        binding.viewPager.setPadding(pageMarginPx, 0, pageMarginPx, 0)

        binding.viewPager.apply {
            clipToPadding = false  // ✅ 양옆 페이지 보이게 설정
            clipChildren = false   // ✅ 양옆 페이지 보이게 설정
            setPageTransformer(PreviewPageTransformer())
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentItemId = viewModel.bookShelfItems.value?.get(position)?.itemId ?: 0
                viewModel.setCurrentBook(currentItemId)
                binding.tvBookTitle.isSelected = true
                sharedView = binding.viewPager.findViewWithTag<View>("page_$position")?.findViewById(R.id.ivBook)
                // 현재 ViewPager의 Transition Name과 position 저장
                val currentItem = viewModel.currentBook.value
                val transitionName = "sharedView_${currentItem?.itemId}" // Transition Name 생성
                findNavController().previousBackStackEntry?.savedStateHandle?.set("current_transition_name", transitionName)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", position)
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }


    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition() {
        val transition =
            TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // ViewPager에서 현재 선택된 페이지의 Transition Name 가져오기
                    val currentPosition = binding.viewPager.currentItem
                    val currentItem = viewModel.currentBook.value

                    if (currentItem == null) return

                    // ViewPager의 현재 페이지 View를 찾음
                    val currentPage = binding.viewPager.findViewWithTag<View>("page_$currentPosition")
                    val imageView = currentPage?.findViewById<View>(R.id.ivBook)

                    if (imageView != null) {
                        // names 리스트의 첫 번째 Transition Name에 매핑
                        sharedElements[names[0]] = imageView
                    }
                }
            }
        )
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        capturedImageUri = getFileUri(photoFile)
        takePictureLauncher.launch(capturedImageUri)
    }

    private fun createImageFile(): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }
    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_select_memo_create_type, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
        // 추가 로직 (예: 버튼 클릭 이벤트)
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            if (checkPermissions()) {
                // 권한이 이미 허용된 경우 카메라 실행
                launchCamera()
            } else {
                // 권한 요청
                requestPermissions()
            }
        }

        view.findViewById<Button>(R.id.btnSelf).setOnClickListener {
            // 버튼 클릭 동작
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}