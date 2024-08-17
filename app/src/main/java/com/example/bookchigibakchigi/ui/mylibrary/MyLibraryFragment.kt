package com.example.bookchigibakchigi.ui.mylibrary

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookchigibakchigi.R

class MyLibraryFragment : Fragment() {

    companion object {
        fun newInstance() = MyLibraryFragment()
    }

    private val viewModel: MyLibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_library, container, false)
    }
}