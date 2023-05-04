package org.wavescale.sourcesync.ui

import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.popup.PopupFactoryImpl
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.configurations.SyncConfigurationType

class AddSyncRemoteConfigurationPopUp(configurationTypes: List<SyncConfigurationType>, private val onChosenCallback: (SyncConfigurationType) -> Unit) : BaseListPopupStep<SyncConfigurationType>(
    SourcesyncBundle.message("add.new.sync.configuration.action.name"),
    configurationTypes
) {

    override fun getTextFor(value: SyncConfigurationType) = value.prettyName

    override fun onChosen(selectedValue: SyncConfigurationType?, finalChoice: Boolean): PopupStep<*>? {
        if (selectedValue != null) {
            onChosenCallback(selectedValue)
        }
        return FINAL_CHOICE
    }

    companion object {
        fun create(configurationTypes: List<SyncConfigurationType>, onChosenCallback: (SyncConfigurationType) -> Unit) =
            PopupFactoryImpl.getInstance().createListPopup(AddSyncRemoteConfigurationPopUp(configurationTypes, onChosenCallback))
    }
}