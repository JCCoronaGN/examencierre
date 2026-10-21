package com.example.examencierre.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.examencierre.R
import com.example.examencierre.data.local.entity.UserEntity
import com.example.examencierre.data.repository.AuthError
import com.example.examencierre.data.session.SessionManager
import com.example.examencierre.databinding.ActivityLoginBinding
import com.example.examencierre.ui.main.MainActivity
import com.example.examencierre.ui.register.RegisterActivity
import com.example.examencierre.util.applySystemBarInsets
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.editTextEmail.doAfterTextChanged { binding.textInputLayoutEmail.error = null }
        binding.editTextPassword.doAfterTextChanged { binding.textInputLayoutPassword.error = null }

        binding.buttonLogin.setOnClickListener { attemptLogin() }
        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBar.isVisible = loading
                        binding.buttonLogin.isEnabled = !loading
                        binding.buttonRegister.isEnabled = !loading
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is LoginEvent.Success -> onLoginSuccess(event.user)
                            is LoginEvent.Error -> showError(event.error)
                        }
                    }
                }
            }
        }
    }

    private fun attemptLogin() {
        val correo = binding.editTextEmail.text?.toString()?.trim().orEmpty()
        val password = binding.editTextPassword.text?.toString().orEmpty()

        var valid = true
        if (!isValidEmail(correo)) {
            binding.textInputLayoutEmail.error = getString(R.string.error_email_invalid)
            valid = false
        }
        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = getString(R.string.error_password_empty)
            valid = false
        }
        if (!valid) return

        viewModel.login(correo, password)
    }

    private fun onLoginSuccess(user: UserEntity) {
        sessionManager.saveSession(user.id, user.nombre, user.apellidos, user.correo)
        goToMain()
    }

    private fun showError(error: AuthError) {
        val message = when (error) {
            AuthError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
            AuthError.EMAIL_ALREADY_REGISTERED -> R.string.error_email_exists
            AuthError.INVALID_DATA -> R.string.error_generic
            AuthError.DATABASE_ERROR -> R.string.error_database
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
