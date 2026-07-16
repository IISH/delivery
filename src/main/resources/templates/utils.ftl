<#ftl strip_text=true strip_whitespace=true/>
<#import "spring.ftl" as spring/>

<#function _ ident msg="">
    <#return springMacroRequestContext.getMessage(ident, msg)>
</#function>

<#macro paramUrl params={} u="">
    <#compress>
        <#assign requestParams = {}>

        <#if rc.queryString??>
            <#assign varValues = rc.queryString?split("&")>
            <#list varValues as varValue>
                <#if varValue?contains("=")>
                    <#assign varValueSplit = varValue?split("=")>
                    <#assign requestParams = requestParams + {varValueSplit[0] : varValueSplit[1]}>
                </#if>
            </#list>
        </#if>

        <#list params?keys as k>
            <#assign requestParams = requestParams + {k : params[k]?url} />
        </#list>

        <#if u != "">
            <#local uri = rc.getContextUrl(u)/>
        <#else>
            <#local uri = rc.getRequestUri()/>
        </#if>
        ${uri}?<#list requestParams?keys as k><#if requestParams[k] !="">${k}=${requestParams[k]}<#if k_has_next>&amp;</#if></#if></#list>
    </#compress>
</#macro>

<#macro sortLink column attributes="">
    <#if pagedItem.sort == "UNSORTED" || !pagedItem.sort?starts_with(column) || pagedItem.sort == column + ": DESC">
        <#local direction = "ASC"/>
    <#else>
        <#local direction = "DESC"/>
    </#if>

    <a href="<@paramUrl {"sort":column+","+direction}/>" ${attributes}>
        <#nested>
    </a>
</#macro>

<#macro pageLinks sort method="GET">
    <#if sort == "UNSORTED">
        <#local order = ""/>
    <#else >
        <#local order = sort?replace(": ", ",")/>
    </#if>

    <#local mostleft = {"page":1,"size":pagedItem.size,"sort":order} />
    <#local left = {"page": pagedItem.number - 1,"size": pagedItem.size, "sort":order} />
    <#local right = {"page": pagedItem.number + 1,"size": pagedItem.size, "sort":order} />
    <#local mostright = {"page": pagedItem.number + 1,"size": pagedItem.size, "sort":order} />
    <#local mostright = {"page": pagedItem.totalPages - 1, "size": pagedItem.size, "sort":order}/>

    <div class="pageLinks">
        <#if pagedItem.number != 0>
            <#if method = "GET">
                <a class="pageLinks" href="<@paramUrl mostleft/>"><<</a>
            <#else >
                <a class="pageLinks" href="javascript:void(0)" onclick="getHoldingList('<@paramUrl mostleft/>')"><<</a>
            </#if>
        </#if>
        <#-- Previous Page Link -->
        <#if pagedItem.hasPrevious()>
            <#if method = "GET">
                <a class="pageLinks" href="<@paramUrl left/>"> < </a>
            <#else>
                <a class="pageLinks" href="javascript:void(0)" onclick="getHoldingList('<@paramUrl left/>')"> < </a>
            </#if>
        </#if>

        <#-- Page Number Information -->
        Page ${pagedItem.number + 1} of ${pagedItem.totalPages}

        <#-- Next Page Link -->
        <#if pagedItem.hasNext()>
            <#if method = "GET">
                <a class="pageLinks" href="<@paramUrl right/>"> > </a>
            <#else>
                <a class="pageLinks" href="javascript:void(0)" onclick="getHoldingList('<@paramUrl right/>')"> > </a>
            </#if>
        </#if>
        <#if pagedItem.number < pagedItem.totalPages>
            <#if method = "GET">
                <a class="pageLinks" href="<@paramUrl mostright/>"> >> </a>
            <#else >
                <a class="pageLinks" href="javascript:void(0)" onclick="getHoldingList('<@paramUrl mostright/>')"> > </a>
            </#if>
        </#if>

    </div>
</#macro>

<#macro pageApiLinks pageChunk>
    <#assign totalPages = (pageChunk.totalResultCount/pageChunk.resultCountPerChunk)?ceiling />
    <#assign prev = max(pageChunk.resultStart-pageChunk.resultCountPerChunk, 1) />
    <#assign last = (totalPages-1)*pageChunk.resultCountPerChunk+1 />
    <#assign next = min(pageChunk.resultStart+pageChunk.resultCountPerChunk, last) />

    <div class="pageLinks">
        <#if pageChunk.resultStart &gt; 1>
            <a href="<@paramUrl {"resultStart": "1"} />"
               class="pageLinks">&lt;&lt;</a>
            <a href="<@paramUrl {"resultStart": prev?c} />"
               class="pageLinks">&lt;</a>
        </#if>

        ${_("pageListHolder.page", "Page")} ${((pageChunk.resultStart/pageChunk.totalResultCount)*totalPages)?ceiling} /
        ${totalPages}

        <#if pageChunk.resultStart &lt; last>
            <a href="<@paramUrl {"resultStart": next?c} />" class="pageLinks">&gt;</a>
            <a href="<@paramUrl {"resultStart": last?c} />" class="pageLinks">&gt;&gt;</a>
        </#if>
    </div>
</#macro>

<#function min nr1 nr2>
    <#if nr1 &gt; nr2>
        <#return nr2>
    <#else>
        <#return nr1>
    </#if>
</#function>

<#function max nr1 nr2>
    <#if nr1 &gt; nr2>
        <#return nr1>
    <#else>
        <#return nr2>
    </#if>
</#function>

<#macro holdingStatus holdingActiveRequests request holding>
    <#assign holdingActiveRequest = holdingActiveRequests[holding.toString()] ! request/>
    ${_("holding.statusType.${holding.status?string}", "${holding.status?string}")}
    <#if (holding.status != "AVAILABLE") && !holdingActiveRequest.equals(request)>
        <em class="info">(${_("anotherRequest", "by another request")})</em>
    </#if>
</#macro>

<#macro holdingPrice price completePrice materialType noPages=1>
    &euro; ${completePrice?string("0.00")}

    <#if noPages gt 1>
        <#if materialType == "BOOK">
            <em class="info">
                (${_("price.page", "Price per page")}: &euro; ${price?string("0.00")},
                ${_("no.pages", "Number of pages")}: ${noPages})
            </em>
        <#else>
            <em class="info">
                (${_("price.copy", "Price per copy")}: &euro; ${price?string("0.00")},
                ${_("no.copies", "Number of copies")}: ${noPages})
            </em>
        </#if>
    </#if>
</#macro>
