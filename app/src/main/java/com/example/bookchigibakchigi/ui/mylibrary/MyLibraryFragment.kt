package com.example.bookchigibakchigi.ui.mylibrary

import android.graphics.Rect
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
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
        adapter = BookShelfAdapter { position ->
            // 클릭된 아이템의 인덱스 사용
//            handleItemClick(position)

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