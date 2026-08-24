<?php
/**
 * class.caseManager.php
 *  
 */

  class caseManagerClass extends PMPlugin {
    function __construct() {
      set_include_path(
        PATH_PLUGINS . 'caseManager' . PATH_SEPARATOR .
        get_include_path()
      );
    }

    function setup()
    {
    }

    function getFieldsForPageSetup()
    {
    }

    function updateFieldsForPageSetup()
    {
    }

  }
?>