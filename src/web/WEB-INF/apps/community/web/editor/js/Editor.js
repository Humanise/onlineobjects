if (!OO) var OO={};

OO.Editor = function(delegate) {
	this.delegate = delegate;
	this.delegate.setEditor(this);
	this.textEditors = [];
	this.active = false;
	this.buildActivator();
	this.toolbarPadder = $class('toolbar_padder')[0];
	this.toolbar = null;
	this.templates = [
		{key:'basic',title:'Basal',image:OnlineObjects.appContext+'/designs/basic/info/thumbnail.png'},
		{key:'modern',title:'Moderne',image:OnlineObjects.appContext+'/designs/modern/info/thumbnail.png'},
		{key:'cartoon',title:'Tegneserie',image:OnlineObjects.appContext+'/designs/cartoon/info/thumbnail.png'},
		{key:'ocean',title:'Ocean',image:OnlineObjects.appContext+'/designs/ocean/info/thumbnail.png'},
		{key:'snow',title:'Snow',image:OnlineObjects.appContext+'/designs/snow/info/thumbnail.png'}/*,
		{key:'beach',title:'Beach',image:OnlineObjects.appContext+'/designs/beach/info/thumbnail.png'}*/
	];
	var self = this;
	var editmode = N2i.Location.getBoolean('edit');
	if (editmode) {
		this.activate(true);
	}
	In2iGui.get().addDelegate(this);
	In2iGui.Editor.get().ignite();
}

OO.Editor.prototype = {
	activate : function(instantly) {
		this.active=true;
		this.activator.className='deactivate';
		N2i.Element.addClassName(document.body,'editor_mode');
		this.buildToolBar();
		$ani(this.toolbarPadder,'padding-top','58px',instantly ? 0 : 600,{ease:N2i.Animation.slowFastSlow});
		this.toolbarRevealer.show(instantly);
		this.enableWebNodeEditing();
		this.delegate.activate();
		In2iGui.Editor.get().activate();
	},
	deactivate : function() {
		if (!this.active) return;
		this.active=false;
		this.activator.className='activate';
		if (this.templatePanel) {
			this.templatePanel.hide();
		}
		if (this.newPagePanel) {
			this.newPagePanel.hide();
		}
		N2i.Element.removeClassName(document.body,'editor_mode');
		$ani(this.toolbarPadder,'padding-top','0px',500,{ease:N2i.Animation.slowFastSlow});
		this.toolbarRevealer.hide();
		this.disableWebNodeEditing();
		this.delegate.deactivate();
		In2iGui.Editor.get().deactivate();
	},


	// Editor
	
	partChanged : function(part) {
		if (part.type=='header') {
			Parts.updateHeaderPart(part.id,part.getValue());
		} else if (part.type=='html') {
			Parts.updateHtmlPart(part.id,part.getValue());
		}
	},
	
	// Activator
	
	buildActivator : function() {
		var self = this;
	
		this.activator = document.createElement('div');
		this.activator.className='activate';
		this.activator.onclick = function() {
			self.toggle()
		};
		document.body.appendChild(this.activator);
	
		this.logout = document.createElement('div');
		this.logout.className='logout';
		this.logout.onclick = function() {
			self.logOut();
		};
		document.body.appendChild(this.logout);
	
		if (In2iGui.browser.gecko || In2iGui.browser.webkit) {
			this.private = document.createElement('div');
			this.private.className='private';
			this.private.onclick = function() {
				self.goPrivate()
			};
			document.body.appendChild(this.private);
		}
	},
	removeActivator : function() {
		this.activator.style.display = 'none';
		this.logout.style.display = 'none';
		this.private.style.display = 'none';
	},
	goPrivate : function() {
		document.location='../private/';
	}
}

OO.Editor.prototype.logOut = function() {
	var self = this;
	CoreSecurity.logOut(function(data) {
		self.removeActivator();
		self.deactivate();
	});
}

OO.Editor.prototype.toggle = function() {
	if (this.active) {
		this.deactivate();
	} else {
		this.activate();
	}
}

OO.Editor.prototype.disableWebNodeEditing = function() {
	for (var i=0; i < this.textEditors.length; i++) {
		this.textEditors[i].destroy();
	};
	this.textEditors = [];
}

