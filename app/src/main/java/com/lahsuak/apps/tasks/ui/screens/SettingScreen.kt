package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.model.SettingItemModel
import com.lahsuak.apps.tasks.util.demo.getSettings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val settingsData = getSettings(LocalContext.current)

    Scaffold(
     topBar = {
         TopAppBar(title = {
             Text(stringResource(id = R.string.settings))
         }, navigationIcon = {
             IconButton(onClick = {
                 navController.popBackStack()
             }) {
                 Icon(Icons.Default.ArrowBack, contentDescription = "Back")
             }
         })
     }
    ){
        LazyColumn(
            Modifier
                .padding(it)
                .padding(horizontal = 16.dp)) {
            settingsData.map {
                item {
                    TitleItem(title = it.title)
                }
                items(it.items) { setting ->
                    SettingItem(setting = setting)
                }
            }
        }
    }
}

@Composable
fun SettingItem(setting: SettingItemModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = setting.drawableRes),
            contentDescription = setting.title
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                setting.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                setting.title,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        if (setting.isEnable != null) {
            Switch(
                checked = setting.isEnable,
                onCheckedChange = {

                }
            )
        }
    }
}

@Composable
fun TitleItem(title: String) {
    Text(
        title,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Preview()
@Composable
fun PreviewSettingScreen() {
    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            SettingScreen(rememberNavController())
        }
    }
}
