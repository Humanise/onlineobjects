/**
 * @constructor
 */
In2iGui.MarkupEditor = function(options) {
	this.name = options.name;
	this.options = n2i.override({debug:false,value:'',autoHideToolbar:true,style:'font-family: sans-serif; font-size: 11px;'},options);
	if (options.replace) {
		options.replace = n2i.get(options.replace);
		options.element = n2i.build('div',{className:'in2igui_markupeditor'});
		options.replace.parentNode.insertBefore(options.element,options.replace);
		options.replace.style.display='none';
		options.value = options.replace.innerHTML;
	}
	this.ready = false;
	this.pending = [];
	this.element = n2i.get(options.element);
	if (n2i.browser.msie) {
		this.impl = In2iGui.MarkupEditor.MSIE;
	} else {
		this.impl = In2iGui.MarkupEditor.webkit;
	}
	this.impl.initialize({element:this.element,controller:this});
	if (this.options.value) {
		this.setValue(this.options.value);
	}
	In2iGui.extend(this);
}

In2iGui.MarkupEditor.create = function(options) {
	options = options || {};
	options.element = n2i.build('div',{className:'in2igui_markupeditor'});
	return new In2iGui.MarkupEditor(options);
}

In2iGui.MarkupEditor.prototype = {
	implIsReady : function() {
		this.ready = true;
		n2i.log('I am ready!');
		for (var i=0; i < this.pending.length; i++) {
			n2i.log(this.pending[i]);
			this.pending[i]();
		};
	},
	implFocused : function() {
		this._showBar();
	},
	implBlurred : function() {
		this.bar.hide();
		this.fire('blur');
	},
	implValueChanged : function() {
		this._valueChanged();
	},
	destroy : function() {
		n2i.dom.remove(this.element);
		In2iGui.destroy(this);
	},
	getValue : function() {
		return this.impl.getHTML();
	},
	setValue : function(value) {
		this._whenReady(function() {
			this.impl.setHTML(value);
		}.bind(this));
	},
	focus : function() {
		this._whenReady(this.impl.focus.bind(this.impl));
	},

	_whenReady : function(func) {
		if (this.ready) {
			func();
		} else {
			this.pending.push(func);
		}
	},
	_showBar : function() {
		if (!this.bar) {
			var things = [
				{key:'bold',icon:'edit/text_bold'},
				{key:'italic',icon:'edit/text_italic'},
				{key:'color',icon:'common/color'},
				/*{key:'image',icon:'common/image'},*/
				{key:'addLink',icon:'common/link'},
				{key:'align',value:'left',icon:'edit/text_align_left'},
				{key:'align',value:'center',icon:'edit/text_align_center'},
				{key:'align',value:'right',icon:'edit/text_align_right'},
				{key:'align',value:'justify',icon:'edit/text_align_justify'},
				{key:'clear',icon:'edit/clear'}/*,
				{key:'strong',icon:'edit/text_bold'}*/
				
				
				/*,
				{key:'insert-table',icon:'edit/text_italic'}*/
			]
			
			this.bar = In2iGui.Bar.create({absolute:true,variant:'mini',small:true});
			n2i.each(things,function(info) {
				var button = new In2iGui.Bar.Button.create({icon:info.icon,stopEvents:true});
				button.listen({
					$mousedown : function() { this._buttonClicked(info) }.bind(this)
				});
				this.bar.add(button);
			}.bind(this));
			this.bar.addToDocument();
		}
		this.bar.placeAbove(this);
		this.bar.show();
	},
	_buttonClicked : function(info) {
		this.impl.saveSelection();
		if (info.key=='color') {
			this._showColorPicker();
		} else if (info.key=='addLink') {
			this._showLinkEditor();
		} else if (info.key=='align') {
			this.impl.align(info.value);
		} else if (info.key=='clear') {
			this.impl.removeFormat();
		} else {
			this.impl.format(info);
		}
		this._valueChanged();
		this.impl.restoreSelection();
	},
	_showColorPicker : function() {
		if (!this.colorPicker) {
			this.colorPicker = In2iGui.Window.create();
			var picker = In2iGui.ColorPicker.create();
			picker.listen(this);
			this.colorPicker.add(picker);
			this.colorPicker.listen({
				$userClosedWindow : function() {
						this.impl.restoreSelection();
				}.bind(this)
			})
		}
		this.colorPicker.show({avoid:this.element});
	},
	_showLinkEditor : function() {
		if (!this.linkEditor) {
			this.linkEditor = In2iGui.Window.create({padding:5,width:300});
			this.linkForm = In2iGui.Formula.create();
			this.linkEditor.add(this.linkForm);
			var group = this.linkForm.buildGroup({},[
				{type : 'Text', options:{key:'url',label:'Address:'}}
			]);
			this.linkEditor.add(this.linkForm);
			var buttons = group.createButtons();
			var ok = In2iGui.Button.create({text:'OK',submit:true});
			this.linkForm.listen({$submit:this._updateLink.bind(this)});
			buttons.add(ok);
		}
		this.temporaryLink = this.impl.getOrCreateLink();
		this.linkForm.setValues({url:this.temporaryLink.href});
		this.linkEditor.show({avoid:this.element});
		this.linkForm.focus();
	},
	_updateLink : function() {
		var values = this.linkForm.getValues();
		this.temporaryLink.href = values.url;
		this.linkForm.reset();
		this.temporaryLink = null;
		this.linkEditor.hide();
		this._valueChanged();
	},
	_valueChanged : function() {
		this.fire('valueChanged',this.impl.getHTML());		
	},
	
	$colorWasSelected : function(color) {
		this.impl.restoreSelection(function() {
			this.impl.colorize(color);
			this._valueChanged();
		}.bind(this));
	}
}

