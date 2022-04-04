**Version 2.0**
- drop support for builds before IntelliJ IDEA 2021.1
- fixed issues with resource location due to trailing "/"
- removed usages of scheduled to be removed API

**Version 1.9**
* support for OS X 10.11
* fix support for passwordless ssh for sftp connections
* add support for passphrase keys for sftp connections
* add support for passwordless ssh for scp connections
* fix issues with known_hosts file on Windows machines
* fix issue with hidden files and directories not showing through private key file chooser
* fix issue with private key file chooser forcing you to select the public key instead of the private one
* fix issues with configuration and target window not getting on top of the IDE
* changed the configuration window, the add target and module selection to centered dialogs
* ***All previous configurations will be lost***

**Version 1.8**
* Add support for IntelliJ IDEA 15.x
* Add support for PyCharm 5.x
* Fix upload of files over FTPS connections using explicit TLS security
* Set the configuration window to be always on top

**Version 1.5**
* added shortcut support
* fixed some NPE when no default file was selected.
* fixed exception due to context switching when using shortcuts.

**Version 1.4**
* added a file sync manager, which forces sourcesync to reuse existing opened connections during the command.
* Sync selected jobs no longer has problems with "Allow simultaneous sync jobs" option.
* build with java 1.6 support

**Version 1.3**
* force OK button to save connection preferences
* Fix a few visual bugs (the Allow ... number connections is not visible until resize)
* Added Sync selected and change files into the Changes View Popup-Menu
* Added "this" changelog 3:)

**Version 1.2**
* add option to limit the number of upload threads
* fix support for PyCharm



