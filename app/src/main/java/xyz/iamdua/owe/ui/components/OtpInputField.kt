package xyz.iamdua.owe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    errorMessage: String? = null,
    onDone: () -> Unit = {}
) {
    val isError = !errorMessage.isNullOrEmpty()
    val isCompact = length > 6
    val boxSpacing = if (isCompact) 6.dp else 10.dp
    val boxCorner = if (isCompact) 10.dp else 14.dp
    val digitFontSize = if (isCompact) 18.sp else 22.sp
    val boxAspectRatio = if (isCompact) 0.78f else 0.85f

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = otp,
            onValueChange = { value ->
                onOtpChange(value.filter { it.isDigit() }.take(length))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(boxSpacing)
                ) {
                    repeat(length) { index ->
                        val char = otp.getOrNull(index)?.toString().orEmpty()
                        val isFocused = otp.length == index || (otp.length == length && index == length - 1)
                        val borderColor = when {
                            isError -> StatusOverdueText
                            isFocused || char.isNotEmpty() -> Dark12
                            else -> GrayBorder
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(boxAspectRatio)
                                .clip(RoundedCornerShape(boxCorner))
                                .background(White)
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(boxCorner)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = digitFontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Dark12
                            )
                        }
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = StatusOverdueText,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}