In2iGui.MarkupEditor.webkit = {
	initialize : function(options) {
		this.element = options.element;
		this.element.style.overflow='auto';
		this.element.contentEditable = true;
		var ctrl = this.controller = options.controller;
		n2i.listen(this.element,'focus',function() {
			ctrl.implFocused();
		});
		n2i.listen(this.element,'blur',function() {
			ctrl.implBlurred();
		});
		n2i.listen(this.element,'keyup',this._keyUp.bind(this));
		ctrl.implIsReady();
	},
	saveSelection : function() {
		
	},
	restoreSelection : function(callback) {
		if (callback) {callback()}
	},
	focus : function() {
		this.element.focus();
	},
	format : function(info) {
		if (info.key=='strong' || info.key=='em') {
			this._wrapInTag(info.key);
		} else if (info.key=='insert-table') {
			this._insertHTML('<table><tbody><tr><td>Lorem ipsum dolor</td><td>Lorem ipsum dolor</td></tr></tbody></table>');
		} else {
			document.execCommand(info.key,null,info.value);
		}
	},
	getOrCreateLink : function() {
		var node = this._getSelectedNode();
		if (node && node.tagName.toLowerCase()=='a') {
			return node;
		}
		document.execCommand('createLink',null,'#');
		return this._getSelectedNode();
	},
	_getSelectedNode : function() {
		var selection = window.getSelection();
		var range = selection.getRangeAt(0);
		var ancestor = range.commonAncestorContainer;
		if (!n2i.dom.isElement(ancestor)) {
			ancestor = ancestor.parentNode;
		}
		return ancestor;
	},
	colorize : function(color) {
		document.execCommand('forecolor',null,color);
	},
	align : function(value) {
		var x = {center:'justifycenter',justify:'justifyfull',left:'justifyleft',right:'justifyright'};
		document.execCommand(x[value],null,null);
	},
	_keyUp : function() {
		this.controller.implValueChanged();
		this._selectionChanged();
	},
	_wrapInTag : function(tag) {
		var selection = window.getSelection();
		if (selection.rangeCount<1) {return}
		var range = selection.getRangeAt(0);
		var ancestor = range.commonAncestorContainer;
		if (!n2i.dom.isElement(ancestor)) {
			ancestor = ancestor.parentNode;
		}
		if (ancestor.tagName.toLowerCase()==tag) {
			this._unWrap(ancestor);
		} else {
			var node = document.createElement(tag);
			range.surroundContents(node);
			selection.selectAllChildren(node);
		}
		//document.execCommand('inserthtml',null,'<'+tag+'>'+n2i.escape(n2i.getSelectedText())+'</'+tag+'>');
	},
	_getInlineTag : function() {
		var selection = window.getSelection();
		if (selection.rangeCount<1) {return}
		
	},
	_unWrap : function(node) {
		var c = node.childNodes;
		for (var i=0; i < c.length; i++) {
			node.parentNode.insertBefore(c[i],node);
		};
		node.parentNode.removeChild(node);
	},
	_insertHTML : function(html) {
		document.execCommand('inserthtml',null,html);
	},
	_selectionChanged : function() {
		n2i.log({
			bold : document.queryCommandState('bold'),
			italic : document.queryCommandState('italic')
		});
	},
	removeFormat : function() {
		document.execCommand('removeFormat',null,null);
	},
	setHTML : function(html) {
		this.element.innerHTML = html;
	},
	getHTML : function() {
		var cleaned = In2iGui.MarkupEditor.util.clean(this.element);
		return cleaned.innerHTML;
	}
}

