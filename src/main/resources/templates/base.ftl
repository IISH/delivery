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
    <link rel="shortcut icon" type="image/x-icon" href="${rc.contextPath}/logo.ico" />
    <style>
        header.main {
          background: url("${rc.contextPath}/css/images/iish_logo_${rc.locale}.jpg") no-repeat 50px 10px;
        }
    </style>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link type="text/css" href="${rc.contextPath}/css/jquery-ui.min.css" rel="stylesheet" />
    <script type="text/javascript" src="${rc.contextPath}/js/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/js/jquery-ui.min.js"></script>
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
            <span><@_ "language" "Language"/></span>
            <ul>
              <li><a href="<@paramUrl {"locale" : "nl"} />">NL</a></li>
              <li><a href="<@paramUrl {"locale" : "en"} />">EN</a></li>
            </ul>
          </div>
        </#if>
    </header>
    <nav class="main">
    </nav>
</#macro>

<#macro heading>
    <header class="main">
      <a href="/"><h1>Delivery</h1></a>
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
            <a href="${rc.contextPath}/reservation/?from_date=${.now?string("yyyy-MM-dd")}&amp;status=pending">
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
            <a href="${rc.contextPath}/permission/?permission=null">
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

<#macro filterSentToPrinter>
	<label for="printed_filter"><@_ "reservation.printed" "Sent to printer"/></label>

	<input type="radio" id="printed_filter" name="printed" value=""
            <#if !RequestParameters["printed"]?? || RequestParameters["printed"] == ""> checked="checked"</#if>>
    <@_ "yesno" "Yes/No"/> &nbsp;
	</input>

	<input type="radio" id="printed_filter" name="printed" value="true"
            <#if (RequestParameters["printed"]?? && RequestParameters["printed"]?lower_case == "true")> checked="checked"</#if>>
    <@_ "yes" "Yes" /> &nbsp;
	</input>

	<input type="radio" id="printed_filter" name="printed" value="false"
            <#if (RequestParameters["printed"]?? && RequestParameters["printed"]?lower_case == "false")> checked="checked"</#if>>
        <@_ "no" "No" />
	</input>
</#macro>

<#macro filterResultsPerPage>
	<label for="page_len_filter">
        <@_ "pageListHolder.nrResultsPerPage" "Results per page"/>
	</label>

    <#list delivery.requestPageSteps as i>
		<input type="radio" id="page_len_filter" name="page_len" value="${i}"
    <#if (RequestParameters["page_len"]?? && RequestParameters["page_len"]?number == i) || (!RequestParameters["page_len"]?? && delivery.requestPageLen?number == i)>
		checked="checked"</#if>>${i} &nbsp; </input>
    </#list>
</#macro>

<#macro filterPermissionsORG>
	<label for="permission_filter"><@_ "recordPermission.granted" "Permission"/></label>

	<select id="permission_filter" name="permission">
		<option value=""
				<#if !RequestParameters["permission"]?? ||
		RequestParameters["permission"] == "">
			selected="selected"</#if>>
			<@_ "permissionList.allPermission" "Permission N/A"/>
		</option>

		<option value="null"
				<#if (RequestParameters["permission"]?? &&
		RequestParameters["permission"]?upper_case == 'NULL')>
			selected="selected"</#if>>
			<@_ "recordPermission.granted.null" "To decide" />
		</option>

		<option value="true"
				<#if (RequestParameters["permission"]?? &&
		RequestParameters["permission"]?upper_case == 'TRUE')>
			selected="selected"</#if>>
			<@_ "recordPermission.granted.true" "Granted" />
		</option>

		<option value="false"
				<#if (RequestParameters["permission"]?? &&
		RequestParameters["permission"]?upper_case == 'FALSE')>
			selected="selected"</#if>>
			<@_ "recordPermission.granted.false" "Denied" />
		</option>

	</select>
</#macro>

<#macro filterPermissions>
	<label for="permission_filter"><@_ "recordPermission.granted" "Permission"/></label>

		<input type="radio" id="permission_filter" name="permission" value=""
                <#if !RequestParameters["permission"]?? || RequestParameters["permission"] == "">
			checked="checked"</#if>>
            <@_ "all" "All"/> &nbsp;
		</input>

		<input type="radio" id="permission_filter" name="permission" value="null"
                <#if (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'NULL')>
			checked="checked"</#if>>
            <@_ "recordPermission.granted.null" "To decide" /> &nbsp;
		</input>

		<input type="radio" id="permission_filter" name="permission" value="true"
                <#if (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'TRUE')>
			checked="checked"</#if>>
            <@_ "recordPermission.granted.true" "Granted" /> &nbsp;
		</input>

		<input type="radio" id="permission_filter" name="permission" value="false"
                <#if (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'FALSE')>
			checked="checked"</#if>>
            <@_ "recordPermission.granted.false" "Denied" />
		</input>
</#macro>
