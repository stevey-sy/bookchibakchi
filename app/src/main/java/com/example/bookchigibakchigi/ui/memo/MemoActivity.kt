package com.example.bookchigibakchigi.ui.memo

import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityMemoBinding
import com.example.bookchigibakchigi.ui.BaseActivity

class MemoActivity : BaseActivity() {

    private lateinit var binding: ActivityMemoBinding
    private val viewModel: MemoActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementEnterTransition = createSharedElementTransition()
        window.sharedElementReturnTransition = createSharedElementTransition()
        enableEdgeToEdge()
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        initToolbar(binding.toolbar, binding.main)

        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // ✅ ViewModel에 데이터 저장
        book?.let {
            viewModel.setCurrentBook(it)  // LiveData 업데이트
            binding.ivBookCover.transitionName = "sharedView_${it.itemId}"
            binding.ivBookCover.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.ivBookCover.viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()  // ✅ 애니메이션 시작
                    return true
                }
            })
        }

//        setupWebView()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

//    private fun setupWebView() {
//        val webView = binding.webView
//
//        // WebView 설정
//        webView.settings.javaScriptEnabled = true
//        webView.settings.domStorageEnabled = true  // 로컬 저장 지원
//        webView.webViewClient = WebViewClient()  // 내부 WebView에서 실행
//        webView.addJavascriptInterface(EditorBridge(), "Android") // JS와 연결
//
//        // 에디터 HTML 파일 로드
//        webView.loadUrl("file:///android_asset/editor.html")
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_memo, menu) // 메뉴 파일 연결
        return true
    }

    // 메뉴 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // JavaScript와 데이터 교환을 위한 Bridge 클래스
    private inner class EditorBridge {
        @JavascriptInterface
        fun saveText(html: String) {
            runOnUiThread {
                Toast.makeText(this@MemoActivity, "저장됨!", Toast.LENGTH_SHORT).show()
                println("저장된 HTML: $html")  // 로그 출력
            }
        }
    }

    // WebView에서 현재 입력된 HTML을 가져오는 함수
//    fun getEditorContent() {
//        binding.webView.evaluateJavascript("getEditorContent();") { html ->
//            println("에디터 내용: $html")
//        }
//    }

    private fun createSharedElementTransition(): Transition {
        return TransitionInflater.from(this)
            .inflateTransition(R.transition.grid_to_pager_transition).apply {
                duration = 500  // 애니메이션 지속 시간 (ms)
                interpolator = AccelerateDecelerateInterpolator()  // 부드러운 가속/감속 효과
            }
    }
}