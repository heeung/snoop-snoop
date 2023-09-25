package com.appa.snoop.presentation.ui.mypage

import androidx.lifecycle.ViewModel
import com.appa.snoop.presentation.ui.mypage.component.BenefitCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "[김진영] MyPageViewModel"

@HiltViewModel
class MyPageViewModel @Inject constructor(
) : ViewModel() {
    private val _cardsState = MutableStateFlow(emptyList<BenefitCard>())
    val cardsState: StateFlow<List<BenefitCard>> = _cardsState.asStateFlow()

    init {
        _cardsState.value = listOf(
            BenefitCard("신한카드", true),
            BenefitCard("하나카드", false),
            BenefitCard("우리카드", false),
            BenefitCard("삼성카드", false),
        )
    }

}