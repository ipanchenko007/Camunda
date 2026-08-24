<?php
/***************************************
 * caseManagerApplication.php
***************************************/

try {
  /* Render page */
  $oHeadPublisher = &headPublisher::getSingleton();
  
  $G_MAIN_MENU        = "processmaker";
  $G_ID_MENU_SELECTED = "ID_CASEMANAGER_MNU_01";
  //$G_SUB_MENU             = "setup";
  //$G_ID_SUB_MENU_SELECTED = "ID_CASEMANAGER_02";

  $config = array();
  $config["pageSize"] = 15;
  $config["message"] = "Hello new Plugin!";

  $oHeadPublisher->addContent("caseManager/caseManagerApplication"); //Adding a html file .html
  $oHeadPublisher->addExtJsScript("caseManager/caseManagerApplication", false); //Adding a javascript file .js
  $oHeadPublisher->assign("CONFIG", $config);

  G::RenderPage("publish", "extJs");
} catch (Exception $e) {
  $G_PUBLISH = new Publisher;
  
  $aMessage["MESSAGE"] = $e->getMessage();
  $G_PUBLISH->AddContent("xmlform", "xmlform", "caseManager/messageShow", "", $aMessage);
  G::RenderPage("publish", "blank");
}
?>