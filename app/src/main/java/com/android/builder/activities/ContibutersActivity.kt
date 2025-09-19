/*
 *  This file is part of Android-builder.
 *
 *  Android-builder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android-builder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Android-builder.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.android.builder.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import com.android.builder.R
import com.android.builder.adapters.ContributorsGridAdapter
import com.android.builder.app.EdgeToEdgeIDEActivity
import com.android.builder.databinding.ActivityContributorsBinding
import com.android.builder.utils.getConnectionInfo
import com.android.builder.viewmodel.ContributorsViewModel

/**
 * Contributors screen for Android-builder.
 */
class ContributorsActivity : EdgeToEdgeIDEActivity() {

    private var _binding: ActivityContributorsBinding? = null
    private val binding: ActivityContributorsBinding
        get() = checkNotNull(_binding) {
            "Activity has been destroyed"
        }

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

            githubContributors.apply {
                sectionTitle.setText(R.string.title_github_contributors)
            }

            // Rebranded: no Crowdin section, keep it general
            translationContributors.apply {
                sectionTitle.setText(R.string.title_translators)
            }

            noConnection.root.setText(R.string.msg_no_internet)
            loadingProgress.isVisible = false
        }

        viewModel._crowdinTranslators.observe(this) { translators ->
            binding.translationContributors.sectionItems.adapter = ContributorsGridAdapter(translators)
        }

        viewModel._githubContributors.observe(this) { githubContributors ->
            binding.githubContributors.sectionItems.adapter = ContributorsGridAdapter(githubContributors)
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
        binding.toolbar.apply {
            setPaddingRelative(
                paddingStart + insets.left,
                paddingTop,
                paddingEnd + insets.right,
                paddingBottom
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
