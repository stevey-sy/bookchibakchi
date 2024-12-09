package com.example.bookchigibakchigi.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchigibakchigi.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.TimeZone

class NotYetReadDialog (
    context: Context,
    private val pageCnt: Int, // 책의 총 페이지 수
    private val onSave: (pagesPerDay: Int) -> Unit // 저장 버튼 클릭 시 호출되는 콜백
) : BottomSheetDialog(context, R.style.CustomBottomSheetDialog) {
    init {
        // 레이아웃 inflate 및 설정
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_not_yet_read, null)
        setContentView(view)

        // 초기화
        setupView(view)
    }

    private fun setupView(view: View) {
        val editText = view.findViewById<EditText>(R.id.etPagePerDay)
        val estimatedDaysTextView = view.findViewById<TextView>(R.id.tvEstimatedDay)
        val completionYearTextView = view.findViewById<TextView>(R.id.tvCompletionYear)
        val completionMonthTextView = view.findViewById<TextView>(R.id.tvCompletionMonth)
        val completionDateTextView = view.findViewById<TextView>(R.id.tvCompletionDate)

        // 저장 버튼 클릭 이벤트
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val pagesPerDay = editText.text.toString().toIntOrNull() ?: 0
            if (pagesPerDay > 0) {
                // 계산
                val estimatedDays = calculateEstimatedDays(pageCnt, pagesPerDay)
                val completionDate = calculateCompletionDate(estimatedDays)

                // 결과 표시
                estimatedDaysTextView.text = "$estimatedDays 일"
                completionYearTextView.text = completionDate.first
                completionMonthTextView.text = completionDate.second
                completionDateTextView.text = completionDate.third

                // 콜백 호출 및 다이얼로그 닫기
                onSave(pagesPerDay)
                dismiss()
            } else {
                Toast.makeText(context, "하루 페이지 수를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 닫기 버튼 이벤트
        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.tvStartDate).setOnClickListener {
            showMaterialDatePicker { selectedRange ->
                view.findViewById<Button>(R.id.tvStartDate).text = selectedRange
            }
        }
    }

    private fun showMaterialDatePicker(onDateSelected: (String) -> Unit) {
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜를 선택하세요")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selectedDate

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            onDateSelected("$year. $month. $day")
        }

        datePicker.show((context as AppCompatActivity).supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun calculateEstimatedDays(pageCnt: Int, pagesPerDay: Int): Int {
        return if (pagesPerDay > 0) {
            (pageCnt + pagesPerDay - 1) / pagesPerDay // 올림 계산
        } else {
            0
        }
    }

    private fun calculateCompletionDate(days: Int): Triple<String, String, String> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)

        val year = calendar.get(Calendar.YEAR).toString()
        val month = (calendar.get(Calendar.MONTH) + 1).toString()
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()

        return Triple(year, month, day)
    }

}

