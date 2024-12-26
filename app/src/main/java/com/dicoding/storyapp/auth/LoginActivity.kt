package com.dicoding.storyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.User
import com.dicoding.storyapp.data.UserConfig
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.model.ViewModelFactory
import com.dicoding.storyapp.repo.Output
import com.dicoding.storyapp.MainActivity
import com.dicoding.storyapp.data.dataStore
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userConfig: UserConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userConfig = UserConfig.getInstance(dataStore)

        lifecycleScope.launch {
            userConfig.getUserSession().collect { user ->
                if (user.isLogin) {
                    navigateToMain()
                }
            }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        setupRegisterLink()
        playAnimation()

        binding.edLoginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val password = charSequence.toString()
                if (password.length < 8) {
                    binding.edLoginPassword.error = getString(R.string.invalid_password)
                } else {
                    binding.edLoginPassword.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text?.toString() ?: ""
            val password = binding.edLoginPassword.text?.toString() ?: ""

            if (!validateInput(email, password)) return@setOnClickListener

            viewModel.login(email, password).observe(this) { result ->
                when (result) {
                    is Output.Error -> {
                        showLoading(false)
                        Snackbar.make(binding.root, result.error, Snackbar.LENGTH_LONG).show()
                    }
                    is Output.Loading -> showLoading(true)
                    is Output.Success -> {
                        showLoading(false)
                        if (result.data.error == true) {
                            Snackbar.make(binding.root, result.data.message.toString(), Snackbar.LENGTH_LONG).show()
                        } else {
                            result.data.loginResult?.let {
                                Log.d("LoginActivity", "Login successful: $it")
                                lifecycleScope.launch {
                                    userConfig.saveUserSession(User(email, it.token, true))
                                }
                                navigateToMain()
                            } ?: run {
                                Snackbar.make(binding.root, "Login result is null", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRegisterLink() {
        binding.registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(binding.root, getString(R.string.invalid_email), Snackbar.LENGTH_LONG).show()
            return false
        }
        if (password.isBlank()) {
            Snackbar.make(binding.root, getString(R.string.invalid_password), Snackbar.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.linearProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {
        val logoAnimator = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val titleAnimator = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(500)
        val messageAnimator = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 0f, 1f).setDuration(500)
        val emailAnimator = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).setDuration(500)
        val passwordAnimator = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).setDuration(500)
        val buttonAnimator = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 0f, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(logoAnimator, titleAnimator, messageAnimator, emailAnimator, passwordAnimator, buttonAnimator)
            start()
        }
    }
}
