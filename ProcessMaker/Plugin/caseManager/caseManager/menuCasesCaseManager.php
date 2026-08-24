<?php
/* menuCasesCaseManager.php *****************
Изменяем конфигурацию бокового меню.
*********************************************/
global $G_TMP_MENU;
global $RBAC;
require_once('logger.php');

$tmpId      = array();
$tmpTypes   = array();
$tmpEnabled = array();
$tmpOptions = array(); //link
$tmpLabels  = array();
$tmpJS      = array();
$tmpIcons   = array();
$tmpEClass  = array();

$i = 0;
$k = 0;

$roleCode = $RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE'];
//if ($RBAC->userCanAccess('CASE_MANAGER_1410') == 1 || $RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE'] == 'PROCESSMAKER_ADMIN') {
foreach ($G_TMP_MENU->Id as $index => $value) {
  //vipLog("menuCasesCaseManager index: $index value: ".vipVarDump($value));
  /*
  vipLog("Id: ".vipVarDump($G_TMP_MENU->Id[$index]));
  vipLog("Types: ".vipVarDump($G_TMP_MENU->Types[$index]));
  vipLog("Enabled: ".vipVarDump($G_TMP_MENU->Enabled[$index]));
  vipLog("Options: ".vipVarDump($G_TMP_MENU->Options[$index]));
  vipLog("Labels: ".vipVarDump($G_TMP_MENU->Labels[$index]));
  vipLog("JS: ".vipVarDump($G_TMP_MENU->JS[$index]));
  vipLog("Icons: ".vipVarDump($G_TMP_MENU->Icons[$index]));
  vipLog("ElementClass: ".vipVarDump($G_TMP_MENU->ElementClass[$index]));
  */
  
  if ($index == 6 && 
	  ($roleCode == 'CASE_MANAGER_1410' || $roleCode == 'PROCESSMAKER_ADMIN')
	  ) { //Добавим пункт перед CASES_PAUSED
  
	$tmpId[$index + $k]      = "ID_CASEMANAGER_MNUCASE_1410";
	$tmpTypes[$index + $k]   = "plugins";
	$tmpEnabled[$index + $k] = 1;
	$tmpOptions[$index + $k] = "../caseManager/caseManagerApplication"; //link
	$tmpLabels[$index + $k]  = "Супервизор 1410";
	$tmpJS[$index + $k]      = null;
	$tmpIcons[$index + $k]   = null;
	$tmpEClass[$index + $k]  = null;
	
	$k = 1;
  }

  $tmpId[$index + $k]      = $G_TMP_MENU->Id[$index];
  $tmpTypes[$index + $k]   = $G_TMP_MENU->Types[$index];
  $tmpEnabled[$index + $k] = $G_TMP_MENU->Enabled[$index];
  $tmpOptions[$index + $k] = $G_TMP_MENU->Options[$index]; //link
  $tmpLabels[$index + $k]  = $G_TMP_MENU->Labels[$index];
  $tmpJS[$index + $k]      = $G_TMP_MENU->JS[$index];
  $tmpIcons[$index + $k]   = $G_TMP_MENU->Icons[$index];
  $tmpEClass[$index + $k]  = $G_TMP_MENU->ElementClass[$index];
  
  $i = $index + $k;
}

$i = $i + 1;

//Добавление пунктов ID_REPORTS_MNU и ID_SUPERVISOR_LIST_MNU перенесено из vipReports, 
//т.к. одновременный запуск plugin vipReports приведит к ошибке!
$tmpId[$i]      = "ID_REPORTS_MNU";
//$tmpId[$i]      = "ADDITIONAL_TABLES"; // это имя WA - от него формируется ссылка на CSS в формате ICON_<tmpId>, соответственно id подобран под красивую иконку
$tmpTypes[$i]   = "blockHeaderNoChild"; //"blockHeaderNoChild";
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = ""; // "../caseManager/ReportApplication"; //link // Akikot 02.11.2023 BPMS-1915 верхний пункт меню без ссылки (стандартный функционал)
$tmpLabels[$i]  = "Отчеты";
// параметры ниже заставить работать не удалось
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;

/* Akikot 02.11.2023 BPMS-1915 Добавил подпункт меню в "Отчеты"
Есть стандартный механизм создания пунктов меню: $G_TMP_MENU->AddIdRawOption(...)
который описан в \apps\processmaker\htdocs\gulliver\system\class.menu.php :254
 */
$i = $i + 1;

$tmpId[$i]      = "ID_REPORT_TO_GO";
$tmpTypes[$i]   = null; //Будет дочерним "blockHeader"
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = "../caseManager/ReportApplication"; //link
$tmpLabels[$i]  = "Перейти к отчетам";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;
/**/

$i = $i + 1;

$tmpId[$i]      = "ID_SUPERVISOR_LIST_MNU";
$tmpTypes[$i]   = null; //Будет дочерним "blockHeader"
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = "../caseManager/ReportApplication2"; //link
$tmpLabels[$i]  = "Секретари процессов";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;

$i = $i + 1;

$tmpId[$i]      = "ID_VACATION_HISTORY_MNU";
$tmpTypes[$i]   = null; //Будет дочерним "blockHeader"
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = "../caseManager/ReportApplication3"; //link
$tmpLabels[$i]  = "История замещений";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;


/*
$i = $i + 1;

$tmpId[$i]      = "ID_CASEMANAGER_MNUCASE_02";
$tmpTypes[$i]   = "blockHeader";
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = null; //link
$tmpLabels[$i]  = "Management application2";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;

$i = $i + 1;

$tmpId[$i]      = "ID_CASEMANAGER_MNUCASE_03";
$tmpTypes[$i]   = "plugins";
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = "../caseManager/caseManagerApplication2"; //link
$tmpLabels[$i]  = "Management application2";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;

$i = $i + 1;

$tmpId[$i]      = "ID_CASEMANAGER_MNUCASE_04";
$tmpTypes[$i]   = "blockHeaderNoChild";
$tmpEnabled[$i] = 1;
$tmpOptions[$i] = "../caseManager/caseManagerApplication3"; //link
$tmpLabels[$i]  = "Management application3";
$tmpJS[$i]      = null;
$tmpIcons[$i]   = null;
$tmpEClass[$i]  = null;
*/

$G_TMP_MENU->Id      = $tmpId;
$G_TMP_MENU->Types   = $tmpTypes;
$G_TMP_MENU->Enabled = $tmpEnabled;
$G_TMP_MENU->Options = $tmpOptions; //link
$G_TMP_MENU->Labels  = $tmpLabels;
$G_TMP_MENU->JS      = $tmpJS;
$G_TMP_MENU->Icons   = $tmpIcons;
$G_TMP_MENU->ElementClass = $tmpEClass;
?>