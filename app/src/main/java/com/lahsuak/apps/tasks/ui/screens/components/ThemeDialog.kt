package com.lahsuak.apps.tasks.ui.screens.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_KEY
import com.lahsuak.apps.tasks.util.demo.getThemes

data class Theme(
    val name: String,
    val mode: Int,
)

@Composable
fun ThemeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val preference = context.getSharedPreferences(
        THEME_KEY,
        Context.MODE_PRIVATE
    )
    var selectedLangPosition by rememberSaveable {
        mutableIntStateOf(0)
    }
    val themes by rememberSaveable {
        mutableStateOf(getThemes())
    }
//    val selectedTheme = preference.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()
//    selectedLangPosition = themes.indexOfFirst {
//        it.mode == selectedTheme
//    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(themes) {
            ThemeItem(it) {
                val selectedTheme = preference.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()
                selectedLangPosition = themes.indexOfFirst {
                    it.mode == selectedTheme
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                onDismiss()
            }) {
                Text("Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThemeItem(theme: Theme, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                onClick()
            }
    ) {
        Text(
            theme.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}