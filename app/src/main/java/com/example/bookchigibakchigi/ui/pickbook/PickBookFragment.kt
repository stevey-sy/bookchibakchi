package com.example.bookchigibakchigi.ui.pickbook

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentHomeBinding
import com.example.bookchigibakchigi.databinding.FragmentPickBookBinding
import com.example.bookchigibakchigi.ui.searchbook.SearchBookActivity

class PickBookFragment : Fragment() {

    private var _binding: FragmentPickBookBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       val pickBookViewModel = ViewModelProvider(this).get(PickBookViewModel::class.java)
        _binding = FragmentPickBookBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // "책 검색하기" TextView 클릭 리스너 설정
        binding.llBookSearch.setOnClickListener {
            // SearchBookActivity를 시작
            val intent = Intent(requireContext(), SearchBookActivity::class.java)
            startActivity(intent)
        }

        return root
    }
}