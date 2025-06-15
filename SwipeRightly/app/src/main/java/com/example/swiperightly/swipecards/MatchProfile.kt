package com.example.swiperightly.swipecards

import androidx.annotation.DrawableRes
import com.example.swiperightly.R

data class MatchProfile(
    val name: String,
    @DrawableRes val drawableResId: Int,
)

// NOTE: You will need to add drawable resources to your res/drawable folder
// that match these names (e.g., erlich.jpg, richard.jpg, etc.)
val profiles = listOf(
    MatchProfile("Snehil", R.drawable.snehil),
    MatchProfile("Richard Hendricks", R.drawable.richard),
    MatchProfile("Laurie Bream", R.drawable.laurie),
    MatchProfile("Russ Hanneman", R.drawable.russ),
    MatchProfile("Dinesh Chugtai", R.drawable.dinesh),
    MatchProfile("Monica Hall", R.drawable.monica),
    MatchProfile("Bertram Gilfoyle", R.drawable.gilfoyle),
    MatchProfile("Peter Gregory", R.drawable.peter),
    MatchProfile("Jared Dunn", R.drawable.jared),
    MatchProfile("Nelson Bighetti", R.drawable.big_head),
    MatchProfile("Gavin Belson", R.drawable.gavin),
    MatchProfile("Jian Yang", R.drawable.jian),
    MatchProfile("Jack Barker", R.drawable.barker),
)