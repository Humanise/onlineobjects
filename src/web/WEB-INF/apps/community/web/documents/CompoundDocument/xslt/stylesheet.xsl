<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:e="http://uri.onlineobjects.com/model/Item/Entity/"
 xmlns:u="http://uri.onlineobjects.com/model/Item/Entity/User/"
 xmlns:i="http://uri.onlineobjects.com/model/Item/Entity/Image/"
 xmlns:h="http://uri.onlineobjects.com/model/Item/Entity/HeaderPart/"
 xmlns:p="http://uri.onlineobjects.com/publishing/WebPage/"
 xmlns:doc="http://uri.onlineobjects.com/publishing/Document/CompoundDocument/"
 exclude-result-prefixes="e u p i ig doc h"
>	
	<xsl:output encoding="UTF-8" method="xml" omit-xml-declaration="yes"/>
	
	<xsl:include href="../../../../xslt/parts.xsl"/>
	
	<xsl:template name="p:content-head">
		<link rel="stylesheet" href="{$app-context}/documents/CompoundDocument/css/style.css" type="text/css" media="screen" title="front" charset="utf-8"/>
	</xsl:template>
	
	<xsl:template name="p:content-editor-head">
		<script src="{$base-context}/dwr/interface/CompoundDocumentDocument.js" type="text/javascript" charset="utf-8"><xsl:comment/></script>
		<script src="{$base-context}/dwr/interface/Parts.js" type="text/javascript" charset="utf-8"><xsl:comment/></script>
		<script src="{$app-context}/documents/CompoundDocument/js/CompoundDocumentEditor.js" type="text/javascript" charset="utf-8"><xsl:comment/></script>
	</xsl:template>
	
	<xsl:template match="doc:CompoundDocument">
		<div class="compounddocument">
			<xsl:apply-templates/>
		</div>
	</xsl:template>
	
	<xsl:template match="doc:structure">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="doc:structure/doc:row">
		<table class="row">
			<tr>
				<xsl:apply-templates/>
			</tr>
		</table>
	</xsl:template>
	
	<xsl:template match="doc:column">
		<td class="column">
			<xsl:attribute name="style">
				<xsl:text>width:</xsl:text><xsl:value-of select="100 div count(../doc:column)"/><xsl:text>%;</xsl:text>
			</xsl:attribute>
			<xsl:apply-templates/>
		</td>
	</xsl:template>
	
	<xsl:template match="doc:section">
		<div class="section">
			<xsl:apply-templates/>
		</div>
	</xsl:template>

</xsl:stylesheet>