
<%@page import="com.mysql.jdbc.ServerPreparedStatement"%>
<%@page import="java.io.Writer"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="com.mysql.jdbc.PreparedStatement"%>
<%@page import="java.util.Properties"%>
<%@page import="com.mysql.jdbc.Blob"%>
<%@page import="com.mysql.jdbc.Connection"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="org.damour.base.client.Logger"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="org.damour.base.client.objects.CategoryMembership"%>
<%@page import="org.damour.base.client.objects.Category"%>
<%@page import="org.damour.base.client.objects.PhotoThumbnail"%>
<%@page import="org.damour.base.client.objects.Photo"%>
<%@page import="org.damour.base.client.objects.Permission"%>
<%@page import="org.damour.base.client.objects.Folder"%>
<%@page import="org.damour.base.client.objects.FileUserAdvisory"%>
<%@page import="org.damour.base.client.objects.FileUserRating"%>
<%@page import="org.damour.base.client.objects.FileComment"%>
<%@page import="org.damour.base.client.objects.File"%>
<%@page import="org.damour.base.client.objects.FileData"%>
<%@page import="org.damour.base.client.objects.PendingGroupMembership"%>
<%@page import="org.damour.base.client.objects.GroupMembership"%>
<%@page import="org.damour.base.client.objects.UserGroup"%>
<%@page import="org.damour.base.client.objects.User"%>
<%@page import="org.damour.base.server.hibernate.helpers.UserHelper"%>
<%@page import="org.damour.base.server.hibernate.HibernateUtil3"%>
<%@page import="java.io.FileOutputStream"%>
<% 

HibernateUtil3.getInstance().resetMappingDocument();
HibernateUtil3.getInstance().generateHibernateMapping(User.class);
HibernateUtil3.getInstance().generateHibernateMapping(UserGroup.class);
HibernateUtil3.getInstance().generateHibernateMapping(GroupMembership.class);
HibernateUtil3.getInstance().generateHibernateMapping(PendingGroupMembership.class);
HibernateUtil3.getInstance().generateHibernateMapping(File.class);
HibernateUtil3.getInstance().generateHibernateMapping(FileData.class);
HibernateUtil3.getInstance().generateHibernateMapping(FileComment.class);
HibernateUtil3.getInstance().generateHibernateMapping(FileUserRating.class);
HibernateUtil3.getInstance().generateHibernateMapping(FileUserAdvisory.class);
HibernateUtil3.getInstance().generateHibernateMapping(Folder.class);
HibernateUtil3.getInstance().generateHibernateMapping(Permission.class);
HibernateUtil3.getInstance().generateHibernateMapping(Photo.class);
HibernateUtil3.getInstance().generateHibernateMapping(PhotoThumbnail.class);
HibernateUtil3.getInstance().generateHibernateMapping(Category.class);
HibernateUtil3.getInstance().generateHibernateMapping(CategoryMembership.class);
HibernateUtil3.getInstance().resetHibernate();

out.println(HibernateUtil3.getInstance().getHbm2ddlMode());
out.println(HibernateUtil3.getInstance().getConnectString());
HibernateUtil3.getInstance().getSessionFactory();


byte data[] = new byte[1000000];
for (int i=0;i<data.length;i++) {
  data[i] = 'z';
}
ServerPreparedStatement ps = null;
try {
  Properties dbprops = new Properties();
  dbprops.setProperty("user", HibernateUtil3.getInstance().getUsername());
  dbprops.setProperty("password", HibernateUtil3.getInstance().getPassword());
  
  Connection conn = (Connection)DriverManager.getConnection(HibernateUtil3.getInstance().getConnectString(), dbprops);
  String insert_data = "insert into " + FileData.class.getSimpleName().toLowerCase() + " (permissibleObject, data) values (?, ?)";
  conn.setAutoCommit(false);
  conn.setBlobSendChunkSize("32768");
  ps = (ServerPreparedStatement)conn.serverPrepareStatement(insert_data);
  ps.setBigDecimal(1, new BigDecimal(34));
  ps.setBinaryStream(2, new ByteArrayInputStream(data));
  ps.executeUpdate();
  conn.commit();
} catch (Throwable t) {
	out.println(t.getMessage());
} finally {
  try {
    ps.close();
  } catch (Throwable t) {
  }
}




%>

