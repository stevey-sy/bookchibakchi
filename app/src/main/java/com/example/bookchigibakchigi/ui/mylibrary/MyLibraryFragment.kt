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
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookShelfAdapter
import kotlinx.coroutines.launch

class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null

    private val binding get() = _binding!!
    private val myLibraryViewModel: MyLibraryViewModel by viewModels()
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

        // RecyclerView 설정
        adapter = BookShelfAdapter()
        binding.rvShelf.layoutManager = GridLayoutManager(context, 3)
        binding.rvShelf.adapter = adapter

        binding.rvShelf.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, 0) // 모든 간격을 0으로 설정
            }
        })

        // ViewModel의 데이터를 관찰하고 RecyclerView에 전달
        myLibraryViewModel.bookShelfItems.observe(viewLifecycleOwner) { items ->
            adapter.setDataList(items) // Adapter에 데이터 세팅
        }

        val database = AppDatabase.getDatabase(requireContext())
        val bookDao = database.bookDao()

        lifecycleScope.launch {
            val books = bookDao.getAllBooks() // 모든 책 데이터 가져오기
            val result = "";
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}