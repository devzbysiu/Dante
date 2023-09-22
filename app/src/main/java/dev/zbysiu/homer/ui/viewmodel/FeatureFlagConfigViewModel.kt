package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.zbysiu.homer.flagging.FeatureFlag
import dev.zbysiu.homer.flagging.FeatureFlagItem
import dev.zbysiu.homer.flagging.FeatureFlagging
import javax.inject.Inject

class FeatureFlagConfigViewModel @Inject constructor(
    private val featureFlagging: FeatureFlagging
) : BaseViewModel() {

    private val featureFlags = MutableLiveData<List<FeatureFlagItem>>()

    init {
        loadFeatureFlags()
    }

    fun getFeatureFlagItems(): LiveData<List<FeatureFlagItem>> = featureFlags

    fun updateFeatureFlag(name: String, value: Boolean) {
        featureFlagging.updateFlag(name, value)
    }

    private fun loadFeatureFlags() {
        val items = FeatureFlag.activeFlags().map { f ->
            FeatureFlagItem(f.key, f.displayName, featureFlagging[f])
        }
        featureFlags.postValue(items)
    }
}