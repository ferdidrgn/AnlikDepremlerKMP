package com.ferdidrgn.anlikdepremler.ui.util

/**
 * Listenin boyutuna göre dinamik reklam aralığı belirler.
 * Liste eleman sayısı < 10 ise her 5 elemanda bir,
 * >= 10 ise her 10 elemanda bir reklam tetikler.
 */
fun shouldShowAdAtIndex(index: Int, totalItemCount: Int): Boolean {
    val interval = if (totalItemCount < 10) 5 else 10
    return (index + 1) % interval == 0
}