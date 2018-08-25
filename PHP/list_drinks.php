<?php
//Databaseconnection creation
require_once("config.php");

$json = array();

$liststatement = $pdo->prepare("SELECT * FROM drinks");
$liststatement->execute();

//create empty drink array
$drinks = array();

//iterate through all results
while($row = $liststatement->fetch()){
  $drinkdata = array();

  $drinkdata['drink_id'] = $row['ID'];
  $drinkdata['name'] = $row['NAME'];
  $drinkdata['drink_description'] = $row['DESCRIPTION'];

  //add bar array to bars array
  array_push($drinks, $drinkdata);
}
$json['results'] = $drinks;
$json['status'] = "OK";

echo json_encode($json, JSON_UNESCAPED_UNICODE);
?>
