<%@page import="java.util.Locale"%>
<%@page import="java.util.Properties"%>
<%@page import="java.io.File"%>
<%@page import="org.damour.base.client.utils.StringUtils"%>
<%@page import="java.io.FileInputStream"%>

<%
  String applicationName = "baseproject";

  // set the BaseSystem classloader for others to enjoy
  // it is likely:  org.apache.jasper.servlet.JasperLoader 
  // compared to :  org.apache.catalina.loader.WebappClassLoader
  BaseSystem.setBaseClassLoader(getClass().getClassLoader());

  Locale effectiveLocale = request.getLocale(); 
  if (!StringUtils.isEmpty(request.getParameter("locale"))) {
    effectiveLocale = new Locale(request.getParameter("locale"));
  }

  Properties properties = new Properties();
  properties.load(new FileInputStream(getServletContext().getRealPath(applicationName + "/messages/messages.properties")));
%>


<%@page import="org.damour.base.server.BaseSystem"%>
<%@page import="org.damour.base.server.Logger"%><html>
	<head>
		<title><%=applicationName%></title>
		<meta name="gwt:property" content="locale=<%=effectiveLocale%>">
		<link rel="shortcut icon" href="favicon.ico">

		<style type="text/css">
#loading {
  background: white;
  position: absolute;
  left: 48%;
  top: 45%;
  margin-left: -45px;
  padding: 2px;
  z-index: 20001;
  height: auto;
  border: 1px solid #ccc;
}

#loading a {
  background: white;
  color: #225588;
}

#loading .loading-indicator {
  background: white;
  color: #444;
  font: bold 13px tahoma, arial, helvetica;
  padding: 10px;
  margin: 0;
  height: auto;
}

#loading .loading-indicator img {
  background: white;
  margin-right:8px;
  float:left;
  vertical-align:top;
}

#loading-msg {
  background: white;
  font: normal 10px arial, tahoma, sans-serif;
}
		</style>


	    <script language='javascript' src='<%=applicationName%>/soundmanager/soundmanager2.js' type="text/javascript"></script>
	</head>
	
	<body>
	<div id="sm2-container-wrapper"></div>
	<div id="soundmanager-debug" style="display: none;"></div>

	<div id="loading">
    		<div class="loading-indicator">
    			<img src="<%=applicationName%>/images/large-loading.gif" width="32" height="32"/>
    			<%= properties.getProperty("loading", "Loading...") %>
    			<br/>
    			<span id="loading-msg">
    				<%= properties.getProperty("pleaseWait", "Please Wait") %>
    			</span>
    		</div>
	</div>

	<div id="content"></div>
	
	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
	</body>
	
	<script type="text/javascript">
		soundManager.flashVersion = 9;
		soundManager.url = '<%=applicationName%>/soundmanager/'; // directory where SM2 .SWFs live
		soundManager.debugMode = false;
		soundManager.onload = function() {
			// SM2 has loaded - now you can create and play sounds!
			soundManager.onLoadExecuted = true;
		};
	</script>
	<script language='javascript' src='<%=applicationName%>/<%=applicationName%>.nocache.js' type="text/javascript"></script>

</html>
