/* 0600 Script Task "Настройка этапов" (setupStages) */
import groovy.json.JsonOutput 
import org.camunda.bpm.engine.delegate.BpmnError
//Использование import groovy.json.JsonBuilder позволяет удобно формировать новые JSON-структуры внутри скрипта Camunda 7
import groovy.json.JsonBuilder

def log = org.slf4j.LoggerFactory.getLogger("${processCode}_setupStages")
def processInstanceId = execution.getProcessInstanceId()
log.info("PROCESS [{}] ---Start Сборка динамического маршрута---", processInstanceId)

def errorMessage = ""

// 1. Определение Типа документа (docType) из метаданных entityMetadata
def entityMetadata = execution.getVariable('entityMetadata')
if (!entityMetadata) {
  errorMessage = "Критическая ошибка: Переменная entityMetadata не найдена в процессе."
  log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
  throw new BpmnError("ERROR", errorMessage)
}

def metaMap = entityMetadata.mapTo(java.util.Map.class)
def docType = (metaMap?.attributes?.ABB0600_REQUEST_TYPE?.code ?: "").toString()
log.info("PROCESS [{}]: Извлечен Тип документа (docType): [{}]", processInstanceId, docType)

if (docType == "") {
  errorMessage = "Критическая ошибка: Не удалось определить код типа документа из entityMetadata."
  log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
  throw new BpmnError("ERROR", errorMessage)
}

// Определение атрибутов Документа, влияющих на формирование этапов
def importNfs   = metaMap?.attributes?.ABB0600_IMPORT_NFS ?: false
def projectCase = metaMap?.attributes?.ABB0600_PROJECT_CASE ?: false
def clientExp   = metaMap?.attributes?.ABB0600_CLIENT_EXPERIENCE ?: false
def ars         = metaMap?.attributes?.ABB0600_ARS ?: false
def procExpert  = metaMap?.attributes?.ABB0600_PROC_EXPERT ?: false
def funcExpert  = metaMap?.attributes?.ABB0600_FUNC_EXPERT ?: false
def regCode     = metaMap?.attributes?.ABB0600_REGION?.code ?: ""
def funcCode    = metaMap?.attributes?.ABB0600_FUNCTION?.code ?: ""

// Инициализация переменных под данные справочников
def stageDataMap = [:]       // Код/Имя этапов (LDB0600_STAGES)
def setupStageDataMap = [:]  // Настройка этапов (LDB0600_SETUP_STAGES)
def requestUserMap = [:]     // Настройка Исполнителей (LDB0600_REQUEST_USERS)
def fullPath = ""

// Общие Local переменные для вызова REST-клиента Симфонии
execution.setVariableLocal("serviceName", "dictionaries")
execution.setVariableLocal("method", "GET")
execution.setVariableLocal("resultVarName", "resultData")

// 2. Получение данных из Справочника LDB0600_STAGES
fullPath = "/private/dictionaries/LDB0600_STAGES/values?onlyData=true&all=true"
execution.setVariableLocal("path", fullPath)
try {
  restClient.execute(execution)
  stageDataMap = execution.getVariable("resultData") 
  log.info("PROCESS [{}] Справочник LDB0600_STAGES успешно загружен", processInstanceId)
} catch (Exception e) {
  stageDataMap = null
  log.error("PROCESS [{}] Ошибка вызова LDB0600_STAGES: {}", processInstanceId, e.getMessage())
}

// 3. Получение данных из Справочника LDB0600_SETUP_STAGES
fullPath = "/private/dictionaries/LDB0600_SETUP_STAGES/values?onlyData=true&all=true"
execution.setVariableLocal("path", fullPath)
try {
  restClient.execute(execution)
  setupStageDataMap = execution.getVariable("resultData") 
  log.info("PROCESS [{}] Справочник LDB0600_SETUP_STAGES успешно загружен", processInstanceId)
} catch (Exception e) {
  setupStageDataMap = null
  log.error("PROCESS [{}] Ошибка вызова LDB0600_SETUP_STAGES: {}", processInstanceId, e.getMessage())
}

