package com.zaheer.lunawell.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LunaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            content = content
        )
    }
}
