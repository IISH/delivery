<#import "spring.ftl" as spring/>
<#include "utils.ftl">
<ul id="holdingSearch" class="holdingReservationDetails">
    <#if pagedItem.hasContent()>
        <#list pagedItem.content as h>
            <#assign info = h.record.externalInfo>

            <#if h.status != "AVAILABLE" || h.record.restriction == "CLOSED">
                <li>
                  <span class="red">
                    ${h.record.title} <#if info.author??>/
                          ${info.author}</#if> - ${h.signature}
                  </span>
                </li>
            <#else>
                <li>
                    <input type="button" class="addButton"
                           onclick="addNewHoldingReservation($(this).parent());"
                           value="${_("addHolding.submit")}"/>

                    <input type="hidden" class="holding" value="${h.id?c}"/>

                    <span class="green">${h.record.toString()}</span>

                    <#if info.materialType == "SERIAL">
                        <ul class="commentlist hidden">
                            <li>
                                <strong>
                                    ${_("holdingReservations.comment", "")}
                                </strong>

                                <input type="text" class="comment"/>

                                <#if h.externalInfo.serialNumbers??> (
                                    <strong>${_("reservationCreate.serialAvailable", "")}:</strong>
                                    ${h.externalInfo.serialNumbers} )
                                </#if>
                            </li>
                        </ul>
                    </#if>
                </li>
            </#if>
        </#list>
        <hr/>
        <@pageLinks "${pagedItem.sort}" "POST" />
    <#else >
        <li><span>${_("search.notfound", "No results...")}</span></li>
    </#if>
</ul>
