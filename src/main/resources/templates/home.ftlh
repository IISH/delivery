<#include "base.ftl">

<@base "Home">
  <form action="" method="POST">
    <fieldset class="config">
      <legend>${_("printer.title", "Printer configuration")}</legend>

      <ul>
        <li>
          <label>
            <input type="radio" name="printer" value="BOTH" <#if printer == "BOTH"> checked="checked"</#if>/>
                ${_("printer.BOTH", " Use both printers")}
          </label>
        </li>

        <li>
          <label>
            <input type="radio" name="printer" value="ARCHIVE" <#if printer == "ARCHIVE"> checked="checked"</#if>/>
              ${_("printer.ARCHIVE", "Use archive printer")}
          </label>
        </li>

        <li>
          <label>
            <input type="radio" name="printer"
                   value="READING_ROOM" <#if printer == "READING_ROOM"> checked="checked"</#if>/>
              ${_("printer.READING_ROOM", "Use reading room printer")}
          </label>
        </li>
      </ul>

      <span class="note">
        ${_( "printer.note", "Note: requests that were already sent to the printer before changing the printer configuration have to be printed again!")}
      </span>

      <input type="submit" name="printerSubmit" value="${_("editrecord.submit", "Submit")}"/>
    </fieldset>
  </form>
</@base>