OO.Editor.prototype.enableWebNodeEditing = function() {
	this.webnodes = $class('webnode');
	var self = this;
	for (var i=0; i < this.webnodes.length; i++) {
		var delegate = {
			textChanged : function(element,value) {
				var id = element.id.split('-')[1];
				self.updateWebNode(id,value);
			}
		}
		var editor = new OO.Editor.TextEditor(this.webnodes[i],delegate);
		this.textEditors[this.textEditors.length] = editor;
	};
}

OO.Editor.prototype.updateWebNode = function(id,name) {
	AppCommunity.updateWebNode(id,name);
}

OO.Editor.prototype.buildToolBar = function() {
	if (!this.toolbar) {
		this.toolbarRevealer = In2iGui.RevealingToolbar.create();
		this.toolbar = this.toolbarRevealer.getToolbar();
		this.toolbar.add(In2iGui.Toolbar.Icon.create('newPage',{icon:'common/page',overlay:'new','title':'Ny side'}));
		this.toolbar.add(In2iGui.Toolbar.Icon.create('deletePage',{icon:'common/page',overlay:'delete','title':'Slet side'}));
		this.toolbar.addDivider();
		this.toolbar.add(In2iGui.Toolbar.Icon.create('pageProperties',{icon:'common/info','title':'Info'}));
		this.toolbar.add(In2iGui.Toolbar.Icon.create('changeTemplate',{icon:'common/design','title':'Skift design'}));
		this.toolbar.addDivider();
		this.delegate.addToToolbar(this.toolbar);
	}
}

OO.Editor.prototype.click$changeTemplate = function() {
	this.openTemplateWindow();
}

OO.Editor.prototype.click$pageProperties = function() {
	if (!this.infoWindow) {
		var w = In2iGui.Window.create(null,{title:'Sidens egenskaber',padding:5,variant:'dark',width:250});
		var f = In2iGui.Formula.create('pageInfoForm');
		w.add(f);
		var g = f.createGroup({above:true});
		g.add(In2iGui.Formula.Text.create(null,{label:'Titel',key:'title'}));
		g.add(In2iGui.Formula.Tokens.create(null,{label:'Nøgleord',key:'tags'}));
		var b = g.createButtons({top:10});
		b.add(In2iGui.Button.create('savePageInfo',{text:'Gem',highlighted:true}));
		b.add(In2iGui.Button.create('cancelPageInfo',{text:'Annuller'}));
		this.infoWindow = w;
	}
	var self = this;
	AppCommunity.getPageInfo(OnlineObjects.page.id,function(data) {
		In2iGui.get('pageInfoForm').setValues(data);
		self.infoWindow.show();
	});
}

OO.Editor.prototype.click$cancelPageInfo = function() {
	this.infoWindow.hide();
}

OO.Editor.prototype.click$savePageInfo = function() {
	var form = In2iGui.get('pageInfoForm');
	var data = form.getValues();
	AppCommunity.savePageInfo(OnlineObjects.page.id,data.title,data.tags);
	form.reset();
	this.infoWindow.hide();
}


OO.Editor.prototype.click$newPage = function() {
	if (!this.newPagePanel) {
		this.newPagePanel = In2iGui.Window.create(null,{title:'Ny side',padding:10,variant:'dark'});
		this.newPagePicker = In2iGui.Picker.create('documentPicker',{title:'Vælg venligst typen af side der skal oprettes',itemWidth:90,itemHeight:120});
		this.newPagePanel.add(this.newPagePicker);
		var self = this;
		var d = {callback:function(list) {
			self.updateAndShowNewPagePicker(list);
		}};
		AppCommunity.getDocumentClasses(d);
	} else {
		this.newPagePanel.show();
	}
}

OO.Editor.prototype.updateAndShowNewPagePicker = function(list) {
	for (var i=0; i < list.length; i++) {
		list[i].image = OnlineObjects.appContext+'/documents/'+list[i].simpleName+'/thumbnail.png';
	};
	this.newPagePicker.setObjects(list);
	this.newPagePanel.show();
}

