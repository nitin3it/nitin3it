package com.nitin3it.kidsafe.data

import java.security.SecureRandom

object PublicIds {
    // No 0/O or 1/I so codes are easy to read aloud and type.
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()

    fun generate(): String {
        val chars = CharArray(8) { ALPHABET[random.nextInt(ALPHABET.length)] }
        return String(chars, 0, 4) + "-" + String(chars, 4, 4)
    }

    /** Accepts `k7qm29tx`, `K7QM 29TX`, `K7QM-29TX` and returns `K7QM-29TX`. */
    fun normalize(input: String): String {
        val raw = input.uppercase().filter { it.isLetterOrDigit() }
        return if (raw.length == 8) raw.take(4) + "-" + raw.drop(4) else raw
    }
}
