# Dec 16, 2009 #

  * Plenty of fixes / enhancements have gone in since June (check [updates](http://code.google.com/p/base-project/updates/list))
  * GWT 2.0 support has been added

# June 3, 2009 #

  * Quick fix for use of java.sql call only available with Java 1.6, found 1.5 workaround

# April 15, 2009 #

  * Moved settings/logging flags out of HibernateUtil and into BaseSystem
  * 'reset' should be performed on BaseSystem now, as it will reset HibernateUtil
  * Implemented year selectors in date picker (simple extension of GWT version)

# April 14, 2009 #

  * Removing thepar datepicker in favor of GWT 1.6 datepicker

# April 13, 2009 #

  * Upgrade to GWT 1.6.4, project restructuring
  * GWT serialization hacks for godaddy re-implemented
  * GWT Exception/Throwable serialization is now possible on godaddy

# April 7, 2009 #

  * Created 'BaseApplication', extend this and you get much for free

# April 6, 2009 #

  * Localization improvements, updated for caching, bundle filtering, bundle merging
  * Settings improvement
  * Hibernate session put in ThreadLocal
  * Various bugs fixed in CommentsWidget
  * Uploads to persist in database (BLOBS), PHP hack killed

# April 1, 2009 #

  * Added file-based logger system (improvement over email system)