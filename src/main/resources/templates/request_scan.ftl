<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title><@_ "scan.title" "Scan Items"/></#assign>

<#assign yes>
  <@_ "yes" "Yes" />
</#assign>
<#assign no>
  <@_ "no" "No" />
</#assign>

<@base title>
<h1>${title}</h1>
  <@form_plain "" "scan">
  <label for="scanid">
    <@_ "scan.id" "ID:"/>
  </label>
  <input type="text" id="scanid" name="id" value=""/>
    <@submit "scan"/>

    <#if error??>
      <ul class="errors">
        <li>
          <@_ "scan.error."+error error />.
        </li>
      </ul>
    <#elseif holding??>
      <ul class="messages">
        <li>
          &quot;${holding.record.title?html} - ${holding.identity?html}&quot;
          <@_ "scan.changedFrom" "has changed from status" />
          &quot;<@_ "holding.statusType.${oldStatus?string}" oldStatus?string/>&quot;
          <@_ "scan.changedTo" "to" />
          &quot;<@_ "holding.statusType.${holding.status?string}" holding.status?string/>&quot;.
        </li>
      </ul>

      <#if reservation??>
        <@reservationDetails reservation/>
      </#if>
      <#if reproduction??>
        <@reproductionDetails reproduction/>
      </#if>
    </#if>
  </@form_plain>
</@base>

<#macro reservationDetails reservation>
  <h3><@_ "scan.assocReservation" "Reservation details"/></h3>

  <ul class="reservationDetails">
    <li><span><@_ "reservation.id" "Reservation"/></span> ${reservation.id?c}</li>
    <li><span><@_ "reservation.visitorName" "Name"/></span> ${reservation.visitorName?html}</li>
    <li><span><@_ "reservation.visitorEmail" "E-mail"/></span> ${reservation.visitorEmail?html}</li>
    <li><span><@_ "reservation.date" "Date"/></span> ${reservation.date?string(delivery.dateFormat)}</li>
    <li>
      <span><@_ "reservation.status" "Status"/></span> <@_ "reservation.statusType.${reservation.status?string}" reservation.status?string/>
    </li>
  </ul>

  <table class="records">
    <caption><@_ "reservation.holdings" "Holdings"/></caption>
    <thead>
    <tr>
      <th>ID</th>
      <th><@_ "record.title" "Titel"/></th>
      <th><@_ "reservation.printed" "Printed"/></th>
      <th><@_ "holding.status" "Status"/></th>
    </tr>
    </thead>
    <tbody>
      <#list reservation.holdingReservations as hr>
        <#assign h = hr.holding>
      <tr>
        <td>${h.id?c}</td>
        <td>${hr.toShortString()?html}</td>
        <td>${hr.printed?string(yes, no)}</td>
        <td><@holdingStatus holdingActiveRequests reservation h/></td>
      </tr>
      </#list>
    </tbody>
  </table>
</#macro>

<#macro reproductionDetails reproduction>
<h3><@_ "scan.assocReproduction" "Reproduction details"/></h3>

