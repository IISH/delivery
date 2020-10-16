<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reservationList.title", "Reservation Overview")/>

<@base title>
  <h1>${title}</h1>

  <ul class="filter-buttons">
    <li>
      <a href="${rc.contextPath}/reservation/?date=${today?string("yyyy-MM-dd")}&amp;status=pending">
        ${_("reservationList.filterToday", "Show Reservations Today")}
      </a>
    </li>

    <li>
      <a href="${rc.contextPath}/reservation/?date=${tomorrow?string("yyyy-MM-dd")}&amp;status=pending">
        ${_("reservationList.filterTomorrow", "Show Reservations Tomorrow")}
      </a>
    </li>

    <li>
      <a href="${rc.contextPath}/reservation/?to_date=${min3months?string("yyyy-MM-dd")}&status=active">
        ${_("reservationList.filterOldActiveReservations" "Show Old Active Reservations")}
      </a>
    </li>

    <li>
      <a href="${rc.contextPath}/reservation/?date=${today?string("yyyy-MM-dd")}&status=pending&printed=false&page_len=100">
        ${_("reservationList.filterNotPrinted", "Show today's not printed reservations")}
      </a>
    </li>

    <li>
      <a href="${rc.contextPath}/reservation/">
        ${_("reservationList.filterEverything", "Show All Reservations")}
      </a>
    </li>

    <li>
      <a href="${rc.contextPath}/reservation/materials">
        ${_("reservationMaterials.noRequests", "Number of requests")}
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
            <input type="hidden" name="${k}" value="${RequestParameters[k]}"/>
          </#if>
      </#list>

      <ul>
        <li>
          <label for="search_filter">
              ${_("reservationList.search", "Search")}
          </label>
          <input type="text" id="search_filter" name="search" value="${(RequestParameters["search"]!"")?trim}"/>
        </li>

        <li>
          <label for="status_filter">${_("reservation.status", "Status")}</label>
          <select id="status_filter" name="status">
            <#assign isSelected = !RequestParameters["status"]?? || RequestParameters["status"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
              ${_("reservationList.allStatus", "Status N/A")}
            </option>

            <#list status_types?keys as k>
              <#assign isSelected = (RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k)/>
              <option value="${k?lower_case}"<#if isSelected> selected="selected"</#if>>
                ${_("reservation.statusType.${k}", "${k}")}
              </option>
            </#list>
          </select>
        </li>

        <li>
          <label for="printed_filter">
            ${_("reservation.printed", "Printed")}
          </label>

          <select id="printed_filter" name="printed">
            <#assign isSelected = !RequestParameters["printed"]?? || RequestParameters["printed"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
              ${_("reservationList.allPrinted", "Printed N/A")}
            </option>

            <#assign isSelected = (RequestParameters["printed"]?? && RequestParameters["printed"]?lower_case == "true")/>
            <option value="true"<#if isSelected> selected="selected"</#if>>
              ${_("yes", "Yes")}
            </option>

            <#assign isSelected = (RequestParameters["printed"]?? && RequestParameters["printed"]?lower_case == "false")/>
            <option value="false"<#if isSelected> selected="selected"</#if>>
              ${_("no", "No")}
            </option>
          </select>
        </li>

        <li>
          <label for="from_date_filter">
            ${_("reservationList.dateFrom", "From")}
          </label>

          <input type="text" maxlength="10" id="from_date_filter" name="from_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["from_date"]!"")?trim}"/>
        </li>

        <li>
          <label for="to_date_filter">
            ${_("reservationList.dateUpTo", "Up To")}
          </label>

          <input type="text" maxlength="10" id="to_date_filter" name="to_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["from_date"]!"")?trim}"/>
        </li>

        <li>
          <label for="page_len_filter">
            ${_("pageListHolder.nrResultsPerPage", "Amount of Results per Page")}
          </label>

          <select id="page_len_filter" name="page_len">
            <#list 1..(delivery.requestMaxPageLen?number/delivery.requestPageStepSize?number)?floor as i>
              <#assign pageSize = (i*delivery.requestPageStepSize?number)?floor/>
              <#assign isSelected = (RequestParameters["page_len"]??
                && RequestParameters["page_len"]?number == pageSize)
                || (!RequestParameters["page_len"]?? && delivery.requestPageLen?number == pageSize)/>

              <option value="${pageSize}"<#if isSelected> selected="selected"</#if>>${pageSize}</option>
            </#list>
          </select>
        </li>
      </ul>

      <input type="submit" value="${_("reservationList.search", "Search")}"/>
    </form>
  </fieldset>

  <#assign submitURL><@paramUrl {} "/reservation/batchprocess"/></#assign>

  <#if holdingReservations?size == 0>
    <span class="bignote">${_("search.notfound", "No results...")}</span>
  <#else>
    <form action="${submitURL}" method="POST">
      <table class="overview">
        <thead>
          <tr>
            <th></th>
            <th></th>
            <th>${_("holding.record", "Item")}</th>
            <th><@sortLink "signature">${_("holding.signature", "Call nr.")}</@sortLink></th>
            <th><@sortLink "visitorName">${_("reservation.visitorName", "Name")}</@sortLink></th>
            <th><@sortLink "date">${_("reservation.date", "Date")}</@sortLink></th>
            <th><@sortLink "printed">${_("reservation.printed", "Printed")}</@sortLink></th>
            <th><@sortLink "status">${_("reservation.extended.status", "Reservation status")}</@sortLink></th>
            <th><@sortLink "holdingStatus">${_("holding.extended.status", "Item status")}</@sortLink></th>
          </tr>
        </thead>

        <tbody>
          <#list holdingReservations as holdingReservation>
            <#assign holding = holdingReservation.holding>
            <#assign reservation = holdingReservation.reservation>

            <tr>
              <td>
                <input type="checkbox" name="checked" value="${reservation.id?c}:${holding.id?c}" class="checkItem"/>
              </td>

              <td>
                <a href="${rc.contextPath}/reservation/${reservation.id?c}">
                  ${_("reservationList.edit", "Administrate")}
                </a>
              </td>

              <td class="leftAligned">
                ${holding.record.title}
                <#if holdingReservation.comment??> - ${holdingReservation.comment}</#if>
              </td>

              <td>
                <#if holding.record.parent??>
                  ${holding.record.parent.holdings[0].signature} /
                </#if>
                ${holding.signature}
              </td>

              <td>${reservation.visitorName}</td>
              <td>${reservation.date?string(delivery.dateFormat)}</td>
              <td>${holdingReservation.printed?string(_("yes", "Yes"), _("no", "No"))}</td>
              <td>${_("reservation.statusType.${reservation.status?string}", "${reservation.status?string}")}</td>

              <#assign holdingActiveRequest = holdingActiveRequests[holding.toString()] ! reservation/>
              <td><@holdingStatus holdingActiveRequests reservation holding/></td>
            </tr>
          </#list>
        </tbody>
      </table>

      <div class="selectButtons">
        <input type="button" value="${_("select_all", "Select All")}" class="selectAll"/>
        <input type="button" value="${_("select_none", "Select None")}" class="selectNone"/>
      </div>

      <@pageLinks holdingReservationsSize RequestParameters["page_len"]!delivery.requestPageLen RequestParameters["page"]!1/>

      <#if _sec.ifAnyGranted("ROLE_RESERVATION_MODIFY,ROLE_RESERVATION_DELETE")>
        <fieldset class="actions">
          <legend>${_("reservationList.withSelectedReservations", "With Selected Reservations")}:</legend>

          <ul>
            <#if _sec.ifAllGranted("ROLE_RESERVATION_MODIFY")>
              <li>
                <select name="newStatus">
                  <#list status_types?keys  as k>
                    <#if k != "PENDING">
                      <#assign isSelected = RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k/>
                      <option value="${k}"<#if isSelected> selected="selected"</#if>>
                        ${_("reservation.statusType.${k}", "${k}")}
                      </option>
                    </#if>
                  </#list>
                </select>

                <input type="submit" name="changeStatus" value="${_("reservationList.toStatus", "Change Status")}"/>
                <span class="note">${_("reservationList.ignoreStatusBackwards", "Reservation status can only be changed in forward order, i.e. Pending->Active->Completed, but not the other way around.")}</span>
              </li>
            </#if>

            <#if _sec.ifAllGranted("ROLE_RESERVATION_DELETE")>
              <li><input type="submit" name="delete" value="${_("reservationList.delete", "Delete")}"
                         onClick="return confirm('${_("reservationList.confirmDelete", "Deletion of reservations is permanent. Are you sure you want to delete the selected reservations?")}');"/>
              </li>
            </#if>
          </ul>
        </fieldset>

        <#if _sec.ifAllGranted("ROLE_RESERVATION_MODIFY")>
          <fieldset class="actions">
            <legend>${_("reservationList.withSelectedHoldings", "With selected holdings")}:</legend>

            <ul>
              <li>
                <input type="submit" name="print" value="${_("reservationList.print", "Print")}"/>
                <input type="submit" name="printForce"
                       value="${_("reservationList.printForce", "Print (Including already printed)")}"
                       onClick="return confirm('${_("reservationList.confirmPrintForce", "Are you sure you want to print already printed reservations?")}');"/>
              </li>

              <li>
                <select name="newHoldingStatus">
                  <#list holding_status_types?keys as k>
                    <option value="${k}">
                      ${_("holding.statusType.${k}", "${k}")}
                    </option>
                  </#list>
                </select>

                <input type="submit" name="changeHoldingStatus" value="${_("reservationList.toStatus", "Change Status")}"/>
              </li>
            </ul>
          </fieldset>
        </#if>
      </#if>
    </#if>
  </form>
</@base>
