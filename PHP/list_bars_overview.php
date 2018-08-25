<?php
//Databaseconnection creation
require_once("config.php");

//statement to view all entries

$msg = "OK";

$liststatement = $pdo->prepare("SELECT * FROM bars");
$liststatement->execute();

//create bar array
$bars = array();

//iterate through all results
while($row = $liststatement->fetch()){
  $bardata = array();

  $bardata['id'] = $row['ID'];
  $bardata['name'] = $row['NAME'];
  //generate image_url by ID
  $bardata['logo_url'] = "https://lennartkaiser.de/ocr/assets/logos/". $row['ID'] .".png";

  //add bar array to bars array
  array_push($bars, $bardata);
}

//create the main array
$json = array();
$json['results'] = $bars;
$json['status'] = $msg;

echo json_encode($json, JSON_UNESCAPED_UNICODE);
?>
