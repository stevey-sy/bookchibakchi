package com.example.bookchigibakchigi.ui.mylibrary

import android.graphics.Rect
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.bookdetail.BookDetailFragment
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookShelfAdapter
import kotlinx.coroutines.launch

class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null

    private val binding get() = _binding!!
    private lateinit var viewModel: MyLibraryViewModel
    private lateinit var adapter: BookShelfAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AppDatabase와 BookShelfRepository 생성
        val bookDao = AppDatabase.getDatabase(requireContext()).bookDao()
        val repository = BookShelfRepository(bookDao)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, MyLibraryViewModelFactory(repository))
            .get(MyLibraryViewModel::class.java)

        // RecyclerView 설정
        adapter = BookShelfAdapter { bookEntity, position, sharedView ->
            val bundle = Bundle().apply {
                putInt("itemId", bookEntity.itemId)
                putString("coverUrl", bookEntity.coverImageUrl)
                putString("transitionName", "sharedView_${bookEntity.itemId}")
            }

            val extras = FragmentNavigatorExtras(
                sharedView to "sharedView_${bookEntity.itemId}" // transitionName과 일치해야 함
            )

            findNavController().navigate(R.id.action_myLibrary_to_bookDetail, bundle, null, extras)

//            // 전달할 데이터
//            val fragment = BookDetailFragment().apply {
//                arguments = Bundle().apply {
//                    putInt("itemId", bookEntity.itemId)
//                    putString("coverUrl", bookEntity.coverImageUrl)
//                }
//            }
//
//            // Fragment 전환
//            parentFragmentManager.commit {
//                setReorderingAllowed(true) // Fragment Transaction 최적화 허용
//                addSharedElement(sharedView, sharedView.transitionName) // Shared Element Transition 설정
//                replace(R.id.nav_host_fragment_activity_main, fragment)
//                addToBackStack(null) // 백스택 추가
//            }
        }
        binding.rvShelf.layoutManager = GridLayoutManager(context, 3)
        binding.rvShelf.adapter = adapter

        binding.rvShelf.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, 0) // 모든 간격을 0으로 설정
            }
        })

        // Observe LiveData
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}