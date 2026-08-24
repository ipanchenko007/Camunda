<?php
//Перенесен из vipReports, т.к. одновременный запуск plugin vipReports приведит к ошибке!
//Дополнен определением supervisorURL из config.php
require_once('config.php'); //Для определения variable REPORT_SERVER_URL

try {

  //Удалил файл conf.ini и заменил на config.php
  //$configurationFile = PATH_PLUGINS . 'caseManager/config/conf.ini';
  //$settings = parse_ini_file($configurationFile);

  $reportsURL = REPORT_SERVER_URL; //See config.php
  
  $script = "<script>".
            "function redirectToReports() {".
            "  var win = window.open('$reportsURL', '_blank');".
            //"  win.focus();". //Вызывает ошибку
            "}".
            "</script>";
  
  
  echo "<html>$script<body onload=\"redirectToReports();\"><div><p/>";
  echo "<a href='$reportsURL' target='_blank'>Перейти к отчетам</a></div>";
  echo "</body></html>";
  
} catch (Exception $e) {
  $G_PUBLISH = new Publisher;
  
  $aMessage["MESSAGE"] = $e->getMessage();
  $G_PUBLISH->AddContent("xmlform", "xmlform", "caseManager/messageShow", "", $aMessage);
  G::RenderPage("publish", "blank");
}
?>