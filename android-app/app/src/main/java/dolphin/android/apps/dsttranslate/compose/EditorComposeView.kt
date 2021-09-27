package dolphin.android.apps.dsttranslate.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dolphin.android.apps.dsttranslate.WordEntry

@Composable
fun EntryEditor(
    target: WordEntry,
    modifier: Modifier = Modifier,
    origin: WordEntry? = null,
    source: String? = null,
    onSave: ((String, String) -> Unit)? = null,
    onCopy: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var text by remember { mutableStateOf(target.string()) }
    var targetVisible by remember { mutableStateOf(!target.newly) }
    var originVisible by remember { mutableStateOf(true) }
    var sourceVisible by remember { mutableStateOf(true) }
    val holoBlue = colorResource(id = android.R.color.holo_blue_bright)
    val holoRed = colorResource(id = android.R.color.holo_red_light)
    val holoGreen = colorResource(id = android.R.color.holo_green_dark)

    Column(modifier = modifier.background(Color.White)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(target.key(), modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis)
//            if (target.newly) {
//                Text(text = "NEW", color = Color.Red, fontSize = 12.sp)
//            }
            IconButton(onClick = { targetVisible = !targetVisible }) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (targetVisible) holoGreen else holoGreen.copy(alpha = .5f)
                )
            }
            origin?.let {
                IconButton(onClick = { originVisible = !originVisible }) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (originVisible) holoRed else holoRed.copy(alpha = .5f)
                    )
                }
            }
            IconButton(onClick = { sourceVisible = !sourceVisible }) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (sourceVisible) holoBlue else holoBlue.copy(alpha = .5f)
                )
            }
        }

        if (sourceVisible) {
            Button(
                onClick = { text = source ?: "" },
                modifier = Modifier.fillMaxWidth(),
                enabled = source?.isNotEmpty() == true,
                colors = ButtonDefaults.buttonColors(backgroundColor = holoBlue),
            ) {
                Text(source ?: "")
            }
        }

        origin?.let { old ->
            if (originVisible) {
                TextButton(
                    onClick = { onCopy?.invoke(old.origin()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = holoRed),
                ) {
                    Text(old.origin(), modifier = Modifier.fillMaxWidth())
                }
                Button(
                    onClick = { text = old.string() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = holoRed),
                ) {
                    Text(old.string())
                }
            }
        }

        if (targetVisible) {
            TextButton(
                onClick = { onCopy?.invoke(target.origin()) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = holoGreen),
            ) {
                Text(target.origin(), modifier = Modifier.fillMaxWidth())
            }
            Button(
                onClick = { text = target.string() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = holoGreen),
            ) {
                Text(target.string())
            }
        }
        TextField(
            value = text,
            onValueChange = { str -> text = str },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { onCancel?.invoke() },
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.requiredWidth(16.dp))
            Button(
                onClick = { onSave?.invoke(target.key, "\"$text\"") },
                modifier = Modifier.weight(3f),
            ) {
                Text("Apply")
            }
        }
    }
}

@Preview(widthDp = 640, heightDp = 480)
@Composable
private fun PreviewEntryEditor() {
    AppTheme {
        EntryEditor(
            target = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happiness",
                "\"Assess Happiness STR\"",
            ),
            origin = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happy",
                "\"Assess Happiness STR\"",
            ),
            source = "\"Climb STR\"",
        )
    }
}

@Preview(widthDp = 640, heightDp = 480)
@Composable
private fun PreviewEntryEditorForNew() {
    AppTheme {
        EntryEditor(
            target = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happiness",
                "\"Assess Happiness STR\"",
                newly = true,
            ),
            source = "\"Climb STR\"",
        )
    }
}
