package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerStatus
import com.example.ui.theme.LocalEuiStatusColors
import kotlinx.coroutines.delay

@Composable
fun EuiCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  content: @Composable ColumnScope.() -> Unit
) {
  val shape = RoundedCornerShape(10.dp)
  val cardModifier = if (onClick != null) {
    modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable { onClick() }
  } else {
    modifier.fillMaxWidth()
  }

  Surface(
    modifier = cardModifier,
    shape = shape,
    color = MaterialTheme.colorScheme.surface,
    border = border,
    tonalElevation = 0.dp,
    shadowElevation = 0.5.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      content = content
    )
  }
}

@Composable
fun EuiSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  badge: String? = null,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
      )
      if (badge != null) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
    if (actionText != null && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
      ) {
        Text(
          text = actionText,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
fun EuiStatusBadge(
  status: ServerStatus,
  modifier: Modifier = Modifier
) {
  val statusColors = LocalEuiStatusColors.current

  val (bgColor, textColor, dotColor, label) = when (status) {
    ServerStatus.ONLINE -> Quadruple(
      statusColors.successContainer,
      statusColors.onSuccessContainer,
      statusColors.success,
      "Online"
    )
    ServerStatus.OFFLINE -> Quadruple(
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      "Offline"
    )
    ServerStatus.STARTING -> Quadruple(
      statusColors.warningContainer,
      statusColors.onWarningContainer,
      statusColors.warning,
      "Starting..."
    )
    ServerStatus.STOPPING -> Quadruple(
      statusColors.warningContainer,
      statusColors.onWarningContainer,
      statusColors.warning,
      "Stopping..."
    )
    ServerStatus.RESTARTING -> Quadruple(
      statusColors.warningContainer,
      statusColors.onWarningContainer,
      statusColors.warning,
      "Restarting..."
    )
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor,
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(dotColor)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = textColor
      )
    }
  }
}

@Composable
fun EuiMetricCard(
  label: String,
  valueText: String,
  progress: Float,
  sublabel: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  progressColor: Color = MaterialTheme.colorScheme.secondary
) {
  val shape = RoundedCornerShape(10.dp)
  Surface(
    modifier = modifier,
    shape = shape,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    shadowElevation = 0.5.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = valueText,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = progressColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )

      Text(
        text = sublabel,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun EuiCopyField(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  onCopied: (() -> Unit)? = null
) {
  val context = LocalContext.current
  var copied by remember { mutableStateOf(false) }

  LaunchedEffect(copied) {
    if (copied) {
      delay(2000)
      copied = false
    }
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      IconButton(
        onClick = {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText(label, value)
          clipboard.setPrimaryClip(clip)
          copied = true
          onCopied?.invoke()
        },
        modifier = Modifier
          .size(36.dp)
          .testTag("copy_button_${label.lowercase().replace(" ", "_")}")
      ) {
        if (copied) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Copied",
            tint = LocalEuiStatusColors.current.success,
            modifier = Modifier.size(18.dp)
          )
        } else {
          Icon(
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = "Copy to clipboard",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun EuiConfirmationDialog(
  title: String,
  message: String,
  confirmButtonText: String = "Confirm",
  dismissButtonText: String = "Cancel",
  isDestructive: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(12.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    confirmButton = {
      if (isDestructive) {
        Button(
          onClick = onConfirm,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.defaultMinSize(minHeight = 44.dp)
        ) {
          Text(confirmButtonText, style = MaterialTheme.typography.labelLarge)
        }
      } else {
        Button(
          onClick = onConfirm,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier.defaultMinSize(minHeight = 44.dp)
        ) {
          Text(confirmButtonText, style = MaterialTheme.typography.labelLarge)
        }
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
      ) {
        Text(
          dismissButtonText,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
