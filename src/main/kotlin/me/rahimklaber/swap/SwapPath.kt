package me.rahimklaber.swap

data class SwapPath<T, T1>(
    val inSettings: T,
    val outSettings: T1
)