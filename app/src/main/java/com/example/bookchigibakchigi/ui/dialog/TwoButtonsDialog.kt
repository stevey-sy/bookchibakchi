package com.example.bookchigibakchigi.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.bookchigibakchigi.databinding.DialogTwoButtonsBinding

class TwoButtonsDialog(
    context: Context,
    private val title: String,
    private val msg: String,
    private val btnText1: String,
    private val btnText2: String,
    private val onBtn1Click: () -> Unit,
    private val onBtn2Click: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogTwoButtonsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogTwoButtonsBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.apply {
            tvTitle.text = title
            tvMessage.text = msg
            btn1.text = btnText1
            btn2.text = btnText2

            btn1.setOnClickListener {
                onBtn1Click()
                dismiss()
            }

            btn2.setOnClickListener {
                onBtn2Click()
                dismiss()
            }
        }
    }
} 