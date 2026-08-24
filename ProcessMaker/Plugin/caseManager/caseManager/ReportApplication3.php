<?php
//Перенесен из vipReports, т.к. одновременный запуск plugin vipReports приведит к ошибке!
//Дополнен определением vacationHistoryURL из config.php
require_once('config.php'); //Для определения variable REPORT_SERVER_URL

try {

  $vacationHistoryURL = REPORT_VACATION_HISTORY_URL; //See config.php
  
  $script = "<script>".
            "function redirectToReports() {".
            "  var win = window.open('$vacationHistoryURL', '_blank');".
            //"  win.focus();". //Вызывает ошибку
            "}".
            "</script>";
  
  
  echo "<html>$script<body onload=\"redirectToReports();\"><div><p/>";
  echo "<a href='$vacationHistoryURL' target='_blank'>Перейти к Истории Замещений</a></div>";
  echo "</body></html>";
  
} catch (Exception $e) {
  $G_PUBLISH = new Publisher;
  
  $aMessage["MESSAGE"] = $e->getMessage();
  $G_PUBLISH->AddContent("xmlform", "xmlform", "caseManager/messageShow", "", $aMessage);
  G::RenderPage("publish", "blank");
}
?>