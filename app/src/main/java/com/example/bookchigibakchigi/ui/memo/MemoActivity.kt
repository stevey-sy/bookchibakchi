package com.example.bookchigibakchigi.ui.memo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.databinding.ActivityMemoBinding
import com.example.bookchigibakchigi.ui.BaseActivity

class MemoActivity : BaseActivity() {

    private lateinit var binding: ActivityMemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbar, binding.main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupWebView()
    }

    private fun setupWebView() {
        val webView = binding.webView

        // WebView 설정
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true  // 로컬 저장 지원
        webView.webViewClient = WebViewClient()  // 내부 WebView에서 실행
        webView.addJavascriptInterface(EditorBridge(), "Android") // JS와 연결

        // 에디터 HTML 파일 로드
        webView.loadUrl("file:///android_asset/editor.html")
    }

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
    fun getEditorContent() {
        binding.webView.evaluateJavascript("getEditorContent();") { html ->
            println("에디터 내용: $html")
        }
    }
}