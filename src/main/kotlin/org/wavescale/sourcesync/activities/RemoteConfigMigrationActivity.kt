package org.wavescale.sourcesync.activities

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig

class RemoteConfigMigrationActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        ConfigConnectionFactory.getInstance().migrate()
        ConnectionConfig.getInstance().migrate()
    }
}