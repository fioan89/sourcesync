# Sourcesync Plugin

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/fioan89)
[![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=W3SKYN2L99GMQ)
[![Sourcesync Plugin Build](https://github.com/fioan89/sourcesync/actions/workflows/build.yml/badge.svg)](https://github.com/fioan89/sourcesync/actions/workflows/build.yml)

<!-- Plugin description -->
A fast, one-way file synchronization tool for your projects.

Local changes can be transferred on the remote infrastructure using the **SCP** or **SSH** protocols.
Other notable features include:

* **password** and **key pair** authentication
* support for **key pairs** with and without *passphrases*
* timestamp preserving
* file filtering
* user-friendly UI with support for IntelliJ's **New UI** feature
* synchronization of single or multi-selection files as well as VCS changes

<!-- Plugin description end -->

## How to install

Sourcesync plugin can be found at http://plugins.jetbrains.com/plugin/7374?pr=idea_ce

Details about how to install a plugin from JetBrains plugin repository can be found at:
http://www.jetbrains.com/idea/webhelp/installing-updating-and-uninstalling-repository-plugins.html

## Getting started

1. Launch the IDE and install the plugin.
2. Restart the IDE.
3. Configure one or more remote sync configurations to target the remote environment where
   you would like to transfer your changes. To create or edit your sync configurations you would have to:
   1. locate the **Sourcesync Configurations** combo box in the IntelliJ's main toolbar panel.
   ![sync_selector_on_main_toolbar](https://github.com/fioan89/sourcesync/assets/1479167/930d2f7c-7a2b-40fb-bed3-2f67852d1697)

   2. Click the combo box drop down icon and then hit the **Edit Sourcesync Configurations** button.
   3. In the new **Sourcesync Remote Configurations** dialog, click **+** on the toolbar or press `Alt + Insert`.
      The list shows the **SCP&SSH** templates available for configuration.
      ![sync_configuration_dialog](https://github.com/fioan89/sourcesync/assets/1479167/9dddbcb3-44ab-4f71-a702-34257f11db9f)

   4. Specify the sync configuration name in the **Name** field. This name will be shown in the list of the available sync configurations.
   5. Fill in the connection details like host, username and password or certificate, depending on the desired authentication type.
   6. Specify the remote base path (excluding the project name) where your files will be transferred.
   7. Apply the changes and close the dialog.
4. From the **Sourcesync Configurations** combo box select a sync configuration as primary target
![sync_configuration_selector](https://github.com/fioan89/sourcesync/assets/1479167/4bda6dda-3706-441b-9d35-e42b04e24b10)

6. Select one or more files, right click, and in the context menu select **Sync selected files to target name**. Alternatively, press `Ctrl + Shift + F2`
7. Alternatively only the file under edit (focused editor) could be transferred by right click in the editor, and in the context
   menu select **Sync this file to target name** or just press `Ctrl + Shift + F1`
7. A third option is to sync all **Git** changed files by right click in the editor or **Project** toolbar, and then select **Sync changed files to target name**. Alternatively, press `Ctrl + Shift + F3`

## FAQ

1. Where are my files transferred?

   Files are transferred on the remote host selected as the main target in the **Sourcesync Configurations** combo box. **Sourcesync** keeps the remote project structure similar
   to the local one, except the project's base path which will be replaced on the remote target with the **Workspace** configuration value.

   For example, say the **Workspace** remote path is configured to `/home/ifaur/workspace`, and your local project is placed in `C:\\Users\\ifaur\\IdeaProjects\\my-awesome-project`
   then a local file placed in `src\\main\\kotlin\\com\\mypackage\\MyFile.kt` will be transferred to `/home/ifaur/workspace/my-awesome-project/src/main/kotlin/com/mypackage/MyFile.kt`

2. Where are the sync configurations stored?

   **Sourcesync** keeps sync configurations per project, and it stores its data in the project's `.idea/sourcesync.xml`

3. Where are passwords and certificate passphrases stored?

   **Sourcesync** makes use of IntelliJ's own credential store framework to securely save sensitive data. **IntelliJ IDEA** does not have its own password store. It uses either the native password management system or KeePass.

4. There are errors when trying to load or persist sync configurations. What do I do next?

   You can simply remove `.idea/sourcesync.xml` from the project's folder and restart IntelliJ. You will have to reconfigure your sync targets.

## License

**Sourcesync** is licensed under MIT License. Please take a look at the *LICENSE* file for more informations.

## Issues

Bugs can be reported at https://github.com/fioan89/sourcesync/issues