In2iGui.MarkupEditor.MSIE = {
	initialize : function(options) {
		this.element = options.element;
		this.iframe = n2i.build('iframe',{style:'display:block; width: 100%; border: 0;',parent:this.element})
		n2i.listen(this.iframe,'load',this._load.bind(this));
		this.controller = options.controller;
	},
	saveSelection : function() {
		this.savedRange = this.document.selection.createRange();
		//this.savedSelection = this.document.selection.createRange().getBookmark();
	},
	restoreSelection : function(callback) {
		window.setTimeout(function() {
			this.body.focus();
			this.savedRange.select();
			if (callback) {callback()};
		}.bind(this));
	},
	_load : function() {
		this.document = n2i.getFrameDocument(this.iframe);
		this.body = this.document.body;
		this.body.contentEditable = true;
		n2i.listen(this.body,'keyup',this._keyUp.bind(this));
		n2i.listen(this.body,'mouseup',this._mouseUp.bind(this));
		this.controller.implIsReady();
	},
	_keyUp : function() {
		this.controller.implValueChanged();	
		this.saveSelection();	
	},
	_mouseUp : function() {
		this.saveSelection();
	},
	focus : function() {
		this.body.focus();
		this.controller.implFocused();
	},
	align : function(value) {
		var x = {center:'justifycenter',justify:'justifyfull',left:'justifyleft',right:'justifyright'};
		this.document.execCommand(x[value],null,null);
	},
	format : function(info) {
		if (info.key=='strong' || info.key=='em') {
			this._wrapInTag(info.key);
		} else if (info.key=='insert-table') {
			this._insertHTML('<table><tbody><tr><td>Lorem ipsum dolor</td><td>Lorem ipsum dolor</td></tr></tbody></table>');
		} else {
			this.document.execCommand(info.key,null,null);
		}
	},
	removeFormat : function() {
		this.document.execCommand('removeFormat',null,null);
	},
	colorize : function(color) {
		this.document.execCommand('forecolor',null,color);
		this.restoreSelection();
	},
	_wrapInTag : function(tag) {
		document.execCommand('inserthtml',null,'<'+tag+'>'+n2i.escape(n2i.getSelectedText())+'</'+tag+'>');
	},
	_insertHTML : function(html) {
		document.execCommand('inserthtml',null,html);
	},
	setHTML : function(html) {
		this.body.innerHTML = html;
	},
	getHTML : function() {
		var cleaned = In2iGui.MarkupEditor.util.clean(this.body);
		return cleaned.innerHTML;
	}
}

In2iGui.MarkupEditor.util = {
	clean : function(node) {
		var copy = node.cloneNode(true);
		this.replaceNodes(copy,{b:'strong',i:'em',font:'span'});

		var apples = n2i.byClass(copy,'Apple-style-span');
		for (var i = apples.length - 1; i >= 0; i--){
			apples[i].removeAttribute('class');
		};
		this.convertAttributesToStyle(copy);
		return copy;
	},
	replaceNodes : function(node,recipe) {
		for (var key in recipe) {
			var bs = node.getElementsByTagName(key);
			for (var i = bs.length - 1; i >= 0; i--) {
				var replacement = document.createElement(recipe[key]);
				var color = bs[i].getAttribute('color');
				if (color) {
					replacement.style.color=color;
				}
				n2i.dom.replaceNode(bs[i],replacement);
			};
		}
	},
	convertAttributesToStyle : function(node) {
		var all = node.getElementsByTagName('*');
		for (var i=0; i < all.length; i++) {
			var n = all[i];
			var align = n.getAttribute('align');
			if (align) {
				n.style.textAlign = align;
				n.removeAttribute('align');
			}
		};
	}
}

/* EOF */