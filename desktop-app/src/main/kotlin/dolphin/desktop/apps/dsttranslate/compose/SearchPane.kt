@file:OptIn(ExperimentalFoundationApi::class)

package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dolphin.android.apps.dsttranslate.WordEntry

private enum class SearchType {
    Key, Origin, Text,
}

// See https://stackoverflow.com/a/69148766
@Composable
fun SearchPane(
    items: List<WordEntry>,
    modifier: Modifier = Modifier,
    selectedValue: String? = null,
    onSelect: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var currentEntry: WordEntry? = null
    var data by remember { mutableStateOf(selectedValue ?: "") }
    var last by remember { mutableStateOf("") }
    var filteredItems by remember { mutableStateOf(emptyList<WordEntry>()) }
    var searchType by remember { mutableStateOf(SearchType.Key) }

    fun refreshItems(text: String) {
        data = text
        filteredItems = items.filter { item ->
            when (searchType) {
                SearchType.Origin -> item.origin()
                SearchType.Key -> item.key()
                SearchType.Text -> item.string()
            }.contains(text, ignoreCase = true)
        }
        // println("found ${filteredItems.size}")
    }

    fun WordEntry.toSearchText() = when (searchType) {
        SearchType.Origin -> origin()
        SearchType.Key -> key()
        SearchType.Text -> string()
    }

    fun changeSearchType(type: SearchType) {
        searchType = type
        refreshItems(data)
    }

    Column(modifier = modifier.background(MaterialTheme.colors.surface)) {
        TextField(
            data,
            onValueChange = { text -> refreshItems(text) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            placeholder = { Text("text to search") },
            trailingIcon = {
                Row {
                    IconButton(onClick = { last = data }) {
                        Icon(
                            Icons.Rounded.Save,
                            contentDescription = "Cached",
                            tint = if (data.isNotEmpty()) Color.Gray else Color.LightGray,
                        )
                    }
                    LazyToolTip(tooltip = last, backgroundColor = Color.Transparent) {
                        IconButton(onClick = { refreshItems(last) }) {
                            Icon(
                                Icons.Rounded.Undo,
                                contentDescription = "Undo",
                                tint = if (last.isNotEmpty()) Color.Gray else Color.LightGray
                            )
                        }
                    }
                    IconButton(onClick = { refreshItems("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchType.values().forEach { type ->
                Row(
                    modifier = Modifier.clickable { changeSearchType(type) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (type == searchType) {
                            Icons.Rounded.RadioButtonChecked
                        } else {
                            Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = null
                    )
                    Text(
                        type.name,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        LazyScrollableColumn(filteredItems, modifier = Modifier.weight(1f)) { _, entry ->
            TextButton(onClick = {
                currentEntry = entry
                refreshItems(entry.toSearchText())
            }) {
                Column(Modifier.fillMaxWidth()) {
                    if (searchType != SearchType.Key) {
                        Text(
                            entry.key(),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.secondary,
                        )
                    }
                    Text(entry.toSearchText(), style = MaterialTheme.typography.body1)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onCancel?.invoke() }, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(onClick = {
                val entry = currentEntry ?: items.find { entry -> entry.toSearchText() == data }
                onSelect?.invoke(entry?.key() ?: "")
            }, modifier = Modifier.weight(1f)) {
                Text("Edit")
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSearchPaneAll() {
    DstTranslatorTheme {
        SearchPane(items = PreviewDefaults.samples)
    }
}

@Preview
@Composable
private fun PreviewSearchPaneShowSelected() {
    DstTranslatorTheme {
        SearchPane(items = PreviewDefaults.samples, selectedValue = "ch")
    }
}
