<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign exceptionTitle=_("reservationDateException.title", "New date exception")/>
<#assign overviewTitle=_("reservationDateExceptionOverview.title", "Date exception overview")/>

<#macro border>
  <table cellpadding=3>
    <tr>
      <td>
          <#nested>
      </td>
    </tr>
  </table>
</#macro>

<@preamble exceptionTitle>
  <script type="text/javascript">
      $(document).ready(function () {
          $(".reservation_form .date").datepicker({
              "dateFormat": "yy-mm-dd",
              "minDate": "0",
              "beforeShowDay": function( date ) {
                  let day = date.getDay();
                  return [ ( day > 0 && day < 5 ), "" ];
              }
          });
      });
  </script>
</@preamble>

<@heading/>

<@body>
  <h1>${exceptionTitle}</h1>

  <@form "" "reservationDateException">
    <fieldset class="reservation_form">
      <ul class="form">
        <li><@input "reservationDateException.description" ""/></li>
        <li><@date "reservationDateException.startDate" ""/></li>
        <li><@date "reservationDateException.endDate" ""/></li>
      </ul>
    </fieldset>

    <#if _sec.ifAnyGranted("ROLE_DATE_EXCEPTION_CREATE")>
        <@buttons>
            <@submit "reservationDateException"/>
        </@buttons>
    </#if>
  </@form>

  <h1>${overviewTitle}</h1>

  <@form "" "reservationDateException" >
    <fieldset class="reservation_form">
      <#if reservationDateExceptions?has_content>
        <@border>
          <thead>
            <tr>
              <th></th>
              <th><@sortLink "startDate">${_("reservationDateException.startDate", "")}</@sortLink></th>
              <th>${_("reservationDateException.endDate", "")}</th>
              <th>${_("reservationDateException.description", "")}</th>
            </tr>
          </thead>

          <#list reservationDateExceptions as exception>
            <tr>
              <td><input type="checkbox" name="checked" id=${exception.id?c} value=${exception.id?c}></td>

              <td>${exception.startDate?string(delivery.dateFormat)}</td>

              <#if exception.endDate??>
                <td>${exception.endDate?string(delivery.dateFormat)}</td>
              <#else>
                <td>${"-"}</td>
              </#if>

              <td>${exception.getdescription()}</td>
            </tr>
          </#list>
        </@border>
      <#else>
        ${_("reservationDateExceptionOverview.empty", "No exception dates exist")}
      </#if>
    </fieldset>

    <#if _sec.ifAnyGranted("ROLE_DATE_EXCEPTION_DELETE")>
      <input type="submit" name="deleteDateException"
             value="${_("reservationDateExceptionOverview.submit", "Delete")}"
             onClick="return confirm('${_("reservationDateExceptionOverview.confirmDelete", "")}');"/>
    </#if>
  </@form>
</@body>
