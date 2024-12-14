package com.example.bookchigibakchigi.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.bookchigibakchigi.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotYetReadDialog (
    context: Context,
    private val fragmentManager: FragmentManager,
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
        val startDate = view.findViewById<TextView>(R.id.tvStartDate)

        // 저장 버튼 클릭 이벤트
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
        }

        // 닫기 버튼 이벤트
        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.tvStartDate).setOnClickListener {
            showMaterialDatePicker { selectedRange ->
                startDate.text = selectedRange
                onDateChange(view)
            }
        }

        view.findViewById<ImageView>(R.id.ivCalendar).setOnClickListener {
            showMaterialDatePicker { selectedRange ->
                startDate.text = selectedRange
                onDateChange(view)
            }
        }

        editText.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                onDateChange(view)
            }
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val result = v.text.toString().toIntOrNull() ?: 0
                if(pageCnt < result) {
                    Toast.makeText(context, "입력하신 페이지수가 책의 전체 페이지수를 초과합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    onDateChange(view) // 완료 버튼 클릭 시 호출
                    // 키보드 닫기
                    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                true
            } else {
                false
            }
        }

        setTodayDate(view.findViewById<TextView>(R.id.tvStartDate))
        onDateChange(view)
        // 1초 지연 후 포커스 및 키보드 표시 (UI 준비 시간 고려)
        editText.postDelayed({
            editText.requestFocus()
            editText.selectAll()

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }

    private fun onDateChange(view: View) {
        val editText = view.findViewById<EditText>(R.id.etPagePerDay)
        val completionYearTextView = view.findViewById<TextView>(R.id.tvCompletionYear)
        val completionMonthTextView = view.findViewById<TextView>(R.id.tvCompletionMonth)
        val completionDateTextView = view.findViewById<TextView>(R.id.tvCompletionDate)
        val startDate = view.findViewById<TextView>(R.id.tvStartDate)
        val pagesPerDay = editText.text.toString().toIntOrNull() ?: 0

        if (pagesPerDay > 0) {
            // 계산
            val estimatedDays = calculateEstimatedDays(pageCnt, pagesPerDay)
            val completionDate = calculateCompletionDate(startDate.text.toString(), estimatedDays)

            completionYearTextView.text = completionDate.first
            completionMonthTextView.text = completionDate.second
            completionDateTextView.text = completionDate.third
        }
    }

    private fun setTodayDate(view: View) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")) // 한국 시간 가져오기
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) // 원하는 날짜 형식
        val todayDate = dateFormat.format(calendar.time)

        view.findViewById<TextView>(R.id.tvStartDate).text = todayDate
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

            onDateSelected("$year/$month/$day")
        }

        datePicker.show(fragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun calculateEstimatedDays(pageCnt: Int, pagesPerDay: Int): Int {
        return if (pagesPerDay > 0) {
            (pageCnt + pagesPerDay - 1) / pagesPerDay // 올림 계산
        } else {
            0
        }
    }

    private fun calculateCompletionDate(startDate: String, days: Int): Triple<String, String, String> {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            // 시작일 문자열을 Date로 변환
            val parsedDate = dateFormat.parse(startDate) ?: throw IllegalArgumentException("Invalid date format")
            calendar.time = parsedDate // 시작일 설정

            // 종료일 계산
            calendar.add(Calendar.DAY_OF_YEAR, days)

            // 종료일 추출
            val year = calendar.get(Calendar.YEAR).toString()
            val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0') // 월 2자리 형식
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0') // 일 2자리 형식

            return Triple(year, month, day)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing date: ${e.message}")
        }
    }


}

