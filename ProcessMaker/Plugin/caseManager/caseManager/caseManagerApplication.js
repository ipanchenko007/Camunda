/*
 * caseManagerApplication.js
*/

//Creates namespaces to be used for scoping variables and classes so that they are not global.
Ext.namespace("caseManager");

//По упрощенной схеме явно зададим значение PRO_UID (simplifiedProcess)
var simplifiedProcess = '9076843266463209ebf1b30090043495';

caseManager.application = {
  init: function () {
    storeCaseProcess = function (n, r, i) {
	  //A modal, floating Component which may be shown above a specified Component while loading data.
      var myMask = new Ext.LoadMask(Ext.getBody(), { msg: "Load cases..." });
      myMask.show();

	  //Отправляем запрос на сервер
      Ext.Ajax.request({
        url: "ajax/caseManagerApplicationAjax",
        //url: "ajax/cases",
        method: "POST",
        params: { "option": "LST", "pageSize": n, "limit": r, "start": i, "process": simplifiedProcess },

		//Обработка успешного завершения
        success: function (result, request) {
		  //Ext.data.Store.loadData - Loads an array of data straight into the Store.
		  //Ext.JSON.decode - Decodes (parses) a JSON string to an object.
          storeCase.loadData(Ext.util.JSON.decode(result.responseText));
          //console.log(result.responseText);
          myMask.hide();
        },
		//Обработка в случае ошибки
        failure: function (result, request) {
          myMask.hide();
          Ext.MessageBox.alert("Alert", "Failure cases load");
        }
      });
    };

	//showAt - Задание положения контекстного меню onMnuContext
    onMnuContext = function (grid, rowIndex, e) {
      e.stopEvent();
      var coords = e.getXY();
      mnuContext.showAt([coords[0], coords[1]]);
    };

    //Variables declared in html file
    var pageSize = parseInt(CONFIG.pageSize);
    var message = CONFIG.message;

    //Создание Store (storeCase) для Grid
    var storeCase = new Ext.data.Store({
	  //AjaxProxy is one of the most widely-used ways of getting data into your application
      proxy: new Ext.data.HttpProxy({
        url: "ajax/caseManagerApplicationAjax",
        method: "POST"
      }),

      //baseParams: {"option": "LST", "pageSize": pageSize},

	  //Data reader class to create an Array of Ext.data.Record objects from a JSON packet based on mappings in a provided Ext.data.Record constructor.
      reader: new Ext.data.JsonReader({
        root: "resultRoot",
        totalProperty: "resultTotal", //Name of the property from which to retrieve the total number of records in the dataset.
        fields: [{name: "APP_NUMBER"}
                ,{name: "APP_TITLE"}
                ,{name: "APP_TAS_TITLE"}
                ,{name: "APP_CURRENT_USER"}
                ,{name: "INIT_USER_NAME"}
                ,{name: "APP_CREATE_DATE"}
                ,{name: "APP_UPDATE_DATE"}
				//Не отображаемые
                ,{name: "SUMMARY_UID"}
                ,{name: "APP_STATUS"}
                ,{name: "USR_UID"}
                ,{name: "APP_UID"}
                ,{name: "DEL_INDEX"}
                ]
      }),


	  //Ext.MessageBox.alert("Alert", "storeCase");
      //autoLoad: true, //First call

      listeners: {
        beforeload: function (store) {
          this.baseParams = { "option": "LST", "pageSize": pageSize, "process": simplifiedProcess /*suggestProcess.value*/ };
        }
      }
    });

    //Создание Store (processStore) для выбора Process
	/* Отказался от использования. Сделаем по упрощенной схеме! *******
	var processStore = new Ext.data.Store({
      proxy: new Ext.data.HttpProxy({
        url: 'ajax/caseManagerApplicationAjax',
        method: 'POST'
      }),

      reader: new Ext.data.JsonReader({
        root: "data",
        fields: [{name: 'PRO_UID'}
		        ,{name: 'PRO_TITLE'}]
      }),

	  listeners: {
        beforeload: function (store) {
		  console.log('suggestProcess.value: ' +Ext.getCmp('suggestProcess').getValue());
		  console.log('suggestProcess.getRawValue: ' +Ext.getCmp('suggestProcess').getRawValue());
          this.baseParams = { "option": "PRO", "process": Ext.getCmp('suggestProcess').getRawValue() };
        }
      }
    });
	******************************************************************/

	//Настройка кол-ва отображаемых строк на странице
    var storePageSize = new Ext.data.SimpleStore({
      fields: ["size"],
      data: [["15"], ["25"], ["35"], ["50"], ["100"]],
      autoLoad: true
    });

	/* Отключаем! Нам не нужно выделение всех записей! ***
    //Define the shared Action для btnSelect
    var btnSelect = new Ext.Action({
      id: "btnSelect",

      text: "Select All",
      //iconCls: "button_menu_ext ss_sprite ss_table_add",

      handler: function () {
        grdpnlCase.getSelectionModel().selectAll();
        //Ext.MessageBox.alert("Alert", message);
      }
    });
	*****************************************************/

	/* Отключаем! Нам не нужно снятие выделения записей! ***
	//Define the shared Action для btnClear
    var btnClear = new Ext.Action({
      id: "btnClear",

      text: "Clear Selected",
      //iconCls: "button_menu_ext ss_sprite ss_table_delete",
      disabled: true,

      handler: function () {
        //Ext.MessageBox.alert("Alert", message);
        grdpnlCase.getSelectionModel().clearSelections();
      }
    });
	*******************************************************/

	//Определяем доступные действия
	//Перенаправление на Task "Администрирование"
    var btnToSupervisor = new Ext.Action({
      id: "btnToSupervisor",

      text: "На Секретаря",
	  //Возможно ss_sprite - это CSS-спрайт, способ объединить много изображений в одно
      //iconCls: "button_menu_ext ss_sprite ss_link_go",
      //iconCls: "button_menu_ext ss_sprite ss_money_dollar",
      disabled: true,

      handler: function () {
        //Ext.MessageBox.alert("Alert", message);
		//getSelectionModel() - Returns the selection model being used and creates it via the configuration if it has not been created already.
		//getSelections(list) - Get the selected records from the specified list.
        var rows = grdpnlCase.getSelectionModel().getSelections();
        var selectedCase = rows.map(function (row) {
          return row.data;
        })
        toSupervisor(JSON.stringify(selectedCase));
      }
    });

	//Перенаправление на Next Task
	/* Пока отключил! *************************************************
    var btnToNextTask = new Ext.Action({
      id: "btnToNextTask",

      text: "На следующую Задачу",
	  //Возможно ss_sprite - это CSS-спрайт, способ объединить много изображений в одно
      //iconCls: "button_menu_ext ss_sprite ss_link_go",
      //iconCls: "button_menu_ext ss_sprite ss_money_dollar",
      disabled: true,

      handler: function () {
        //Ext.MessageBox.alert("Alert", message);
		//getSelectionModel() - Returns the selection model being used and creates it via the configuration if it has not been created already.
		//getSelections(list) - Get the selected records from the specified list.
        var rows = grdpnlCase.getSelectionModel().getSelections();
        var selectedCase = rows.map(function (row) {
          return row.data;
        })
        toNextTask(JSON.stringify(selectedCase));
      }
    });
	* Пока отключил! *************************************************/

	//Задание пунктов действия в Context menu
    var mnuContext = new Ext.menu.Menu({
      id: "mnuContext",

      //items: [btnToNextTask, btnToSupervisor] //Пока убрал btnToNextTask
      items: [btnToSupervisor]
    });


	//Create the combo box (поле со списком), attached to the states data store
    var cboPageSize = new Ext.form.ComboBox({
      id: "cboPageSize", //Настройка кол-ва выдаваемых строк на странице

      mode: "local",
      triggerAction: "all",
      store: storePageSize, //См. настройку значений в списке
      valueField: "size",
      displayField: "size",
      width: 50,
      editable: false,

      listeners: {
        select: function (combo, record, index) {
          pageSize = parseInt(record.data["size"]);

          pagingUser.pageSize = pageSize;
          pagingUser.moveFirst();
        }
      }
    });

	//Paging Toolbar is typically used as one of the Grid's toolbars
	//Paging is used to reduce the amount of data exchanged with the client.
	//https://docs.sencha.com/extjs/4.2.0/#!/api/Ext.toolbar.Paging
    var pagingUser = new Ext.PagingToolbar({
      id: "pagingUser",

      pageSize: pageSize,
      store: storeCase,
      displayInfo: true,
      displayMsg: "Displaying cases " + "{" + "0" + "}" + " - " + "{" + "1" + "}" + " of " + "{" + "2" + "}",
      emptyMsg: "No cases to display",
      items: ["-", "Page size:", cboPageSize]
    });

    var cmodel = new Ext.grid.ColumnModel({
      defaults: {
        width: 50,
        sortable: true
      },
	  
      columns:[{APP_UID: "APP_UID", dataIndex: "APP_UID", hidden: true}, //Почему вместо header стоит APP_UID ?
	           {header: "#", dataIndex: "APP_NUMBER",width: 5, align: "left"},
               {header: "Case Title", dataIndex: "APP_TITLE", width: 28, align: "left"},
               {header: "Task Title", dataIndex: "APP_TAS_TITLE", width: 15, align: "left"},
               {header: "Current User", dataIndex: "APP_CURRENT_USER", width: 16, align: "left"},
               {header: "Initial User", dataIndex: "INIT_USER_NAME", width: 16, align: "left"},
               {header: "Create Date", dataIndex: "APP_CREATE_DATE", width: 10, align: "left"},
               {header: "Last Upadte Date", dataIndex: "APP_UPDATE_DATE", width: 10, align: "left"},
               {header: "SUMMARY_UID", dataIndex: "SUMMARY_UID", hidden: true},
               {header: "Status", dataIndex: "APP_STATUS", hidden: true},
               {header: "User UID", dataIndex: "USR_UID", hidden: true},
               {header: "DEL_INDEX", dataIndex: "DEL_INDEX", hidden: true}
              ]
    });

    var smodel = new Ext.grid.RowSelectionModel({
      singleSelect: true, //false - множественный выбор
      listeners: {
        rowselect: function (sm) {
          //btnToNextTask.enable(); //Пока убрал!
          btnToSupervisor.enable();
        },
        rowdeselect: function (sm) {
          //btnToNextTask.disable(); //Пока убрал!
          btnToSupervisor.disable();
        }
      }
    });

	/* Отказался от использования. Сделаем по упрощенной схеме! *******
	//Поле списка выбора Process
    var suggestProcess = new Ext.form.ComboBox({
	  
	  fieldLabel: 'Процесс', //Увы, но fieldLabel не отображается! Приходится вводить явно в tbar:
	  id: "suggestProcess", //Попробую добавить id для получения значения при обращении. 
	                        //Буду потом искать введенное значение Ext.getCmp('suggestProcess').getRawValue()
      store: processStore,
      valueField: 'PRO_UID',
      displayField: 'PRO_TITLE',
      typeAhead: false, //true to populate and autoselect the remainder of the text being typed after a configurable delay (typeAheadDelay) if ...
      triggerAction: 'all',
      emptyText: 'Введите название процесса', //_('ID_EMPTY_PROCESSES'),
      selectOnFocus: true,
      editable: true,
      width: 200,
      allowBlank: true,
	  value: '', //A value to initialize this field with (defaults to undefined).
      autocomplete: true,
      minChars: 1,
      hideTrigger: true,
      listeners: {
        scope: this,
        select: function () {
          filterProcess = suggestProcess.value;
          //console.log('filterProcess: ' + filterProcess);
          storeCase.setBaseParam('process', filterProcess);
          doSearch();
        }
      }
    });
	
    //Очистка поля выбора Процесс
	var resetProcessButton = {
      text: 'X',
      ctCls: "pm_search_x_button_des",
      handler: function () {
        storeCase.setBaseParam('process', '');
        suggestProcess.setValue('');
        doSearch();
      }
    };
	******************************************************************/
	
	/* Этот вариант из draftManagerApplication.js
	//Добавляем кнопку btnSearch
    var btnSearch = new Ext.Button({
      text: _('ID_SEARCH'),
      //iconCls: 'button_menu_ext ss_sprite ss_page_find',
      // cls: 'x-form-toolbar-standardButton',
      handler: doSearch
    });
	********************************************/

    /*Этот вариант из DemoApplication.js ***/
    var btnSearch = new Ext.Action({
      id: "btnSearch",
      text: "Search",
      handler: function() {
		//getCmp(id): Object - This is shorthand reference to Ext.ComponentManager.get. 
		//Looks up an existing Component by id. 
        var app_num = Ext.getCmp('txtSearch').getValue();
		//console.log('btnSearch app_num: ' + app_num);
		
        Ext.getCmp('grdpnlCase').getStore().load({params: {"option": "SEARCH_BY_NUM", app_num: app_num}});
      }
    });
    /***************************************/
    //Добавляем поле для ввода номера Case
	var txtSearch = new Ext.form.TextField({
      id: "txtSearch",
      maskRe:/[0-9.]/, // allow only numbers
      emptyText: "Введите номер Заявки",
      width: 150,
      allowBlank: true,
      
      listeners:{
        specialkey: function (f, e) {
          if (e.getKey() == e.ENTER) {

           var app_num = Ext.getCmp('txtSearch').getValue();
		   //console.log('txtSearch app_num: ' + app_num);
		   
           //Ext.getCmp('grdpnlCase').getStore().load({params: {app_num: app_num}});
		   Ext.getCmp('grdpnlCase').getStore().load({params: {"option": "SEARCH_BY_NUM", app_num: app_num}});
         
          }
        }
      }
    });

	//Очистка поля номера Case для поиска
    var btnTextClear = new Ext.Action({
      id: "btnTextClear",
      
      text: "X",
      ctCls: "pm_search_x_button",
      handler: function() {
        txtSearch.reset();
      }
    });

	/* Пример из: \apps\processmaker\htdocs\shared\compiled\ExtJs\processes_main.js ********
	var ProcessCategories = new Ext.form.ComboBox({
	fieldLabel : _('ID_CATEGORY'),
	hiddenName : 'PRO_CATEGORY',
	valueField : 'CATEGORY_UID',
	displayField : 'CATEGORY_NAME',
	triggerAction : 'all',
	selectOnFocus : true,
	editable : false,
	width: 180,
	allowBlank : true,
	value: '',
	store : new Ext.data.Store( {
	  autoLoad: true,  //autoload the data
	  proxy : new Ext.data.HttpProxy( {
		url : '../processProxy/getCategoriesList',
		method : 'POST'
	  }),

	  reader : new Ext.data.JsonReader( {
		fields : [ {
		  name : 'CATEGORY_UID'
		}, {
		  name : 'CATEGORY_NAME'
		} ]
	  })
	})
	});
	ProcessCategories.store.on('load',function(store) {
	ProcessCategories.setValue(store.getAt(0).get('CATEGORY_UID'));
	});
	***************************************************************************************/


	//Описание выполняемой функции на событие 
    function doSearch() {
      storeCase.load({ params: { start: 0, limit: pageSize } });
    }

	//Описание выполняемой функции на событие
	//Перевод на Task "Администрирование"
    function toSupervisor(selectedCase) {
      var myMask = new Ext.LoadMask(Ext.getBody(), { msg: "Маршрутизация..." });
      myMask.show();

      Ext.Ajax.request({
        url: "ajax/caseManagerApplicationAjax",
        method: "POST",
        params: { "option": "TO_ADMIN", "selectedCase": selectedCase },

        success: function (result, request) {
		  //console.log("result.responseText: '" + result.responseText + "'");
          var resp = JSON.parse(result.responseText);
          var respString = "";
          resp.data.forEach(function(c) {
            respString += "Заявка: " + c.app_number + " - " + c.action_result + "<br>";
          });
          Ext.MessageBox.alert("Результат маршрутизации", respString);
          doSearch();
          myMask.hide();
        },
        failure: function (result, request) {
          myMask.hide();
          Ext.MessageBox.alert("Alert", "Ошибка маршрутизации");
        }
      });
    };

	//Перевод на Next Task (пока отключена)
    function toNextTask(selectedCase) {
      var myMask = new Ext.LoadMask(Ext.getBody(), { msg: "Маршрутизация..." });
      myMask.show();

      Ext.Ajax.request({
        url: "ajax/caseManagerApplicationAjax",
        method: "POST",
        params: { "option": "TO_NEXT_TASK", "selectedCase": selectedCase },

        success: function (result, request) {
		  //console.log("result.responseText: '" + result.responseText + "'");
          var resp = JSON.parse(result.responseText);
          var respString = "";
          resp.data.forEach(function(c) {
            respString += "Заявка: " + c.app_number + " - " + c.action_result + "<br>";
          });
          Ext.MessageBox.alert("Результат маршрутизации", respString);
          doSearch();
          myMask.hide();
        },
        failure: function (result, request) {
          myMask.hide();
          Ext.MessageBox.alert("Alert", "Ошибка маршрутизации");
        }
      });
    };

    //Создание Grid (grdpnlCase)
	var grdpnlCase = new Ext.grid.GridPanel({
      id: "grdpnlCase",

      store: storeCase,
      colModel: cmodel,
      selModel: smodel,

      columnLines: true,
      viewConfig: { forceFit: true },
      enableColumnResize: true,
      enableHdMenu: true, //Menu of the column

	  //Config 'Top Bar'
	  //fieldLabel приходится задавать явно!
	  //{xtype: 'tbfill'} (альтернатива: '->') - A non-rendering placeholder item which instructs the Toolbar's Layout to begin using the right-justified button container.
      //tbar: [btnToSupervisor, btnToNextTask, {xtype: 'tbfill'}, 'Процесс:', suggestProcess, resetProcessButton, {xtype: 'tbseparator'}, 'Заявка:', txtSearch, btnTextClear, btnSearch],
      tbar: [btnToSupervisor, /*btnToNextTask, - Пока убрал!*/ {xtype: 'tbfill'}, 'Процесс: 1410 Корпоративные клиенты России', /*suggestProcess, resetProcessButton,*/ {xtype: 'tbseparator'}, 'Заявка:', txtSearch, btnTextClear, btnSearch],
	  //Config 'Bottom Bar'
      bbar: pagingUser,

      style: "margin: 0 auto 0 auto;",
      //width: 550,
      //height: 450, 
      autoHeight: true,
      title: "Manage Cases",

	  //id of the element, a DOM element or an existing Element that this component will be rendered into.
      renderTo: "divMain", //"divMain" определен в caseManagerApplication.html

	  //A config object containing one or more event handlers to be added to this object during initialization.
      listeners: {
		//on row double click
		//Пример из DemoApplication.js (добавил расчет url)
        //rowdblclick: function(grid, index, e) {
          //window.location.href = CONFIG.server + "/sysworkflow/en/neoclassic/cases/open?APP_UID="+storeCase.data.items[index].data.APP_UID+"&DEL_INDEX=1&action=sent";
		  //var url = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');
		  //window.location.href = url + "/sysworkflow/en/neoclassic/cases/open?APP_UID="+storeCase.data.items[index].data.APP_UID+"&DEL_INDEX=1&action=sent";
        //}
      
		//Пример из CreateReportsApplication.js
		//on row double click
		rowdblclick: function (grid, index, e) {
		  var url = location.protocol + "//" + location.hostname + (location.port ? ":" + location.port : "") + "/sysworkflow/ru-RU/neoclassic";
		  
		  //Формируем ссылку (как в Demo)
		  //url = url + "/cases/open?APP_UID=" + storeUser.data.items[index].data.APP_UID + "&DEL_INDEX=1&action=sent";

		  if (storeCase.data.items[index].data.APP_TAS_TITLE == 'Администрирование') {
		    //Формируем универсальную ссылку
		    //url = url + "/cases/opencase/" + storeCase.data.items[index].data.APP_UID;
			
			//Формируем ссылку для Редактирования (можно: "&action=draft")
			url = url + "/cases/open?APP_UID=" + storeCase.data.items[index].data.APP_UID + "&DEL_INDEX=" +storeCase.data.items[index].data.DEL_INDEX + "&action=todo";
		    //console.log ("Редактирования url=" + url);
		  
		    //Открывает в текущем окне
            window.location.href = url;

		  } else {
		    //Формируем ссылку с указанием SUMMARY_UID (как в CreateReports)
		    url = url + "/cases/summary?APP_UID=" + storeCase.data.items[index].data.APP_UID + "&DEL_INDEX=1&DYN_UID=" + storeCase.data.items[index].data.SUMMARY_UID;
		    //console.log("SUMMARY url: " + url);

		    //Открывает в новом окне
            window.open(url);
		  }
		}
	  }
    });

	//A specialized container representing the viewable application area (the browser viewport).
	//Взял образец из: \apps\processmaker\htdocs\shared\compiled\ExtJs\processes_main.js
	/* Пока отключил. НЕ понял для чего надо использовать. *
	var viewport = new Ext.Viewport({
	  layout: 'border',
	  autoScroll: true,
	  items: [
	    grdpnlCase
	  ]
	});
	******************************************************/

    //Initialize events
    storeCaseProcess(pageSize, pageSize, 0);

	//on(eventName, [fn], [scope], [options]): Object. Shorthand for addListener
	//addListener(eventName, [fn], [scope], [options]): Object - Appends an event handler to this object.
	//eventName: String - The name of the event to listen for. May also be an object who's property names are event names.
	//fn: Function (optional)
	//scope: Object (optional) The scope (this reference) in which the handler function is executed. If omitted, defaults to the object which fired the event.
    grdpnlCase.on("rowcontextmenu",
      function (grid, rowIndex, evt) {
        var sm = grid.getSelectionModel();
        sm.selectRow(rowIndex, sm.isSelected(rowIndex));
      },
      this
    );

	//addListener(eventName, [fn], [scope], [options]): Object - Appends an event handler to this object.
	//eventName: String - The name of the event to listen for. May also be an object who's property names are event names.
	//fn: Function (optional)
	//scope: Object (optional) The scope (this reference) in which the handler function is executed. If omitted, defaults to the object which fired the event.
    grdpnlCase.addListener("rowcontextmenu", onMnuContext, this);

	//Заполнеие значения cboPageSize (задается в caseManagerApplication.php)
    cboPageSize.setValue(pageSize); //Изначально равно 15
  }
}

//onReady(fn, scope, options) - Adds a function to be called when the DOM is ready, and all required classes have been loaded.
//If the DOM is ready and all classes are loaded, the passed function is executed immediately.
//fn - Function
//scope - Object. The execution scope (this reference) of the callback function
Ext.onReady(caseManager.application.init, caseManager.application);