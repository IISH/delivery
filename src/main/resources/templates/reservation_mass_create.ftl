<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "reservationMassCreate.title" "New Reservation"/>
</#assign>

<#-- Build the page -->
<@preamble title>
<script type="text/javascript">
$(document).ready(function(){
    $(".reservation_form .date").datepicker({
        "dateFormat": "yy-mm-dd",
        "minDate": "0",
        "maxDate": "+${delivery.reservationMaxDaysInAdvance}",
        "beforeShowDay": $.datepicker.noWeekends
    });
});
</script>
</@preamble>
<@heading/>
<@body>
<h1>${title}</h1>
<fieldset class="reservation_form">
<@form "" "reservation">

    <ul class="form">
      <li><@input "reservation.visitorName" ""/></li>
      <li><@input "reservation.visitorEmail" ""/></li>
      <li><@date "reservation.date" ""/></li>
      <li><@date "reservation.returnDate" "" false/></li>
      <li><@input "reservation.comment" "" false/></li>
    </ul>

<ul id="holdingReservations" class="holdingReservationDetails">
    <li id="newHoldingReservation" class="hidden">
        <input type="button" class="removeButton"
               onclick="removeNewHoldingReservation($(this).parent());
        renumberHoldingReservations();"
               value="<@_ "deleteHolding.submit"/>"/>
        <input type="button" class="addButton" onclick="addNewHoldingReservation($(this)
        .parent());" value="<@_ "addHolding.submit"/>"/></li>
    <#assign i = 0>
    <#list reservation.holdingReservations as hr>
    <#assign info = hr.holding.record.externalInfo>
    <li class="holdingReservations" id="holdingReservation${i}">
        <input type="button" class="removeButton"
               onclick="removeNewHoldingReservation($(this).parent());
        renumberHoldingReservations();"
               value="<@_ "deleteHolding.submit"/>"/>
        <input type="hidden" id="holdingReservations[${i}].holding"
               name="holdingReservations[${i}].holding"
               class="holding" value="${hr.holding.id?c}"/>
        <#if hr.holding.status != "AVAILABLE" || hr.holding.record.restriction == "CLOSED">
        <span class="red">
        <#else>
        <span class="green">
        </#if>
        ${hr.holding.record.toString()?html}</span>
        <#if info.materialType == "SERIAL">
        <ul class id="commentlist">
            <li><strong>
        <@_ "holdingReservations.comment" "" />
        </strong>
        <@input_nolabel "reservation.holdingReservations[${i}].comment" "comment" />
            <#if hr.holding.externalInfo.serialNumbers??> ( <strong><@_ "reservationCreate.serialAvailable" ""/>:</strong> ${hr.holding.externalInfo.serialNumbers} )
            </#if>
            </li>
        </ul>
        </#if>
    </li>
    <#assign i = i + 1>
    </#list>

</ul>
<ul class="form">
<li><label for="print" class="field"><@_ "reservationMassCreate.print" "Print"/></label>
    <input type="checkbox" name="print" id="print" class="field"/></li>
<li><label for="mail" class="field"><@_ "reservationMassCreate.mail" "Mail"/></label>
    <input type="checkbox" name="mail" id="mail" class="field"/></li>
</ul>
</fieldset>
<fieldset class="actions">
    <legend><@_ "reservationMassCreate.search" "Search for Holdings to Add"/></legend>

    <ul>
    <li>
    <label for="searchTitle">
        <@_ "record.title" "Title"/>
    </label>
    <input id="searchTitle" type="text" name="searchTitle"
           value="${RequestParameters.searchTitle!""}" />
    </li>
    <li>
    <label for="searchSignature">
      <@_ "holding.signature" "Signature"/>
    </label>
    <input id="searchSignature" type="text" name="searchSignature"
           value="${RequestParameters.searchSignature!""}" />
    <#assign searchLabel>
    <@_ "reservationMassCreate.searchSubmit" "Search"/>
    </#assign>
    </li>
    </ul>
     <input type="submit" name="searchSubmit"
            value="${searchLabel}"/>

    <#if holdingList??>
      <ul id="holdingSearch" class="holdingReservationDetails">
      <#assign noResults = holdingList?size == 0>
      <#list holdingList as h>
        <#assign info = h.record.externalInfo>
        <#if h.status != "AVAILABLE" || h.record.restriction == "CLOSED">
        <li><span class="red">${h.record.title?html} <#if info.author??>/
        ${info.author}</#if> - ${h.signature?html}</span></li>
        <#else>
        <li><input type="button" class="addButton" onclick="addNewHoldingReservation($(this)
        .parent());" value="<@_ "addHolding.submit"/>"/>
            <input type="hidden" class="holding" value="${h.id?c}"/>

            <span class="green">${h.record.toString()?html}</span>
        <#if info.materialType == "SERIAL">
        <ul class="commentlist hidden">
            <li><strong>
        <@_ "holdingReservations.comment" "" />
        </strong>
        <input type="text" class="comment" />
            <#if h.externalInfo.serialNumbers??> ( <strong><@_ "reservationCreate.serialAvailable" ""/>:</strong> ${h.externalInfo.serialNumbers} )
            </#if>
            </li>
        </ul>
        </#if>

        </li>
        </#if>
      </#list>
          <#if noResults><li><span><@_ "search.notfound" "No results..."/></span></li></#if>
      </ul>
    </#if>
</fieldset>
  <@buttons>
    <@submit "reservation" />
  </@buttons>
 </@form>

</@body>
