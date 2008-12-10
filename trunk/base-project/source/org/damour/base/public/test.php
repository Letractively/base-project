<?php
  foreach ($_FILES as $key => $value) {
    move_uploaded_file($value['tmp_name'], "./files/" . $value['name']);
  }
?>