<?php
$G_MAIN_MENU            = 'processmaker';
$G_ID_MENU_SELECTED     = 'ID_CASEMANAGER_MNU_01';
$G_PUBLISH = new Publisher;
$G_PUBLISH->AddContent('view', 'caseManager/mainLoad');
G::RenderPage('publish');
?>