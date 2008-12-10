<?php

$DEBUG = true;
$fileId = $_GET['file'];
$height = $_GET['height'];
$width = $_GET['width'];
$force = $_GET['force'];
$rotate = $_GET['rotate'];
$preview = $_GET['preview'];
$tablePrefix = "";
$columnPrefix = "";

if ($DEBUG) {  
  $user = "mdamour1976";
  $pwhash = md5('t@k30ff');
  // Database connection variables
  $dbServer = "localhost";
  $dbDatabase = "agedicator";
  $dbUser = "root";
  $dbPass = "t@k30ff";
} else {
  $user = $_COOKIE['user'];
  $pwhash = $_COOKIE['auth'];
  $dbServer = "%dbserver%";
  $dbDatabase = "%dbname%";
  $dbUser = "%username%";
  $dbPass = "%password%";
}
$sConn = mysql_connect($dbServer, $dbUser, $dbPass)
  or die("Couldn't connect to database server");
$dConn = mysql_select_db($dbDatabase, $sConn)
  or die("Couldn't connect to database $dbDatabase");

$userQuery = "SELECT " . $columnPrefix . "passwordHash as passwordHash from " . $tablePrefix . "user where " . $columnPrefix . "username = '" . $user . "'";
$userResult = mysql_query($userQuery) or  die ("User is not authorized to perform this action.");
if(mysql_num_rows($userResult ) > 0) {
  $dbpwhash = @mysql_result($userResult, mysql_num_rows($userResult)-1, "passwordHash");
  if (strcasecmp($dbpwhash, $pwhash) != 0) {
    die ("User is not authenticated.");
  }
} else {
  die ("User is not authenticated.");
}

// check if the user has permission to write to the FileData
if (strcmp($_SERVER['HTTPS'], '') != 0) {
  $protocol = 'https://';
} else {
  $protocol = 'http://';
}
if ($DEBUG) {
  $port = '8888';
} else {
  $port = $_SERVER['SERVER_PORT'];
}
$server = $_SERVER['SERVER_NAME'];
$url = $protocol . $server . ':' . $port . '/servlet/BaseService?method=doesUserHavePermission&user=' . $user . '&permission=WRITE&permissibleObjectId=' . $fileId;

$urlresult = file_get_contents($url);

if (strcmp($urlresult,'<result>true</result>') != 0) {
  die ("User is not authorized to perform this action.");
}

function manipulateImage($src_img, $max_w, $max_h, $force, $rotate) {
  $width=imageSX($src_img);
  $height=imageSY($src_img);

  if (strcasecmp($force,"true") == 0) {
    $new_w = $max_w;
    $new_h = $max_h;
  } else {
    if ($width > $max_w || $height > $max_h) {
      $scale = min($max_w/$width, $max_h/$height);
      $new_w = floor($scale*$width);
      $new_h = floor($scale*$height);
    } else {
      $new_w = $width;
      $new_h = $height;
    }
  }

  $dst_img=imagecreatetruecolor($new_w,$new_h);
  imagecopyresized($dst_img,$src_img,0,0,0,0,$new_w, $new_h, $width, $height);   

  if (!empty($rotate)) {
    $dst_img = imagerotate($dst_img, $rotate, 0xffffff);
  }
  
  return $dst_img;
}

if(!is_numeric($fileId))
  die("Invalid blobId specified");

$dbQuery = "SELECT " . $columnPrefix . "data, " . $columnPrefix . "contentType, " . $columnPrefix . "name ";
$dbQuery .= "FROM " . $tablePrefix. "filedata, " . $tablePrefix . "file, " . $tablePrefix . "permissibleobject ";
$dbQuery .= "WHERE " . $tablePrefix . "filedata." . $columnPrefix . "permissibleObject = " . $tablePrefix . "file." . $columnPrefix . "id and " . $tablePrefix . "permissibleobject." . $columnPrefix . "id = " . $tablePrefix . "file." . $columnPrefix . "id and " . $tablePrefix . "filedata." . $columnPrefix . "permissibleobject = " . $fileId;
$result = mysql_query($dbQuery) or die(mysql_error());

if(mysql_num_rows($result) > 0) {
  $fileType = @mysql_result($result, mysql_num_rows($result)-1, "contentType");
  $fileContent = @mysql_result($result, mysql_num_rows($result)-1, "data");
  $name = @mysql_result($result, mysql_num_rows($result)-1, "name");
  $src_img = imagecreatefromstring($fileContent);
  $out_img = manipulateImage($src_img, $width, $height, $force, $rotate);

  if (!empty($preview)) {
    header("Content-type: $fileType");
    header("Content-Disposition: inline; filename=$name");
    imagepng($out_img, NULL, 9);
    return;
  } 

  if ($DEBUG) {
    $tmpfile = tempnam("e:/tmp/uploads", $name);
  } else {
    $tmpfile = tempnam("/tmp/", $name);
  }
  imagepng($out_img, $tmpfile, 8);

  $handle = fopen($tmpfile, "r");
  $newsize = filesize($tmpfile);
  $contents = addslashes(fread($handle, $newsize));
  fclose($handle);

  $deleteQuery = "delete from " . $tablePrefix . "filedata where " . $columnPrefix . "permissibleObject = $fileId";
  $deleteResult = mysql_query($deleteQuery) or die("Pwned");

  $insertQuery = "insert into ". $tablePrefix . "filedata (" . $columnPrefix . "permissibleObject," . $columnPrefix . "data) VALUES ('$fileId', '$contents')";
  $insertResult = mysql_query($insertQuery) or die("Pwned");

  $updateQuery = "update " . $tablePrefix . "file set " . $columnPrefix . "size = " . $newsize . " where " . $columnPrefix . "id = " . $fileId;
  $updateResult = mysql_query($updateQuery) or die("Pwned");

  $updateQuery = "update " . $tablePrefix . "permissibleobject set " . $columnPrefix . "lastModifiedDate = " . (time()*1000) . " where " . $columnPrefix . "id = " . $fileId;
  $updateResult = mysql_query($updateQuery) or die("Pwned");

  // evict that File from the cache because we just touched it
  $evicturl = $protocol . $server . ':' . $port . '/servlet/BaseService?method=evictFile&id=' . $fileId;
  $evictresult = file_get_contents($evicturl);

  echo "true";
  imagedestroy($src_img);
  imagedestroy($out_img);
} else {
  echo "Record doesn't exist.";
}
?>