sourcesync
==========
Source Synchronizer is a powerful plugin for performing one-way file synchronization for your Intellij Idea and Pycharm projects. It synchronizes the remote target so that it will match your local project. It features **SFTP**, **SCP**, **FTP** and **FTPS** protocol support.

Installation
------------  
sourcesync pluging can be found at:
http://plugins.jetbrains.com/plugin/7374?pr=idea_ce

Details about how to install a plugin from JetBrains plugin repository can be found at: 
http://www.jetbrains.com/idea/webhelp/installing-updating-and-uninstalling-repository-plugins.html

How to use it
-------------
**sourcesync** is pretty easy to use. First of all you need to configure a remote connection so that the plugin will know where to sync the files. To do so please access the *Tools* menu, and then **Source Sync** like in the below picture:  

![Tools --> Source Sync](https://raw.github.com/fioan89/sourcesync/master/resources/sourcesync/tools_menu.png)  

Hit Add to create connections or remove them:
![Add / Remove connections](https://raw.github.com/fioan89/sourcesync/master/resources/sourcesync/new_connection.png)  

After you are done creating/editing the connection, hit *Apply* in order to save the configurations and then *OK* to exit the window.  
Now - after you created the configuration you need to specify which connection you want to use for the current project. For that matter please select you project in your *Project View* (Alt + 1 shortcut to bring the view forward) and then right click on the desired project and select *Project Connection Configuration*  

![Right click on the desired project --> Project Connection Configuration](https://raw.github.com/fioan89/sourcesync/master/resources/sourcesync/select_connection.png)  

Now that you have done this step you are up and ready to sync files. Just right click on the project, module, or even on a opened file. You will be presented with three options like in the below screenshot:  

![Sync files](https://raw.github.com/fioan89/sourcesync/master/resources/sourcesync/sync_files.png)  

Donations
=========
Any support is graciously accepted :)  

https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=E6F2NPVCNZK4C&lc=US&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted  

A few notes
-----------  
**Sync this file to remote target** is only present on opened file and it will sync just that file to the remote machine.  

**Sync changed files to remote target** will upload version tracked files that are changed but not yet committed.  

**Sync selected files to remote target** this one is pretty straight forward. It will upload just the selected files.  

* If **SCP** protocol is used, please take note that this type of connection can only sync the file, it cannot create the entire directory tree where the file will be saved. Thus you need to have the tree created. This is not a bug of sourcesync. SCP is not a file protocol like **FTP**. It's only purpose is to transfer files.  
* For **FTP** and **FTPS** preserving timestamp is not yet support. This is a work in progress.  


License
-------
**sourcesync** is licensed under MIT License. Please take a look at the *LICENSE* file for more informations.  

Issues
------
Bugs can be reported at:  
https://github.com/fioan89/sourcesync/issues

Contact
-------
You can find me at the follwoing email address:
fioan89 at gmail dot com