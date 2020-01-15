<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
  <@_ "reproductionList.title" "Reproduction Overview"/>
</#assign>

<#-- Build the page -->
<@base "${title}">
<h1>${title}</h1>

<ul class="filter-buttons">
  <li>
    <a href="${rc.contextPath}/reproduction/?date=${today?string("yyyy-MM-dd")}">
      <@_ "reproductionList.filterToday" "Show today's reproductions"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reproduction/?status=waiting_for_order_details">
      <@_ "reproductionList.filterWaitForOrderDetails" "Show reproductions waiting for order details"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reproduction/?status=active">
      <@_ "reproductionList.filterActive" "Show reproductions active for repro"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reproduction/?status=completed">
      <@_ "reproductionList.filterCompleted" "Show reproductions handled by repro (waiting for delivery)"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reproduction/">
      <@_ "reproductionList.filterEverything" "Show All Reproductions"/>
    </a>
  </li>
  <li>
    <a href="${rc.contextPath}/reproduction/materials">
      <@_ "reproductionMaterials.noRequestsAndTotalAmount" " Number of requests and total amount paid"/>
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

        <label for="search_filter">
          <@_ "reproductionList.search" "Search"/>
        </label>

        <input type="text" id="search_filter" name="search" value="${search_value?trim}"/>
      </li>

      <li>
        <label for="status_filter"><@_ "reproduction.status" "Status"/></label>
        <select id="status_filter" name="status">
          <option value=""
                  <#if !RequestParameters["status"]?? || RequestParameters["status"] == "">selected="selected"</#if>>
            <@_ "reproductionList.allStatus" "Status N/A"/>
          </option>

          <#list status_types?keys as k>
            <option value="${k?lower_case}"
              <#if (RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k)>
                    selected="selected"</#if>>
              <@_ "reproduction.statusType.${k}" "${k}"/>
            </option>
          </#list>
        </select>
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

        <label for="from_date_filter">
          <@_ "reproductionList.date" "Date"/>
        </label>

		  <strong><@_ "misc.from" "from"/></strong> <input type="text" maxlength="10" id="from_date_filter" name="from_date" value="${from_date_value?trim}"
               class="filter_date"/>

        <#assign to_date_value>
          <#if RequestParameters["to_date"]??>
            ${RequestParameters["to_date"]?html}
          </#if>
        </#assign>

		 <strong><@_ "misc.to" "to"/></strong>
        <input type="text" maxlength="10" id="to_date_filter" name="to_date" value="${to_date_value?trim}"
               class="filter_date"/>
      </li>

      <li>
          <@filterResultsPerPage />
      </li>
    </ul>

    <#assign searchLabel>
      <@_ "reproductionList.search" "Search"/>
    </#assign>

    <input type="submit" value="${searchLabel}"/>
  </form>
</fieldset>

<#assign submitURL>
  <@paramUrl {} "/reproduction/batchprocess"/>
</#assign>

<#if holdingReproductions?size == 0>
  <span class="bignote">
    <@_ "search.notfound" "No results..."/>
  </span>
