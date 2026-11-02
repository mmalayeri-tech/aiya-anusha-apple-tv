package app.aiya.tv.ui.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.aiya.tv.R

object P {
    val orange = Color(0xFFFF9900)
    val lilac = Color(0xFFCC99CC)
    val blue = Color(0xFF9999FF)
    val peach = Color(0xFFFFCC99)
    val gold = Color(0xFFFFAA33)
    val red = Color(0xFFCC6666)
    val text = Color(0xFFFFCC99)
    val dim = Color(0xFFB38A5E)
    val panel = Color(0xFF15100A)
    val cycle = listOf(orange, lilac, blue, peach, gold, red)
    fun pick(i: Int) = cycle[i.mod(cycle.size)]
}

val LcarsFont = FontFamily(
    Font(R.font.antonio, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.antonio, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

@Composable
fun LText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 18.sp,
    color: Color = P.text,
    bold: Boolean = false,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = LcarsFont,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = size,
            color = color,
            textAlign = align,
            letterSpacing = 0.5.sp,
        ),
    )
}
