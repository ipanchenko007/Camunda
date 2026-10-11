<?php
/**
 * The Windows IIS SSO class
 *
 * @author Philipp Hungerbьhler
 * @package plugins.pmzhawsso
 * @copyright Copyright (C) 2012 ZHAW.
 */

G::LoadClass('plugin');

class pmzhawssoClass extends PMPlugin {

    /**
     * Contructor of the class
     * @return void
     */
    public function __construct() {
        set_include_path(PATH_PLUGINS . 'pmzhawsso' . PATH_SEPARATOR . get_include_path());
    }

    /**
     * The generic setup function
     *
     * @return void
     */
    public function setup() {
    }

    /**
     * Add a line in the log
     *
     * @author Philipp Hungerbьhler
     * @param String $text The text to save in the log
     */
    public static function log($text) {
        
		//$fpt = fopen(PATH_DATA . 'log/pmzhawsso.log', 'a');
		$fpt = fopen(PATH_DATA.'log/pmzhawsso.log.'.date("yW"), 'a'); //BPMS-3201

        fwrite($fpt, sprintf("%s %s %s %s\n", date('Y-m-d H:i:s'), getenv('REMOTE_ADDR'), SYS_SYS, $text));
        fclose($fpt);
    }

    public function singleSignOn() {
        global $RBAC;

        //$this->log('SSO trigger start');
		//error_log("SSO trigger start");
		
        $server = $_SERVER['SERVER_SOFTWARE'];
        $webserver = explode("/", $server);

        // у нас ту не сервер - не богато живем
		/*if (!is_array($webserver) || (is_array($webserver) && ($webserver[0] != 'Microsoft-IIS'))){
            return false;
        }*/
        
		//error_log('SSO trigger poin 0');
		
        $RBAC =& RBAC::getSingleton();
        $RBAC->initRBAC();
        
        // We actually should already be authenticated
		if(!isset($_SERVER['REMOTE_USER'])) { 
			return false;
		}
			
        $userFull = $_SERVER['REMOTE_USER'];
		
		//error_log('SSO trigger userFull ' . $userFull);
		
        $userPN = explode("\\", $userFull);
		
	
        if (is_array($userPN)){
            //$user = $userPN[1];
			// наша настройка без домена
			$user = $userPN[0];
        } else {
			$user = $userFull;
		}
		
		//error_log('SSO trigger poin 1');
		
        //$user = 'hune';
        if(empty($user) || $user == '')
        {
            $RBAC->singleSignOn = false;            
            return false;   
        } 
		
		//error_log('SSO trigger poin 1.1');

        // If the user exists, the VerifyUser function will return the user properties
        $resVerifyUser = $RBAC->verifyUser($user);
        $RBAC->singleSignOn = true;                        

        $this->log('SSO trigger user name: ' . $user);
		//error_log('SSO trigger user name: ' . $user);
        
        if ($resVerifyUser == 0) {
			//error_log('SSO trigger poin 2');
            // Here we are checking if the automatic user Register is enabled, ioc return -1
            $res = $RBAC->checkAutomaticRegister($user, 'fakepassword');
            if ($res === -1) {
				//error_log('SSO trigger poin 6');
                return false; // No sucessful auto register, skipping the auto register and back to normal login form
            }
            $RBAC->verifyUser($user);
        }
		
		//error_log('SSO trigger poin 3');

		//RFC 543547/19 IPanchenko Start **********************************************************
		//Вариант разрешения входа в статусе VACATION без изменения USR_STATUS
		//См. блок изменения USR_STATUS в скрипте authentication.php (контекст: 'The user is inactive')
		require_once 'classes/model/Users.php';
		//Контроль значения USR_STATUS
        if (isset($RBAC->userObj->fields['USR_UID'])) {
			$uid = $RBAC->userObj->fields['USR_UID'];
			$userCheck = new Users();
			$aUser = $userCheck->load($uid); //Поиск текстового значения 'USR_STATUS'
			if ($aUser['USR_STATUS'] == 'VACATION') {
				$this->log('Single Sign On successful for user ' . $user .' in Status=VACATION');
				//error_log('SSO trigger poin 3.1 USR_STATUS=VACATION');
				return true;
				//Изменение значения USR_STATUS
				//$RBAC->changeUserStatus($uid, 1);
                //$aUser['USR_STATUS'] = 'ACTIVE';
				//$userCheck->update($aUser);
			}
        }
		//RFC 543547/19 IPanchenko End ************************************************************
		
        if (!isset($RBAC->userObj->fields['USR_STATUS']) || $RBAC->userObj->fields['USR_STATUS'] == 0) {
            $this->log('Single Sign On failed for user ' . $user);
			//error_log('SSO trigger poin 8');
            return false;
        }

		$this->log('Single Sign On successful for user ' . $user);
		//error_log('SSO trigger poin 99');
        return true;
    }
}