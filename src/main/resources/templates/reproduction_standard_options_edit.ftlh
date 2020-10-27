<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reproductionStandardOption.title", "Standard reproduction options")/>

<@preamble title/>
<@heading/>
<@body>
  <@form "" "standardOptions">
    <h1>${title}</h1>

    <table class="reproduction_options reproduction_standard_options">
      <thead>
        <tr>
          <td>${_("reproductionStandardOption.materialType", "Material type")}</td>
          <td>${_("reproductionStandardOption.level", "SOR level")}</td>
          <td>${_("reproductionStandardOption.optionName", "Option name")}</td>
          <td>${_("reproductionStandardOption.optionDescription", "Option description")}</td>
          <td>${_("reproductionStandardOption.price", "Price")}</td>
          <td>${_("reproductionStandardOption.deliveryTime", "Delivery time")}</td>
          <td>${_("reproductionStandardOption.administrationCosts", "Charge handling costs?")}</td>
          <td>&nbsp;</td>
          <td>${_("reproductionStandardOption.enabled", "Is enabled?")}</td>
        </tr>
      </thead>

      <tbody>
        <#assign idx = 0>
        <#list standardOptions.options as stOption>
          <tr class="standard_option">
            <td>
              <input type="hidden" name="options[${idx}].id" value="${standardOptions.options[idx].id?c}" class="id"/>
              <input type="hidden" name="options[${idx}].materialType"
                     value="${standardOptions.options[idx].materialType}"/>
              ${_("record.externalInfo.materialType." + materialTypes[standardOptions.options[idx].materialType] "")}
            </td>

            <td>
              <input type="hidden" name="options[${idx}].level" value="${standardOptions.options[idx].level}"/>
              ${_("reproductionStandardOption.level." + levels[standardOptions.options[idx].level] "")}
            </td>

            <td>
              <label>
                <span>NL:</span>
                <@input_nolabel "standardOptions.options[${idx}].optionNameNL"/>
              </label>

              <label>
                <span>EN:</span>
                <@input_nolabel "standardOptions.options[${idx}].optionNameEN"/>
              </label>
            </td>

            <td>
              <label>
                <span>NL:</span>
                <@textarea_nolabel "standardOptions.options[${idx}].optionDescriptionNL"/>
              </label>

              <label>
                <span>EN:</span>
                <@textarea_nolabel "standardOptions.options[${idx}].optionDescriptionEN"/>
              </label>
            </td>

            <td>
              <label>
                <@spring.formInput "standardOptions.options[${idx}].price" "class='small'"/>
                <span>EUR</span>
                <#if spring.status.errorMessages?size != 0>
                  <ul class="errors">
                    <li>
                      <@spring.showErrors "</li><li>"/>
                    </li>
                  </ul>
                </#if>
              </label>
            </td>

            <td>
              <label>
                <@spring.formInput "standardOptions.options[${idx}].deliveryTime" "class='smaller'"/>
                <span>${_("days", "days")}</span>
                <#if spring.status.errorMessages?size != 0>
                  <ul class="errors">
                    <li>
                      <@spring.showErrors "</li><li>"/>
                    </li>
                  </ul>
                </#if>
              </label>
            </td>

            <td>
              <label>
                <@spring.formCheckbox "standardOptions.options[${idx}].administrationCosts"/>
                <span>${_("yes", "Yes")}</span>
              </label>
            </td>

            <td>
              <#if standardOptions.options[idx].materialType == "VISUAL">
                <label>
                  <@spring.formCheckbox "standardOptions.options[${idx}].poster"/>
                  ${_("reproductionStandardOption.isPoster", "Is poster?")}
                </label>
              </#if>
            </td>

            <td>
              <label>
                <@spring.formCheckbox "standardOptions.options[${idx}].enabled"/>
                <span>${_("yes", "Yes")}</span>
              </label>
            </td>
          </tr>

          <#assign idx = idx + 1>
        </#list>
      </tbody>

      <tfoot>
        <tr id="newStandardOption" class="hidden">
          <td>
            <select id="new.materialType" name="new.materialType" class="field">
                <#list materialTypes?keys as value>
                  <#assign label = "record.externalInfo.materialType." + materialTypes[value]/>

                  <option value="${value}">
                    ${_(label materialTypes[value]?string)}
                  </option>
                </#list>
            </select>
          </td>

          <td>
            <select id="new.level" name="new.level" class="field">
              <#list levels?keys as value>
                <#assign label = "reproductionStandardOption.level." + levels[value]/>

                <option value="${value}">
                  ${_(label levels[value]?string)}
                </option>
              </#list>
            </select>
          </td>

          <td>
            <label>
              <span>NL:</span>
              <input type="text" id="new.optionNameNL" name="new.optionNameNL">
            </label>
            <label>
              <span>EN:</span>
              <input type="text" id="new.optionNameEN" name="new.optionNameEN">
            </label>
          </td>

          <td>
            <label>
              <span>NL:</span>
              <textarea id="new.optionDescriptionNL" name="new.optionDescriptionNL"
                        class="field"></textarea>
            </label>
            <label>
              <span>EN:</span>
              <textarea id="new.optionDescriptionEN" name="new.optionDescriptionEN"
                        class="field"></textarea>
            </label>
          </td>

          <td>
            <label>
              <input type="text" id="new.price" name="new.price" value="${0?string('0.00')}" class="small"/>
              <span>EUR</span>
            </label>
          </td>

          <td>
            <label>
              <input type="text" id="new.deliveryTime" name="new.deliveryTime" value="0" class="smaller">
              <span>${_("days", "days")}</span>
            </label>
          </td>

          <td>
            <label>
              <input id="new.administrationCosts" name="new.administrationCosts" checked="checked" type="checkbox"/>
              <span>${_("yes", "Yes")}</span>
            </label>
          </td>

          <td></td>

          <td>
            <label>
              <input id="new.enabled" name="new.enabled" checked="checked" type="checkbox"/>
              <span>${_("yes", "Yes")}</span>
            </label>
          </td>
        </tr>
      </tfoot>
    </table>

    <table class="reproduction_options reproduction_custom_notes">
      <thead>
        <tr>
          <td>${_("reproductionStandardOption.materialType", "Material type")}</td>
          <td>${_("reproductionStandardOption.customNote", "Custom reproduction note")}</td>
        </tr>
      </thead>

      <tbody>
        <#assign idx = 0>
        <#list standardOptions.customNotes as customNote>
          <tr class="standard_option">
            <td>
              <input type="hidden" name="customNotes[${idx}].id" value="${standardOptions.customNotes[idx].id?c}"
                     class="id"/>
              <input type="hidden" name="customNotes[${idx}].materialType"
                     value="${standardOptions.customNotes[idx].materialType}"/>
              ${_("record.externalInfo.materialType." + materialTypes[standardOptions.customNotes[idx].materialType] "")}
            </td>

            <td>
              <label>
                <span>NL:</span>
                <@textarea_nolabel "standardOptions.customNotes[${idx}].noteNL"/>
              </label>
            </td>

            <td>
              <label>
                <span>EN:</span>
                <@textarea_nolabel "standardOptions.customNotes[${idx}].noteEN"/>
              </label>
            </td>
          </tr>

          <#assign idx = idx + 1>
        </#list>
      </tbody>
    </table>

    <@buttons>
      <@submit "reproduction" />
      <button type="button" onclick="addNewStandardOption();">
        ${_("reproductionStandardOption.addNew", "Add new standard option")}
      </button>
    </@buttons>
  </@form>
</@body>
