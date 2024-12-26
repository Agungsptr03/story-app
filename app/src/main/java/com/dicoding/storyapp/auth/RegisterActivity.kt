package com.dicoding.storyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityRegisterBinding
import com.dicoding.storyapp.model.ViewModelFactory
import com.dicoding.storyapp.repo.Output

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        setupRegisterAction()
        setupLoginPrompt()
        startAnimation()

        binding.edRegisterEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val email = charSequence.toString()
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.edRegisterEmail.error = getString(R.string.invalid_email)
                } else {
                    binding.edRegisterEmail.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        binding.edRegisterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val password = charSequence.toString()
                if (password.length < 8) {
                    binding.edRegisterPassword.error = getString(R.string.invalid_password)
                } else {
                    binding.edRegisterPassword.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupRegisterAction() {
        binding.registerButton.setOnClickListener {
            val userName = binding.edRegisterName.text.toString()
            val userEmail = binding.edRegisterEmail.text.toString()
            val userPassword = binding.edRegisterPassword.text.toString()

            if (userEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userPassword.length < 8) {
                Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(userName, userEmail, userPassword).observe(this) { response ->
                when (response) {
                    is Output.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, response.error, Toast.LENGTH_SHORT).show()
                    }
                    is Output.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Output.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, response.data.message, Toast.LENGTH_SHORT).show()
                        if (!response.data.error!!) finish()
                    }
                }
            }
        }
    }

    private fun setupLoginPrompt() {
        binding.loginPromptTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startAnimation() {
        ObjectAnimator.ofFloat(binding.logoImageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleAnimator = ObjectAnimator.ofFloat(binding.signupTitleTextView, View.ALPHA, 1f).setDuration(100)
        val nameAnimator = ObjectAnimator.ofFloat(binding.nameInputLayout, View.ALPHA, 1f).setDuration(100)
        val emailAnimator = ObjectAnimator.ofFloat(binding.emailInputLayout, View.ALPHA, 1f).setDuration(100)
        val passwordAnimator = ObjectAnimator.ofFloat(binding.passwordInputLayout, View.ALPHA, 1f).setDuration(100)
        val signupAnimator = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(100)
        val loginAnimator = ObjectAnimator.ofFloat(binding.loginPromptTextView, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(titleAnimator, nameAnimator, emailAnimator, passwordAnimator, signupAnimator, loginAnimator)
            startDelay = 100
        }.start()
    }
}