OO.Editor.prototype.selectionChanged$documentPicker = function(picker) {
	var obj = picker.getSelection();
	AppCommunity.createWebPage(OnlineObjects.site.id,obj.simpleName,function(pageId) {
		document.location='./?id='+pageId+'&edit=true';
	});
}

OO.Editor.prototype.click$deletePage = function() {
	In2iGui.get().confirm('confirmDeletePage',{
		title:'Er du sikker på at du vil slette siden?',
		text:'Handlingen kan ikke fortrydes og siden kan ikke genskabes',
		ok:'Ja, slet siden',
		cancel:'Nej, jeg fortryder',
		highlighted:'cancel',emotion:'gasp'
	});
}

OO.Editor.prototype.ok$confirmDeletePage = function() {
	AppCommunity.deleteWebPage(OnlineObjects.page.id,function() {
		document.location='.?edit=true';
	});
}


OO.Editor.prototype.openTemplateWindow = function() {
	if (!this.templatePanel) {
		this.templatePanel = In2iGui.Window.create(null,{title:'Skift design',variant:'dark'});
		var c = In2iGui.Columns.create();
		var tp = In2iGui.Picker.create('templatePicker',{itemWidth:92,itemHeight:120});
		tp.setObjects(this.templates);
		c.addToColumn(0,In2iGui.Selection.create());
		c.addToColumn(1,tp);
		this.templatePanel.add(c);
	}
	this.templatePanel.show();
}

OO.Editor.prototype.selectionChanged$templatePicker = function(picker) {
	var obj = picker.getSelection();
	var path = OnlineObjects.appContext+'/designs/'+obj.key+'/css/style.css';
	$('pageDesign').setAttribute('href',path);
	AppCommunity.changePageTemplate(OnlineObjects.page.id,obj.key,
		function(data) {
			//N2i.Location.setParameter('edit',true);
		}
	);
}

/*********************** Utilities **********************/

OO.Editor.getEntityProperty = function(entity,key) {
	for (var i=0; i < entity.properties.length; i++) {
		if (entity.properties[i].key==key) {
			return entity.properties[i].value;
		}
	};
	return null;
}

OO.Editor.setEntityProperty = function(entity,key,value) {
	for (var i=0; i < entity.properties.length; i++) {
		if (entity.properties[i].key==key) {
			entity.properties[i].value=value;
		}
	};
}

OO.Editor.getEntityProperties = function(entity,key) {
	var values = [];
	for (var i=0; i < entity.properties.length; i++) {
		if (entity.properties[i].key==key) {
			values.push(entity.properties[i].value);
		}
	};
	return values;
}

/*********************** Text editor **********************/

OO.Editor.TextEditor = function(element,delegate) {
	this.element=element;
	this.delegate=delegate || {};
	var self = this;
	this.element.onclick = function() {
		self.activate();
		return false;
	}
}

OO.Editor.TextEditor.prototype.activate = function() {
	this.field = document.createElement('input');
	this.field.style.width=(N2i.Element.getWidth(this.element)+30)+'px';
	this.element.style.display='none';
	this.field.value = this.element.innerHTML;
	var fontSize = N2i.getStyle(this.element,'font-size');
	if (fontSize) {
		this.field.style.fontSize = fontSize;
	}
	var color = N2i.getStyle(this.element,'color');
	if (color) {
		this.field.style.color = color;
	}
	// TODO: Move more styles over
	this.element.parentNode.insertBefore(this.field,this.element);
	this.field.focus();
	this.field.select();
	var self = this;
	this.field.onblur = function() {
		self.deactivate();
	}
	this.field.onkeydown = function(e) {
		if (new N2i.Event(e).isReturnKey()) {
			self.deactivate();
		}
	}
}

OO.Editor.TextEditor.prototype.deactivate = function() {
	var value = this.field.value;
	this.element.innerHTML = value;
	this.field.style.display='none';
	this.element.style.display='';
	this.field.parentNode.removeChild(this.field);
	delete(this.field);
	if (this.delegate.textChanged) {
		this.delegate.textChanged(this.element,value);
	}
}

OO.Editor.TextEditor.prototype.destroy = function() {
	this.element.onclick = null;
}