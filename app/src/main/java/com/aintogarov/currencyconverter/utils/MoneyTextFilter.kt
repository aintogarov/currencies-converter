package com.aintogarov.currencyconverter.utils

import android.text.InputFilter
import android.text.Spanned

class MoneyTextFilter(
    private val beforeDotMax: Int,
    private val afterDotMax: Int
) : InputFilter {
    private val textToCheck = StringBuilder()

    override fun filter(
        source: CharSequence, start: Int, end: Int,
        dest: Spanned, destStart: Int, destEnd: Int
    ): CharSequence? {
        textToCheck.clear()
        textToCheck.append(dest).replace(destStart, destEnd, source.toString())

        val length = textToCheck.length
        val dotIndex = findDotIndex(textToCheck)

        return if (isValid(length, dotIndex)) null else ""
    }

    private fun isValid(length: Int, dotIndex: Int): Boolean {
        var result = true

        if (dotIndex == -1) {
            if (length > beforeDotMax) {
                result = false
            }
        } else {
            if (dotIndex > beforeDotMax) {
                result = false
            } else if (length - dotIndex - 1 > afterDotMax) {
                result = false
            }
        }

        return result
    }

    private fun findDotIndex(source: StringBuilder): Int {
        val length = source.length
        var dotIndex = -1

        for (i in 0 until length) {
            if (source[i] == '.') {
                dotIndex = i
                break
            }
        }
        return dotIndex
    }
}