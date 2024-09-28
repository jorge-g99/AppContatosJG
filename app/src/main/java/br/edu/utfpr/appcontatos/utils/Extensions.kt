package br.edu.utfpr.appcontatos.utils

import android.icu.text.DecimalFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//46988887777
//(46) 98888-7777
fun String.toFormattedPhone(): String = this.mapIndexed { index, char ->
    when {
        index == 0 -> "($char"
        index == 2 -> ") $char"
        (index == 6 && length < 11 ) ||
                (index == 7 && length == 11) -> "-$char"
        else -> char
    }
}.joinToString("")

fun BigDecimal.format(): String {
    val formatter = DecimalFormat("R$#,##0.00")
    return formatter.format(this)
}

fun LocalDate.format(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return format(formatter)
}