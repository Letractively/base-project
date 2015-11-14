# Introduction #

This document will outline and describe the steps needed to get the base-project & base-project-demo up and running in debug/development mode.

## Prerequisites ##
  * Java 5 or higher http://java.sun.com
  * Eclipse http://www.eclipse.org
  * Subclipse http://subclipse.tigris.org/
  * MySQL http://dev.mysql.com/downloads/mysql/5.1.html
  * MySQL GUI Tools http://dev.mysql.com/downloads/gui-tools/5.0.html

## Installation Steps ##
  1. Download MySQL and perform a typical installation
  1. Download mysql-gui-tools
  1. Using MySQL Administrator (from mysql-gui-tools) create a new catalog for the base-project
  1. Using the Administrator, create a base-project user for mysql and give the user all available privileges (Schema Privileges tab under User Administration)
  1. Download Eclipse (for Java) and run it, create a new workspace if desired
  1. Install the subversion plugin for Eclipse
  1. Checkout the base-project SVN project:
    1. New->Project->SVN->Checkout Projects from SVN
    1. Create a new repository location (http://base-project.googlecode.com/svn/trunk/)
    1. Checkout base-project & base-project-demo (highlight both and hit finish)
  1. After the checkout, Eclipse will initialize and compile the projects, there should be no errors in either of the projects
  1. Edit base-project-demo/source/settings\_override.properties
    1. Enter the mysql base-project username/password you created
    1. Enter the mysql jdbc connect string, default is jdbc:mysql://localhost/baseproject?autoReconnect=true focus on the baseproject part if you created a different schema
    1. If you would like table prefixes to be added to all tables that are created (for example, if you are reusing an existing database/catalog and want to be sure table names do not collide), then edit the tablePrefix setting
  1. Find demo.launch in base-project-demo
  1. Right-click demo.launch and select Run As->demo
  1. The application will create all of the database tables, and will update them if their pojos changes
  1. Congratulations, the application is up and running, you can now create a new user or login as the administrator (admin/p@$$w0rd)