// 4. Получение данных из Справочника LDB0600_REQUEST_USERS
fullPath = "/private/dictionaries/LDB0600_REQUEST_USERS/values?onlyData=true&all=true"
execution.setVariableLocal("path", fullPath)
try {
  restClient.execute(execution)
  requestUserMap = execution.getVariable("resultData") 
  log.info("PROCESS [{}] Справочник LDB0600_REQUEST_USERS успешно загружен", processInstanceId)
} catch (Exception e) {
  requestUserMap = null
  log.error("PROCESS [{}] Ошибка вызова LDB0600_REQUEST_USERS: {}", processInstanceId, e.getMessage())
}

// Валидация ответов: если справочники не загрузились, останавливаем процесс бизнес-ошибкой
if (!setupStageDataMap || !stageDataMap || !requestUserMap) {
  errorMessage = "Ошибка загрузки конфигурационных справочников из REST API Симфонии."
  log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
  throw new BpmnError("ERROR", errorMessage)
}

// Безопасное извлечение строк из любых типов ответов (SPIN, Map, String)
def extractDataRows = { responseData ->
  if (responseData == null) return []
  
  // Сценарий 1: Проверяем на CharSequence (захватывает и String, и GStringImpl из логов)
  if (responseData instanceof java.lang.CharSequence) {
    try {
      // Принудительно приводим к java.lang.String перед парсингом
      def jsonStr = responseData.toString()
      def parsed = new groovy.json.JsonSlurper().parseText(jsonStr)
      return parsed?.data ?: []
    } catch (Exception e) {
      org.slf4j.LoggerFactory.getLogger("extractDataRows").error("Ошибка парсинга строки JSON: " + e.getMessage())
      return []
    }
  }
  
  // Сценарий 2: Если пришел объект SPIN (JacksonJsonNode)
  if (responseData instanceof org.camunda.spin.json.SpinJsonNode) {
    if (responseData.hasProp("data")) {
      return responseData.prop("data").mapTo(java.util.List.class)
    }
    return []
  } 
  
  // Сценарий 3: Если пришла готовая Java Map
  if (responseData instanceof java.util.Map) {
    return responseData.data ?: []
  }
  
  return []
}

// Извлекаем строки из справочников
def setupRows = extractDataRows(setupStageDataMap)
def userRows = extractDataRows(requestUserMap)
def stageNameRows = extractDataRows(stageDataMap)

log.info("PROCESS [{}]: Данные успешно извлечены. Строк: setupRows={}, userRows={}, stageNameRows={}", 
    processInstanceId, setupRows.size(), userRows.size(), stageNameRows.size())
    
//Контроль setupRows
//log.info("PROCESS [{}]: Контроль setupRows: {}", processInstanceId, setupRows)

// Шаг 1: Фильтрация этапов по текущему Типу документа и атрибутам Документа
def filteredSetupStages = setupRows.findAll { row ->
  def currentStageCode = row.stage_code?.toString() ?: ""
  
  //Если настройка для другого Типа документа или признак обязательности не равен 'Y', то пропускаем
  if (row.request_code != docType || row.stage_required != 'Y') {
    return false
  
  //Исключение ТРЕХ этапов по признаку importNfs
  } else if (!importNfs && currentStageCode in ['PRODUCT_OWNER', 'CBO_OWNER', 'EXEC']) {
    return false
  
  //Исключение этапа 'Финансовая экспертиза ARS sharing' по признаку ars
  } else if (!ars && currentStageCode == 'FIN_FUNC_ARS') {
    return false
  
  //Исключение этапа 'Экспертиза функции' по признаку funcExpert
  } else if (!funcExpert && currentStageCode == 'FUNC_EXPERT') {
    return false
  
  //Исключение этапа 'Клиентский опыт' по признаку clientExp
  } else if (!clientExp && currentStageCode == 'CLIENT_EXPERIENCE') {
    return false
  
  //Исключение этапа 'Финансовый анализ' по признаку projectCase
  } else if (!projectCase && currentStageCode == 'FIN_ANALYS') {
    return false
  
  //Исключение этапа 'Экспертиза закупок' по признаку procExpert или по значению expertCount
  } else if (currentStageCode == 'PROC_EXPERT') {
    def expertList = metaMap?.attributes?.PROC_EXPERT_LIST ?: []
    def expertCount = expertList.size()
    if (!procExpert || expertCount == 0) {
      log.warn("PROCESS [{}]: Этап PROC_EXPERT исключен. Флаг procExpert=${procExpert}, найдено экспертов в документе=${expertCount}", processInstanceId)
      return false
    }
    return true
  
  //Исключение этапа 'Владелец ИМ' по значению docType
  } else if (docType != 'IC' && currentStageCode == 'IM_OWNER') {
    return false
  
  //Исключение этапа 'Владелец продукта' по значению docType и funcCode
  } else if (docType == 'IC' && funcCode == 'WORK WITH DATA' && currentStageCode == 'PRODUCT_OWNER') {
    return false
  
  //В остальных случаях
  } else {
    return true
  }
}