<#else>
  <form action="${submitURL}" method="POST">
    <table class="overview">
      <thead>
      <tr>
        <th></th>
        <th></th>
        <th><@_ "holding.record" "Item"/></th>
        <th><@sortLink "signature"><@_ "holding.signature" "Call nr."/></@sortLink></th>
        <th><@sortLink "customerName"><@_ "reproduction.customerName" "Name"/></@sortLink></th>
        <th><@sortLink "date"><@_ "reproduction.date" "Date"/></@sortLink></th>
        <th><@sortLink "printed"><@_ "reproduction.printed" "Printed"/></@sortLink></th>
        <th><@sortLink "status"><@_ "reproduction.extended.status.status" "Reproduction status"/></@sortLink></th>
        <th><@sortLink "holdingStatus"><@_ "holding.extended.status" "Item status"/></@sortLink></th>
      </tr>
      </thead>
      <tbody>
        <#list holdingReproductions as holdingReproduction>
          <#assign holding = holdingReproduction.holding>
          <#assign reproduction = holdingReproduction.reproduction>

            <tr>
              <td><input type="checkbox" name="checked" class="checkItem"
                         value="${reproduction.id?c}:${holding.id?c}"/></td>
              <td>
                <a href="${rc.contextPath}/reproduction/${reproduction.id?c}">
                  <@_ "reproductionList.show" "Show"/>
                </a>
                <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY") && (reproduction.status == "WAITING_FOR_ORDER_DETAILS" || reproduction.status == "HAS_ORDER_DETAILS")>
                  /
                  <a href="${rc.contextPath}/reproduction/${reproduction.id?c}/edit">
                    <@_ "reproductionList.edit" "Edit"/>
                  </a>
                </#if>
              </td>
              <td class="leftAligned">
              ${holding.record.title?html}
                <#if holdingReproduction.comment??> - ${holdingReproduction.comment}</#if>
              </td>
              <td>
                <#if holding.record.parent??>
                  ${holding.record.parent.holdings[0].signature?html} /
                </#if>
                ${holding.signature?html}
              </td>
              <td>${reproduction.customerName?html}</td>
              <td>${reproduction.creationDate?string(delivery.dateFormat)}</td>

              <#assign yes>
                <@_ "yes" "Yes"/>
              </#assign>
              <#assign no>
                <@_ "no" "No"/>
              </#assign>

              <td>
                ${holdingReproduction.printed?string(yes, no)}
                <#if !holdingReproduction.printed && (reproduction.getStatus() != "CANCELLED")>
                  <#if holdingReproduction.isInSor()>
                    <em class="info">(<@_ "reproduction.print.inSor" "in SOR"/>)</em>
                  <#elseif holdingReproduction.hasOrderDetails() && (reproduction.getStatus() != "ACTIVE")>
                    <em class="info">(<@_ "reproduction.print.notYetPayed" "not yet paid"/>)</em>
                  </#if>
                </#if>
              </td>
              <td><@_ "reproduction.statusType.${reproduction.status?string}" "${reproduction.status?string}" /></td>
              <td><@holdingStatus holdingActiveRequests reproduction holding/></td>
            </tr>
        </#list>
      </tbody>
    </table>

    <div class="selectButtons">
      <#assign st><@_ "select_all" "Select All"/></#assign>
      <input type="button" value="${st}" class="selectAll"/>
      <#assign st><@_ "select_none" "Select None"/></#assign>
      <input type="button" value="${st}" class="selectNone"/>
    </div>

    <@pageLinks holdingReproductionsSize RequestParameters["page_len"]!delivery.requestPageLen?number RequestParameters["page"]!1 />

    <#if _sec.ifAnyGranted("ROLE_REPRODUCTION_MODIFY,ROLE_REPRODUCTION_DELETE")>
      <fieldset class="actions">
        <legend><@_ "reproductionList.withSelectedReproductions" "With selected reproductions"/>:</legend>

        <#assign deleteLabel>
          <@_ "reproductionList.delete" "Delete"/>
        </#assign>
        <#assign statusLabel>
          <@_ "reproductionList.toStatus" "Change Status"/>
        </#assign>
        <#assign deleteConfirm>
          <@_ "reproductionList.confirmDelete" "Deletion of reproductions is permanent. Are you sure you want to delete the selected reproductions?" />
        </#assign>
        <#assign printForceConfirm>
          <@_ "reproductionList.confirmPrintForce" "Are you sure you want to print already printed reproductions?"/>
        </#assign>

        <ul>
          <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
            <li>
              <select name="newStatus">
                <#list status_types?keys as k>
                  <#if k != "WAITING_FOR_ORDER_DETAILS">
                    <option value="${k}"
                      <#if RequestParameters["status"]?? && RequestParameters["status"]?upper_case == k>
                            selected="selected"</#if>>
                      <@_ "reproduction.statusType.${k}" "${k}"/>
                    </option>
                  </#if>
                </#list>
              </select>

              <input type="submit" name="changeStatus" value="${statusLabel}"/>

              <span class="note">
                <@_ "reproductionList.ignoreStatusBackwards" "The status can only be changed forward (Waiting for order details->Has order details->Confirmed->Payed->Active->Completed->Delivered->Cancelled), not the other way around."/>
              </span>
            </li>
          </#if>
          <#if _sec.ifAllGranted("ROLE_REPRODUCTION_DELETE")>
            <li>
              <input type="submit" name="delete" value="${deleteLabel}" onClick="return confirm('${deleteConfirm}');"/>
            </li>
          </#if>
        </ul>
      </fieldset>

      <#if _sec.ifAllGranted("ROLE_REPRODUCTION_MODIFY")>
        <fieldset class="actions">
          <legend><@_ "reproductionList.withSelectedHoldings" "With selected holdings"/>:</legend>

          <#assign printLabel>
            <@_ "reproductionList.print" "Print"/>
          </#assign>
          <#assign printForceLabel>
            <@_ "reproductionList.printForce" "Print (Including already printed)"/>
          </#assign>
          <#assign statusLabel>
            <@_ "reproductionList.toStatus" "Change Status"/>
          </#assign>

          <ul>
            <li>
              <input type="submit" name="print" value="${printLabel}"/>
              <input type="submit" name="printForce" value="${printForceLabel}"
                     onClick="return confirm('${printForceConfirm}');"/>
            </li>

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
  </form>
</#if>

</@base>
