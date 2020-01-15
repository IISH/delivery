<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "reservationList.title" "Reservation Overview"/>
</#assign>

<#-- Build the page -->
<@base "${title}">
<h1>${title}</h1>

<ul class="filter-buttons">
  <li>
    <a href="${rc.contextPath}/reservation/?date=${today?string("yyyy-MM-dd")}&amp;status=pending">
    <@_ "reservationList.filterToday" "Show Reservations Today"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reservation/?date=${tomorrow?string("yyyy-MM-dd")}&amp;status=pending">
    <@_ "reservationList.filterTomorrow" "Show Reservations Tomorrow"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reservation/?to_date=${min3months?string("yyyy-MM-dd")}&status=active">
    <@_ "reservationList.filterOldActiveReservations" "Show Old Active
    Reservations"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reservation/?date=${today?string("yyyy-MM-dd")}&status=pending&printed=false&page_len=100">
    <@_ "reservationList.filterNotPrinted" "Show today's not printed reservations"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reservation/">
    <@_ "reservationList.filterEverything" "Show All Reservations"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reservation/materials">
	  <@_ "reservationMaterials.noRequests" "Number of requests"/>
    </a>
  </li>
</ul>


<fieldset class="actions">
<legend>Filter:</legend>
<form action="" method="GET">
  <#-- Generate hidden input for already existing GET vars -->
  <#list RequestParameters?keys as k>
  <#if k!="search" && k!="from_date" && k!="to_date" && k!="status" &&
  k!="page_len" && k!="date" && k!="page" && k!="printed">
  <input type="hidden" name="${k?html}" value="${RequestParameters[k]?html}"/>
  </#if>
  </#list>
  <ul>
    <li>
      <#assign search_value>
        <#if RequestParameters["search"]??>
          ${RequestParameters["search"]?html}
        </#if>
      </#assign>
      <label for="search_filter"><@_ "reservationList.search" "Search"/>
      </label>
      <input type="text" id="search_filter" name="search"
             value="${search_value?trim}" />
    </li>
    <li>
      <label for="status_filter"><@_ "reservation.status" "Status"/></label>

        <input type="radio" id="status_filter" name="status" value=""
        <#if !RequestParameters["status"]?? || RequestParameters["status"] == ""> checked="checked"</#if>>
        <@_ "all" "All"/> &nbsp;
        </input>

        <#list status_types?keys  as k>
        <input type="radio" id="status_filter" name="status" value="${k?lower_case}"
        <#if (RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k)> checked="checked"</#if>>
        <@_ "reservation.statusType.${k}" "${k}" /> &nbsp;
        </input>
        </#list>

    </li>
    <li>
        <@filterSentToPrinter />
    </li>

    <li>
      <#assign from_date_value>
        <#-- The date field has priority over from_date -->
        <#if RequestParameters["date"]??>
          ${RequestParameters["date"]?html}
        <#elseif RequestParameters["from_date"]??>
          ${RequestParameters["from_date"]?html}
        </#if>
      </#assign>
      <label for="from_date_filter"><@_ "reservationList.date" "Date"/>
      </label>
      <strong><@_ "misc.from" "from"/></strong> <input type="text" maxlength="10" size="15" id="from_date_filter" name="from_date"
             value="${from_date_value?trim}" class="filter_date" />

      <#assign to_date_value>
        <#if RequestParameters["to_date"]??>
			${RequestParameters["to_date"]?html}
        </#if>
      </#assign>

      <strong><@_ "misc.to" "to"/> </strong>
      <input type="text" maxlength="10" size="15" id="to_date_filter" name="to_date"
             value="${to_date_value?trim}" class="filter_date" />
    </li>
    <li>
        <@filterResultsPerPage />
    </li>
  </ul>
    <#assign searchLabel>
    <@_ "reservationList.search" "Search"/>
    </#assign>
    <input type="submit" value="${searchLabel}"/>
</form>
</fieldset>

