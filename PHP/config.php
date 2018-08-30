<?php

$mysql_host = "localhost";
$mysql_username = "xxxxxxxxxxxxxxxxxxxx";
$mysql_password = "xxxxxxxxxxxxxxxxxxxx";
$mysql_database = "ocr_android_app";

//establish Databaseconnection
$pdo = new PDO("mysql:host=". $mysql_host . ";dbname=". $mysql_database, $mysql_username,$mysql_password,array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));

?>
