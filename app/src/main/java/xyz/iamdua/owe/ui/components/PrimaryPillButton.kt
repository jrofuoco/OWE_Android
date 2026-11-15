package xyz.iamdua.owe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = Dark12,
    contentColor: Color = White
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = GrayBorder,
            disabledContentColor = GrayText
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        if (isLoading) {
            OwlLoadingSpinner(
                color = Color(0xFF22C55E),
                strokeWidth = 2.dp,
                modifier = Modifier.size(26.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = if (enabled) contentColor else GrayText
            )
        }
    }
}
