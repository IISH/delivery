<#include "base.ftl"/>
<#include "form.ftl"/>
<#import "spring.ftl" as spring/>

<#assign title=_("reproductionList.title", "Reproduction Overview")/>

<@base title>
  <h1>${title}</h1>

  <ul class="filter-buttons">
    <li>
      <a href="${rc.contextPath}/reproduction/?date=${today?string("yyyy-MM-dd")}">
        <@spring.message "reproductionList.filterToday"/>
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/?date=${today?string("yyyy-MM-dd")}">
        ${_("reproductionList.filterToday", "Show today's reproductions")}
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/?status=waiting_for_order_details">
        ${_("reproductionList.filterWaitForOrderDetails", "Show reproductions waiting for order details")}
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/?status=active">
        ${_("reproductionList.filterActive", "Show reproductions active for repro")}
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/?status=completed">
        ${_("reproductionList.filterCompleted", "Show reproductions handled by repro (waiting for delivery)")}
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/">
        ${_("reproductionList.filterEverything", "Show All Reproductions")}
      </a>
    </li>
    <li>
      <a href="${rc.contextPath}/reproduction/materials">
        ${_("reproductionMaterials.noRequestsAndTotalAmount", " Number of requests and total amount paid")}
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
            ${_("reproductionList.search", "Search")}
          </label>

          <input type="text" id="search_filter" name="search" value="${RequestParameters["search"]!""?trim}"/>
        </li>

        <li>
          <label for="status_filter">${_("reproduction.status", "Status")}</label>
          <select id="status_filter" name="status">
            <#assign isSelected = !RequestParameters["status"]?? || RequestParameters["status"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
              ${_("reproductionList.allStatus", "Status N/A")}
            </option>

            <#list status_types?keys as k>
              <#assign isSelected = (RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k)/>
              <option value="${k?lower_case}"<#if isSelected> selected="selected"</#if>>
                ${_("reproduction.statusType.${k}", "${k}")}
              </option>
            </#list>
          </select>
        </li>

        <li>
          <label for="printed_filter">
              ${_("reproduction.printed", "Printed")}
          </label>

          <select id="printed_filter" name="printed">
            <#assign isSelected = !RequestParameters["printed"]?? || RequestParameters["printed"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
                ${_("reproductionList.allPrinted", "Printed N/A")}
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
            ${_("reproductionList.dateFrom", "From")}
          </label>

          <input type="text" maxlength="10" id="from_date_filter" name="from_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["from_date"]!"")?trim}"/>
        </li>

        <li>
          <label for="to_date_filter">
            ${_("reproductionList.dateUpTo", "Up To")}
          </label>

          <input type="text" maxlength="10" id="to_date_filter" name="to_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["to_date"]!"")?trim}"/>
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

      <input type="submit" value="${_("reproductionList.search", "Search")}"/>
    </form>
  </fieldset>

  <#assign submitURL><@paramUrl {} "/reproduction/batchprocess"/></#assign>

  <#if holdingReproductions?size == 0>
    <span class="bignote">
      ${_("search.notfound", "No results...")}
    </span>
  <#else>
    <form action="${submitURL}" method="POST">
      <table class="overview">
        <thead>
        <tr>
          <th></th>
          <th></th>
          <th>${_("holding.record", "Item")}</th>
          <th><@sortLink "signature">${_("holding.signature", "Call nr.")}</@sortLink></th>
          <th><@sortLink "customerName">${_("reproduction.customerName", "Name")}</@sortLink></th>
          <th><@sortLink "date">${_("reproduction.date", "Date")}</@sortLink></th>
          <th><@sortLink "printed">${_("reproduction.printed", "Printed")}</@sortLink></th>
          <th><@sortLink "status">${_("reproduction.extended.status.status", "Reproduction status")}</@sortLink></th>
          <th><@sortLink "holdingStatus">${_("holding.extended.status", "Item status")}</@sortLink></th>
        </tr>
        </thead>
        <tbody>
        <#list holdingReproductions as holdingReproduction>
          <#assign holding = holdingReproduction.holding>
          <#assign reproduction = holdingReproduction.reproduction>

          <tr>
            <td>
              <input type="checkbox" name="checked" class="checkItem" value="${reproduction.id?c}:${holding.id?c}"/>
            </td>

            <td>
              <a href="${rc.contextPath}/reproduction/${reproduction.id?c}">
                ${_("reproductionList.show", "Show")}
              </a>
              <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY") && (reproduction.status == "WAITING_FOR_ORDER_DETAILS" || reproduction.status == "HAS_ORDER_DETAILS")>
                /
                <a href="${rc.contextPath}/reproduction/${reproduction.id?c}/edit">
                  ${_("reproductionList.edit", "Edit")}
                </a>
              </#if>
            </td>

            <td class="leftAligned">
              ${holding.record.title}
              <#if holdingReproduction.comment??> - ${holdingReproduction.comment}</#if>
            </td>

            <td>
              <#if holding.record.parent??>
                ${holding.record.parent.holdings[0].signature} /
              </#if>
              ${holding.signature}
            </td>

            <td>${reproduction.customerName}</td>

            <td>${reproduction.creationDate?string(delivery.dateFormat)}</td>

            <td>
              ${holdingReproduction.printed?string(_("yes", "Yes"), _("no", "No"))}
              <#if !holdingReproduction.printed && (reproduction.getStatus() != "CANCELLED")>
                <#if holdingReproduction.isInSor()>
                  <em class="info">(${_("reproduction.print.inSor", "in SOR")})</em>
                <#elseif holdingReproduction.hasOrderDetails() && (reproduction.getStatus() != "ACTIVE")>
                  <em class="info">(${_("reproduction.print.notYetPayed", "not yet paid")})</em>
                </#if>
              </#if>
            </td>

            <td>${_("reproduction.statusType.${reproduction.status?string}", "${reproduction.status?string}")}</td>

            <td><@holdingStatus holdingActiveRequests reproduction holding/></td>
          </tr>
        </#list>
        </tbody>
      </table>

      <div class="selectButtons">
        <input type="button" value="${_("select_all", "Select All")}" class="selectAll"/>
        <input type="button" value="${_("select_none", "Select None")}" class="selectNone"/>
      </div>

      <@pageLinks holdingReproductionsSize RequestParameters["page_len"]!delivery.requestPageLen?number RequestParameters["page"]!1 />

      <#if _sec.ifAnyGranted("ROLE_REPRODUCTION_MODIFY,ROLE_REPRODUCTION_DELETE")>
        <fieldset class="actions">
          <legend>${_("reproductionList.withSelectedReproductions", "With selected reproductions")}:</legend>

          <ul>
            <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
              <li>
                <select name="newStatus">
                    <#list status_types?keys as k>
                      <#if k != "WAITING_FOR_ORDER_DETAILS">
                        <#assign isSelected = RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k/>
                        <option value="${k}"<#if isSelected> selected="selected"</#if>>
                          ${_("reproduction.statusType.${k}", "${k}")}
                        </option>
                      </#if>
                    </#list>
                </select>

                <input type="submit" name="changeStatus" value="${_("reproductionList.toStatus", "Change Status")}"/>
                <input type="submit" name="merge" value="${_("reproductionList.merge", "Merge reproductions")}"/>

                <span class="note">
                  ${_("reproductionList.ignoreStatusBackwards", "The status can only be changed forward (Waiting for order details->Has order details->Confirmed->Payed->Active->Completed->Delivered->Cancelled), not the other way around.")}
                </span>
              </li>
            </#if>

            <#if _sec.ifAllGranted("ROLE_REPRODUCTION_DELETE")>
              <li>
                <input type="submit" name="delete" value="${_("reproductionList.delete", "Delete")}"
                       onClick="return confirm('${_("reproductionList.confirmDelete", "Deletion of reproductions is permanent. Are you sure you want to delete the selected reproductions?")}');"/>
              </li>
            </#if>
          </ul>
        </fieldset>

        <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
          <fieldset class="actions">
            <legend>${_("reproductionList.withSelectedHoldings", "With selected holdings")}:</legend>

            <ul>
              <li>
                <input type="submit" name="print" value="${_("reproductionList.print", "Print")}"/>
                <input type="submit" name="printForce"
                       value="${_("reproductionList.printForce", "Print (Including already printed)")}"
                       onClick="return confirm('${_("reproductionList.confirmPrintForce", "Are you sure you want to print already printed reproductions?")}');"/>
              </li>

              <li>
                <select name="newHoldingStatus">
                  <#list holding_status_types?keys as k>
                    <option value="${k}">
                      ${_("holding.statusType.${k}", "${k}")}
                    </option>
                  </#list>
                </select>

                <input type="submit" name="changeHoldingStatus" value="${_("reproductionList.toStatus" "Change Status")}"/>
              </li>
            </ul>
          </fieldset>
        </#if>
      </#if>
    </form>
  </#if>
</@base>
