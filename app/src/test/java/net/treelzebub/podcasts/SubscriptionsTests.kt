package net.treelzebub.podcasts

import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SubscriptionsTests {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Inject
    lateinit var subsVM: SubscriptionsViewModel

    @Before
    fun init() {
        rule.inject()
    }

    @Test
    fun `subscriptions do the thing`() {
        subsVM.addRssFeed("", {})
    }
}