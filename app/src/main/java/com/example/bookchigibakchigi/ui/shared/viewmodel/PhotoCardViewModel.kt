package com.example.bookchigibakchigi.ui.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoCardViewModel @Inject constructor(
    private val photoCardRepository: PhotoCardRepository
) : ViewModel() {
    private val _photoCardList = MutableLiveData<List<PhotoCardWithTextContents>>()
    val photoCardList: LiveData<List<PhotoCardWithTextContents>> get() = _photoCardList

    /** 📌 특정 ISBN의 포토카드 리스트 가져오기 */
    fun loadPhotoCards(isbn: String) {
        viewModelScope.launch {
            val photoCards = photoCardRepository.getPhotoCardListByIsbn(isbn)
            _photoCardList.value = photoCards
        }
    }

}