// Шаг 2: Сортировка этапов по возрастанию stage_num
filteredSetupStages.sort { a, b -> 
  def numA = (a.stage_num as Integer) ?: 0
  def numB = (b.stage_num as Integer) ?: 0
  return numA <=> numB
}

log.info("PROCESS [{}]: Найдено обязательных этапов после фильтрации: {}", processInstanceId, filteredSetupStages.size())

// Структуры для наполнения
def setupStages = [:] // Порядок добавления сохранится (LinkedHashMap)
def initialList = [] // Сюда пишем данные для статической таблицы INITIAL_EXECUTION_LIST

// Кэш профилей сотрудников для предотвращения избыточных вызовов к /private/users
def userProfilesCache = [:]

//START Функция обогащения логина ФИО, Должностью и Департаментом
def fetchUserFormattedName = { uLogin ->
  if (uLogin == null || uLogin == "") return ""
  if (userProfilesCache.containsKey(uLogin)) return userProfilesCache[uLogin]

  execution.setVariableLocal("serviceName", "users")
  execution.setVariableLocal("method", "GET")
  execution.setVariableLocal("resultVarName", "userInfo")
  execution.setVariableLocal("path", "/private/users/${uLogin}/employee".toString())

  def formattedName = uLogin
  try {
    restClient.execute(execution)
    def responseNode = execution.getVariable("userInfo")
    if (responseNode != null) {
      def userData = org.camunda.spin.Spin.S(responseNode).prop("data").mapTo(java.util.Map.class)
      def employee = userData?.employee
      def person   = employee?.person
      def position = employee?.position?.toString() ?: ""
      def dept     = employee?.dept
      def pFullName = person?.fullName?.toString() ?: ""
      def dFullName = dept?.fullName?.toString() ?: ""

      if (pFullName != "") {
        def details = [position, dFullName].findAll { it != null && it != "" }.join(" ")
        formattedName = details != "" ? "${pFullName} (${details})".toString() : pFullName.toString()
      }
    }
  } catch (Exception e) {
      log.error("PROCESS [{}]: Ошибка запроса профиля [{}]: {}", processInstanceId, uLogin, e.getMessage())
  }
  userProfilesCache[uLogin] = formattedName
  return formattedName
}
//END Функция обогащения логина ФИО, Должностью и Департаментом

