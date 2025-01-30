package com.example.bookchigibakchigi.ui.record

import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.databinding.ActivityRecordBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.MainActivityViewModel
import com.example.bookchigibakchigi.ui.MainActivityViewModelFactory

class RecordActivity : BaseActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementEnterTransition = createSharedElementTransition()
        window.sharedElementReturnTransition = createSharedElementTransition()

        enableEdgeToEdge()

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        // ✅ ViewModel에 데이터 저장
        book?.let {
            viewModel.setCurrentBook(it)  // LiveData 업데이트
            binding.ivBookCover.transitionName = "sharedView_${it.itemId}"
            binding.ivBookCover.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.ivBookCover.viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()  // ✅ 애니메이션 시작
                    viewModel.toggleTimer()
                    return true
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun createSharedElementTransition(): Transition {
        return TransitionInflater.from(this)
            .inflateTransition(R.transition.image_shared_element_transition).apply {
                duration = 500  // 애니메이션 지속 시간 (ms)
                interpolator = AccelerateDecelerateInterpolator()  // 부드러운 가속/감속 효과
            }
    }
}