<#assign submitURL>
<@paramUrl {} "/reservation/batchprocess"/>
</#assign>
<#if holdingReservations?size == 0>
<span class="bignote"><@_ "search.notfound" "No results..."/></span>
<#else>
<form action="${submitURL}" method="POST">
<table class="overview">
  <thead>
  <tr>
    <th></th>
    <th></th>
    <th><@_ "holding.record" "Item"/></th>
    <th><@sortLink "signature"><@_ "holding.signature" "Call nr."/></@sortLink></th>
    <th>
      <@sortLink "visitorName"><@_ "reservation.visitorName" "Name"/></@sortLink>
    </th>
    <th><@sortLink "date"><@_ "reservation.date" "Date"/></@sortLink></th>
    <th><@sortLink "printed"><@_ "reservation.printed" "Printed"/></@sortLink></th>
    <th><@sortLink "status"><@_ "reservation.extended.status" "Reservation status"/></@sortLink></th>
    <th><@sortLink "holdingStatus"><@_ "holding.extended.status" "Item status"/></@sortLink></th>
  </tr>
  </thead>
  <tbody>
  <#list holdingReservations as holdingReservation>
	 <#assign holding = holdingReservation.holding>
     <#assign reservation = holdingReservation.reservation>
  <tr>
    <td><input type="checkbox" name="checked" value="${reservation.id?c}:${holding.id?c}"
               class="checkItem" /></td>
    <td>
      <a href="${rc.contextPath}/reservation/${reservation.id?c}">
      <@_ "reservationList.edit" "Administrate"/>
      </a>
    </td>
    <td class="leftAligned">
      ${holding.record.title?html}
	  <#if holdingReservation.comment??> - ${holdingReservation.comment}</#if>
    </td>
	  <td>
      <#if holding.record.parent??>
        ${holding.record.parent.holdings[0].signature?html} /
      </#if>
      ${holding.signature?html}
    </td>
    <td>${reservation.visitorName?html}</td>
      <td>${reservation.date?string(delivery.dateFormat)}</td>
    <#assign yes>
    <@_ "yes" "Yes"/>
    </#assign>
    <#assign no>
    <@_ "no" "No"/>
    </#assign>
    <td>${holdingReservation.printed?string(yes, no)}</td>
    <td><@_ "reservation.statusType.${reservation.status?string}" "${reservation.status?string}" /></td>
    <#assign holdingActiveRequest = holdingActiveRequests[holding.toString()] ! reservation/>
    <td><@holdingStatus holdingActiveRequests reservation holding/></td>
  </tr>
  </#list>
  </tbody>
</table>

<div class="selectButtons">
  <#assign st><@_ "select_all" "Select All"/></#assign>
  <input type="button" value="${st}" class="selectAll" />
  <#assign st><@_ "select_none" "Select None"/></#assign>
  <input type="button" value="${st}" class="selectNone" />
</div>

<@pageLinks holdingReservationsSize RequestParameters["page_len"]!delivery.requestPageLen RequestParameters["page"]!1 />

<#if _sec.ifAnyGranted("ROLE_RESERVATION_MODIFY,ROLE_RESERVATION_DELETE")>
<fieldset class="actions">
  <legend><@_ "reservationList.withSelectedReservations" "With Selected Reservations"/>:</legend>
  <#assign deleteLabel>
  <@_ "reservationList.delete" "Delete"/>
  </#assign>
  <#assign statusLabel>
  <@_ "reservationList.toStatus" "Change Status"/>
  </#assign>
    <#assign deleteConfirm>
    <@_ "reservationList.confirmDelete" "Deletion of reservations is permanent. Are you sure you want to delete the selected reservations?" />
    </#assign>
    <#assign printForceConfirm>
    <@_ "reservationList.confirmPrintForce" "Are you sure you want to print already printed reservations?"/>
    </#assign>
  <ul>
  <#if _sec.ifAllGranted("ROLE_RESERVATION_MODIFY")>
  <li>
    <select name="newStatus">
      <#list status_types?keys  as k>
      <#if k != "PENDING">
      <option value="${k}"
      <#if RequestParameters["status"]?? &&
           RequestParameters["status"]?upper_case == k>
      selected="selected"</#if>>
      <@_ "reservation.statusType.${k}" "${k}" />
      </option>
      </#if>
      </#list>
    </select>
    <input type="submit" name="changeStatus" value="${statusLabel}" />
    <span class="note"><@_ "reservationList.ignoreStatusBackwards" "Reservation status can only be changed in forward order, i.e. Pending->Active->Completed, but not the other way around."/></span>
  </li>
  </#if>
  <#if _sec.ifAllGranted("ROLE_RESERVATION_DELETE")>
  <li><input type="submit" name="delete" value="${deleteLabel}"
             onClick="return confirm('${deleteConfirm}');" /></li>
  </#if>
  </ul>
</fieldset>

<#if _sec.ifAllGranted("ROLE_RESERVATION_MODIFY")>
  <fieldset class="actions">
    <legend><@_ "reservationList.withSelectedHoldings" "With selected holdings"/>:</legend>

    <#assign printLabel>
      <@_ "reservationList.print" "Print"/>
    </#assign>
    <#assign printForceLabel>
      <@_ "reservationList.printForce" "Print (Including already printed)"/>
    </#assign>
    <#assign statusLabel>
      <@_ "reservationList.toStatus" "Change Status"/>
    </#assign>

    <ul>
      <li><input type="submit" name="print" value="${printLabel}" /> <input type="submit" name="printForce" value="${printForceLabel}" onClick="return confirm('${printForceConfirm}');" /></li>

      <li>
        <select name="newHoldingStatus">
          <#list holding_status_types?keys as k>
            <option value="${k}">
              <@_ "holding.statusType.${k}" "${k}"/>
            </option>
          </#list>
        </select>

        <input type="submit" name="changeHoldingStatus" value="${statusLabel}"/>
      </li>
    </ul>
  </fieldset>
</#if>
</#if>
</#if>
</form>

</@base>
