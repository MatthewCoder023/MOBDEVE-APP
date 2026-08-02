package com.dlsu.unisync.util

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

// Small threshold so a stray pixel of movement, or the settle after a swipe,
// does not make the FAB flicker between states.
private const val SCROLL_THRESHOLD = 4

// Collapses an extended FAB to its icon while the list is scrolling down and
// expands it on the way back up: the label is there when you go looking for the
// action, and out of the way while you are reading the list.
fun ExtendedFloatingActionButton.shrinkOnScroll(recycler: RecyclerView) {
    recycler.addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > SCROLL_THRESHOLD -> shrink()
                    dy < -SCROLL_THRESHOLD -> extend()
                }
            }
        }
    )
}
