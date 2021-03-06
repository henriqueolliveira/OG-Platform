<#escape x as x?html>
{
    "header": {
        "type": "Portfolios",
        "dataFields": ["id", "node", "name", "validFrom"],
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
	      "count": ${"${paging.pagingSize}"?replace(',','')}
	      </#if>
    },
    "data": [<#if searchResult??>
      <#list searchResult.documents as item>
	       "${item.portfolio.uniqueId.objectId}|${item.portfolio.rootNode.uniqueId.objectId}|${item.portfolio.name}|${item.versionFromInstant}"<#if item_has_next>,</#if>
	    </#list> </#if>]
}
</#escape>