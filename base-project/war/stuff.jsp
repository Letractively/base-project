
<%@page import="org.damour.base.client.exceptions.LoginException"%>
<%@page import="org.damour.base.server.gwt.SerializabilityUtil"%>
<%@page import="java.util.zip.CRC32"%>
<%@page import="org.damour.base.server.Logger"%>
<%@page import="org.damour.base.server.BaseSystem"%>
<%@page import="java.util.Properties"%>
<%

out.println(BaseSystem.getBaseClassLoader().getClass().getName());
out.println(getClass().getClassLoader().getClass().getName());

Properties override = new Properties();
override.load(BaseSystem.getBaseClassLoader().getResourceAsStream("settings_override.properties"));
Logger.dump(override);

override.load(getClass().getClassLoader().getResourceAsStream("settings_override.properties"));
Logger.dump(override);


%>

