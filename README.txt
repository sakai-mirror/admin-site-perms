This is the Admin Site Role Permissions updater, it is an admin tool for Sakai
Author: Aaron Zeckoski (azeckoski@gmail.com)
Description: This will add or remove the selected permissions in all sites of the types selected and all roles selected, 
you must select at least one permission, one site type, and one role to do an update, 
this can be somewhat slow when updating a very large number of sites
This is a super user/admin tool only

To use this project, you should copy this entire folder and all the files
into your sakai source directory. If you do not know what that is you may
want to check this website out:
	http://bugs.sakaiproject.org/confluence/display/BOOT

Now that you have copied this over to your sakai source directory 
(or maybe it was already there), you should run the following command 
from within the root directory of your new project to download all the 
dependencies using maven and compile the code:
	MAVEN 1.0.2: maven bld
	MAVEN 2.x: mvn install

Now you can import the project into eclipse (using the current location)
or continue using it in eclipse if you created it in your sakai source tree.

Frequently Asked Questions:
Q: I have missing dependencies in the project I just created in Eclipse, what do I do?
A: You need to run 'maven bld' or 'mvn install' in the project root directory first 
to download the dependencies using maven. Then do a refresh, clean, and build in Eclipse 
and the errors will go away.

Q: How do I create multiple eclipse projects like some Sakai tools do?
A: This plugin does not support that practice. You are welcome to split things
apart if you like though I do not recommend that. You can use the maven 2 eclipse
x plugin to do so by running mvn eclipse:eclipse on your project after it is generated.

Comments or questions about the plugin should go to Aaron Zeckoski (aaronz@vt.edu)