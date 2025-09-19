package com.android.builder.activities

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.updatePaddingRelative
import com.android.builder.BuildConfig
import com.android.builder.R
import com.android.builder.adapters.SimpleIconTitleDescriptionAdapter
import com.android.builder.app.BaseApplication
import com.android.builder.app.EdgeToEdgeActivity
import com.android.builder.databinding.ActivityAboutBinding
import com.android.builder.models.IconTitleDescriptionItem
import com.android.builder.models.SimpleIconTitleDescriptionItem
import com.android.builder.utils.BuildInfoUtils
import com.android.builder.utils.flashSuccess

/**
 * About screen for Android-builder.
 */
class AboutActivity : EdgeToEdgeActivity() {

    private var _binding: ActivityAboutBinding? = null
    private val binding: ActivityAboutBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }

    override fun bindLayout(): View {
        _binding = ActivityAboutBinding.inflate(layoutInflater)
        return _binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setTitle(R.string.about)
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

            aboutHeader.apply {
                ideVersion.text = createVersionText()
                ideVersion.setOnClickListener {
                    flashSuccess(R.string.copied)
                }
            }

            socials.apply {
                sectionTitle.setText(R.string.title_socials)
                sectionItems.adapter =
                    AboutSocialItemsAdapter(createSocialItems(), ::handleActionClick)
            }

            misc.apply {
                sectionTitle.setText(R.string.title_misc)
                sectionItems.adapter =
                    AboutSocialItemsAdapter(createMiscItems(), ::handleActionClick)
            }
        }
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        binding.toolbar.updatePaddingRelative(
            start = binding.toolbar.paddingStart + insets.left,
            end = binding.toolbar.paddingEnd + insets.right
        )
    }

    private fun handleActionClick(action: SimpleIconTitleDescriptionItem) {
        when (action.id) {
            ACTION_WEBSITE -> app.openWebsite()
            ACTION_EMAIL -> app.emailUs()
            ACTION_TG_GROUP -> app.openTelegramGroup()
            ACTION_TG_CHANNEL -> app.openTelegramChannel()
            ACTION_CONTRIBUTE -> app.openUrl(BaseApplication.CONTRIBUTOR_GUIDE_URL)
            ACTION_CONTRIBUTORS -> startActivity(Intent(this, ContributorsActivity::class.java))
        }
    }

    private fun createSocialItems(): List<IconTitleDescriptionItem> {
        return listOf(
            createSimpleItem(this, ACTION_WEBSITE, R.drawable.ic_website, R.string.about_option_website, BuildInfoUtils.PROJECT_SITE),
            createSimpleItem(this, ACTION_EMAIL, R.drawable.ic_email, R.string.about_option_email, BaseApplication.EMAIL),
            createSimpleItem(this, ACTION_TG_GROUP, R.drawable.ic_telegram, R.string.discussions_on_telegram, BaseApplication.TELEGRAM_GROUP_URL),
            createSimpleItem(this, ACTION_TG_CHANNEL, R.drawable.ic_telegram, R.string.official_tg_channel, BaseApplication.TELEGRAM_CHANNEL_URL)
        )
    }

    private fun createMiscItems(): List<IconTitleDescriptionItem> {
        return listOf(
            SimpleIconTitleDescriptionItem.create(this, ACTION_CONTRIBUTE, R.drawable.ic_code, R.string.title_contribute, R.string.summary_contribute),
            SimpleIconTitleDescriptionItem.create(this, ACTION_CONTRIBUTORS, R.drawable.ic_heart_outline, R.string.title_contributors, R.string.summary_contributors)
        )
    }

    private fun createSimpleItem(
        context: android.content.Context,
        id: Int,
        @DrawableRes icon: Int,
        @StringRes title: Int,
        description: CharSequence
    ): SimpleIconTitleDescriptionItem {
        return SimpleIconTitleDescriptionItem(
            id,
            ContextCompat.getDrawable(context, icon),
            ContextCompat.getString(context, title),
            description
        )
    }

    private fun createVersionText(): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("v${BuildConfig.VERSION_NAME} ")

        val colorPositive = ContextCompat.getColor(this, R.color.color_success)
        val colorNegative = ContextCompat.getColor(this, R.color.color_error)

        appendBuildType(builder, colorPositive, colorNegative)
        return builder
    }

    private fun appendBuildType(
        builder: SpannableStringBuilder,
        @ColorInt colorPositive: Int,
        @ColorInt colorNegative: Int
    ) {
        val color = if (BuildConfig.DEBUG) colorNegative else colorPositive
        builder.append("(")
        builder.append(BuildConfig.BUILD_TYPE, ForegroundColorSpan(color), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(")")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private var id = 0
        private val ACTION_WEBSITE = id++
        private val ACTION_EMAIL = id++
        private val ACTION_TG_CHANNEL = id++
        private val ACTION_TG_GROUP = id++
        private val ACTION_CONTRIBUTE = id++
        private val ACTION_CONTRIBUTORS = id++
    }
}
