package xyz.iamdua.owe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.White

data class FeedbackMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false
)

object AppFeedback {
    private val _current = MutableStateFlow<FeedbackMessage?>(null)
    val current: StateFlow<FeedbackMessage?> = _current.asStateFlow()

    fun showSuccess(message: String) {
        _current.value = FeedbackMessage(text = message, isError = false)
    }

    fun showError(message: String) {
        _current.value = FeedbackMessage(text = message, isError = true)
    }

    fun dismiss() {
        _current.value = null
    }
}

@Composable
fun AppFeedbackHost(modifier: Modifier = Modifier) {
    val message by AppFeedback.current.collectAsState()

    LaunchedEffect(message?.id) {
        if (message != null) {
            delay(2800L)
            AppFeedback.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val current = message ?: return@AnimatedVisibility
            val bg = if (current.isError) StatusOverdueText else ForestGreen
            val icon = if (current.isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = bg,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = current.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
