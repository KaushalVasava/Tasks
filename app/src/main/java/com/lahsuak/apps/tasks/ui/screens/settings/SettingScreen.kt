package com.lahsuak.apps.tasks.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.viewmodel.SettingsViewModel
import com.lahsuak.apps.tasks.util.AppConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    settingViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(AppConstants.ANY_MIME_TYPE),
        onResult = { uri ->
            uri?.let { settingViewModel.onExport(uri) }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { settingViewModel.onImport(uri) }
        }
    )

    // Set launchers in ViewModel and initialize settings
    LaunchedEffect(Unit) {
        settingViewModel.setLaunchers(exportLauncher, importLauncher)
        settingViewModel.initializeSettings(context)
    }

    val settings by settingViewModel.settingsList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(id = R.string.settings))
            }, navigationIcon = {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_back),
                        stringResource(id = R.string.back)
                    )
                }
            })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            settings.groupBy {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(it.items, key = { settingItem ->
                    settingItem.id
                }) { item ->
                    Spacer(modifier = Modifier.height(2.dp))
                    when (item.type) {
                        PreferenceType.NORMAL -> ClickPreference(
                            title = item.title,
                            placeHolder = item.placeholder,
                            icon = item.icon
                        ) {
                            item.action("", -1)
                        }

                        PreferenceType.DROPDOWN -> DropDownPreference(
                            title = item.title,
                            initialValue = item.placeholder.orEmpty(),
                            icon = item.icon,
                            items = item.items
                        ) { selectedOption, index ->
                            item.action(selectedOption, index)
                        }

                        PreferenceType.SWITCH -> SwitchPreference(
                            title = item.title,
                            placeHolder = item.placeholder,
                            icon = item.icon,
                            value = item.initialValue ?: false,
                            onValueChange = { checked ->
                                item.onCheckedChange(checked)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Text(
                    text = context.getString(R.string.made_in_india),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillParentMaxWidth(),
                )
            }
        }
    }
}