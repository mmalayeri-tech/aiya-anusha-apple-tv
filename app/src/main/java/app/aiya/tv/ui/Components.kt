package app.aiya.tv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P

/** LCARS elbow: a sidebar block curving into a horizontal arm. [flip] draws the lower one. */
class ElbowShape(
    private val sidebar: Dp,
    private val arm: Dp,
    private val outer: Dp,
    private val inner: Dp,
    private val flip: Boolean = false,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { sidebar.toPx() }
        val a = with(density) { arm.toPx() }
        val ro = with(density) { outer.toPx() }
        val ri = with(density) { inner.toPx() }
        val w = size.width
        val h = size.height
        val p = Path()
        if (!flip) {
            p.moveTo(ro, 0f); p.lineTo(w, 0f); p.lineTo(w, a); p.lineTo(s + ri, a)
            p.arcTo(Rect(s, a, s + 2 * ri, a + 2 * ri), 270f, -90f, false)
            p.lineTo(s, h); p.lineTo(0f, h); p.lineTo(0f, ro)
            p.arcTo(Rect(0f, 0f, 2 * ro, 2 * ro), 180f, 90f, false)
        } else {
            p.moveTo(ro, h); p.lineTo(w, h); p.lineTo(w, h - a); p.lineTo(s + ri, h - a)
            p.arcTo(Rect(s, h - a - 2 * ri, s + 2 * ri, h - a), 90f, 90f, false)
            p.lineTo(s, 0f); p.lineTo(0f, 0f); p.lineTo(0f, h - ro)
            p.arcTo(Rect(0f, h - 2 * ro, 2 * ro, h), 180f, -90f, false)
        }
        p.close()
        return Outline.Generic(p)
    }
}

fun Color.focused(): Color = lerp(this, Color.White, 0.45f)

/** Remote-friendly LCARS pill button: focus brightens it and draws a white edge. */
@Composable
fun LcarsButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(percent = 50),
    height: Dp = 44.dp,
    textSize: Int = 18,
    textAlign: TextAlign = TextAlign.End,
    selected: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = color,
            contentColor = Color.Black,
            focusedContainerColor = color.focused(),
            focusedContentColor = Color.Black,
            pressedContainerColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(3.dp, Color.White))),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = if (textAlign == TextAlign.Center) 4.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Box(Modifier.width(10.dp).fillMaxHeight(0.6f).clip(RoundedCornerShape(3.dp)).background(Color.Black))
                Spacer(Modifier.width(8.dp))
            }
            LText(text, Modifier.weight(1f), size = textSize.sp, color = Color.Black, bold = true, align = textAlign)
        }
    }
}

/** A big service/action tile: coloured side block, title, subtitle and a status chip. */
@Composable
fun LcarsTile(
    title: String,
    subtitle: String,
    status: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 110.dp,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp, topEnd = 8.dp, bottomEnd = 8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = P.panel,
            contentColor = P.text,
            focusedContainerColor = color,
            focusedContentColor = Color.Black,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(2.dp, color.copy(alpha = 0.7f))),
            focusedBorder = Border(BorderStroke(4.dp, Color.White)),
        ),
    ) {
        val content = androidx.tv.material3.LocalContentColor.current
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(26.dp).fillMaxHeight().background(color))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LText(title, size = 28.sp, bold = true, color = content)
                LText(subtitle, size = 15.sp, color = content.copy(alpha = 0.8f))
            }
            LText(
                status,
                Modifier.padding(end = 12.dp).clip(RoundedCornerShape(50)).background(color).padding(horizontal = 12.dp, vertical = 4.dp),
                size = 14.sp,
                bold = true,
                color = Color.Black,
            )
        }
    }
}

/** A titled, non-focusable information panel with a coloured header bar. */
@Composable
fun LcarsPanel(title: String, color: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(28.dp).height(22.dp).clip(RoundedCornerShape(topStart = 11.dp, bottomStart = 11.dp)).background(color))
            Spacer(Modifier.width(6.dp))
            LText(title, size = 18.sp, color = color, bold = true)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f).height(22.dp).background(color))
        }
        Column(
            Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp).background(P.panel.copy(alpha = 0.6f)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) { content() }
    }
}

@Composable
fun InfoLine(label: String, value: String, valueColor: Color = P.text) {
    Row(Modifier.fillMaxWidth()) {
        LText(label, size = 16.sp, color = P.dim)
        Spacer(Modifier.width(10.dp))
        LText(value, Modifier.weight(1f), size = 16.sp, color = valueColor, align = TextAlign.End)
    }
}

/** A numbered instruction line. */
@Composable
fun Step(n: Int, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        LText(
            n.toString().padStart(2, '0'),
            Modifier.width(44.dp).clip(RoundedCornerShape(50)).background(color).padding(vertical = 2.dp),
            size = 16.sp, bold = true, color = Color.Black, align = TextAlign.Center,
        )
        Spacer(Modifier.width(12.dp))
        LText(text, Modifier.weight(1f), size = 18.sp, maxLines = 3)
    }
}

@Composable
fun Bar(color: Color, modifier: Modifier = Modifier, shape: Shape = RectangleShape) {
    Box(modifier.clip(shape).background(color))
}
