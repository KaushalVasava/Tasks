package com.lahsuak.apps.tasks.ui.screens

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.SettingPreferences
import com.lahsuak.apps.tasks.model.SettingItemModel
import com.lahsuak.apps.tasks.ui.fragments.settings.SettingsFragment
import com.lahsuak.apps.tasks.ui.viewmodel.SettingViewModel
import com.lahsuak.apps.tasks.util.demo.getSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, settingViewModel: SettingViewModel, fragmentManager: FragmentManager) {
//    val preference by settingViewModel.preferencesFlow.collectAsState(
//        SettingPreferences(
//            voiceButtonEnable = true,
//            progressBarEnable = false,
//            showReminder = true,
//            showSubTask = true
//        )
//    )
//    val settingsData = getSettings(LocalContext.current, preference, settingViewModel)
//    Log.d(
//        "TAG",
//        "SettingScreen: reminder ${preference.showReminder}, voice ${preference.voiceButtonEnable}, "
//    )
//    Log.d(
//        "TAG",
//        "SettingScreen: progress ${preference.progressBarEnable}, subtask ${preference.showSubTask}, "
//    )
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val containerId = R.id.my_container // some unique id
            val fragmentContainerView = FragmentContainerView(context).apply {
                id = containerId
            }

            val fragment = SettingsFragment()
            fragmentManager.beginTransaction()
                .replace(containerId, fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()

            fragmentContainerView
        }
    )
    val bottomSheet = rememberModalBottomSheetState()
    var isBottomSheetOpened by rememberSaveable {
        mutableStateOf(false)
    }
    if (isBottomSheetOpened) {
        ModalBottomSheet(
            sheetState = bottomSheet,
            onDismissRequest = {
                isBottomSheetOpened = false
            }
        ) {
//            FilterDialog {
//                isBottomSheetOpened = false
//            }
        }
    }
//    Scaffold(
//        topBar = {
//            TopAppBar(title = {
//                Text(stringResource(id = R.string.settings))
//            }, navigationIcon = {
//                IconButton(onClick = {
//                    navController.popBackStack()
//                }) {
//                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                }
//            })
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            Modifier
//                .padding(paddingValues)
//                .padding(horizontal = 16.dp)
//        ) {
//            settingsData.map {
//                item {
//                    TitleItem(title = it.title)
//                }
//                items(it.items) { setting ->
//                    SettingItem(setting) {
//                        Log.d("TAG", "SettingScreen: ${setting.title} click")
//                        setting.action(setting.isEnable?:false)
//                    }
//                }
//            }
//        }
//    }
}

fun setTheme() {
    when (SettingsFragment.selectedTheme) {
        -1 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM //system theme
        1 -> AppCompatDelegate.MODE_NIGHT_NO
        2 -> AppCompatDelegate.MODE_NIGHT_YES //dark theme
    }
    AppCompatDelegate.setDefaultNightMode(SettingsFragment.selectedTheme)
//    if (SettingsFragment.selectedTheme == 2) {
//        WindowInsetsControllerCompat(
//            requireActivity().window,
//            requireActivity().window.decorView
//        ).isAppearanceLightStatusBars =
//            false
//        WindowInsetsControllerCompat(
//            requireActivity().window,
//            requireActivity().window.decorView
//        ).isAppearanceLightNavigationBars =
//            false
//    }
}

@Composable
fun SettingItem(setting: SettingItemModel, onClick: (Boolean) -> Unit) {
    var selected by rememberSaveable {
        mutableStateOf(setting.isEnable)
    }

    val modifier by remember {
        derivedStateOf {
            if (selected != null) {
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        onClick(selected!!)
                    }
                    .toggleable(
                        value = selected!!,
                        onValueChange = {
                            selected = it
                            onClick(it)
                        },
                        role = Role.Checkbox
                    )
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        Log.d("TAG", "SettingItem: click")
                        onClick(false)
                    }
            }
        }
    }
    Row(
        modifier,
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
        if (selected != null) {
            Switch(
                checked = selected!!,
                onCheckedChange = {
                    selected = it
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
//            SettingScreen(rememberNavController(), fragmentManager = supportF)
        }
    }
}
