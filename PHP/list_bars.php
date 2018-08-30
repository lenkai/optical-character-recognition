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

  $bardata['name'] = $row['NAME'];
  $bardata['description'] = $row['DESCRIPTION'];
  //generate image_url by ID
  $bardata['logo_url'] = "https://lennartkaiser.de/ocr/assets/logos/". $row['ID'] .".png";

  $files = glob("./assets/" . $row['ID'] . "_*.png", GLOB_BRACE);
  $imgs = array();

  //$lastimageid = -1;
  foreach ($files as $path) {
    array_push($imgs, "https://lennartkaiser.de/ocr/assets/" . basename($path));
    //$lastimageid = str_replace(".png", "", explode(basename($path), "_")[1]);
  }

  $bardata['images_urls'] = $imgs;

  //generate Array of features
  $features = explode("\r\n", $row['FEATURES']);
  $bardata['features'] = $features;

  //extra data to array
  $bardata['closing_time'] = $row['CLOSE_AT'];
  $bardata['website'] = $row['WEBSITE'];
  $bardata['adress'] = $row['ADRESS'];
  $bardata['latitude'] = $row['LATITUDE'];
  $bardata['longtitude'] = $row['LONGTITUDE'];
  $bardata['rating'] = $row['RATING'];
  $bardata['last_update'] = $row['LAST_UPDATE_DATE'];
  $bardata['create_date'] = $row['CREATE_DATE'];

  //add bar array to bars array
  array_push($bars, $bardata);
}

$json = array();
$json['results'] = $bars;
$json['status'] = $msg;

echo json_encode($json, JSON_UNESCAPED_UNICODE);
?>
