package com.example.bookchigibakchigi.ui.mylibrary

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding

class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myLibraryViewModel = ViewModelProvider(this).get(MyLibraryViewModel::class.java)

        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }
}