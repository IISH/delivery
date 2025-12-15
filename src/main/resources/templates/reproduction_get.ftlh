<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reproductionSingle.title", "Reproduction") + " " + reproduction.id?c/>

<@base title>
  <h1>${title}</h1>

  <#if error??>
    <div class="errors">${_("reproduction.error."+error, error)}</div>
  </#if>

  <ul class="reproductionDetails">
    <li><span>${_("reproduction.customerName", "Name")}</span> ${reproduction.customerName}</li>
    <li><span>${_("reproduction.customerEmail", "E-mail")}</span> ${reproduction.customerEmail}</li>

    <li class="spacing">
      <span>${_("reproduction.creationDate", "Created on")}</span>
      ${reproduction.creationDate?string(delivery.dateFormat)}
    </li>

    <li>
      <span>${_("reproduction.status", "Status")}</span>
      ${_("reproduction.statusType.${reproduction.status}" reproduction.status?string)}
    </li>

    <#if reproduction.comment??>
      <li>
        <span>${_("reproduction.comment", "Comment")}</span>
        ${reproduction.comment}
      </li>
    </#if>

    <#if reproduction.order??>
      <li class="spacing">
        <span>${_("reproduction.order", "Order id")}</span>
        ${reproduction.order.id} <i>(${_("order.payed.${reproduction.order.payed}", "")})</i>
      </li>

      <li>
        <span>${_("reproduction.total", "Total amount")}</span>
        &euro; ${reproduction.order.amountAsBigDecimal?string("0.00")}
      </li>

      <#if reproduction.order.payed == 2 || reproduction.order.payed == 3>
        <li>
          <span>${_("reproduction.refunded", "Total refunded amount")}</span>
          &euro; ${reproduction.order.refundedAmountAsBigDecimal?string("0.00")}
        </li>
      </#if>
    </#if>

    <#if reproduction.discountPercentage gt 0>
      <li class="spacing">
        <span>${_("reproduction.discountPercentage", "Discount")}</span>
        ${reproduction.discountPercentage} &percnt;
      </li>
    </#if>

    <li class="spacing">
      <a href="${rc.contextPath}/reproduction/${reproduction.id?c}/invoice">
        ${_("reproductionSingle.invoice")}
      </a>
    </li>

    <#if _sec.ifAllGranted("ROLE_REPRODUCTION_CREATE")>
      <li>
        <a href="${rc.contextPath}/reproduction/masscreateform?fromReproductionId=${reproduction.id?c}">
          ${_("reproductionSingle.newReproduction")}
        </a>
      </li>
    </#if>

    <#if _sec.ifAllGranted("ROLE_RESERVATION_CREATE,ROLE_REPRODUCTION_MODIFY")>
      <li>
        <a href="${rc.contextPath}/reproduction/${reproduction.id?c}/convert">
          ${_("reproductionSingle.convert")}
        </a>
      </li>
    </#if>

    <#list reproduction.holdingReproductions as hr>
      <#assign h = hr.holding>
      <#assign info = h.record.externalInfo>

      <li class="spacing">
        <#if hr.standardOption??>
          <h3>${hr.standardOption.optionName} - Holding ${h.id?c}</h3>
        <#else>
          <h3>${_("reproduction.customReproduction.backend", "Custom reproduction")} - Holding ${h.id?c}</h3>
        </#if>

        <ul class="holdingDetails">
          <li>
            <span>${_("record.externalInfo.title", "Title")}</span>
            ${info.title}
          </li>

          <#if info.author??>
            <li>
              <span>${_("record.externalInfo.author", "Author")}</span>
              ${info.author}
            </li>
          </#if>

          <#if info.displayYear??>
            <li>
              <span>${_("record.externalInfo.displayYear", "Year")}</span>
              ${info.displayYear}
            </li>
          </#if>

          <li class="spacing">
            <span>${_("record.externalInfo.materialType", "Material Type")}</span>
            ${_("record.externalInfo.materialType.${info.materialType}", "")}
          </li>

          <li>
            <span>${_("holding.signature", "Signature")}</span>
            ${h.signature}
          </li>

          <li>
            <span>${_("holding.pid", "Item PID")}</span>
            ${h.determinePid()}
          </li>

          <li>
            <span>PID</span>
            <#if _sec.ifAllGranted("ROLE_RECORD_MODIFY")>
              <a target="_blank" href="${rc.contextPath}/record/editform/${h.record.pid?url}">${h.record.pid}</a>
            <#else>
              ${h.record.pid}
            </#if>
          </li>

          <li class="spacing">
            <span>${_("reproduction.printed", "Printed")}</span>
            ${hr.printed?string(_("yes", "Yes"), _("no", "No"))}
            <#if !hr.printed && (reproduction.getStatus() != "CANCELLED")>
              <#if hr.isInSor()>
                <em class="info">(${_("reproduction.print.inSor", "in SOR")})</em>
              <#elseif hr.hasOrderDetails() && (reproduction.getStatus() != "ACTIVE")>
                <em class="info">(${_("reproduction.print.notYetPayed", "not yet paid")})</em>
              </#if>
            </#if>
          </li>

          <li>
            <span>${_("holding.status", "Status")}</span>
            <@holdingStatus holdingActiveRequests reproduction h/>
          </li>

          <li class="spacing">
            <span>${_("reproductionStandardOption.price", "Price")}</span>
            <#if hr.price??>
              <@holdingPrice hr.price hr.completePrice info.materialType hr.numberOfPages/>
            <#else>
              ${_("tbd", "To be determined")}
            </#if>
          </li>

          <#if hr.price??>
            <li>
              <span>${_("holdingReproductions.discount", "Computed discount")}</span>
              &euro; ${hr.discount?string("0.00")}
            </li>

            <li>
              <span>${_("holdingReproductions.btw", "Computed BTW")}</span>
              &euro; ${hr.btwPrice?string("0.00")} (${hr.btwPercentage}&percnt;)
            </li>
          </#if>

          <li class="spacing">
            <span>${_("reproductionStandardOption.deliveryTime", "Estimated delivery time")}</span>
            <#if hr.deliveryTime??>
                ${hr.deliveryTime} ${_("days", "days")}
            <#else>
                ${_("tbd", "To be determined")}
            </#if>
          </li>

          <#if hr.comment??>
            <li>
              <span>${_("holdingReproductions.comment", "Comment")} </span>
              ${hr.comment}
            </li>
          </#if>

          <#if !hr.standardOption??>
            <li class="spacing">
              <span>${_("reproduction.customReproductionCustomer.backend", "Customer's wish")} </span>
              <em>${hr.customReproductionCustomer}</em>
            </li>

            <#if hr.customReproductionReply??>
              <li class="spacing">
                <span>${_("reproduction.customReproductionReply", "Reply on wish")} </span>
                <em>${hr.customReproductionReply}</em>
              </li>
            </#if>
          </#if>
        </ul>
      </li>
    </#list>
  </ul>
</@base>
