<?php
//Databaseconnection creation
require_once("config.php");

//get selected bar
$bar_id = $_GET["barid"];

$json = array();

if(is_numeric($bar_id)){
  $liststatement = $pdo->prepare("SELECT * FROM drinkmenu LEFT JOIN drinks ON drinkmenu.DRINK_ID = drinks.ID WHERE BAR_ID = " . htmlspecialchars($bar_id));
  $liststatement->execute();

  //create empty drink array
  $drinks = array();

  //iterate through all results
  while($row = $liststatement->fetch()){
    $drinkdata = array();

    $drinkdata['drink_id'] = $row['DRINK_ID'];
    $drinkdata['name'] = $row['NAME'];
    $drinkdata['price'] = $row['PRICE'];
    $drinkdata['quantity_in_ml'] = $row['MILLILITER'];

    $drinkdata['drink_description'] = $row['DESCRIPTION'];
    $drinkdata['last_update'] = $row['LAST_UPDATE'];

    //add bar array to bars array
    array_push($drinks, $drinkdata);
  }
  $json['results'] = $drinks;
  $json['status'] = "OK";
} else {
  $json['status'] = "WRONG_BAR_ID";
}

echo json_encode($json, JSON_UNESCAPED_UNICODE);
?>
