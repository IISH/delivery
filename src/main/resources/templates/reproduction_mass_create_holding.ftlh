<#import "spring.ftl" as spring/>
<#include "utils.ftl">
<ul id="holdingSearch" class="holdingReproductionDetails">
    <#if pagedItem.hasContent()>
        <#assign results = 0>

        <#list pagedItem.content as h>
            <#assign info = h.record.externalInfo>

            <#assign reproductionOptions = []/>
            <#if reproductionStandardOptions[h.signature]??>
                <#assign reproductionOptions = reproductionStandardOptions[h.signature]/>
            </#if>

            <#if reproductionOptions?has_content || (h.status == "AVAILABLE")>
                <#assign results = results + 1>

                <li>
                    <input type="button" class="addButton"
                           onclick="addNewHoldingReproduction($(this).parent());"
                           value="${_("addHolding.submit")}"/>
                    <input type="hidden" class="holding" value="${h.id?c}"/>

                    <ul class="inner">
                        <li>
                            ${h.record.title} <#if info.author??>/ ${info.author}</#if> - ${h.signature}
                            <#if h.record.parent??>/ ${_("editrecord.item", "Item")} ${h.record.pid?substring(h.record.parent.pid?length + 1)}</#if>
                        </li>

                        <li class="hidden">
                            <#list reproductionOptions as value>
                                <label class="group">
                                    <input type="radio" class="standardOption" value="${value.id?c}"/>
                                    ${value.optionName}
                                </label>

                                <span>
                          (&euro; ${h.record.determinePrice(value.price)?string("0.00")} - ${value.deliveryTime} ${_("days", "days")})
                        </span>
                            </#list>

                            <#if h.status == "AVAILABLE">
                                <label class="group">
                                    <input type="radio" class="standardOption custom" value="0" checked="checked"/>
                                    ${_("reproduction.customReproduction.backend", "Custom reproduction")}
                                </label>
                            </#if>
                        </li>

                        <#if info.materialType == "SERIAL">
                            <li class="hidden">
                                <label class="group">
                                    <strong class="label">${_("holdingReproductions.comment", "")}</strong>
                                    <input type="text" class="comment"/>

                                    <#if h.externalInfo.serialNumbers??>
                                        ( <strong>${_("reproductionCreate.serialAvailable", "")}:</strong>
                                        ${h.externalInfo.serialNumbers} )
                                    </#if>
                                </label>
                            </li>
                        </#if>

                        <li class="hidden on-custom">
                            <label class="group">
                                <strong class="label">${_("reproductionStandardOption.price", "Price")}</strong>
                                <input type="text" class="price" value="${0}"/>
                                EUR
                            </label>
                        </li>

                        <li class="hidden on-custom">
                            <label class="group">
                                <strong class="label">${_("reproductionStandardOption.deliveryTime", "Estimated delivery time")}</strong>
                                <input type="text" class="deliveryTime" value="0"/>
                                ${_("days", "days")}
                            </label>
                        </li>

                        <li class="hidden on-custom">
                            <label class="group">
                                <strong class="label">${_("reproduction.customReproductionCustomer.backend", "Customer's wish")}</strong>
                                <textarea class="customReproductionCustomer"></textarea>
                            </label>
                        </li>

                        <li class="hidden on-custom">
                            <label class="group">
                                <strong class="label">${_("reproduction.customReproductionReply", "Reply on wish")}</strong>
                                <textarea class="customReproductionReply"></textarea>
                            </label>
                        </li>
                    </ul>
                </li>
            </#if>
        </#list>

        <#if results == 0>
            <li>
                <span>${_("search.notfound", "No results...")}</span>
            </li>
        <#else >
            <@pageLinks "${pagedItem.sort}" "POST" />
        </#if>
    <#else>
        <a href="${rc.contextPath}/record/createform">${_("search.suggestion")}</a>
    </#if>
</ul>
