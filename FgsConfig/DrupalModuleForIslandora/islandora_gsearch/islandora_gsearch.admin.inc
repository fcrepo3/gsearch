<?php

function islandora_gsearch_admin_settings($form, &$form_state) {

  $form = array();
  
  $form['gsearch_config']['FEDORA_HOME'] = array(
    '#type' => 'textfield',
    '#title' => t('FEDORA_HOME'),
    '#size' => 80,
    '#weight' => -7,
    '#description' => t('The location of the Fedora installation.'),
    '#default_value' => variable_get('FEDORA_HOME', '/FEDORA_HOME'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['fedoraUser'] = array(
    '#type' => 'textfield',
    '#title' => t('fedoraUser'),
    '#size' => 20,
    '#weight' => -6,
    '#description' => t('Used for access to Fedora. You must enter its password in fgsrepository.fedoraPass in the final repository.properties'),
    '#default_value' => variable_get('fedoraUser', 'fedoraAdmin'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['objectStoreBase'] = array(
    '#type' => 'textfield',
    '#title' => t('objectStoreBase'),
    '#size' => 80,
    '#weight' => -5,
    '#description' => t('The location of the objects of the repository.'),
    '#default_value' => variable_get('objectStoreBase', '/FEDORA_HOME/data/objectStore'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['gsearchUser'] = array(
    '#type' => 'textfield',
    '#title' => t('gsearchUser'),
    '#size' => 20,
    '#weight' => -4,
    '#description' => t('Used for SOAP deployment. You must enter its password in fedoragsearch.soapPass in the final fedoragsearch.properties'),
    '#default_value' => variable_get('gsearchUser', 'fgsAdmin'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['indexDir'] = array(
    '#type' => 'textfield',
    '#title' => t('indexDir'),
    '#size' => 80,
    '#weight' => -3,
    '#description' => t('The location of the Solr index directory.'),
    '#default_value' => variable_get('indexDir', '/FEDORA_HOME/gsearch/FgsIndex/index'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['logFilePath'] = array(
    '#type' => 'textfield',
    '#title' => t('logFilePath'),
    '#size' => 80,
    '#weight' => -2,
    '#description' => t('At startup, GSearch will find the file log4j.xml in tomcat classpath.
						 logFilePath is where to find the log file.'),
    '#default_value' => variable_get('logFilePath', '/FEDORA_HOME/server/logs'),
    '#required' => TRUE,
  );
  
  $form['gsearch_config']['finalConfigPath'] = array(
    '#type' => 'textfield',
    '#title' => t('finalConfigPath'),
    '#size' => 80,
    '#weight' => -1,
    '#description' => t('The location, where the generated GSearch config is placed.
    					 It must be in the classpath of the web server.'),
    '#default_value' => variable_get('finalConfigPath', '/PATH_TO/tomcat/webapps/fedoragsearch/WEB-INF/classes'),
    '#required' => TRUE,
  );
  
  $form['actions'] = array(
    '#type' => 'actions'
  );
  
  $form['actions']['submit'] = array(
    '#type' => 'submit',
    '#value' => t('Save GSearch Configuration'),
    '#weight' => 0,
    '#submit' => array('_islandora_gsearch_admin_settings_submit'),
    '#validate' => array('_islandora_gsearch_admin_settings_validate'),
  );
  
  return $form;
}

function _islandora_gsearch_admin_settings_validate($form, &$form_state) {
  
}

function _islandora_gsearch_admin_settings_submit($form, &$form_state) {
	
    foreach ($form_state['values'] as $key => $value) {
      variable_set($key, $value);
    }
    $ant_command = 'ant -f modules/islandora_gsearch/fgsconfig-basic.xml '
    	. ' -Dlocal.FEDORA_HOME=' . variable_get('FEDORA_HOME')
    	. ' -DfedoraUser=' . variable_get('fedoraUser')
    	. ' -DobjectStoreBase=' . variable_get('objectStoreBase')
    	. ' -DgsearchUser=' . variable_get('gsearchUser')
    	. ' -DindexDir=' . variable_get('indexDir')
    	. ' -DlogFilePath=' . variable_get('logFilePath')
    	. ' -DfinalConfigPath=' . variable_get('finalConfigPath')
    	. ' -propertyfile modules/islandora_gsearch/fgsconfig-basic-for-islandora.properties';
    drupal_set_message(t('The gsearch configuration has been generated and placed at finalConfigPath.'));
    drupal_set_message(t('It was done by running this ant command:'));
    drupal_set_message(t($ant_command));
    $output = array();
    $returnvar = -9;
    exec($ant_command, $output, $returnvar);
    drupal_set_message(t('Running the ant command gave this output:'));
    drupal_set_message('return='.$returnvar);
    foreach ($output as $line) {
    	drupal_set_message($line);
    }
    drupal_set_message(t('FINALLY: You have to look through the configuration at finalConfigPath to verify it and enter the passwords.'));
    cache_clear_all();
    drupal_theme_rebuild();
    $form_state['rebuild'] = TRUE;
}