// Шаг 3: Цикл сборки каждого шага маршрута
filteredSetupStages.each { setupRow ->
  // Принудительно приводим коды к java.lang.String
  def currentStageCode = setupRow.stage_code?.toString() ?: ""
  
  // Мэтчинг человекочитаемого названия этапа из LDB0600_STAGES по коду
  def stageNameRecord = stageNameRows.find { it.code?.toString() == currentStageCode }
  
  // Безопасно формируем имя этапа, исключая GStringImpl через явный .toString()
  def rawStageName = setupRow.stage_name ?: stageNameRecord?.name ?: "Этап ${currentStageCode}"
  def currentStageName = rawStageName.toString()
  
  // Чтение плоских настроек этапа
  def userType = setupRow.user_type?.toString() ?: ""
  def assignmentType = setupRow.assignment_type?.toString() ?: ""
  def workingDays = (setupRow.working_days as Integer) ?: 2
  def btnRevise = setupRow.button_revise?.toString() ?: "N"
  def btnDecline = setupRow.button_decline?.toString() ?: "N"
  
  //Контроль наличия assignmentType равного CANDIDATE_GROUPS или GROUP
  if (assignmentType == 'CANDIDATE_GROUPS' || assignmentType == 'GROUP') {
    errorMessage = "В настройках этапа '${currentStageCode}' используется нереализованный Тип назначения (assignmentType): '${assignmentType}'"
    log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
    throw new BpmnError("ERROR", errorMessage)
  }
  
  //Контроль наличия userType равного NFS
  if (userType == 'NFS') {
    errorMessage = "В настройках этапа '${currentStageCode}' используется нереализованный Способ получение Исполнителей(userType): '${userType}'"
    log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
    throw new BpmnError("ERROR", errorMessage)
  }
  
  //Определение доступны Действий
  def allowedActions = ["TAB0600_ACCEPT"]
  if (btnRevise == "Y") allowedActions.add("TAB0600_REVISE")
  if (btnDecline == "Y") allowedActions.add("TAB0600_DECLINE")
  def actionsString = allowedActions.join(",").toString()

  // Переменные для хранения вычисленных исполнителей в stepConfig (для Camunda)
  def targetAssignee = ""
  def targetCandidateGroup = null
  def targetCandidateUsers = null
  def targetParallelAssignees = []

  // Строка названия этапа в формате "Имя этапа (КОД)" для записи в таблицу Симфонии
  def fullStageNameInTable = "${currentStageName} (${currentStageCode})".toString()
  
  // Промежуточный список для унифицированной сборки ЛОГИНОВ: [[login: '...', tableValue: '...'], ...]
  def collectedUsers = []

  // ==================================================================================
  // СЦЕНАРИЙ А: Если это этап Экспертизы закупок, забираем людей напрямую из метаданных документа
  // ==================================================================================
  if (procExpert && currentStageCode == "PROC_EXPERT") {
    def expertList = metaMap?.attributes?.PROC_EXPERT_LIST ?: []
    
    // Извлекаем логины сотрудников по цепочке: ABB0600_PROC_EXPERT_USER -> login
    def documentLogins = expertList.collect { it?.ABB0600_PROC_EXPERT_USER?.login?.toString() }.findAll { it != null && it != "" }
    
    log.info("PROCESS [{}] documentLogins: {} loginsCount: {}", processInstanceId, documentLogins, documentLogins.size())
    if (documentLogins.size() < 1) {
      errorMessage = "Не заданы эксперты по Закупкам"
      log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
      throw new BpmnError("ERROR", errorMessage)
    }

    // Заполняем переменные для Camunda 7
    if (assignmentType == 'PARALLEL') {
      targetParallelAssignees = documentLogins
    } else if (assignmentType == 'CANDIDATE_USERS') {
      targetCandidateUsers = documentLogins.join(", ").toString()
    } else if (assignmentType == 'CANDIDATE_GROUPS' || assignmentType == 'GROUP') {
      targetCandidateGroup = documentLogins.join(", ").toString()
    } else { // PERSONAL
      targetAssignee = documentLogins ? documentLogins[0] : ""
    }

    // Наполняем промежуточный список collectedUsers для генерации INITIAL_EXECUTION_LIST
    documentLogins.each { uLogin ->
        def uFullName = fetchUserFormattedName(uLogin)
        collectedUsers.add([login: uLogin, tableValue: uFullName])
    }
  } 
  // ==================================================================================
  // СЦЕНАРИЙ Б: Для всех остальных этапов с типом USER тянем данные из LDB0600_REQUEST_USERS
  // ==================================================================================
  else if (userType == 'USER') {
    // Находим ВСЕ строки исполнителей для данного документа и текущего этапа
    def stepUsers = userRows.findAll { uRow ->
      return uRow.request_code?.toString() == docType && uRow.stage_code?.toString() == currentStageCode
    }

    // Заполняем переменные для Camunda 7
    if (assignmentType == 'CANDIDATE_GROUPS' || assignmentType == 'GROUP') {
      def groupUserIds = stepUsers.collect { (it.group_id ?: it.user_id)?.toString() }.findAll { it != null && it != "" }
      targetCandidateGroup = groupUserIds.join(", ").toString() ?: null
    } 
    else if (assignmentType == 'CANDIDATE_USERS') {
      def userIds = stepUsers.collect { it.user_id?.toString() }.findAll { it != null && it != "" }
      targetCandidateUsers = userIds.join(", ").toString() ?: null
    }
    else if (assignmentType == 'PARALLEL') {
      targetParallelAssignees = stepUsers.collect { it.user_id?.toString() }.findAll { it != null && it != "" }
    } 
    else { // PERSONAL
      def firstRecord = stepUsers ? stepUsers[0] : null
      targetAssignee = firstRecord?.user_id?.toString() ?: ""
    }

    // Наполняем промежуточный список collectedUsers для генерации INITIAL_EXECUTION_LIST
    stepUsers.each { flatUser ->
      def uLogin = flatUser.user_id?.toString() ?: ""
      if (uLogin != "") {
        if (assignmentType == 'CANDIDATE_GROUPS' || assignmentType == 'GROUP') {
          def rName = flatUser.name?.toString() ?: flatUser.displayTitle?.toString() ?: flatUser.group_id?.toString() ?: uLogin
          collectedUsers.add([login: uLogin, tableValue: rName])
        } else {
          def uFullName = fetchUserFormattedName(uLogin)
          collectedUsers.add([login: uLogin, tableValue: uFullName])
        }
      }
    }
  }

  // НАПОЛНЕНИЕ ТАБЛИЦЫ INITIAL_EXECUTION_LIST КРАСИВЫМИ ФИО+ДОЛЖНОСТЬ
  if (collectedUsers) {
    // Правило 1: Параллельное выполнение -> физическое дублирование строк с tableValue
    if (assignmentType == 'PARALLEL') {
      collectedUsers.each { userItem ->
        initialList.add([
          "ABB0600_STAGE_NUM"         : setupRow.stage_num?.toString() ?: "0",
          "ABB0600_INITIAL_USER_NAME" : userItem.tableValue.toString(),
          "ABB0600_INITIAL_STAGE_NAME": fullStageNameInTable
        ])
      }
    } 
    // Правило 2: Пользователи-кандидаты -> перечисление tableValue через запятую в одной строке
    else if (assignmentType == 'CANDIDATE_USERS') {
      def allFullNames = collectedUsers.collect { it.tableValue }.findAll { it != null && it != "" }
      initialList.add([
        "ABB0600_STAGE_NUM"         : setupRow.stage_num?.toString() ?: "0",
        "ABB0600_INITIAL_USER_NAME" : allFullNames.join(", ").toString(),
        "ABB0600_INITIAL_STAGE_NAME": fullStageNameInTable
      ])
    }
    // Правило 3: Группы кандидатов / Роли -> перечисление уникальных tableValue
    else if (assignmentType == 'CANDIDATE_GROUPS' || assignmentType == 'GROUP') {
      def allGroupNames = collectedUsers.collect { it.tableValue }.findAll { it != null && it != "" }.unique()
      initialList.add([
        "ABB0600_STAGE_NUM"         : setupRow.stage_num?.toString() ?: "0",
        "ABB0600_INITIAL_USER_NAME" : allGroupNames.join(", ").toString(),
        "ABB0600_INITIAL_STAGE_NAME": fullStageNameInTable
      ])
    }
    // Правило 4: Персональное назначение -> строка одного конкретного tableValue
    else { // PERSONAL
      def firstUser = collectedUsers[0]
      initialList.add([
        "ABB0600_STAGE_NUM"         : setupRow.stage_num?.toString() ?: "0",
        "ABB0600_INITIAL_USER_NAME" : firstUser.tableValue.toString(),
        "ABB0600_INITIAL_STAGE_NAME": fullStageNameInTable
      ])
    }
  }
  else {
    initialList.add([
      "ABB0600_STAGE_NUM"         : setupRow.stage_num?.toString() ?: "0",
      "ABB0600_INITIAL_USER_NAME" : "", 
      "ABB0600_INITIAL_STAGE_NAME": fullStageNameInTable
    ])
  }

  // Формируем результирующий объект шага для внутренней коллекции Multi-Instance Camunda
  def stepConfig = [
    code             : currentStageCode,
    name             : currentStageName,
    stage_num        : setupRow.stage_num,
    user_type        : userType,
    assignment_type  : assignmentType,
    working_days     : workingDays,
    actions          : actionsString,
    assignee         : targetAssignee,
    candidateGroup   : targetCandidateGroup ?: null,
    candidateUsers   : targetCandidateUsers ?: null,
    parallelAssignees: targetParallelAssignees
  ]
  setupStages[currentStageCode] = stepConfig
}