<ul class="reproductionDetails">
  <li><span><@_ "reproduction.id" "Reproduction"/></span> ${reproduction.id?c}</li>
  <li><span><@_ "reproduction.customerName" "Name"/></span> ${reproduction.customerName?html}</li>
  <li><span><@_ "reproduction.customerEmail" "E-mail"/></span> ${reproduction.customerEmail?html}</li>

  <li class="spacing">
    <span><@_ "reproduction.creationDate" "Created on"/></span> ${reproduction.creationDate?string(delivery.dateFormat)}
  </li>
  <li>
    <span><@_ "reproduction.status" "Status"/></span> <@_ "reproduction.statusType.${reproduction.status}" reproduction.status?string />
  </li>

  <#if reproduction.comment??>
    <li>
      <span><@_ "reproduction.comment" "Comment"/></span>
    ${reproduction.comment?html}
    </li>
  </#if>

  <#if reproduction.order??>
    <li class="spacing">
      <span><@_ "reproduction.order" "Order id (PayWay)"/></span>
    ${reproduction.order.id?c} <i>(<@_ "order.payed.${reproduction.order.payed}" "" />)</i>
    </li>

    <li>
      <span><@_ "reproduction.total" "Total amount"/></span>
      &euro; ${reproduction.order.amountAsBigDecimal?string("0.00")}
    </li>

    <#if reproduction.order.payed == 2 || reproduction.order.payed == 3>
      <li>
        <span><@_ "reproduction.refunded" "Total refunded amount"/></span>
        &euro; ${reproduction.order.refundedAmountAsBigDecimal?string("0.00")}
      </li>
    </#if>
  </#if>

  <#if reproduction.discountPercentage gt 0>
    <li class="spacing">
      <span><@_ "reproduction.discountPercentage" "Discount"/></span>
     ${reproduction.discountPercentage} &percnt;
    </li>
  </#if>

  <#list reproduction.holdingReproductions as hr>
    <#assign h = hr.holding>
    <#assign info = h.record.externalInfo>

    <li class="spacing">
      <#if hr.standardOption??>
        <h3>${hr.standardOption.optionName?html} - Holding ${h.id?c}</h3>
      <#else>
        <h3><@_ "reproduction.customReproduction.backend" "Custom reproduction"/> - Holding ${h.id?c}</h3>
      </#if>

      <ul class="holdingDetails">
        <li>
          <span><@_ "record.externalInfo.title" "Title"/></span>
        ${info.title?html}
        </li>

        <#if info.author??>
          <li>
            <span><@_ "record.externalInfo.author" "Author"/></span>
          ${info.author?html}
          </li>
        </#if>

        <#if info.displayYear??>
          <li>
            <span><@_ "record.externalInfo.displayYear" "Year"/></span>
          ${info.displayYear?html}
          </li>
        </#if>

        <li class="spacing">
          <span><@_ "record.externalInfo.materialType" "Material Type"/></span>
          <@_ "record.externalInfo.materialType.${info.materialType}" ""/>
        </li>

        <li>
          <span><@_ "holding.signature" "Signature"/></span>
          ${h.signature?html}
        </li>

        <li>
          <span><@_ "holding.pid" "Item PID"/></span>
          ${h.determinePid()?html}
        </li>

        <li>
          <span>PID</span>
          <#if _sec.ifAllGranted("ROLE_RECORD_MODIFY")>
            <a target="_blank"
               href="${rc.contextPath}/record/editform/${h.record.pid?url}">${h.record.pid?html}</a>
          <#else>
          ${h.record.pid?html}
          </#if>
        </li>

        <li class="spacing">
          <span><@_ "reproduction.printed" "Printed"/></span>
          ${hr.printed?string(yes, no)}
          <#if !hr.printed && (reproduction.getStatus() != "CANCELLED")>
            <#if hr.isInSor()>
              <em class="info">(<@_ "reproduction.print.inSor" "in SOR"/>)</em>
            <#elseif hr.hasOrderDetails() && (reproduction.getStatus() != "ACTIVE")>
              <em class="info">(<@_ "reproduction.print.notYetPayed" "not yet paid"/>)</em>
            </#if>
          </#if>
        </li>

        <li>
          <span><@_ "holding.status" "Status"/></span>
          <@holdingStatus holdingActiveRequests reproduction h/>
        </li>

        <li class="spacing">
          <span><@_ "reproductionStandardOption.price" "Price"/></span>
          <#if hr.price??>
            <@holdingPrice hr.price hr.completePrice info.materialType hr.numberOfPages/>
          <#else>
            <@_ "tbd" "To be determined"/>
          </#if>
        </li>

        <#if hr.price??>
          <li>
            <span><@_ "holdingReproductions.discount" "Computed discount"/></span>
            &euro; ${hr.discount?string("0.00")}
          </li>

          <li>
            <span><@_ "holdingReproductions.btw" "Computed BTW"/></span>
            &euro; ${hr.btwPrice?string("0.00")} (${hr.btwPercentage}&percnt;)
          </li>
        </#if>

        <li class="spacing">
          <span><@_ "reproductionStandardOption.deliveryTime" "Estimated delivery time"/></span>
          <#if hr.deliveryTime??>
          ${hr.deliveryTime?html} <@_ "days" "days"/>
          <#else>
            <@_ "tbd" "To be determined"/>
          </#if>
        </li>

        <#if hr.comment??>
          <li>
            <span><@_ "holdingReproductions.comment" "Comment" /> </span>
          ${hr.comment?html}
          </li>
        </#if>

        <#if !hr.standardOption??>
          <li class="spacing">
            <span><@_ "reproduction.customReproductionCustomer.backend" "Customer's wish" /> </span>
            <em>${hr.customReproductionCustomer?html}</em>
          </li>

          <#if hr.customReproductionReply??>
            <li class="spacing">
              <span><@_ "reproduction.customReproductionReply" "Reply on wish" /> </span>
              <em>${hr.customReproductionReply?html}</em>
            </li>
          </#if>
        </#if>
      </ul>
    </li>
  </#list>
</ul>
</#macro>
