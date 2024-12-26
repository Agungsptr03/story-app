package com.dicoding.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.auth.LoginActivity
import com.dicoding.storyapp.data.UserConfig
import com.dicoding.storyapp.data.StoryAdapter
import com.dicoding.storyapp.data.dataStore
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.model.ViewModelFactory
import com.dicoding.storyapp.model.ViewModelMain
import com.dicoding.storyapp.repo.Output
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<ViewModelMain> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private val storyAdapter = StoryAdapter()
    private lateinit var userConfig: UserConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userConfig = UserConfig.getInstance(dataStore)

        lifecycleScope.launch {
            userConfig.getUserSession().collect { user ->
                if (!user.isLogin) {
                    redirectToLogin()
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.show()
        setSupportActionBar(binding.toolbar)

        initializeView()
        initializeActions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                lifecycleScope.launch {
                    userConfig.clearSession()
                }
                Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
                redirectToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeView() {
        binding.rvStory.apply {
            adapter = storyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.getAllStory().observe(this) { result ->
            when (result) {
                is Output.Error -> {
                    binding.linearProgressBar.visibility = View.GONE
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
                is Output.Loading -> {
                    binding.linearProgressBar.visibility = View.VISIBLE
                }
                is Output.Success -> {
                    binding.linearProgressBar.visibility = View.GONE
                    if (result.data.error == true) {
                        Toast.makeText(this, result.data.message, Toast.LENGTH_SHORT).show()
                    } else {
                        storyAdapter.submitList(result.data.listStory)
                    }
                }
            }
        }
    }

    private fun initializeActions() {
        binding.buttonAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun redirectToLogin() {
        Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }
}
