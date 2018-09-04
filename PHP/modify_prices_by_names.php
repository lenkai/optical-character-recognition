<?php

//Databaseconnection creation
require_once("config.php");

//get selected bar
$bar_id = $_GET["barid"];
//get password
$bar_password = $_GET["password"];
//overide oder overide and delete all existing
$delete_others = $_GET["deleteothers"];

//Attention the following parameter are all! comma-seperated. Don't use commata
//in any of the values!

//drinks:         Bier, Sprite, Fanta
$drinks     = explode(",", $_GET["drinks"]);
//quantity_in_ml: 400, 500, 500
$quantities = explode(",", $_GET["quantities"]);
//PRICE in cent:  340, 220, 220
$prices     = explode(",", $_GET["prices"]);
//DESCRIPTION ():  Lecker, vollmundig, Starkbier
$description= explode(",", $_GET["description"]);

$json = array();

//Bar code abfrage
if(is_numeric($bar_id) && is_numeric($bar_password) && is_numeric($delete_others)){
  if(sizeof($drinks) == sizeof($quantities) && sizeof($drinks) == sizeof($prices) && sizeof($drinks) == sizeof($description)) {
    //check password
    $pwstatement = $pdo->prepare("SELECT COUNT(*) As COUNT FROM `bars` WHERE ID = '" . htmlspecialchars($bar_id) . "' AND CHANGE_PASSWORD = '" . htmlspecialchars($bar_password) . "'");
    $pwstatement->execute();
    if($pwstatement->fetch()['COUNT'] == 1){

      //delete all pre_existing, if wanted
      if($delete_others == 1){
        $delstatement = $pdo->prepare("DELETE FROM `drinkmenu` WHERE BAR_ID = '" . htmlspecialchars($bar_id) . "'");
        $delstatement->execute();
      }

      //iterate through array and add all drinks
      for($iter = 0; $iter < sizeof($drinks); $iter++){
        //try to get id if exists in database:
        $reqstatement = $pdo->prepare("SELECT COUNT(*) AS co FROM `drinks` WHERE NAME = '" . $drinks[$iter] . "'");
        $reqstatement->execute();
        if($reqstatement->fetch()['co'] == 0){
          //add the drink automatically, if it doesn't exist
          $drstatement = $pdo->prepare("INSERT IGNORE INTO `drinks` (NAME, DESCRIPTION) VALUES ('" . $drinks[$iter] . "', '" . $description[$iter] . "')");
          $drstatement->execute();
        }

        $reqqstatement = $pdo->prepare("SELECT ID FROM `drinks` WHERE NAME = '" . $drinks[$iter] . "'");
        $reqqstatement->execute();

        $liststatement = $pdo->prepare("INSERT IGNORE INTO `drinkmenu` (BAR_ID, DRINK_ID, PRICE, MILLILITER, DESCRIPTION, LAST_UPDATE) VALUES (" . htmlspecialchars($bar_id) . ", " . $reqqstatement->fetch()['ID'] . ", " . ($prices[$iter] / 100) . ", " . $quantities[$iter] . ", '" . $description[$iter] . "', '" . date('Y-m-d H:i:s') . "')");
        $liststatement->execute();
      }

      $json['status'] = "OK";
    }else{
      $json['status'] = "Wrong password";
    }
  } else {
    $json['status'] = "Different length of array";
  }
} else {
  $json['status'] = "None-numeric number in URL";
}

echo json_encode($json, JSON_UNESCAPED_UNICODE);
?>
