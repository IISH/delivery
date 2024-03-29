<#import "spring.ftl" as spring/>
<#include "utils.ftl">

<#macro base title>
  <@preamble title/>
  <@heading/>
  <@body>
    <#nested>
  </@body>
</#macro>

<#macro userbase title disableLanguage=false>
  <@preamble title/>
  <@userHeading disableLanguage/>
  <@body>
    <#nested>
  </@body>
</#macro>

<#macro printbase title>
  <@preamble title/>
  <@body>
    <#nested>
  </@body>
</#macro>

<#macro preamble title>
  <!DOCTYPE html>
  <html>
  <head>
    <title>${title} - Delivery</title>
    <link rel="stylesheet" media="all" href="${rc.contextPath}/css/screen.css"/>
    <link rel="shortcut icon" type="image/x-icon" href="${rc.contextPath}/logo.ico"/>
    <style>
        header.main {
            background: url("${rc.contextPath}/css/images/iish_logo_${rc.locale}.jpg") no-repeat 50px 10px;
        }
        <#if profile??>body { background-color: ${profile} }</#if>
    </style>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link type="text/css" href="${rc.contextPath}/css/jquery-ui.min.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/css/style.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="${rc.contextPath}/js/jquery-3.6.0.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/js/jquery-ui.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/js/jstree.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/js/delivery.js"></script>
    <#-- Locale dependent javascript -->
    <script type="text/javascript" src="${rc.contextPath}/js/delivery.locale.${rc.locale}.js"></script>
    <#nested>
  </head>
  <body>
</#macro>

<#macro userHeading disableLanguage=false>
  <header class="main">
    <a href="/"><h1>Delivery</h1></a>
    <#if !disableLanguage>
      <div class="languageSelect">
        <span>${_("language", "Language")}</span>
        <ul>
          <li><a href="<@paramUrl {"locale" : "nl"} />">NL</a></li>
          <li><a href="<@paramUrl {"locale" : "en"} />">EN</a></li>
        </ul>
      </div>
    </#if>

    <#if gitCommitId??><div class="release"><a href="https://github.com/IISH/delivery/commit/${gitCommitId}" target="_blank">${gitClosestTagName}</a></div></#if>
  </header>

  <nav class="main"></nav>
</#macro>

<#macro heading>
  <header class="main">
    <a href="/"><h1>Delivery</h1></a>
    <div class="languageSelect">
      <span>${_("language", "Language")}</span>
      <ul>
        <li><a href="<@paramUrl {"locale" : "nl"} />">NL</a></li>
        <li><a href="<@paramUrl {"locale" : "en"} />">EN</a></li>
      </ul>
    </div>
    <#if gitCommitId??><div class="release"><a href="https://github.com/IISH/delivery/commit/${gitCommitId}" target="_blank">${gitClosestTagName}</a></div></#if>
  </header>

  <nav class="main">
    <ul>
      <#if _sec.ifAllGranted("ROLE_RESERVATION_VIEW")>
        <li>
          <a href="${rc.contextPath}/reservation/?date=${.now?string("yyyy-MM-dd")}&amp;status=PENDING">
          ${_("reservationList.title", "Reservation Overview")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_RESERVATION_CREATE")>
        <li>
          <a href="${rc.contextPath}/reservation/masscreateform">
            ${_("reservationMassCreate.title", "New Reservation")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAnyGranted("ROLE_RESERVATION_MODIFY,ROLE_REPRODUCTION_MODIFY")>
        <li>
          <a href="${rc.contextPath}/request/scan">
          ${_("scan.title", "Scan Items")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_RECORD_MODIFY")>
        <li>
          <a href="${rc.contextPath}/record/">
            ${_("homerecord.title", "Edit Records")}
          </a>
        </li>
      </#if>
      <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
        <li>
          <a href="${rc.contextPath}/record/createform">
            ${_("newrecord.title", "Create Record")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_PERMISSION_VIEW")>
        <li>
          <a href="${rc.contextPath}/permission/">
            ${_("permissionList.title", "Permission Request Overview")}
          </a>
        </li>
      </#if>

      <br>

      <#if _sec.ifAllGranted("ROLE_REPRODUCTION_VIEW")>
        <li>
          <a href="${rc.contextPath}/reproduction/?date=${.now?string("yyyy-MM-dd")}">
            ${_("reproductionList.title", "Reproduction Overview")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_REPRODUCTION_CREATE")>
        <li>
          <a href="${rc.contextPath}/reproduction/masscreateform">
            ${_("reproductionMassCreate.title", "New Reproduction")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
        <li>
          <a href="${rc.contextPath}/reproduction/standardoptions">
            ${_("reproductionStandardOption.title", "Standard reproduction options")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_USER_MODIFY")>
        <li>
          <a href="${rc.contextPath}/user/">
            ${_("userList.title", "User Management")}
          </a>
        </li>
      </#if>

      <#if _sec.ifAllGranted("ROLE_DATE_EXCEPTION_VIEW")>
        <li>
          <a href="${rc.contextPath}/reservation_date_exception/date_exception">
            ${_("reservationDateException.title", "New date exception")}
          </a>
        </li>
      </#if>

      <#if _sec.ifNotGranted("ROLE_ANONYMOUS")>
        <li>
          <a href="${rc.contextPath}/user/logout">
            ${_("userList.logout", "Logout")}
          </a>
        </li>
      </#if>
    </ul>

    <div></div>
  </nav>
</#macro>

<#macro body>
    <section>
      <#nested>
    </section>
  </body>
</html>
</#macro>
