<#--

    Copyright (C) 2013 International Institute of Social History

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<#import "spring.ftl" as spring/>
<#include "localization.ftl">
<#include "utils.ftl">

<#macro base title>
  <@preamble title/>
  <@heading/>
  <@body>
    <#nested>
  </@body>
</#macro>

<#macro userbase title>
  <@preamble title/>
  <@userHeading/>
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
    <link rel="stylesheet" media="all" href="${rc.contextPath}/resources/css/screen.css"/>
    <link rel="shortcut icon" type="image/x-icon" href="${rc.contextPath}/resources/logo.ico" />
    <style>
        header.main {
            background-image: url("${rc.contextPath}/resources/css/images/iish_logo_${rc.locale}.jpg");
            background-repeat: no-repeat;
            background-position: 50px 10px;
        }
    </style>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link type="text/css" href="${rc.contextPath}/resources/css/jquery-ui.min.css" rel="stylesheet" />
    <script type="text/javascript" src="${rc.contextPath}/resources/js/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/resources/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/resources/js/delivery.js"></script>
    <#-- Locale dependent javascript -->
    <script type="text/javascript" src="${rc.contextPath}/resources/js/delivery.locale.${rc.locale}.js"></script>
    <#nested>
  </head>
  <body>
</#macro>


<#macro userHeading>
    <header class="main">
      <h1>Delivery</h1>
        <div class="languageSelect">
        <span><@_ "language" "Language"/></span>
        <ul>
          <li><a href="<@paramUrl {"locale" : "nl"} />">NL</a></li>
          <li><a href="<@paramUrl {"locale" : "en"} />">EN</a></li>
        </ul>
      </div>
    </header>
    <nav class="main">
    </nav>

</#macro>
<#macro heading>
    <header class="main">
      <h1>Delivery</h1>
      <div class="languageSelect">
        <span><@_ "language" "Language"/></span>
        <ul>
          <li><a href="<@paramUrl {"locale" : "nl"} />">NL</a></li>
          <li><a href="<@paramUrl {"locale" : "en"} />">EN</a></li>
        </ul>
      </div>
    </header>
    <nav class="main">
      <ul>
        <#if  _sec.ifAllGranted("ROLE_RESERVATION_VIEW")>
          <li>
            <a href="${rc.contextPath}/reservation/?date=${.now?string("yyyy-MM-dd")}&amp;status=PENDING">
            <@_ "reservationList.title" "Reservation Overview"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_RESERVATION_CREATE")>
          <li>
            <a href="${rc.contextPath}/reservation/masscreateform">
              <@_ "reservationMassCreate.title" "New Reservation"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAnyGranted("ROLE_RESERVATION_MODIFY,ROLE_REPRODUCTION_MODIFY")>
          <li>
            <a href="${rc.contextPath}/request/scan">
              <@_ "scan.title" "Scan Items"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_RECORD_MODIFY")>
          <li>
            <a href="${rc.contextPath}/record/">
              <@_ "homerecord.title" "Edit Records"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_PERMISSION_VIEW")>
          <li>
            <a href="${rc.contextPath}/permission/">
              <@_ "permissionList.title" "Permission Request Overview"/>
            </a>
          </li>
        </#if>
        <br>
        <#if  _sec.ifAllGranted("ROLE_REPRODUCTION_VIEW")>
          <li>
            <a href="${rc.contextPath}/reproduction/?date=${.now?string("yyyy-MM-dd")}">
              <@_ "reproductionList.title" "Reproduction Overview"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_REPRODUCTION_CREATE")>
          <li>
            <a href="${rc.contextPath}/reproduction/masscreateform">
              <@_ "reproductionMassCreate.title" "New Reproduction"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
          <li>
            <a href="${rc.contextPath}/reproduction/standardoptions">
              <@_ "reproductionStandardOption.title" "Standard reproduction options"/>
            </a>
          </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_USER_MODIFY")>
        <li>
          <a href="${rc.contextPath}/user/">
          <@_ "userList.title" "User Management"/>
          </a>
        </li>
        </#if>
        <#if  _sec.ifAllGranted("ROLE_DATE_EXCEPTION_VIEW")>
        <li>
          <a href="${rc.contextPath}/reservation_date_exception/date_exception">
          <@_ "reservationDateException.title" "New date exception"/>
          </a>
        </li>
        </#if>
        <#if  _sec.ifNotGranted("ROLE_ANONYMOUS")>
        <li>
          <a href="${rc.contextPath}/user/logout">
          <@_ "userList.logout" "Logout"/>
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
