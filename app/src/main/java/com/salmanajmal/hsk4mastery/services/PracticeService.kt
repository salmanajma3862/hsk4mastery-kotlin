package com.salmanajmal.hsk4mastery.services

import javax.inject.Inject
import javax.inject.Singleton
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel

@Singleton
class PracticeService @Inject constructor() {
    // Direct translation of the JS shuffle used in the Expo prototype
    fun generateScramble(tokens: List<SentenceTokenModel>): List<SentenceTokenModel> {
        val copy = tokens.toMutableList()
        copy.shuffle()
        return copy.toList()
    }
}
