<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Source Synchronizer Changelog

## Unreleased

### Added

- support for experimental new UI

### Changed

- simplified and improved the error reporting

### Fixed

- path location issues when uploading files with scp from Windows to Unix

## 2.0.2 - 2022-12-07

### Added

- support for latest OpenSSH private key format
- support for latest IntelliJ 2022.3

## 2.0.1

### Fixed

- upload issues when using SFTP from local Windows to remote Linux
- behavior for SSH keys component, now they properly enable or disable if users want to authenticate with SSH keys
- plugin icon shown in marketplace

### Changed

- project's base location label from "Root path" to "Workspace base path". This is a breaking change, users
  will have to reconfigure the connections again.

## 2.0.0

### Added

- support for semver
- build with Kotlin&Gradle
- support for Java 11
- new menu icons

### Changed

- migrated UI layout from JGoodie's `FormLayout` to java.awt + IntelliJ layouts
- migrated most of the dialogs and panels to programmatic code. Reduces the UI Designer footprint
- connection configuration and selection dialogs can now be done from a single place, a combo
  box placed in the toolbar. Similar to run/edit configurations.

### Removed

- support for builds before IntelliJ IDEA 2021.1
- removed usages of scheduled to be removed API
- support for builds with Java 1.6
- context menus to create and select connection configurations

### Fixed

- fixed issues with resource location due to trailing "/"
- remove all project associations when there is no Sourcesync connection available

## 1.9.0

### Added

- support for OS X 10.11
- support for passphrase keys for sftp connections
- support for passwordless ssh for scp connections

### Changed

- the configuration window, the add target and module selection to centered dialogs
- `All previous configurations will be lost`

### Fixed

- support for passwordless ssh for sftp connections
- issues with known_hosts file on Windows machines
- issue with hidden files and directories not showing through private key file chooser
- issue with private key file chooser forcing you to select the public key instead of the private one
- issues with configuration and target window not getting on top of the IDE

## 1.8.0

### Added

- support for IntelliJ IDEA 15.x
- support for PyCharm 5.x

### Changed

- Set the configuration window to be always on top

### Fixed

- upload of files over FTPS connections using explicit TLS security

## 1.5.0

### Added

- support for shortcuts

### Fixed

- NPE when no default file was selected.
- exception due to context switching when using shortcuts.

## 1.4.0

### Added

- a file sync manager, which forces `Sourcesync` to reuse existing opened connections during the command.

### Changed

- build with java 1.6 support

### Fixed

- Sync selected jobs no longer has problems with "Allow simultaneous sync jobs" option.

## 1.3.0

### Added

- sync selected and changed files into the Changes View Popup-Menu

### Fixed

- force OK button to save connection preferences
- a few visual bugs (the `Allow ... number of connections` was not visible until resize)

## 1.2.0

### Added

- option to limit the number of upload threads

### Fixed

- support for `PyCharm`
