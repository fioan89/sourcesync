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

<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
<input type="hidden" name="cmd" value="_s-xclick">
<input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----
">
<input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
<img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
</form>

  
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