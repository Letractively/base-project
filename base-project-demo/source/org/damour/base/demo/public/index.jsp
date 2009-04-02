<%@page import="java.util.Locale"%>
<%@page import="java.util.Properties"%>
<%@page import="java.io.File"%>
<%@page import="org.damour.base.client.utils.StringUtils"%>
<%@page import="java.io.FileInputStream"%>

<%
  Locale effectiveLocale = request.getLocale(); 
  if (!StringUtils.isEmpty(request.getParameter("locale"))) {
    effectiveLocale = new Locale(request.getParameter("locale"));
  }

  Properties properties = new Properties();
  properties.load(new FileInputStream(getServletContext().getRealPath("base_messages/base_messages.properties")));
%>

<html>
	<head>
		<title>Demo</title>
		<meta name="gwt:property" content="locale=<%=effectiveLocale%>">
		<link rel="shortcut icon" href="favicon.ico">
	    <script language='javascript' src='soundmanager/soundmanager2.js' type="text/javascript"></script>
	</head>
	
	<body>
	<div id="sm2-container-wrapper"></div>
	<div id="soundmanager-debug" style="display: none;"></div>

	<div id="loading">
    		<div class="loading-indicator">
    			<img src="images/large-loading.gif" width="32" height="32"/>
    			<%= properties.getProperty("loading", "Loading...") %>
    			<br/>
    			<span id="loading-msg">
    				<%= properties.getProperty("pleaseWait", "Please Wait") %>
    			</span>
    		</div>
	</div>

	<div id="content"/>
	
	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
	</body>
	
	<script type="text/javascript">
		soundManager.flashVersion = 9;
		soundManager.url = 'soundmanager/'; // directory where SM2 .SWFs live
		soundManager.debugMode = false;
		soundManager.onload = function() {
			// SM2 has loaded - now you can create and play sounds!
			soundManager.onLoadExecuted = true;
		};
	</script>
	<script language='javascript' src='org.damour.base.demo.DemoApplication.nocache.js' type="text/javascript"></script>

</html>
