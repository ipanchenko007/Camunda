<?php
/**
 * zhawLogin.php
 *
 */

@session_destroy();
session_start();
session_regenerate_id();

//error_log("TEST!!");

$_SESSION['USER_ENV'] = 'workflow'; // Set default workspace for the moment. We also could show a dialog and post back.
$_SESSION ['zhawLogin'] = 'zhawLogin'; // So we know where we come from.
	
//error_log("zhawLogin.php called");	
	
G::header ('location: /sys' . $_SESSION['USER_ENV'] . '/' . SYS_LANG . '/' . SYS_SKIN . '/pmzhawsso/zhawLoginVerify');
die ();
