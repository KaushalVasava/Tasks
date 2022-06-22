package com.lahsuak.apps.mytask.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.User
import com.lahsuak.apps.mytask.data.util.Util
import com.lahsuak.apps.mytask.data.util.Util.notifyUser
import com.lahsuak.apps.mytask.data.util.viewBinding
import com.lahsuak.apps.mytask.databinding.FragmentLoginBinding
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding: FragmentLoginBinding by viewBinding {
        FragmentLoginBinding.bind(it)
    }
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btnLogin.setOnClickListener {
            signIn()
        }
    }


    private var singInActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task: com.google.android.gms.tasks.Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        singInActivityResultLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
//            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            // Signed in successfully
            val account: GoogleSignInAccount =
                GoogleSignIn.getLastSignedInAccount(requireContext())!!
            var personId: String? = null
            if (account != null) {
                personId = account.id
            }
            var flag = false
            if (personId != null) {
                flag = true
            }
            if (flag) {
                Util.setUserLoginStatus(requireContext(), true)
                if (account.id != null && account.displayName != null) {
                    val user = User(personId!!, account.displayName!!)
                    viewModel.insertUser(user)
                    val action = LoginFragmentDirections.actionLoginFragmentToTaskFragment()
                    findNavController().navigate(action)
                }
            } else
                notifyUser(requireContext(), "Login failed")
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            notifyUser(requireContext(), "Login failed")
        }
    }
}