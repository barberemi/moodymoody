package com.example.moodymoody.data

import com.example.moodymoody.R

enum class MoodIconFamily(val prefix: String, val label: String) {
    POUSSIN("poussin", "Poussin"),
    NUAGE("nuage", "Nuage"),
    PANDA("panda", "Panda"),
    OURSON("ourson", "Ourson");

    fun drawableForMood(key: String): Int {
        val resourceName = "${prefix}_${key}"
        return RESOURCES_MAP[resourceName] ?: RESOURCES_MAP["poussin_${key}"] ?: R.drawable.poussin_neutre
    }

    companion object {
        fun fromName(name: String?): MoodIconFamily = values().firstOrNull {
            it.name == name
        } ?: POUSSIN

        private val RESOURCES_MAP = mapOf(
            "poussin_super" to R.drawable.poussin_super,
            "poussin_bien" to R.drawable.poussin_bien,
            "poussin_neutre" to R.drawable.poussin_neutre,
            "poussin_moyen" to R.drawable.poussin_moyen,
            "poussin_mauvais" to R.drawable.poussin_mauvais,
            "nuage_super" to R.drawable.nuage_super,
            "nuage_bien" to R.drawable.nuage_bien,
            "nuage_neutre" to R.drawable.nuage_neutre,
            "nuage_moyen" to R.drawable.nuage_moyen,
            "nuage_mauvais" to R.drawable.nuage_mauvais,
            "panda_super" to R.drawable.panda_super,
            "panda_bien" to R.drawable.panda_bien,
            "panda_neutre" to R.drawable.panda_neutre,
            "panda_moyen" to R.drawable.panda_moyen,
            "panda_mauvais" to R.drawable.panda_mauvais,
            "ourson_super" to R.drawable.panda_super,
            "ourson_bien" to R.drawable.panda_bien,
            "ourson_neutre" to R.drawable.ourson_neutre,
            "ourson_moyen" to R.drawable.panda_moyen,
            "ourson_mauvais" to R.drawable.panda_mauvais,
        )
    }
}
