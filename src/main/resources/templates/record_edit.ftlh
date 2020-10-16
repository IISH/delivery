<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=isNewRecord?then(_("newrecord.title", "New Record"), _("editrecord.title", "Edit Record"))>

<#macro fakeInput path value>
  <@spring.bind path/>
  <input type="text" id="${spring.status.expression}"
         name="${spring.status.expression}"
         value="${value}"/>
</#macro>

<#macro parentTree rec>
  <#if rec.parent??>
    <@parentTree rec.parent/>
  <#else>
    <ul class="record_tree">
      <li>
        <a <#if rec == record>class="current"</#if> href="${rc.contextPath}/record/editform/${rec.pid?url}">
          ${rec.title} (${rec.pid})
        </a>

        <ul class="parent_tree">
          <@tree rec/>

          <#if !isNewRecord??>
            <#assign item = record>
            <#if record.parent??>
              <#assign item = record.parent>
            </#if>

            </ul>
            <ul>
              <li class="edit_item">
                <@form_plain "" "edititem">
                  <input type="hidden" name="edit" value="${item.pid}"/>
                  <label for="${item.pid}_item">
                    ${_("edititem.label", "Sub-Item:")}
                  </label>
                  <input type="text" id="${item.pid}_item" name="item" value=""/>
                  <@submit "edititem"/>
                </@form_plain>
            </#if>
          </li>
        </ul>
      </li>
    </ul>
  </#if>
</#macro>

<#macro tree rec>
  <#list rec.children as child>
    <li>
      <a <#if child.pid == record.pid>class="current"</#if> href="${rc.contextPath}/record/editform/${child.pid?url}">
        <#assign length = rec.pid?length />
        ${_("editrecord.item", "Item")} ${child.pid?substring(length + 1)}
      </a>

      <ul class="record_tree">
        <@tree child/>
      </ul>
    </li>
  </#list>
</#macro>

<@base title>
  <#assign info = record.externalInfo>

  <h1>${title}</h1>

  <h3>${record.pid}: ${record.title} (${_("record.externalInfo.materialType.${info.materialType}", "")})</h3>

  <#if info.materialType == "ARCHIVE">
    <nav class="related_records">
      <h1>${_("editrecord.relatedRecords", "Related Records")}</h1>
      <@parentTree record/>
    </nav>
  </#if>

  <@form "" "record" "save">
    <fieldset class="holdings">
      <legend>
        ${_("editrecord.set.holdings", "Holdings")}
      </legend>

      <table class="holdings">
        <thead>
          <tr>
            <td>${_("holding.signature", "Signature/Type")}</td>
            <td>${_("holding.usageRestriction", "Usage Restriction")}</td>
            <td>${_("holding.status", "Status")}</td>
            <td></td>
          </tr>
        </thead>

        <#assign idx = 0>
        <#list record.holdings as h>
          <tr class="holding" id="holding${idx}">
            <@spring.bind "record.holdings[${idx}].signature"/>

            <#if spring.status.errorMessages?size == 0>
              <input type="hidden" id="${spring.status.expression}" name="${spring.status.expression}" value="${(h.signature!'')}"/>
              <td class="signature">${(h.signature!'')}</td>
            <#else>
              <td><@input_nolabel "record.holdings[${idx}].signature"/></td>
            </#if>

            <td><@select_nolabel "record.holdings[${idx}].usageRestriction" "holding.usageRestriction" usageRestriction_types/></td>

            <#if h.status == "AVAILABLE">
              <td class="green">${_("holding.statusType.AVAILABLE", "Available")}</td>
            <#else>
              <td class="orange">${_("holding.statusType.${h.status}", h.status?string)}</td>
            </#if>

            <td>
              <input type="button"
                     onclick="var y = confirm('${_("deleteHolding.confirmDelete", "Are you sure you want to delete the holding?")}'); if (y) {$(this).parent().parent().remove();renumberHoldings();}"
                     value="${_("deleteHolding.submit", "Remove")}"/>
            </td>
          </tr>

          <#assign idx = idx + 1>
        </#list>

        <tfoot>
          <tr id="newHolding" class="hidden">
            <td><input type="text" id="holdings.new.signature"/></td>

            <td>
              <select id="holdings.new.usageRestriction" class="field">
                <#list usageRestriction_types?keys as value>
                  <#assign label = "holding.usageRestriction." + usageRestriction_types[value]/>
                  <option value="${value}">
                    ${_(label, usageRestriction_types[value])}
                  </option>
                </#list>
              </select>
            </td>

            <td class="green">${_("holding.statusType.AVAILABLE", "Available")}</td>

            <td>
              <input type="button"
                     onclick="$(this).parent().parent().remove();renumberHoldings();"
                     value="${_("deleteHolding.submit", "Remove")}"/>
            </td>
          </tr>
        </tfoot>
      </table>

      <div class="btlist">
        <input id="newHoldingButton" type="button"
               onclick="addNewHolding();"
               value="${_("addHolding.submit", "Add New")}"/>
      </div>
    </fieldset>

    <div class="btlist">
      <@buttons>
        <@submit "editrecord"/>
      </@buttons>
    </div>
  </@form>

  <div class="btlist">
    <#if _sec.ifAllGranted("ROLE_RECORD_DELETE")>
      <@buttons>
        <@button "" "delete" "editrecord.delete" "return confirm('${_("editrecord.confirmDelete", "Are you sure you want to delete the record?")}');"/>
      </@buttons>
    </#if>
  </div>
</@base>
