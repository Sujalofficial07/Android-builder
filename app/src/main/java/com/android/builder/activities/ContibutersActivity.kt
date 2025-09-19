package com.android.builder.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import com.android.builder.R
import com.android.builder.adapters.ContributorsGridAdapter
import com.android.builder.app.EdgeToEdgeActivity
import com.android.builder.databinding.ActivityContributorsBinding
import com.android.builder.utils.getConnectionInfo
import com.android.builder.viewmodel.ContributorsViewModel

/**
 * Displays project contributors and translators.
 */
class ContributorsActivity : EdgeToEdgeActivity() {

    private var _binding: ActivityContributorsBinding? = null
    private val binding: ActivityContributorsBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }

    private val viewModel by viewModels<ContributorsViewModel>()

    override fun bindLayout(): View {
        _binding = ActivityContributorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar!!.setTitle(R.string.title_contributors)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

            githubContributors.sectionTitle.setText(R.string.title_github_contributors)
            translationContributors.sectionTitle.setText(R.string.title_crowdin_translators)

            noConnection.root.setText(R.string.msg_no_internet)
            loadingProgress.isVisible = false
        }

        viewModel._crowdinTranslators.observe(this) { translators ->
            binding.translationContributors.sectionItems.adapter =
                ContributorsGridAdapter(translators)
        }

        viewModel._githubContributors.observe(this) { githubContributors ->
            binding.githubContributors.sectionItems.adapter =
                ContributorsGridAdapter(githubContributors)
        }

        val connectionInfo = getConnectionInfo(this)
        binding.apply {
            noConnection.root.isVisible = !connectionInfo.isConnected
            githubContributorsCard.isVisible = connectionInfo.isConnected
            translationContributorsCard.isVisible = connectionInfo.isConnected

            if (connectionInfo.isConnected) {
                viewModel.observeLoadingState(this@ContributorsActivity) { isLoading ->
                    binding.loadingProgress.isVisible = isLoading
                }
                viewModel.fetchAll()
            }
        }
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        super.onApplySystemBarInsets(insets)
        binding.toolbar.setPaddingRelative(
            binding.toolbar.paddingStart + insets.left,
            binding.toolbar.paddingTop,
            binding.toolbar.paddingEnd + insets.right,
            binding.toolbar.paddingBottom
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
