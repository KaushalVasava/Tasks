package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.fragments.settings.SettingsFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, fragmentManager: FragmentManager) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(id = R.string.settings))
            }, navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_back),
                        stringResource(id = R.string.back)
                    )
                }
            })
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
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
    }
}