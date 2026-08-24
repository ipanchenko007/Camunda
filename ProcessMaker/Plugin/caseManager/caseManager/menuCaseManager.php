<?php
/**************************************************************
The Menu::AddIdRawOption() method adds an option to a menu using an ID string. The option will be added to the end of the list of existing options. .

void Menu::AddIdRawOption(variant $strId, string $strURL = "", string $label = "", string $icon = "",
                          string $js = "", string $strType = "plugins", string $elementClass = "")
Parameters:
variant $strId: Sets the unique string to identify the menu. It is recommended to use a ID in upper case like "MYMENU". This parameter can either be a string with the ID or an array which also sets both the ID and a class for the ID. For example: array("MYMENU", "MyMenuClass")
string $strURL: Optional. A URL (which is generally a relative path) to the code file which should be executed when the menu option is clicked.
string $label: Optional. A text label for the menu.
string $icon: Optional. A URL (which is generally a relative path) to the icon image which is displayed next to the text label in the menu.
string $js: Optional. A bit of JavaScript to be executed when user clicks on the menu option, such as: 'showDbConnectionsList(Pm.options.uid); return false;'
string $strType: Optional. Identifies the type of the menu option. Possible Values: 'settings', 'users', 'plugins'
string $elementClass: Optional. The name of an ExtJS class associated with the menu option, such as: 'blockHeader', 'blockHeaderNoChild', 'ss_sprite ss_arrow_switch', 'ss_sprite ss_calendar_view_day', 'ss_sprite ss_page_white_put', 'ss_sprite ss_page_white_get', 'ss_sprite ss_application_form', 'ss_sprite ss_database_connect', 'ss_sprite ss_arrow_switch', 'ss_sprite ss_cog'
Return Value:
none

Example:
Use the $G_TMP_MENU global variable, which is an instance of the Menu class, to add the "Inbox" option to the temporary menu:

global $G_TMP_MENU;
$G_TMP_MENU->AddIdRawOption('CASES_INBOX', 'casesListExtJs?action=todo', G::LoadTranslation('ID_INBOX'), 'icon-cases-inbox.png' );
**************************************************************/

global $G_TMP_MENU;
global $RBAC;

require_once('logger.php');
//Контроль значения RBAC->userCanAccess
//$RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE']
//vipLog("menuCaseManager RBAC->userCanAccess('CASE_MANAGER_1410'): ".vipVarDump($RBAC->userCanAccess('CASE_MANAGER_1410')));
//vipLog("menuCaseManager RBAC->userCanAccess('PROCESSMAKER_ADMIN'): ".vipVarDump($RBAC->userCanAccess('PROCESSMAKER_ADMIN')));
//vipLog("menuCaseManager aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE']: ".vipVarDump($RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE']));

$roleCode = $RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE'];
// HOME MODULE
//if ($RBAC->userCanAccess('CASE_MANAGER_1410') == 1 || $RBAC->aUserInfo['PROCESSMAKER']['ROLE']['ROL_CODE'] == 'PROCESSMAKER_ADMIN') {
if ($roleCode == 'CASE_MANAGER_1410' || $roleCode == 'PROCESSMAKER_ADMIN') {
  //$G_TMP_MENU->AddIdRawOption("ID_CASEMANAGER_MNU_1410", "caseManager/main", "Супервизор 1410");
}

?>