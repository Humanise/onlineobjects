/**
 * @constructor
 */
In2iGui.RichText = function(options) {
	this.name = options.name;
	var e = this.element = n2i.get(options.element);
	this.options = n2i.override({debug:false,value:'',autoHideToolbar:true,style:'font-family: sans-serif;'},options);
	this.textarea = n2i.build('textarea');
	e.appendChild(this.textarea);
	this.editor = WysiHat.Editor.attach(this.textarea);
	this.editor.setAttribute('frameborder','0');
	/* @private */
	this.toolbar = n2i.firstByClass(e,'in2igui_richtext_toolbar');
	this.toolbarContent = n2i.firstByClass(e,'in2igui_richtext_toolbar_content');
	this.value = this.options.value;
	this.document = null;
	this.ignited = false;
	this.buildToolbar();
	this.ignite();
	In2iGui.extend(this);
}

In2iGui.RichText.actions = [
	{key:'bold',				cmd:'bold',				value:null,		icon:'edit/text_bold'},
	{key:'italic',				cmd:'italic',			value:null,		icon:'edit/text_italic'},
	{key:'underline',			cmd:'underline',		value:null,		icon:'edit/text_underline'},
	null,
	{key:'justifyleft',			cmd:'justifyleft',		value:null,		icon:'edit/text_align_left'},
	{key:'justifycenter',		cmd:'justifycenter',	value:null,		icon:'edit/text_align_center'},
	{key:'justifyright',		cmd:'justifyright',		value:null,		icon:'edit/text_align_right'},
	null,
	{key:'increasefontsize',	cmd:'increasefontsize',	value:null,		icon:'edit/increase_font_size'},
	{key:'decreasefontsize',	cmd:'decreasefontsize',	value:null,		icon:'edit/decrease_font_size'},
	{key:'color',				cmd:null,				value:null,		icon:'common/color'}
	/*,
	null,
	{key:'p',				cmd:'formatblock',		value:'p'},
	{key:'h1',				cmd:'formatblock',		value:'h1'},
	{key:'h2',				cmd:'formatblock',		value:'h2'},
	{key:'h3',				cmd:'formatblock',		value:'h3'},
	{key:'h4',				cmd:'formatblock',		value:'h4'},
	{key:'h5',				cmd:'formatblock',		value:'h5'},
	{key:'h6',				cmd:'formatblock',		value:'h6'},
	{key:'removeformat', 	cmd:'removeformat', 	'value':null}*/
];

In2iGui.RichText.replaceInput = function(options) {
	options = options || {};
	var input = n2i.get(options.input);
	input.style.display='none';
	options.value = input.value;
	var obj = In2iGui.RichText.create(options);
	input.parentNode.insertBefore(obj.element,input);
	obj.ignite();
}

In2iGui.RichText.create = function(options) {
	options = options || {};
	options.element = n2i.build('div',{'class':'in2igui_richtext',html:'<div class="in2igui_richtext_toolbar"><div class="in2igui_richtext_inner_toolbar"><div class="in2igui_richtext_toolbar_content"></div></div></div>'});
	return new In2iGui.RichText(options);
}

