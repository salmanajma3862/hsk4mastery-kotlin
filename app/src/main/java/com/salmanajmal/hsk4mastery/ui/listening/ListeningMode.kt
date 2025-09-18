package com.salmanajmal.hsk4mastery.ui.listening

enum class ListeningMode {
    IMMERSION, // Play word audio -> sentence (zh) -> meaning (en) -> translation (en)
    QUIZ,      // Play word audio only, delay, then sentence
    DICTATION; // Play sentence only (pause between), user can try to recall
}