// ПОСТОБРАБОТКА: Сквозной динамический перерасчет номеров строк (с одинаковым именем этапа)
def nextStageNum = 1
def stageNameToNumMap = [:]

initialList.each { row ->
  def stageName = row["ABB0600_INITIAL_STAGE_NAME"] ?: ""
  if (!stageNameToNumMap.containsKey(stageName)) {
    stageNameToNumMap[stageName] = (nextStageNum++).toString()
  }
  row["ABB0600_STAGE_NUM"] = stageNameToNumMap[stageName]
}

// Превращаем значения LinkedHashMap в плоский список List для Camunda Multi-Instance
def stepsCollection = new ArrayList(setupStages.values())

// Шаг 4: Защитная проверка на пустой маршрут
if (stepsCollection.isEmpty()) {
  errorMessage = "Для типа документа ${docType} не настроено ни одного обязательного этапа согласования в LDB0600_SETUP_STAGES.".toString()
  log.error("PROCESS [{}]: {}", processInstanceId, errorMessage)
  throw new BpmnError("ERROR", errorMessage)
}

//Если в форме документа сохранена информация об ошибке, а теперь ошибки нет, то чистим значение ERROR_MESSAGE и ERROR_FLAG_YN (если есть)
def oldErrorValue = metaMap?.attributes?.ERROR_MESSAGE ?: ""
if (oldErrorValue != "" && errorMessage == "") {
  //Вариант 1. С использованием groovy.json.JsonBuilder
  errorJsonMap = new JsonBuilder(["ERROR_MESSAGE": "", "ERROR_FLAG_YN": "N"]).toString() //Ошибка при запуске через через Cockpit
  //Вариант 2. Создаем JSON-строку через встроенный движок Spin
  //errorJsonMap = S("{}").prop("ERROR_MESSAGE", "").prop("ERROR_FLAG_YN", "N").toString()
  execution.setVariableLocal("body", errorJsonMap)
  
  //Общие Local переменные для вызова метода обновления атрибутов Документа
  execution.setVariableLocal("serviceName", "documents")
  execution.setVariableLocal("method", "PUT")
  
  fullPath = "private/documents/${entityGuid}/_attributes"
  execution.setVariableLocal("path", fullPath)
  try {
    //log.info("PROCESS [{}] Clear ERROR_MESSAGE fullPath: {} body: {}", processInstanceId, fullPath, errorJsonMap)
    restClient.execute(execution)
  
  } catch (Exception e) {
    log.info("PROCESS [{}] Error call fullPath: {} body: {}", processInstanceId, fullPath, errorJsonMap)
    errorMessage = "Не удалось очистить атрибут ERROR_MESSAGE"
    //execution.setVariable("errorMessage", errorMessage) //Сохраним значение errorMessage - Лишнее, т.к. значение сохранится в boundaryEvent
    throw new BpmnError("ERROR", errorMessage)
  }  
}

// Шаг 5: Запись результатов в переменные процесса Camunda как чистые Java-объекты
execution.setVariable("steps", stepsCollection) 
execution.setVariable("INITIAL_EXECUTION_LIST", initialList) 

def setupStagesJson = groovy.json.JsonOutput.toJson(setupStages).toString()
execution.setVariable("setupStagesJson", setupStagesJson)

log.info("PROCESS [{}]: initialList (количество строк): {}", processInstanceId, initialList.size())
log.info("PROCESS [{}]: setupStagesJson: {}", processInstanceId, setupStagesJson)
log.info("PROCESS [{}]: ==================================================", processInstanceId)
log.info("PROCESS [{}]: Маршрутизация успешно построена. Всего шагов: {}. Данные сохранены в 'steps'.", processInstanceId, stepsCollection.size())
log.info("PROCESS [{}] ---End Сборка динамического маршрута---", processInstanceId)