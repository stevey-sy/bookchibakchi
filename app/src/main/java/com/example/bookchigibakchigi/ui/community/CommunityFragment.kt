package com.example.bookchigibakchigi.ui.community

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.ui.main.MainActivity

class CommunityFragment : Fragment() {

    companion object {
        fun newInstance() = CommunityFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onResume() {
        super.onResume()
//        (activity as? MainActivity)?.apply {
//            updateToolbarTitle(
//                title = "오독오독",
//                fontResId = R.font.dashi,
//                textSizeSp = 30f,
//                menuResId = null
//            )
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }
}