In2iGui.RichText.prototype = {
	isCompatible : function() {
	    var agt=navigator.userAgent.toLowerCase();
		return true;
		return (agt.indexOf('msie 6')>-1 || agt.indexOf('msie 7')>-1 || (agt.indexOf('gecko')>-1 && agt.indexOf('safari')<0));
	},
	ignite : function() {
		var self = this;
		this.editor.observe("wysihat:loaded", function(event) {
			if (this.ignited) {
				return;
			}
			this.editor.setStyle(this.options.style);
			this.editor.setRawContent(this.value);
			this.document = this.editor.getDocument();
			if (this.document.body) {
				this.document.body.style.minHeight='100%';
				this.document.body.style.margin='0';
				this.document.documentElement.style.cursor='text';
				this.document.documentElement.style.minHeight='100%';
				Element.setStyle(this.document.body,this.options.style);
			}
			this.window = this.editor.getWindow();
			Event.observe(this.window,'focus',function() {self.documentFocused()});
			Event.observe(this.window,'blur',function() {self.documentBlurred()});
			this.document.body.focus();
			this.ignited = true;
     	}.bind(this));
		this.editor.observe("wysihat:change", function(event) {
        	this.documentChanged();
     	}.bind(this));
	},
	setHeight : function(height) {
		this.editor.style.height=height+'px';
	},
	focus : function() {
		try { // TODO : May only work in gecko
			var r = this.document.createRange();
			r.selectNodeContents(this.document.body);
			this.window.getSelection().addRange(r);
		} catch (ignore) {}
		if (this.window)this.window.focus();
	},
	setValue : function(value) {
		this.value = value;
		this.editor.setRawContent(this.value);
	},
	getValue : function() {
		return this.value;
	},
	deactivate : function() {
		if (this.colorPicker) this.colorPicker.hide();
		if (this.toolbar) this.toolbar.style.display='none';
	},
	
	buildToolbar : function() {
		this.toolbar.onmousedown = function() {this.toolbarMouseDown=true}.bind(this);
		this.toolbar.onmouseup = function() {this.toolbarMouseDown=false}.bind(this);
		var self = this;
		var actions = In2iGui.RichText.actions;
		for (var i=0; i < actions.length; i++) {
			if (actions[i]==null) {
				this.toolbarContent.appendChild(n2i.build('div',{'class':'in2igui_richtext_divider'}));
			} else {
				var div = n2i.build('div',{'class':'action action_'+actions[i].key});
				div.title=actions[i].key;
				div.in2iguiRichTextAction = actions[i]
				div.onclick = div.ondblclick = function(e) {return self.actionWasClicked(this.in2iguiRichTextAction,e);}
				var img = n2i.build('img');
				img.src=In2iGui.context+'/In2iGui/gfx/trans.png';
				if (actions[i].icon) {
					div.style.backgroundImage='url('+In2iGui.getIconUrl(actions[i].icon,1)+')';
				}
				div.appendChild(img);
				this.toolbarContent.appendChild(div);
				div.onmousedown = In2iGui.RichText.stopEvent;
			}
		};
	},
	documentFocused : function() {
		if (n2i.browser.msie) {
			this.toolbar.style.display='block';
			return;
		}
		if (this.toolbar.style.display!='block') {
			this.toolbar.style.marginTop='-40px';
			n2i.setOpacity(this.toolbar,0);
			this.toolbar.style.display='block';
			n2i.ani(this.toolbar,'opacity',1,300);
			n2i.ani(this.toolbar,'margin-top','-32px',300);
		}
	},
	
	documentBlurred : function() {
		if (this.toolbarMouseDown) return;
		if (this.options.autoHideToolbar) {
			if (n2i.browser.msie) {
				var self = this;
				window.setTimeout(function() {
					self.toolbar.style.display='none';
				},100);
				return;
			}
			n2i.ani(this.toolbar,'opacity',0,300,{hideOnComplete:true});
			n2i.ani(this.toolbar,'margin-top','-40px',300);
		}
		this.documentChanged();
		In2iGui.callDelegates(this,'richTextDidChange');
	},
	
	documentChanged : function() {
		this.value = this.editor.content();
		if (this.options.input) {
			n2i.get(this.options.input).value=this.value;
		}
	},
	
	disabler : function(e) {
		var evt = e ? e : window.event; 
		if (evt.returnValue) {
			evt.returnValue = false;
		} else if (evt.preventDefault) {
			evt.preventDefault( );
		}
		return false;
	},
	actionWasClicked : function(action,e) {
		In2iGui.RichText.stopEvent(e);
		if (action.key=='color') {
			this.showColorPicker();
		} else {
			this.execCommand(action);
		}
		this.document.body.focus();
		return false;
	},
	execCommand : function(action) {
		this.editor.execCommand(action.cmd,false,action.value);
		this.documentChanged();
	},
	showColorPicker : function() {
		if (!this.colorPicker) {
			var panel = In2iGui.Window.create({variant:'dark'});
			var picker = In2iGui.ColorPicker.create();
			picker.listen(this);
			panel.add(picker);
			panel.show();
			this.colorPicker = panel;
		}
		this.colorPicker.show();
	},
	$colorWasHovered : function(color) {
		//this.document.execCommand('forecolor',false,color);
	},
	$colorWasSelected : function(color) {
		this.document.execCommand('forecolor',false,color);
		this.documentChanged();
	}
}



In2iGui.RichText.stopEvent = function(e) {
  var evt = e ? e : window.event; 
  if (evt.returnValue) {
    evt.returnValue = false;
  } else if (evt.preventDefault) {
    evt.preventDefault( );
  } else {
    return false;
  }
}

/* EOF */