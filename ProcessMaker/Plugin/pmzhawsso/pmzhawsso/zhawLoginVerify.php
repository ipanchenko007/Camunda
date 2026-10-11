<?php
/**
 * zhawLoginVerify.php
 * 
 */

if (isset ($_SESSION ['zhawLogin'])) {
	$pluginRegistry =& PMPluginRegistry::getSingleton();
	if (defined('PM_SINGLE_SIGN_ON')) {
		//error_log("zhawLoginVerify defined PM_SINGLE_SIGN_ON");
		if ($pluginRegistry->existsTrigger(PM_SINGLE_SIGN_ON)) {
			//error_log("zhawLoginVerify existsTrigger PM_SINGLE_SIGN_ON");
			if ($pluginRegistry->executeTriggers(PM_SINGLE_SIGN_ON, null)) {
				//error_log("zhawLoginVerify Start new session");
				// Start new session
				@session_destroy();
				session_start();
				session_regenerate_id();
				
				//error_log("zhawLoginVerify pre authentication");
                require_once PATH_HOME . 'engine\methods\login\authentication.php';
				die();
			}
		}
	}
} 


//redirect to login
G::SendTemporalMessage ('ID_USER_HAVENT_RIGHTS_SYSTEM', "error");
// verify if the current skin is a 'ux' variant
$urlPart = substr(SYS_SKIN, 0, 2) == 'ux' && SYS_SKIN != 'uxs' ? '/main/login' : '/login/login';
//error_log("zhawLoginVerify redirect to login : " . $urlPart);
header('Location: /sys' . SYS_SYS . '/' . SYS_LANG . '/' . SYS_SKIN . $urlPart);
die;

