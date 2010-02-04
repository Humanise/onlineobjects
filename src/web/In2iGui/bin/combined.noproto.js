/** @namespace */
var n2i = {
	/** @namespace */
	browser : {}
}

n2i.browser.opera = /opera/i.test(navigator.userAgent);
n2i.browser.msie = !n2i.browser.opera && /MSIE/.test(navigator.userAgent);
n2i.browser.msie6 = navigator.userAgent.indexOf('MSIE 6')!==-1;
n2i.browser.msie7 = navigator.userAgent.indexOf('MSIE 7')!==-1;
n2i.browser.msie8 = navigator.userAgent.indexOf('MSIE 8')!==-1;
n2i.browser.webkit = navigator.userAgent.indexOf('WebKit')!==-1;
n2i.browser.safari = navigator.userAgent.indexOf('Safari')!==-1;
n2i.browser.webkitVersion = null;
n2i.browser.gecko = !n2i.browser.webkit && navigator.userAgent.indexOf('Gecko')!=-1;

(function() {
	var result = /Safari\/([\d.]+)/.exec(navigator.userAgent);
	if (result) {
		n2i.browser.webkitVersion=parseFloat(result[1]);
	}
})()

n2i.ELEMENT_NODE=1;
n2i.ATTRIBUTE_NODE=2;
n2i.TEXT_NODE=3;

/** Log something */
n2i.log = function(obj) {
	try {
		console.log(obj);
	} catch (ignore) {};
}

/** Make a string camelized */
n2i.camelize = function(str) {
    var oStringList = str.split('-');
    if (oStringList.length == 1) return oStringList[0];

    var camelizedString = str.indexOf('-') == 0
      ? oStringList[0].charAt(0).toUpperCase() + oStringList[0].substring(1)
      : oStringList[0];

    for (var i = 1, len = oStringList.length; i < len; i++) {
      var s = oStringList[i];
      camelizedString += s.charAt(0).toUpperCase() + s.substring(1);
    }

    return camelizedString;
}

/** Override the properties on the first argument with properties from the last object */
n2i.override = function(original,subject) {
	if (subject) {
		for (prop in subject) {
			original[prop] = subject[prop];
		}
	}
	return original;
}

/** Trim whitespace including unicode chars */
n2i.trim = function(str) {
	if (!str) return str;
	return str.replace(/^[\s\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000]+|[\s\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000]+$/g, '');
}

/** Escape the html in a string */
n2i.escapeHTML = function(str) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}

/** Checks if a string has characters */
n2i.isEmpty = n2i.isBlank = function(str) {
	if (str===null || typeof(str)==='undefined') return true;
	return typeof(str)=='string' && n2i.trim(str).length==0;
}

n2i.isDefined = function(obj) {
	return obj!==null && typeof(obj)!=='undefined';
}

n2i.inArray = function(arr,value) {
	for (var i=0; i < arr.length; i++) {
		if (arr[i]==value) return true;
	};
	return false;
}


n2i.flipInArray = function(arr,value) {
	if (n2i.inArray(arr,value)) {
		n2i.removeFromArray(arr,value);
	} else {
		arr.push(value);
	}
}

n2i.removeFromArray = function(arr,value) {
	for (var i = arr.length - 1; i >= 0; i--){
		if (arr[i]==value) {
			arr.splice(i,1);
		}
	};
}

n2i.addToArray = function(arr,value) {
	if (value.constructor==Array) {
		for (var i=0; i < value.length; i++) {
			if (!n2i.inArray(arr,value[i])) {
				arr.push(value);
			}
		};
	} else {
		if (!n2i.inArray(arr,value)) {
			arr.push(value);
		}
	}
}

n2i.scrollTo = function(element) {
	element = $(element);
	if (element) {
		var pos = element.cumulativeOffset();
		window.scrollTo(pos.left, pos.top-50);
	}
}

////////////////////// DOM ////////////////////

/** @namespace */
n2i.dom = {
	isElement : function(n,name) {
		return n.nodeType==n2i.ELEMENT_NODE && (name===undefined ? true : n.nodeName==name);
	},
	isDefinedText : function(node) {
		return node.nodeType==n2i.TEXT_NODE && node.nodeValue.length>0;
	},
	addText : function(node,text) {
		node.appendChild(document.createTextNode(text));
	},
	getNodeText : function(node) {
		var txt = '';
		var c = node.childNodes;
		for (var i=0; i < c.length; i++) {
			if (c[i].nodeType==n2i.TEXT_NODE && c[i].nodeValue!=null) {
				txt+=c[i].nodeValue;
			}
		};
		return txt;
	}
}

///////////////////// Style ///////////////////

n2i.getStyle = function(element, style) {
	element = $(element);
	var cameled = n2i.camelize(style);
	var value = element.style[cameled];
	if (!value) {
		if (document.defaultView && document.defaultView.getComputedStyle) {
			var css = document.defaultView.getComputedStyle(element, null);
			value = css ? css.getPropertyValue(style) : null;
		} else if (element.currentStyle) {
			value = element.currentStyle[cameled];
		}
	}
	if (window.opera && ['left', 'top', 'right', 'bottom'].include(style)) {
		if (n2i.getStyle(element, 'position') == 'static') value = 'auto';
	}
	return value == 'auto' ? null : value;
}

n2i.setOpacity = function(element,opacity) {
	if (n2i.browser.msie) {
		if (opacity==1) {
			element.style['filter']=null;
		} else {
			element.style['filter']='alpha(opacity='+(opacity*100)+')';
		}
	} else {
		element.style['opacity']=opacity;
	}
}

n2i.copyStyle = function(source,target,styles) {
	styles.each(function(s) {
		var r = source.getStyle(s);
		if (r) target.style[s] = source.getStyle(s);
	});
}

///////////////////// Events ////////////////////

n2i.isReturnKey = function(e) {
	return e.keyCode==13;
}
n2i.isRightKey = function(e) {
	return e.keyCode==39;
}
n2i.isLeftKey = function(e) {
	return e.keyCode==37;
}
n2i.isEscapeKey = function(e) {
	return e.keyCode==27;
}
n2i.isSpaceKey = function(e) {
	return e.keyCode==32;
}

//////////////////// Frames ////////////////////

n2i.getFrameDocument = function(frame) {
    if (frame.contentDocument) {
        return frame.contentDocument;
    } else if (frame.contentWindow) {
        return frame.contentWindow.document;
    } else if (frame.document) {
        return frame.document;
    }
}

n2i.getFrameWindow = function(frame) {
    if (frame.defaultView) {
        return frame.defaultView;
    } else if (frame.contentWindow) {
        return frame.contentWindow;
    }
}

/////////////////// Selection /////////////////////

n2i.getSelectedText = function(doc) {
	doc = doc || document;
	if (doc.getSelection) {
		return doc.getSelection()+'';
	} else if (doc.selection) {
		return doc.selection.createRange().text;
	}
	return '';
}

/////////////////// Position /////////////////////

n2i.getScrollTop = function() {
	if (self.pageYOffset) {
		return self.pageYOffset;
	} else if (document.documentElement && document.documentElement.scrollTop) {
		return document.documentElement.scrollTop;
	} else if (document.body) {
		return document.body.scrollTop;
	}
}

n2i.getScrollLeft = function() {
	if (self.pageYOffset) {
		return self.pageXOffset;
	} else if (document.documentElement && document.documentElement.scrollTop) {
		return document.documentElement.scrollLeft;
	} else if (document.body) {
		return document.body.scrollLeft;
	}
}

/**
 * Get the height of the viewport
 */
n2i.getViewPortHeight = function() {
	var y;
	if (window.innerHeight) {
		return window.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		return document.documentElement.clientHeight;
	} else if (document.body) {
		return document.body.clientHeight;
	}
}

n2i.getInnerHeight = function() {
	var y;
	if (window.innerHeight) {
		return window.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		return document.documentElement.clientHeight;
	} else if (document.body) {
		return document.body.clientHeight;
	}
}

n2i.getDocumentWidth = n2i.getInnerWidth = function() {
	if (window.innerHeight) {
		return window.innerWidth;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		return document.documentElement.clientWidth;
	} else if (document.body) {
		return document.body.clientWidth;
	}
}

n2i.getDocumentHeight = function() {
	if (n2i.browser.msie6) {
		// In IE6 check the children too
		var max = Math.max(document.body.clientHeight,document.documentElement.clientHeight,document.documentElement.scrollHeight);
		$(document.body).childElements().each(function(node) {
			max = Math.max(max,node.getHeight());
		});
		return max;
	}
	if (window.scrollMaxY && window.innerHeight) {
		return window.scrollMaxY+window.innerHeight;
	} else {
		return Math.max(document.body.clientHeight,document.documentElement.clientHeight,document.documentElement.scrollHeight);
	}
}

//////////////////////////// Placement /////////////////////////

n2i.place = function(options) {
	var left=0,top=0;
	var trgt = options.target.element;
	var trgtPos = trgt.cumulativeOffset();
	left = trgtPos.left+trgt.clientWidth*options.target.horizontal;
	top = trgtPos.top+trgt.clientHeight*options.target.vertical;
	
	
	var src = options.source.element;
	src.style.top=top+'px';
	src.style.left=left+'px';
}

//////////////////////////// Preloader /////////////////////////

/** @constructor */
n2i.Preloader = function(options) {
	this.options = options || {};
	this.delegate = {};
	this.images = [];
	this.loaded = 0;
}

n2i.Preloader.prototype = {
	addImages : function(imageOrImages) {
		if (typeof(imageOrImages)=='object') {
			for (var i=0; i < imageOrImages.length; i++) {
				this.images.push(imageOrImages[i]);
			};
		} else {
			this.images.push(imageOrImages);
		}
	},
	setDelegate : function(d) {
		this.delegate = d;
	},
	load : function(startIndex) {
		startIndex = startIndex || 0;
		var self = this;
		this.obs = [];
		for (var i=startIndex; i < this.images.length+startIndex; i++) {
			var index=i;
			if (index>=this.images.length) {
				index = index-this.images.length;
			}
			var img = new Image();
			img.n2iPreloaderIndex = index;
			img.onload = function() {self.imageChanged(this.n2iPreloaderIndex,'imageDidLoad')};
			img.onerror = function() {self.imageChanged(this.n2iPreloaderIndex,'imageDidGiveError')};
			img.onabort = function() {self.imageChanged(this.n2iPreloaderIndex,'imageDidAbort')};
			img.src = (this.options.context ? this.options.context : '')+this.images[index];
			this.obs.push(img);
		};
	},
	imageChanged : function(index,method) {
		this.loaded++;
		if (this.delegate[method]) {
			this.delegate[method](this.loaded,this.images.length,index);
		}
		if (this.loaded==this.images.length && this.delegate.allImagesDidLoad) {
			this.delegate.allImagesDidLoad();
		}
	}
}

/* @namespace */
n2i.cookie = {
	set : function(name,value,days) {
		if (days) {
			var date = new Date();
			date.setTime(date.getTime()+(days*24*60*60*1000));
			var expires = "; expires="+date.toGMTString();
		}
		else var expires = "";
		document.cookie = name+"="+value+expires+"; path=/";
	},
	get : function(name) {
		var nameEQ = name + "=";
		var ca = document.cookie.split(';');
		for(var i=0;i < ca.length;i++) {
			var c = ca[i];
			while (c.charAt(0)==' ') c = c.substring(1,c.length);
			if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
		}
		return null;
	},
	clear : function(name) {
		this.set(name,"",-1);
	}
}

///////////////////////// URL/Location /////////////////////

n2i.URL = function(url) {
	this.url = url || '';
}

n2i.URL.prototype = {
	addParameter : function(key,value) {
		this.url+=this.url.indexOf('?')!=-1 ? '&' : '?';
		this.url+=key+'='+(n2i.isDefined(value) ? value : '');
	},
	toString : function() {
		return this.url;
	}
}

/* @namespace */
n2i.location = {
	getParameter : function(name) {
		var parms = n2i.location.getParameters();
		for (var i=0; i < parms.length; i++) {
			if (parms[i].name==name) {
				return parms[i].value;
			}
		};
		return null;
	},
	setParameter : function(name,value) {
		var parms = n2i.location.getParameters();
		var found = false;
		for (var i=0; i < parms.length; i++) {
			if (parms[i].name==name) {
				parms[i].value=value;
				found=true;
				break;
			}
		};
		if (!found) {
			parms.push({name:name,value:value});
		}
		n2i.location.setParameters(parms);
	},
	hasHash : function(name) {
		var h = document.location.hash;
		if (h!=='') {
			return h=='#'+name;
		}
		return false;
	},
	getHashParameter : function(name) {
		var h = document.location.hash;
		if (h!=='') {
			var i = h.indexOf(name+'=');
			if (i!==-1) {
				var remaining = h.substring(i+name.length+1);
				if (remaining.indexOf('&')!==-1) {
					return remaining.substring(0,remaining.indexOf('&'));
				}
				return remaining;
			}
		}
		return null;
	},
	clearHash : function() {
		document.location.hash='#';
	},
	setParameters : function(parms) {
		var query = '';
		for (var i=0; i < parms.length; i++) {
			query+= i==0 ? '?' : '&';
			query+=parms[i].name+'='+parms[i].value;
		};
		document.location.search=query;
	},
	getBoolean : function(name) {
		var value = n2i.location.getParameter(name);
		return (value=='true' || value=='1');
	},
	getParameters : function() {
		var items = document.location.search.substring(1).split('&');
		var parsed = [];
		for( var i = 0; i < items.length; i++) {
			var item = items[i].split('=');
			var name = unescape(item[0]).replace(/^\s*|\s*$/g,"");
			var value = unescape(item[1]).replace(/^\s*|\s*$/g,"");
			if (name) {
				parsed.push({name:name,value:value});
			}
		};
		return parsed;
	}	
};

/////////////////////////// Animation ///////////////////////////


n2i.ani = n2i.animate = function(element,style,value,duration,delegate) {
	n2i.animation.get(element).animate(null,value,style,duration,delegate);
}

/** @namespace */
n2i.animation = {
	objects : {},
	running : false,
	latestId : 0,
	get : function(element) {
		element = $(element);
		if (!element.n2iAnimationId) element.n2iAnimationId = this.latestId++;
		if (!this.objects[element.n2iAnimationId]) {
			this.objects[element.n2iAnimationId] = new n2i.animation.Item(element);
		}
		return this.objects[element.n2iAnimationId];
	},
	start : function() {
		if (!this.running) {
			n2i.animation.render();
		}
	}
};

n2i.animation.render = function(element) {
	this.running = true;
	var next = false;
	var stamp = new Date().getTime();
	for (id in this.objects) {
		var obj = this.objects[id];
		if (obj.work) {
			for (var i=0; i < obj.work.length; i++) {
				var work = obj.work[i];
				if (work.finished) continue;
				var place = (stamp-work.start)/(work.end-work.start);
				if (place<0) {
					next=true;
					continue;
				}
				else if (isNaN(place)) place = 1;
				else if (place>1) place=1;
				else if (place<1) next=true;
				var v = place;
				if (work.delegate && work.delegate.ease) {
					v = work.delegate.ease(v);
				}
				var value = null;
				if (!work.css) {
					obj.element[work.property] = Math.round(work.from+(work.to-work.from)*v);
				} else if (work.to.red!=null) {
					var red = Math.round(work.from.red+(work.to.red-work.from.red)*v);
					var green = Math.round(work.from.green+(work.to.green-work.from.green)*v);
					var blue = Math.round(work.from.blue+(work.to.blue-work.from.blue)*v);
					value = 'rgb('+red+','+green+','+blue+')';
					obj.element.style[work.property]=value;
				} else if (n2i.browser.msie && work.property=='opacity') {
					var opacity = (work.from+(work.to-work.from)*v);
					if (opacity==1) {
						obj.element.style.removeAttribute('filter');
					} else {
						obj.element.style['filter']='alpha(opacity='+(opacity*100)+')';
					}
				} else {
					value = new String(work.from+(work.to-work.from)*v)+(work.unit!=null ? work.unit : '');
					obj.element.style[work.property]=value;
				}
				if (place==1) {
					work.finished = true;
					if (work.delegate && work.delegate.onComplete) {
						window.setTimeout(work.delegate.onComplete);
					} else if (work.delegate && work.delegate.hideOnComplete) {
						obj.element.style.display='none';
					}
				}
			};
		}
	}
	if (next) {
		window.setTimeout(function() {
			n2i.animation.render();
		},0);
	} else {
		this.running = false;
	}
}

n2i.animation.parseStyle = function(value) {
	var parsed = {type:null,value:null,unit:null};
	var match;
	if (!n2i.isDefined(value)) {
		return parsed;
	} else if (!isNaN(value)) {
		parsed.value=parseFloat(value);
	} else if (match=value.match(/([\-]?[0-9\.]+)(px|pt|%)/)) {
		parsed.type = 'length';
		parsed.value = parseFloat(match[1]);
		parsed.unit = match[2];
	} else if (match=value.match(/rgb\(([0-9]+),[ ]?([0-9]+),[ ]?([0-9]+)\)/)) {
		parsed.type = 'color';
		parsed.value = {
			red:parseInt(match[1]),
			green:parseInt(match[2]),
			blue:parseInt(match[3])
		};
	} else {
		var color = new n2i.Color(value);
		if (color.ok) {
			parsed.value = {
				red:color.r,
				green:color.g,
				blue:color.b
			};
		}
	}
	return parsed;
}

///////////////////////////// Item ///////////////////////////////

n2i.animation.Item = function(element) {
	this.element = element;
	this.work = [];
}

n2i.animation.Item.prototype.animate = function(from,to,property,duration,delegate) {
	var css = true;
	if (property=='scrollLeft' || property=='scrollTop') {
		css = false;
	}
	
	var work = this.getWork(css ? n2i.camelize(property) : property);
	work.delegate = delegate;
	work.finished = false;
	work.css = css;
	if (from!=null) {
		work.from = from;
	} else if (work.css && n2i.browser.msie && property=='opacity') {
		work.from = this.getIEOpacity(this.element);
	} else if (work.css) {
		var style = n2i.getStyle(this.element,property);
		var parsedStyle = n2i.animation.parseStyle(style);
		work.from = parsedStyle.value;
	} else {
		work.from = this.element[property];
	}
	if (work.css) {
		var parsed = n2i.animation.parseStyle(to);
		work.to = parsed.value;
		work.unit = parsed.unit;
	} else {
		work.to = to;
		work.unit = null;
	}
	work.start = new Date().getTime();
	if (delegate && delegate.delay) work.start+=delegate.delay;
	work.end = work.start+duration;
	n2i.animation.start();
}

n2i.animation.Item.prototype.getIEOpacity = function(element) {
	var filter = n2i.getStyle(element,'filter').toLowerCase();
	var match;
	if (match = filter.match(/opacity=([0-9]+)/)) {
		return parseFloat(match[1])/100;
	} else {
		return 1;
	}
}

n2i.animation.Item.prototype.getWork = function(property) {
	for (var i=0; i < this.work.length; i++) {
		if (this.work[i].property==property) {
			return this.work[i];
		}
	};
	var work = {property:property};
	this.work[this.work.length] = work;
	return work;
}

/////////////////////////////// Loop ///////////////////////////////////

n2i.animation.Loop = function(recipe) {
	this.recipe = recipe;
	this.position = -1;
	this.running = false;
}

n2i.animation.Loop.prototype.next = function() {
	this.position++;
	if (this.position>=this.recipe.length) {
		this.position = 0;
	}
	var item = this.recipe[this.position];
	if (typeof(item)=='function') {
		item();
	} else if (item.element) {
		n2i.ani(item.element,item.property,item.value,item.duration,{ease:item.ease});
	}
	var self = this;
	var time = item.duration || 0;
	window.setTimeout(function() {self.next()},time);
}

n2i.animation.Loop.prototype.start = function() {
	this.running=true;
	this.next();
}

/**
	@constructor
*/
n2i.Color = function(color_string) {
    this.ok = false;

    // strip any leading #
    if (color_string.charAt(0) == '#') { // remove # if any
        color_string = color_string.substr(1,6);
    }

    color_string = color_string.replace(/ /g,'');
    color_string = color_string.toLowerCase();

    // before getting into regexps, try simple matches
    // and overwrite the input
    var simple_colors = {
        aliceblue: 'f0f8ff',
        antiquewhite: 'faebd7',
        aqua: '00ffff',
        aquamarine: '7fffd4',
        azure: 'f0ffff',
        beige: 'f5f5dc',
        bisque: 'ffe4c4',
        black: '000000',
        blanchedalmond: 'ffebcd',
        blue: '0000ff',
        blueviolet: '8a2be2',
        brown: 'a52a2a',
        burlywood: 'deb887',
        cadetblue: '5f9ea0',
        chartreuse: '7fff00',
        chocolate: 'd2691e',
        coral: 'ff7f50',
        cornflowerblue: '6495ed',
        cornsilk: 'fff8dc',
        crimson: 'dc143c',
        cyan: '00ffff',
        darkblue: '00008b',
        darkcyan: '008b8b',
        darkgoldenrod: 'b8860b',
        darkgray: 'a9a9a9',
        darkgreen: '006400',
        darkkhaki: 'bdb76b',
        darkmagenta: '8b008b',
        darkolivegreen: '556b2f',
        darkorange: 'ff8c00',
        darkorchid: '9932cc',
        darkred: '8b0000',
        darksalmon: 'e9967a',
        darkseagreen: '8fbc8f',
        darkslateblue: '483d8b',
        darkslategray: '2f4f4f',
        darkturquoise: '00ced1',
        darkviolet: '9400d3',
        deeppink: 'ff1493',
        deepskyblue: '00bfff',
        dimgray: '696969',
        dodgerblue: '1e90ff',
        feldspar: 'd19275',
        firebrick: 'b22222',
        floralwhite: 'fffaf0',
        forestgreen: '228b22',
        fuchsia: 'ff00ff',
        gainsboro: 'dcdcdc',
        ghostwhite: 'f8f8ff',
        gold: 'ffd700',
        goldenrod: 'daa520',
        gray: '808080',
        green: '008000',
        greenyellow: 'adff2f',
        honeydew: 'f0fff0',
        hotpink: 'ff69b4',
        indianred : 'cd5c5c',
        indigo : '4b0082',
        ivory: 'fffff0',
        khaki: 'f0e68c',
        lavender: 'e6e6fa',
        lavenderblush: 'fff0f5',
        lawngreen: '7cfc00',
        lemonchiffon: 'fffacd',
        lightblue: 'add8e6',
        lightcoral: 'f08080',
        lightcyan: 'e0ffff',
        lightgoldenrodyellow: 'fafad2',
        lightgrey: 'd3d3d3',
        lightgreen: '90ee90',
        lightpink: 'ffb6c1',
        lightsalmon: 'ffa07a',
        lightseagreen: '20b2aa',
        lightskyblue: '87cefa',
        lightslateblue: '8470ff',
        lightslategray: '778899',
        lightsteelblue: 'b0c4de',
        lightyellow: 'ffffe0',
        lime: '00ff00',
        limegreen: '32cd32',
        linen: 'faf0e6',
        magenta: 'ff00ff',
        maroon: '800000',
        mediumaquamarine: '66cdaa',
        mediumblue: '0000cd',
        mediumorchid: 'ba55d3',
        mediumpurple: '9370d8',
        mediumseagreen: '3cb371',
        mediumslateblue: '7b68ee',
        mediumspringgreen: '00fa9a',
        mediumturquoise: '48d1cc',
        mediumvioletred: 'c71585',
        midnightblue: '191970',
        mintcream: 'f5fffa',
        mistyrose: 'ffe4e1',
        moccasin: 'ffe4b5',
        navajowhite: 'ffdead',
        navy: '000080',
        oldlace: 'fdf5e6',
        olive: '808000',
        olivedrab: '6b8e23',
        orange: 'ffa500',
        orangered: 'ff4500',
        orchid: 'da70d6',
        palegoldenrod: 'eee8aa',
        palegreen: '98fb98',
        paleturquoise: 'afeeee',
        palevioletred: 'd87093',
        papayawhip: 'ffefd5',
        peachpuff: 'ffdab9',
        peru: 'cd853f',
        pink: 'ffc0cb',
        plum: 'dda0dd',
        powderblue: 'b0e0e6',
        purple: '800080',
        red: 'ff0000',
        rosybrown: 'bc8f8f',
        royalblue: '4169e1',
        saddlebrown: '8b4513',
        salmon: 'fa8072',
        sandybrown: 'f4a460',
        seagreen: '2e8b57',
        seashell: 'fff5ee',
        sienna: 'a0522d',
        silver: 'c0c0c0',
        skyblue: '87ceeb',
        slateblue: '6a5acd',
        slategray: '708090',
        snow: 'fffafa',
        springgreen: '00ff7f',
        steelblue: '4682b4',
        tan: 'd2b48c',
        teal: '008080',
        thistle: 'd8bfd8',
        tomato: 'ff6347',
        turquoise: '40e0d0',
        violet: 'ee82ee',
        violetred: 'd02090',
        wheat: 'f5deb3',
        white: 'ffffff',
        whitesmoke: 'f5f5f5',
        yellow: 'ffff00',
        yellowgreen: '9acd32'
    };
    for (var key in simple_colors) {
        if (color_string == key) {
            color_string = simple_colors[key];
        }
    }
    // emd of simple type-in colors

    // array of color definition objects
    var color_defs = [
        {
            re: /^rgb\((\d{1,3}),\s*(\d{1,3}),\s*(\d{1,3})\)$/,
            example: ['rgb(123, 234, 45)', 'rgb(255,234,245)'],
            process: function (bits){
                return [
                    parseInt(bits[1]),
                    parseInt(bits[2]),
                    parseInt(bits[3])
                ];
            }
        },
        {
            re: /^(\w{2})(\w{2})(\w{2})$/,
            example: ['#00ff00', '336699'],
            process: function (bits){
                return [
                    parseInt(bits[1], 16),
                    parseInt(bits[2], 16),
                    parseInt(bits[3], 16)
                ];
            }
        },
        {
            re: /^(\w{1})(\w{1})(\w{1})$/,
            example: ['#fb0', 'f0f'],
            process: function (bits){
                return [
                    parseInt(bits[1] + bits[1], 16),
                    parseInt(bits[2] + bits[2], 16),
                    parseInt(bits[3] + bits[3], 16)
                ];
            }
        }
    ];

    // search through the definitions to find a match
    for (var i = 0; i < color_defs.length; i++) {
        var re = color_defs[i].re;
        var processor = color_defs[i].process;
        var bits = re.exec(color_string);
        if (bits) {
            channels = processor(bits);
            this.r = channels[0];
            this.g = channels[1];
            this.b = channels[2];
            this.ok = true;
        }

    }

    // validate/cleanup values
    this.r = (this.r < 0 || isNaN(this.r)) ? 0 : ((this.r > 255) ? 255 : this.r);
    this.g = (this.g < 0 || isNaN(this.g)) ? 0 : ((this.g > 255) ? 255 : this.g);
    this.b = (this.b < 0 || isNaN(this.b)) ? 0 : ((this.b > 255) ? 255 : this.b);

    // some getters
    this.toRGB = function () {
        return 'rgb(' + this.r + ', ' + this.g + ', ' + this.b + ')';
    }
    this.toHex = function () {
        var r = this.r.toString(16);
        var g = this.g.toString(16);
        var b = this.b.toString(16);
        if (r.length == 1) r = '0' + r;
        if (g.length == 1) g = '0' + g;
        if (b.length == 1) b = '0' + b;
        return '#' + r + g + b;
    }

}


/** @namespace */
n2i.ease = {
	slowFastSlow : function(val) {
		var a = 1.6;
		var b = 1.4;
		return -1*Math.pow(Math.cos((Math.PI/2)*Math.pow(val,a)),Math.pow(Math.PI,b))+1;
	},
	fastSlow : function(val) {
		var a = .5;
		var b = .7
		return -1*Math.pow(Math.cos((Math.PI/2)*Math.pow(val,a)),Math.pow(Math.PI,b))+1;
	},
	elastic : function(t) {
		return 1 - n2i.ease.elastic2(1-t);
	},

	elastic2 : function (t, a, p) {
		if (t<=0 || t>=1) return t;
		if (!p) p=0.45;
		var s;
		if (!a || a < 1) {
			a=1;
			s=p/4;
		} else {
			s = p/(2*Math.PI) * Math.asin (1/a);
		}
		return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t-s)*(2*Math.PI)/p ));
	},
	bounce : function(t) {
		if (t < (1/2.75)) {
			return 7.5625*t*t;
		} else if (t < (2/2.75)) {
			return (7.5625*(t-=(1.5/2.75))*t + .75);
		} else if (t < (2.5/2.75)) {
			return (7.5625*(t-=(2.25/2.75))*t + .9375);
		} else {
			return (7.5625*(t-=(2.625/2.75))*t + .984375);
		}
	},
	
	linear: function(/* Decimal? */n){
		// summary: A linear easing function
		return n;
	},

	quadIn: function(/* Decimal? */n){
		return Math.pow(n, 2);
	},

	quadOut: function(/* Decimal? */n){
		return n * (n-2) * -1;
	},

	quadInOut: function(/* Decimal? */n){
		n=n*2;
		if(n<1){ return Math.pow(n, 2) / 2; }
		return -1 * ((--n)*(n-2) - 1) / 2;
	},

	cubicIn: function(/* Decimal? */n){
		return Math.pow(n, 3);
	},

	cubicOut: function(/* Decimal? */n){
		return Math.pow(n-1, 3) + 1;
	},

	cubicInOut: function(/* Decimal? */n){
		n=n*2;
		if(n<1){ return Math.pow(n, 3) / 2; }
		n-=2;
		return (Math.pow(n, 3) + 2) / 2;
	},

	quartIn: function(/* Decimal? */n){
		return Math.pow(n, 4);
	},

	quartOut: function(/* Decimal? */n){
		return -1 * (Math.pow(n-1, 4) - 1);
	},

	quartInOut: function(/* Decimal? */n){
		n=n*2;
		if(n<1){ return Math.pow(n, 4) / 2; }
		n-=2;
		return -1/2 * (Math.pow(n, 4) - 2);
	},

	quintIn: function(/* Decimal? */n){
		return Math.pow(n, 5);
	},

	quintOut: function(/* Decimal? */n){
		return Math.pow(n-1, 5) + 1;
	},

	quintInOut: function(/* Decimal? */n){
		n=n*2;
		if(n<1){ return Math.pow(n, 5) / 2; };
		n-=2;
		return (Math.pow(n, 5) + 2) / 2;
	},

	sineIn: function(/* Decimal? */n){
		return -1 * Math.cos(n * (Math.PI/2)) + 1;
	},

	sineOut: function(/* Decimal? */n){
		return Math.sin(n * (Math.PI/2));
	},

	sineInOut: function(/* Decimal? */n){
		return -1 * (Math.cos(Math.PI*n) - 1) / 2;
	},

	expoIn: function(/* Decimal? */n){
		return (n==0) ? 0 : Math.pow(2, 10 * (n - 1));
	},

	expoOut: function(/* Decimal? */n){
		return (n==1) ? 1 : (-1 * Math.pow(2, -10 * n) + 1);
	},

	expoInOut: function(/* Decimal? */n){
		if(n==0){ return 0; }
		if(n==1){ return 1; }
		n = n*2;
		if(n<1){ return Math.pow(2, 10 * (n-1)) / 2; }
		--n;
		return (-1 * Math.pow(2, -10 * n) + 2) / 2;
	},

	circIn: function(/* Decimal? */n){
		return -1 * (Math.sqrt(1 - Math.pow(n, 2)) - 1);
	},

	circOut: function(/* Decimal? */n){
		n = n-1;
		return Math.sqrt(1 - Math.pow(n, 2));
	},

	circInOut: function(/* Decimal? */n){
		n = n*2;
		if(n<1){ return -1/2 * (Math.sqrt(1 - Math.pow(n, 2)) - 1); }
		n-=2;
		return 1/2 * (Math.sqrt(1 - Math.pow(n, 2)) + 1);
	},

	backIn: function(/* Decimal? */n){
		var s = 1.70158;
		return Math.pow(n, 2) * ((s+1)*n - s);
	},

	backOut: function(/* Decimal? */n){
		// summary: an easing function that pops past the range briefly, and 
		// 	slowly comes back. 
		n = n - 1;
		var s = 1.70158;
		return Math.pow(n, 2) * ((s + 1) * n + s) + 1;
	},

	backInOut: function(/* Decimal? */n){
		var s = 1.70158 * 1.525;
		n = n*2;
		if(n < 1){ return (Math.pow(n, 2)*((s+1)*n - s))/2; }
		n-=2;
		return (Math.pow(n, 2)*((s+1)*n + s) + 2)/2;
	},

	elasticIn: function(/* Decimal? */n){
		if(n==0){ return 0; }
		if(n==1){ return 1; }
		var p = .3;
		var s = p/4;
		n = n - 1;
		return -1 * Math.pow(2,10*n) * Math.sin((n-s)*(2*Math.PI)/p);
	},

	elasticOut: function(/* Decimal? */n){
		// summary: An easing function that elasticly snaps around the target value, near the end of the Animation
		if(n==0) return 0;
		if(n==1) return 1;
		var p = .3;
		var s = p/4;
		return Math.pow(2,-10*n) * Math.sin((n-s)*(2*Math.PI)/p) + 1;
	},

	elasticInOut: function(/* Decimal? */n){
		// summary: An easing function that elasticly snaps around the value, near the beginning and end of the Animation		
		if(n==0) return 0;
		n = n*2;
		if(n==2) return 1;
		var p = .3*1.5;
		var s = p/4;
		if(n<1){
			n-=1;
			return -.5*(Math.pow(2,10*n) * Math.sin((n-s)*(2*Math.PI)/p));
		}
		n-=1;
		return .5*(Math.pow(2,-10*n) * Math.sin((n-s)*(2*Math.PI)/p)) + 1;
	},

	bounceIn: function(/* Decimal? */n){
		// summary: An easing function that "bounces" near the beginning of an Animation
		return (1 - n2i.ease.bounceOut(1-n)); // Decimal
	},

	bounceOut: function(/* Decimal? */n){
		// summary: An easing function that "bounces" near the end of an Animation
		var s=7.5625;
		var p=2.75;
		var l; 
		if(n < (1 / p)){
			l = s*Math.pow(n, 2);
		}else if(n < (2 / p)){
			n -= (1.5 / p);
			l = s * Math.pow(n, 2) + .75;
		}else if(n < (2.5 / p)){
			n -= (2.25 / p);
			l = s * Math.pow(n, 2) + .9375;
		}else{
			n -= (2.625 / p);
			l = s * Math.pow(n, 2) + .984375;
		}
		return l;
	},

	bounceInOut: function(/* Decimal? */n){
		// summary: An easing function that "bounces" at the beginning and end of the Animation
		if(n<0.5){ return n2i.ease.bounceIn(n*2) / 2; }
		return (n2i.ease.bounceOut(n*2-1) / 2) + 0.5; // Decimal
	}
};var WysiHat = {};

WysiHat.Editor = {
  /**
   *  WysiHat.Editor.attach(textarea)
   *  - textarea (String | Element): an id or DOM node of the textarea that
   *  you want to convert to rich text.
   *
   *  Creates a new editor for the textarea.
   **/
  attach: function(textarea) {
    textarea = $(textarea);
    textarea.hide();

    return WysiHat.iFrame.create(textarea, function(editArea) {
      var document = editArea.getDocument();
      var window = editArea.getWindow();

      editArea.load();

      Event.observe(window, 'focus', function(event) { editArea.focus(); });
      Event.observe(window, 'blur', function(event) { editArea.blur(); });


      Event.observe(document, 'mouseup', function(event) {
        editArea.fire("wysihat:mouseup");
      });

      Event.observe(document, 'mousemove', function(event) {
        editArea.fire("wysihat:mousemove");
      });

      Event.observe(document, 'keypress', function(event) {
        editArea.fire("wysihat:change");
        editArea.fire("wysihat:keypress");
      });

      Event.observe(document, 'keyup', function(event) {
        editArea.fire("wysihat:change");
        editArea.fire("wysihat:keyup");
      });

      Event.observe(document, 'keydown', function(event) {
        if (event.keyCode == 86)
          editArea.fire("wysihat:paste");
      });

      Event.observe(window, 'paste', function(event) {
        editArea.fire("wysihat:paste");
      });


      editArea.observe("wysihat:change", function(event) {
        event.target.save();
      });
      //editArea.focus(); HACK!
    });
  }
};

/**
 * mixin WysiHat.Commands
 *
 *  Methods will be mixed into the editor element. Most of these
 *  methods will be used to bind to button clicks or key presses.
 *
 *  var editor = WysiHat.Editor.attach(textarea);
 *  $('bold_button').observe('click', function(event) {
 *    editor.boldSelection();
 *    Event.stop(event);
 *  });
 *
 *  In this example, it is important to stop the click event so you don't
 *  lose your current selection.
 **/
WysiHat.Commands = {
  /**
   * WysiHat.Commands#boldSelection() -> undefined
   *  Bolds the current selection.
   **/
  boldSelection: function() {
    this.execCommand('bold', false, null);
  },

  /**
   * WysiHat.Commands#underlineSelection() -> undefined
   *  Underlines the current selection.
   **/
  underlineSelection: function() {
    this.execCommand('underline', false, null);
  },

  /**
   * WysiHat.Commands#italicSelection() -> undefined
   *  Italicizes the current selection.
   **/
  italicSelection: function() {
    this.execCommand('italic', false, null);
  },

  /**
   * WysiHat.Commands#italicSelection() -> undefined
   *  Strikethroughs the current selection.
   **/
  strikethroughSelection: function() {
    this.execCommand('strikethrough', false, null);
  },

  /**
   * WysiHat.Commands#italicSelection() -> undefined
   *  Blockquotes the current selection.
   **/
  blockquoteSelection: function() {
    this.execCommand('blockquote', false, null);
  },

  /**
   * WysiHat.Commands#colorSelection(color) -> undefined
   *  - color (String): a color name or hexadecimal value
   *  Sets the foreground color of the current selection.
   **/
  colorSelection: function(color) {
    this.execCommand('forecolor', false, color);
  },

  /**
   * WysiHat.Commands#linkSelection(url) -> undefined
   *  - url (String): value for href
   *  Wraps the current selection in a link.
   **/
  linkSelection: function(url) {
    this.execCommand('createLink', false, url);
  },

  /**
   * WysiHat.Commands#insertOrderedList() -> undefined
   *  Formats current selection as an ordered list. If the selection is empty
   *  a new list is inserted.
   **/
  insertOrderedList: function() {
    this.execCommand('insertorderedlist', false, null);
  },

  /**
   * WysiHat.Commands#insertUnorderedList() -> undefined
   *  Formats current selection as an unordered list. If the selection is empty
   *  a new list is inserted.
   **/
  insertUnorderedList: function() {
    this.execCommand('insertunorderedlist', false, null);
  },

  /**
   * WysiHat.Commands#insertImage(url) -> undefined
   *  - url (String): value for src
   *  Insert an image at the insertion point with the given url.
   **/
  insertImage: function(url) {
    this.execCommand('insertImage', false, url);
  },

  /**
   * WysiHat.Commands#insertHTML(html) -> undefined
   *  - html (String): HTML or plain text
   *  Insert HTML at the insertion point.
   **/
  insertHTML: function(html) {
    if (Prototype.Browser.IE) {
      var range = this._selection.getRange();
      range.pasteHTML(html);
      range.collapse(false);
      range.select();
    } else {
      this.execCommand('insertHTML', false, html);
    }
  },

  /**
   * WysiHat.Commands#execCommand(command[, ui = false][, value = null]) -> undefined
   *  - command (String): Command to execute
   *  - ui (Boolean): Boolean flag for showing UI. Currenty this not
   *  implemented by any browser. Just use false.
   *  - value (String): Value to pass to command
   *  A simple delegation method to the documents execCommand method.
   **/
  execCommand: function(command, ui, value) {
    var document = this.getDocument();
    document.execCommand(command, ui, value);
  },

  queryStateCommands: $A(['bold', 'italic', 'underline', 'strikethrough']),

  /**
   * WysiHat.Commands#queryCommandState(state) -> Boolean
   *  - state (String): bold, italic, underline, etc
   *  Determines whether the current selection has the given state.
   *  queryCommandState('bold') would return true if the selected text
   *  is bold.
   *
   * You can extend this behavior by adding a custom method to the editor
   * element, queryCustom(). However this API is not final.
   * queryCommandState('link') would call queryLink().
   **/
  queryCommandState: function(state) {
    var document = this.getDocument();

    if (this.queryStateCommands.include(state))
      return document.queryCommandState(state);
    else if ((f = this['query' + state.capitalize()]))
      return f.bind(this).call();
    else
      return false;
  }
};
/**
 * mixin WysiHat.Persistence
 *
 *  Methods will be mixed into the editor element. These methods deal with
 *  extracting and filtering content going in and out of the editor.
 */
WysiHat.Persistence = (function() {
  /**
   * WysiHat.Persistence#outputFilter(text) -> String
   *  - text (String): HTML string
   *
   *  Use to filter content coming out of the editor. By default it calls
   *  text.format_html_output. This method has been extract so you can override
   *  it and provide your own custom output filter.
   */
  function outputFilter(text) {
    return text.format_html_output();
  }

  /**
   * WysiHat.Persistence#inputFilter(text) -> String
   *  - text (String): HTML string
   *
   *  Use to filter content going into the editor. By default it calls
   *  text.format_html_input. This method has been extract so you can override
   *  it and provide your own custom input filter.
   */
  function inputFilter(text) {
    return text.format_html_input();
  }

  /**
   * WysiHat.Persistence#content() -> String
   *  Returns the editors HTML contents. The contents are first passed
   *  through outputFilter.
   *
   *  You can replace the generic outputFilter with your own function. The
   *  default behavior is to use String#format_html_output.
   *
   *  editor.outputFilter = function(text) {
   *    return MyUtils.format_and_santize(text);
   *  };
   **/
  function content() {
    return this.outputFilter(this.rawContent());
  }

  /**
   * WysiHat.Persistence#setContent(text) -> undefined
   *  - text (String): HTML string
   *
   *  Replaces editor's entire contents with the given HTML. The contents are
   *  first passed through inputFilter.
   *
   *  You can replace the generic inputFilter with your own function. The
   *  default behavior is to use String#format_html_input.
   *
   *  editor.inputFilter = function(text) {
   *    return MyUtils.format_and_santize(text);
   *  };
   **/
  function setContent(text) {
    this.setRawContent(this.inputFilter(text));
  }

  /**
   * WysiHat.Persistence#save() -> undefined
   *  Saves editors contents back out to the textarea.
   **/
  function save() {
    this.textarea.value = this.content();
  }

  /**
   * WysiHat.Persistence#load() -> undefined
   *  Loads textarea contents into editor.
   **/
   function load() {
     this.setContent(this.textarea.value);
  }

  /**
   * WysiHat.Persistence#reload() -> undefined
   *  Saves current contents and loads contents into editor.
   **/
  function reload() {
    this.selection.setBookmark();
    this.save();
    this.load();
    this.selection.moveToBookmark();
  }

  return {
    outputFilter: outputFilter,
    inputFilter:  inputFilter,
    content:      content,
    setContent:   setContent,
    save:         save,
    load:         load,
    reload:       reload
  };
})();
/**
 * mixin WysiHat.Window
 *
 *  Methods will be mixed into the editor element. These methods handle window
 *  events such as focus and blur events on the editor.
 */
WysiHat.Window = (function() {
  /**
   * WysiHat.Window#getDocument() -> Document
   *  Cross browser method to return the iFrame's document.
   *  You should not need to access this directly, and this API is not final.
   */
  function getDocument() {
    return this.contentDocument || this.contentWindow.document;
  }

  /**
   * WysiHat.Window#getWindow() -> Window
   *  Cross browser method to return the iFrame's window.
   *  You should not need to access this directly, and this API is not final.
   */
  function getWindow() {
    if (this.contentWindow.document)
      return this.contentWindow;
    else if (this.contentDocument)
      return this.contentDocument.defaultView;
    else
      return null;
  }

  /**
   * WysiHat.Window#focus() -> undefined
   *  binds observers to mouseup, mousemove, keypress, and keyup on focus
   **/
  function focus() {
    this.getWindow().focus();

    if (this.hasFocus)
      return;

    this.hasFocus = true;
  }

  /**
   * WysiHat.Window#blur() -> undefined
   *  removes observers to mouseup, mousemove, keypress, and keyup on blur
   **/
  function blur() {
    this.hasFocus = false;
  }

  return {
    getDocument: getDocument,
    getWindow: getWindow,
    focus: focus,
    blur: blur
  };
})();

WysiHat.iFrame = {
  create: function(textarea, callback) {
    var editArea = new Element('iframe', { 'id': textarea.id + '_editor', 'class': 'editor', allowtransparency:'true' });

    Object.extend(editArea, WysiHat.Commands);
    Object.extend(editArea, WysiHat.Persistence);
    Object.extend(editArea, WysiHat.Window);
    Object.extend(editArea, WysiHat.iFrame.Methods);

    editArea.attach(textarea, callback);

    textarea.insert({before: editArea});

    return editArea;
  }
};

WysiHat.iFrame.Methods = {
  attach: function(element, callback) {
    this.textarea = element;

    this.observe('load', function() {
      function setDocumentStyles(document, styles) {
        if (Prototype.Browser.IE) {
          var style = document.createStyleSheet();
          style.addRule("body", "border: 0");
          style.addRule("p", "margin: 0");

          $H(styles).each(function(pair) {
            var value = pair.first().underscore().dasherize() + ": " + pair.last();
            style.addRule("body", value);
          });
        } else if (Prototype.Browser.Opera) {
          var style = Element('style').update("p { margin: 0; }");
          var head = document.getElementsByTagName('head')[0];
          head.appendChild(style);
        } else {
          Element.setStyle(document.body, styles);
        }
      }

      try {
        var document = this.getDocument();
      } catch(e) { return; } // No iframe, just stop

      this.selection = new WysiHat.Selection(this);

      if (this.ready && document.designMode == 'on')
        return;

      setDocumentStyles(document, WysiHat.Editor.styles || {});

      document.designMode = 'on';
      callback(this);
      this.ready = true;
	if (document.body) {
      this.fire("wysihat:loaded"); // HACK
}
    });
  },

  rawContent: function() {
    var document = this.getDocument();
    return document.body.innerHTML;
  },

  setRawContent: function(text) {
    var document = this.getDocument();
    if (document.body)
      document.body.innerHTML = text;
  }
};

Object.extend(String.prototype, (function() {
  /**
   * String#format_html_output() -> String
   *
   *  Cleanup browser's HTML mess!
   *
   *  There is no standard formatting among the major browsers for the rich
   *  text output. Safari wraps its line breaks with "div" tags, Firefox puts
   *  "br" tags at the end of the line, and other such as Internet Explorer
   *  wrap lines in "p" tags.
   *
   *  The output is a standarizes these inconsistencies and produces a clean
   *  result. A single return creates a line break "br" and double returns
   *  create a new paragraph. This is similar to how Textile and Markdown
   *  handle whitespace.
   *
   *  Raw browser content => String#format_html_output => Textarea
   **/
  function format_html_output() {
    var text = String(this);
    text = text.tidy_xhtml();

    if (Prototype.Browser.WebKit) {
      text = text.replace(/(<div>)+/g, "\n");
      text = text.replace(/(<\/div>)+/g, "");

      text = text.replace(/<p>\s*<\/p>/g, "");

      text = text.replace(/<br \/>(\n)*/g, "\n");
    } else if (Prototype.Browser.Gecko) {
      text = text.replace(/<p>/g, "");
      text = text.replace(/<\/p>(\n)?/g, "\n");

      text = text.replace(/<br \/>(\n)*/g, "\n");
    } else if (Prototype.Browser.IE || Prototype.Browser.Opera) {
      text = text.replace(/<p>(&nbsp;|&#160;|\s)<\/p>/g, "<p></p>");

      text = text.replace(/<br \/>/g, "");

      text = text.replace(/<p>/g, '');

      text = text.replace(/&nbsp;/g, '');

      text = text.replace(/<\/p>(\n)?/g, "\n");

      text = text.gsub(/^<p>/, '');
      text = text.gsub(/<\/p>$/, '');
    }

    text = text.gsub(/<b>/, "<strong>");
    text = text.gsub(/<\/b>/, "</strong>");

    text = text.gsub(/<i>/, "<em>");
    text = text.gsub(/<\/i>/, "</em>");

    text = text.replace(/\n\n+/g, "</p>\n\n<p>");

    text = text.gsub(/(([^\n])(\n))(?=([^\n]))/, "#{2}<br />\n");

    text = '<p>' + text + '</p>';

    text = text.replace(/<p>\s*/g, "<p>");
    text = text.replace(/\s*<\/p>/g, "</p>");

    var element = Element("body");
    element.innerHTML = text;

    if (Prototype.Browser.WebKit || Prototype.Browser.Gecko) {
      var replaced;
      do {
        replaced = false;
        element.select('span').each(function(span) {
          if (span.hasClassName('Apple-style-span')) {
            span.removeClassName('Apple-style-span');
            if (span.className == '')
              span.removeAttribute('class');
            replaced = true;
          } else if (span.getStyle('fontWeight') == 'bold') {
            span.setStyle({fontWeight: ''});
            if (span.style.length == 0)
              span.removeAttribute('style');
            span.update('<strong>' + span.innerHTML + '</strong>');
            replaced = true;
          } else if (span.getStyle('fontStyle') == 'italic') {
            span.setStyle({fontStyle: ''});
            if (span.style.length == 0)
              span.removeAttribute('style');
            span.update('<em>' + span.innerHTML + '</em>');
            replaced = true;
          } else if (span.getStyle('textDecoration') == 'underline') {
            span.setStyle({textDecoration: ''});
            if (span.style.length == 0)
              span.removeAttribute('style');
            span.update('<u>' + span.innerHTML + '</u>');
            replaced = true;
          } else if (span.attributes.length == 0) {
            span.replace(span.innerHTML);
            replaced = true;
          }
        });
      } while (replaced);

    }

    for (var i = 0; i < element.descendants().length; i++) {
      var node = element.descendants()[i];
      if (node.innerHTML.blank() && node.nodeName != 'BR' && node.id != 'bookmark')
        node.remove();
    }

    text = element.innerHTML;
    text = text.tidy_xhtml();

    text = text.replace(/<br \/>(\n)*/g, "<br />\n");
    text = text.replace(/<\/p>\n<p>/g, "</p>\n\n<p>");

    text = text.replace(/<p>\s*<\/p>/g, "");

    text = text.replace(/\s*$/g, "");

    return text;
  }

  /**
   * String#format_html_input() -> String
   *
   *  Prepares sane HTML for editing.
   *
   *  This function preforms the reserve function of String#format_html_output. Each
   *  browser has difficulty editing mix formatting conventions. This restores
   *  most of the original browser specific formatting tags and some other
   *  styling conventions.
   *
   *  Textarea => String#format_html_input => Raw content
  **/
  function format_html_input() {
    var text = String(this);

    var element = Element("body");
    element.innerHTML = text;

    if (Prototype.Browser.Gecko || Prototype.Browser.WebKit) {
      element.select('strong').each(function(element) {
        element.replace('<span style="font-weight: bold;">' + element.innerHTML + '</span>');
      });
      element.select('em').each(function(element) {
        element.replace('<span style="font-style: italic;">' + element.innerHTML + '</span>');
      });
      element.select('u').each(function(element) {
        element.replace('<span style="text-decoration: underline;">' + element.innerHTML + '</span>');
      });
    }

    if (Prototype.Browser.WebKit)
      element.select('span').each(function(span) {
        if (span.getStyle('fontWeight') == 'bold')
          span.addClassName('Apple-style-span');

        if (span.getStyle('fontStyle') == 'italic')
          span.addClassName('Apple-style-span');

        if (span.getStyle('textDecoration') == 'underline')
          span.addClassName('Apple-style-span');
      });

    text = element.innerHTML;
    text = text.tidy_xhtml();

    text = text.replace(/<\/p>(\n)*<p>/g, "\n\n");

    text = text.replace(/(\n)?<br( \/)?>(\n)?/g, "\n");

    text = text.replace(/^<p>/g, '');
    text = text.replace(/<\/p>$/g, '');

    if (Prototype.Browser.Gecko) {
      text = text.replace(/\n/g, "<br>");
      text = text + '<br>';
    } else if (Prototype.Browser.WebKit) {
      text = text.replace(/\n/g, "</div><div>");
      text = '<div>' + text + '</div>';
      text = text.replace(/<div><\/div>/g, "<div><br></div>");
    } else if (Prototype.Browser.IE || Prototype.Browser.Opera) {
      text = text.replace(/\n/g, "</p>\n<p>");
      text = '<p>' + text + '</p>';
      text = text.replace(/<p><\/p>/g, "<p>&nbsp;</p>");
      text = text.replace(/(<p>&nbsp;<\/p>)+$/g, "");
    }

    return text;
  }

  /**
   * String#tidy_xhtml() -> String
   *
   *  Normalizes and tidies text into XHTML content.
   *   * Strips out browser line breaks, '\r'
   *   * Downcases tag names
   *   * Closes line break tags
   **/
  function tidy_xhtml() {
    var text = String(this);

    text = text.gsub(/\r\n?/, "\n");

    text = text.gsub(/<([A-Z]+)([^>]*)>/, function(match) {
      return '<' + match[1].toLowerCase() + match[2] + '>';
    });

    text = text.gsub(/<\/([A-Z]+)>/, function(match) {
      return '</' + match[1].toLowerCase() + '>';
    });

    text = text.replace(/<br>/g, "<br />");

    return text;
  }

  return {
    format_html_output: format_html_output,
    format_html_input:  format_html_input,
    tidy_xhtml:         tidy_xhtml
  };
})());
Object.extend(String.prototype, {
  /**
   * String#sanitize([options]) -> String
   * - options (Hash): Whitelist options
   *
   *  Sanitizes HTML tags and attributes. Options accepts an array of
   *  allowed tags and attributes.
   #
   *  "<a href='#'>Example</a>".sanitize({tags: ['a'], attributes: ['href']})
   **/
  sanitize: function(options) {
    return Element("div").update(this).sanitize(options).innerHTML.tidy_xhtml();
  }
});

Element.addMethods({
  /**
   * Element#sanitize([options]) -> Element
   * - options (Hash): Whitelist options
   *
   *  Sanitizes element tags and attributes. Options accepts an array of
   *  allowed tags and attributes.
   #
   *  This method is called by String#sanitize().
   **/
  sanitize: function(element, options) {
    element = $(element);
    options = $H(options);
    var allowed_tags = $A(options.get('tags') || []);
    var allowed_attributes = $A(options.get('attributes') || []);
    var sanitized = Element(element.nodeName);

    $A(element.childNodes).each(function(child) {
      if (child.nodeType == 1) {
        var children = $(child).sanitize(options).childNodes;

        if (allowed_tags.include(child.nodeName.toLowerCase())) {
          var new_child = Element(child.nodeName);
          allowed_attributes.each(function(attribute) {
            if ((value = child.readAttribute(attribute)))
              new_child.writeAttribute(attribute, value);
          });
          sanitized.appendChild(new_child);

          $A(children).each(function(grandchild) { new_child.appendChild(grandchild); });
        } else {
          $A(children).each(function(grandchild) { sanitized.appendChild(grandchild); });
        }
      } else if (child.nodeType == 3) {
        sanitized.appendChild(child);
      }
    });
    return sanitized;
  }
});

/**
 * class Range
 *
 *  *Under construction*
 *
 *  An attempt to implement the W3C Range in IE.
 *
 **/
if (Prototype.Browser.IE) {
  function Range(ownerDocument) {
    this.ownerDocument = ownerDocument;

    this.startContainer = this.ownerDocument.documentElement;
    this.startOffset    = 0;
    this.endContainer   = this.ownerDocument.documentElement;
    this.endOffset      = 0;

    this.collapsed = true;
    this.commonAncestorContainer = null;

    this.START_TO_START = 0;
    this.START_TO_END   = 1;
    this.END_TO_END     = 2;
    this.END_TO_START   = 3;
  }

  document.createRange = function() {
    return new Range(this);
  };

  Object.extend(Range.prototype, {
    setStart: function(parent, offset) {},
    setEnd: function(parent, offset) {},
    setStartBefore: function(node) {},
    setStartAfter: function(node) {},
    setEndBefore: function(node) {},
    setEndAfter: function(node) {},

    collapse: function(toStart) {},

    selectNode: function(n) {},
    selectNodeContents: function(n) {},

    compareBoundaryPoints: function(how, sourceRange) {},

    deleteContents: function() {},
    extractContents: function() {},
    cloneContents: function() {},

    insertNode: function(n) {
      var range = this.ownerDocument.selection.createRange();
      var parent = this.ownerDocument.createElement('div');
      parent.appendChild(n);
      range.collapse();
      range.pasteHTML(parent.innerHTML);
    },
    surroundContents: function(newParent) {
      var range = this.ownerDocument.selection.createRange();
      var parent = this.document.createElement('div');
      parent.appendChild(newParent);
      node.innerHTML += range.htmlText;
      range.pasteHTML(parent.innerHTML);
    },

    cloneRange: function() {},
    toString: function() {},
    detach: function() {}
  });
}
/**
 * class WysiHat.Selection
 **/
WysiHat.Selection = Class.create((function() {
  /**
   *  new WysiHat.Selection(editor)
   *  - editor (WysiHat.Editor): the editor object that you want to bind to
   **/
  function initialize(editor) {
    this.window = editor.getWindow();
    this.document = editor.getDocument();
  }

  /**
   * WysiHat.Selection#getSelection() -> Selection
   *  Get selected text.
   **/
  function getSelection() {
    return this.window.getSelection ? this.window.getSelection() : this.document.selection;
  }

  /**
   * WysiHat.Selection#getRange() -> Range
   *  Get range for selected text.
   **/
  function getRange() {
    var selection = this.getSelection();

    try {
      var range;
      if (selection.getRangeAt)
        range = selection.getRangeAt(0);
      else
        range = selection.createRange();
    } catch(e) { return null; }

    if (Prototype.Browser.WebKit) {
      range.setStart(selection.baseNode, selection.baseOffset);
      range.setEnd(selection.extentNode, selection.extentOffset);
    }

    return range;
  }

  /**
   * WysiHat.Selection#selectNode(node) -> undefined
   * - node (Element): Element or node to select
   **/
  function selectNode(node) {
    var selection = this.getSelection();

    if (Prototype.Browser.IE) {
      var range = createRangeFromElement(this.document, node);
      range.select();
    } else if (Prototype.Browser.WebKit) {
      selection.setBaseAndExtent(node, 0, node, node.innerText.length);
    } else if (Prototype.Browser.Opera) {
      range = this.document.createRange();
      range.selectNode(node);
      selection.removeAllRanges();
      selection.addRange(range);
    } else {
      var range = createRangeFromElement(this.document, node);
      selection.removeAllRanges();
      selection.addRange(range);
    }
  }

  /**
   * WysiHat.Selection#getNode() -> Element
   *  Returns selected node.
   **/
  function getNode() {
    var nodes = null, candidates = [], children, el;
    var range = this.getRange();

    if (!range)
      return null;

    var parent;
    if (range.parentElement)
      parent = range.parentElement();
    else
      parent = range.commonAncestorContainer;

    if (parent) {
      while (parent.nodeType != 1) parent = parent.parentNode;
      if (parent.nodeName.toLowerCase() != "body") {
        el = parent;
        do {
          el = el.parentNode;
          candidates[candidates.length] = el;
        } while (el.nodeName.toLowerCase() != "body");
      }
      children = parent.all || parent.getElementsByTagName("*");
      for (var j = 0; j < children.length; j++)
        candidates[candidates.length] = children[j];
      nodes = [parent];
      for (var ii = 0, r2; ii < candidates.length; ii++) {
        r2 = createRangeFromElement(this.document, candidates[ii]);
        if (r2 && compareRanges(range, r2))
          nodes[nodes.length] = candidates[ii];
      }
    }

    return nodes.first();
  }

  function createRangeFromElement(document, node) {
    if (document.body.createTextRange) {
      var range = document.body.createTextRange();
      range.moveToElementText(node);
    } else if (document.createRange) {
      var range = document.createRange();
      range.selectNodeContents(node);
    }
    return range;
  }

  function compareRanges(r1, r2) {
    if (r1.compareEndPoints) {
      return !(
        r2.compareEndPoints('StartToStart', r1) == 1 &&
        r2.compareEndPoints('EndToEnd', r1) == 1 &&
        r2.compareEndPoints('StartToEnd', r1) == 1 &&
        r2.compareEndPoints('EndToStart', r1) == 1
        ||
        r2.compareEndPoints('StartToStart', r1) == -1 &&
        r2.compareEndPoints('EndToEnd', r1) == -1 &&
        r2.compareEndPoints('StartToEnd', r1) == -1 &&
        r2.compareEndPoints('EndToStart', r1) == -1
      );
    } else if (r1.compareBoundaryPoints) {
      return !(
        r2.compareBoundaryPoints(0, r1) == 1 &&
        r2.compareBoundaryPoints(2, r1) == 1 &&
        r2.compareBoundaryPoints(1, r1) == 1 &&
        r2.compareBoundaryPoints(3, r1) == 1
        ||
        r2.compareBoundaryPoints(0, r1) == -1 &&
        r2.compareBoundaryPoints(2, r1) == -1 &&
        r2.compareBoundaryPoints(1, r1) == -1 &&
        r2.compareBoundaryPoints(3, r1) == -1
      );
    }

    return null;
  };

  function setBookmark() {
    var bookmark = this.document.getElementById('bookmark');
    if (bookmark)
      bookmark.parentNode.removeChild(bookmark);

    bookmark = this.document.createElement('span');
    bookmark.id = 'bookmark';
    bookmark.innerHTML = '&nbsp;';

    var range;
    if (Prototype.Browser.IE)
      range = new Range(this.document);
    else
      range = this.getRange();
    range.insertNode(bookmark);
  }

  function moveToBookmark() {
    var bookmark = this.document.getElementById('bookmark');
    if (!bookmark)
      return;

    if (Prototype.Browser.IE) {
      var range = this.getRange();
      range.moveToElementText(bookmark);
      range.collapse();
      range.select();
    } else if (Prototype.Browser.WebKit) {
      var selection = this.getSelection();
      selection.setBaseAndExtent(bookmark, 0, bookmark, 0);
    } else {
      var range = this.getRange();
      range.setStartBefore(bookmark);
    }

    bookmark.parentNode.removeChild(bookmark);
  }

  return {
    initialize:     initialize,
    getSelection:   getSelection,
    getRange:       getRange,
    getNode:        getNode,
    selectNode:     selectNode,
    setBookmark:    setBookmark,
    moveToBookmark: moveToBookmark
  };
})());

/**
 * class WysiHat.Toolbar
 **/
WysiHat.Toolbar = Class.create((function() {
  /**
   * new WysiHat.Toolbar(editor)
   *  - editor (WysiHat.Editor): the editor object that you want to attach to
   *
   *  Creates a toolbar element above the editor. The WysiHat.Toolbar object
   *  has many helper methods to easily add buttons to the toolbar.
   *
   *  This toolbar class is not required for the Editor object to function.
   *  It is merely a set of helper methods to get you started and to build
   *  on top of.
   **/
  function initialize(editArea) {
    this.editArea = editArea;

    this.hasMouseDown = false;
    this.element = new Element('div', { 'class': 'editor_toolbar' });

    var toolbar = this;
    this.element.observe('mousedown', function(event) { toolbar.mouseDown(event); });
    this.element.observe('mouseup', function(event) { toolbar.mouseUp(event); });

    this.editArea.insert({before: this.element});
  }

  /**
   * WysiHat.Toolbar#addButtonSet(set) -> undefined
   *  - set (Array): The set array contains nested arrays that hold the
   *  button options, and handler.
   *
   *  Adds a button set to the toolbar.
   *
   *  WysiHat.Toolbar.ButtonSets.Basic is a built in button set,
   *  that looks like:
   *  [
   *    [{ name: 'bold', label: "Bold" }, function(editor) {
   *      editor.boldSelection();
   *    }],
   *    [{ name: 'underline', label: "Underline" }, function(editor) {
   *      editor.underlineSelection();
   *    }],
   *    [{ name: 'italic', label: "Italic" }, function(editor) {
   *      editor.italicSelection();
   *    }]
   *  ]
   **/
  function addButtonSet(set) {
    var toolbar = this;
    $A(set).each(function(button) {
      var options = button.first();
      var handler = button.last();
      toolbar.addButton(options, handler);
    });
  }

  /**
   * WysiHat.Toolbar#addButton(options, handler) -> undefined
   *  - options (Hash): Required options hash
   *  - handler (Function): Function to bind to the button
   *
   *  The options hash accepts two required keys, name and label. The label
   *  value is used as the link's inner text. The name value is set to the
   *  link's class and is used to check the button state.
   *
   *  toolbar.addButton({
   *    name: 'bold', label: "Bold" }, function(editor) {
   *      editor.boldSelection();
   *  });
   *
   *  Would create a link,
   *  "<a href='#' class='button bold'><span>Bold</span></a>"
   **/
  function addButton(options, handler) {
    options = $H(options);
    var button = Element('a', { 'class': 'button', 'href': '#' }).update('<span>' + options.get('label') + '</span>');
    button.addClassName(options.get('name'));

    this.observeButtonClick(button, handler);
    this.observeStateChanges(button, options.get('name'));
    this.element.appendChild(button);
  }

  /**
   * WysiHat.Toolbar#observeButtonClick(element, handler) -> undefined
   *  - element (String | Element): Element to bind handler to
   *  - handler (Function): Function to bind to the element
   *  fires wysihat:change
   *
   *  In addition to binding the given handler to the element, this observe
   *  function also sets up a few more events. When the elements onclick is
   *  fired, the toolbars hasMouseDown property will be set to true and
   *  back to false on exit.
   **/
  function observeButtonClick(element, handler) {
    var toolbar = this;
    $(element).observe('click', function(event) {
      toolbar.hasMouseDown = true;
      handler(toolbar.editArea);
      toolbar.editArea.fire("wysihat:change");
      Event.stop(event);
      toolbar.hasMouseDown = false;
    });
  }

  /**
   * WysiHat.Toolbar#observeStateChanges(element, command) -> undefined
   *  - element (String | Element): Element to receive changes
   *  - command (String): Name of editor command to observe
   *
   *  Adds the class "selected" to the given Element when the selected text
   *  matches the command.
   *
   *  toolbar.observeStateChanges(buttonElement, 'bold')
   *  would add the class "selected" to the buttonElement when the editor's
   *  selected text was bold.
   **/
  function observeStateChanges(element, command) {
    this.editArea.observe("wysihat:mousemove", function(event) {
      if (event.target.queryCommandState(command))
        element.addClassName('selected');
      else
        element.removeClassName('selected');
    });
  }

  /**
   * WysiHat.Toolbar#mouseDown(event) -> undefined
   *  - event (Event)
   *  This function is triggered when the user clicks their mouse down on
   *  the toolbar element. For now, it only updates the hasMouseDown property
   *  to true.
   **/
  function mouseDown(event) {
    this.hasMouseDown = true;
  }

  /**
   * WysiHat.Toolbar#mouseDown(event) -> undefined
   *  - event (Event)
   *  This function is triggered when the user releases their mouse from
   *  the toolbar element. It resets the hasMouseDown property back to false
   *  and refocuses on the editing window.
   **/
  function mouseUp(event) {
    this.editArea.focus();
    this.hasMouseDown = false;
  }

  return {
    initialize:          initialize,
    addButtonSet:        addButtonSet,
    addButton:           addButton,
    observeButtonClick:  observeButtonClick,
    observeStateChanges: observeStateChanges,
    mouseDown:           mouseDown,
    mouseUp:             mouseUp
  };
})());

WysiHat.Toolbar.ButtonSets = {};

/**
 * WysiHat.Toolbar.ButtonSets.Basic = $A([
 *    [{ name: 'bold', label: "Bold" }, function(editor) {
 *      editor.boldSelection();
 *    }],
 *
 *    [{ name: 'underline', label: "Underline" }, function(editor) {
 *      editor.underlineSelection();
 *    }],
 *
 *    [{ name: 'italic', label: "Italic" }, function(editor) {
 *      editor.italicSelection();
 *    }]
 *  ])
 *
 *  A basic set of buttons: bold, underline, and italic. This set is
 *  compatible with WysiHat.Toolbar, and can be added to the toolbar with:
 *  toolbar.addButtonSet(WysiHat.Toolbar.ButtonSets.Basic);
 **/
WysiHat.Toolbar.ButtonSets.Basic = $A([
  [{ name: 'bold', label: "Bold" }, function(editor) {
    editor.boldSelection();
  }],

  [{ name: 'underline', label: "Underline" }, function(editor) {
    editor.underlineSelection();
  }],

  [{ name: 'italic', label: "Italic" }, function(editor) {
    editor.italicSelection();
  }]
]);

/*	SWFObject v2.2 <http://code.google.com/p/swfobject/> 
	is released under the MIT License <http://www.opensource.org/licenses/mit-license.php> 
*/
var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?/win/.test(Y):/win/.test(ah),ac=Y?/mac/.test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!/loaded|complete/.test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!/%$/.test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!/%$/.test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();/**
 * SWFUpload: http://www.swfupload.org, http://swfupload.googlecode.com
 *
 * mmSWFUpload 1.0: Flash upload dialog - http://profandesign.se/swfupload/,  http://www.vinterwebb.se/
 *
 * SWFUpload is (c) 2006-2007 Lars Huring, Olov Nilz�n and Mammon Media and is released under the MIT License:
 * http://www.opensource.org/licenses/mit-license.php
 *
 * SWFUpload 2 is (c) 2007-2008 Jake Roberts and is released under the MIT License:
 * http://www.opensource.org/licenses/mit-license.php
 *
 */


/* ******************* */
/* Constructor & Init  */
/* ******************* */

var SWFUpload = function (settings) {
	this.initSWFUpload(settings);
};

SWFUpload.prototype.initSWFUpload = function (settings) {
	try {
		this.customSettings = {};	// A container where developers can place their own settings associated with this instance.
		this.settings = settings;
		this.eventQueue = [];
		this.movieName = "SWFUpload_" + SWFUpload.movieCount++;
		this.movieElement = null;

		// Setup global control tracking
		SWFUpload.instances[this.movieName] = this;

		// Load the settings.  Load the Flash movie.
		this.initSettings();
		this.loadFlash();
		this.displayDebugInfo();
	} catch (ex) {
		delete SWFUpload.instances[this.movieName];
		throw ex;
	}
};

/* *************** */
/* Static Members  */
/* *************** */
SWFUpload.instances = {};
SWFUpload.movieCount = 0;
SWFUpload.version = "2.2.0 Alpha";
SWFUpload.QUEUE_ERROR = {
	QUEUE_LIMIT_EXCEEDED	  		: -100,
	FILE_EXCEEDS_SIZE_LIMIT  		: -110,
	ZERO_BYTE_FILE			  		: -120,
	INVALID_FILETYPE		  		: -130
};
SWFUpload.UPLOAD_ERROR = {
	HTTP_ERROR				  		: -200,
	MISSING_UPLOAD_URL	      		: -210,
	IO_ERROR				  		: -220,
	SECURITY_ERROR			  		: -230,
	UPLOAD_LIMIT_EXCEEDED	  		: -240,
	UPLOAD_FAILED			  		: -250,
	SPECIFIED_FILE_ID_NOT_FOUND		: -260,
	FILE_VALIDATION_FAILED	  		: -270,
	FILE_CANCELLED			  		: -280,
	UPLOAD_STOPPED					: -290
};
SWFUpload.FILE_STATUS = {
	QUEUED		 : -1,
	IN_PROGRESS	 : -2,
	ERROR		 : -3,
	COMPLETE	 : -4,
	CANCELLED	 : -5
};
SWFUpload.BUTTON_ACTION = {
	SELECT_FILE  : -100,
	SELECT_FILES : -110,
	START_UPLOAD : -120
};

/* ******************** */
/* Instance Members  */
/* ******************** */

// Private: initSettings ensures that all the
// settings are set, getting a default value if one was not assigned.
SWFUpload.prototype.initSettings = function () {
	this.ensureDefault = function (settingName, defaultValue) {
		this.settings[settingName] = (this.settings[settingName] == undefined) ? defaultValue : this.settings[settingName];
	};
	
	// Upload backend settings
	this.ensureDefault("upload_url", "");
	this.ensureDefault("file_post_name", "Filedata");
	this.ensureDefault("post_params", {});
	this.ensureDefault("use_query_string", false);
	this.ensureDefault("requeue_on_error", false);
	
	// File Settings
	this.ensureDefault("file_types", "*.*");
	this.ensureDefault("file_types_description", "All Files");
	this.ensureDefault("file_size_limit", 0);	// Default zero means "unlimited"
	this.ensureDefault("file_upload_limit", 0);
	this.ensureDefault("file_queue_limit", 0);

	// Flash Settings
	this.ensureDefault("flash_url", "swfupload.swf");
	this.ensureDefault("prevent_swf_caching", true);
	
	// Button Settings
	this.ensureDefault("button_image_url", "");
	this.ensureDefault("button_width", 1);
	this.ensureDefault("button_height", 1);
	this.ensureDefault("button_text", "");
	this.ensureDefault("button_text_style", "color: #000000; font-size: 16pt;");
	this.ensureDefault("button_text_top_padding", 0);
	this.ensureDefault("button_text_left_padding", 0);
	this.ensureDefault("button_action", SWFUpload.BUTTON_ACTION.SELECT_FILES);
	this.ensureDefault("button_disabled", false);
	this.ensureDefault("button_placeholder_id", null);
	
	// Debug Settings
	this.ensureDefault("debug", false);
	this.settings.debug_enabled = this.settings.debug;	// Here to maintain v2 API
	
	// Event Handlers
	this.settings.return_upload_start_handler = this.returnUploadStart;
	this.ensureDefault("swfupload_loaded_handler", null);
	this.ensureDefault("file_dialog_start_handler", null);
	this.ensureDefault("file_queued_handler", null);
	this.ensureDefault("file_queue_error_handler", null);
	this.ensureDefault("file_dialog_complete_handler", null);
	
	this.ensureDefault("upload_start_handler", null);
	this.ensureDefault("upload_progress_handler", null);
	this.ensureDefault("upload_error_handler", null);
	this.ensureDefault("upload_success_handler", null);
	this.ensureDefault("upload_complete_handler", null);
	
	this.ensureDefault("debug_handler", function(msg) {n2i.log(msg)});

	this.ensureDefault("custom_settings", {});

	// Other settings
	this.customSettings = this.settings.custom_settings;
	
	// Update the flash url if needed
	if (this.settings.prevent_swf_caching) {
		this.settings.flash_url = this.settings.flash_url + "?swfuploadrnd=" + Math.floor(Math.random() * 999999999);
	}
	
	delete this.ensureDefault;
};

SWFUpload.prototype.loadFlash = function () {
	if (this.settings.button_placeholder_id !== "") {
		this.replaceWithFlash();
	} else {
		this.appendFlash();
	}
};

// Private: appendFlash gets the HTML tag for the Flash
// It then appends the flash to the body
SWFUpload.prototype.appendFlash = function () {
	var targetElement, container;

	// Make sure an element with the ID we are going to use doesn't already exist
	if (document.getElementById(this.movieName) !== null) {
		throw "ID " + this.movieName + " is already in use. The Flash Object could not be added";
	}

	// Get the body tag where we will be adding the flash movie
	targetElement = document.getElementsByTagName("body")[0];

	if (targetElement == undefined) {
		throw "Could not find the 'body' element.";
	}

	// Append the container and load the flash
	container = document.createElement("div");
	container.style.width = "1px";
	container.style.height = "1px";
	container.style.overflow = "hidden";

	targetElement.appendChild(container);
	container.innerHTML = this.getFlashHTML();	// Using innerHTML is non-standard but the only sensible way to dynamically add Flash in IE (and maybe other browsers)
};

// Private: replaceWithFlash replaces the button_placeholder element with the flash movie.
SWFUpload.prototype.replaceWithFlash = function () {
	var targetElement, tempParent;

	// Make sure an element with the ID we are going to use doesn't already exist
	if (document.getElementById(this.movieName) !== null) {
		throw "ID " + this.movieName + " is already in use. The Flash Object could not be added";
	}
	// Get the element where we will be placing the flash movie
	targetElement = this.settings.button_placeholder || document.getElementById(this.settings.button_placeholder_id);

	if (targetElement == undefined) {
		throw "Could not find the placeholder element.";
	}

	// Append the container and load the flash
	tempParent = document.createElement("div");
	tempParent.innerHTML = this.getFlashHTML();	// Using innerHTML is non-standard but the only sensible way to dynamically add Flash in IE (and maybe other browsers)
	targetElement.parentNode.replaceChild(tempParent.firstChild, targetElement);
};

// Private: getFlashHTML generates the object tag needed to embed the flash in to the document
SWFUpload.prototype.getFlashHTML = function () {
	var transparent = this.settings.button_image_url === "" ? true : false;
	
	// Flash Satay object syntax: http://www.alistapart.com/articles/flashsatay
	return ['<object id="', this.movieName, '" type="application/x-shockwave-flash" data="', this.settings.flash_url, '" width="', this.settings.button_width, '" height="', this.settings.button_height, '" class="swfupload">',
				'<param name="wmode" value="', transparent ? "transparent" : "window", '" />',
				'<param name="movie" value="', this.settings.flash_url, '" />',
				'<param name="quality" value="high" />',
				'<param name="menu" value="false" />',
				'<param name="allowScriptAccess" value="always" />',
				'<param name="flashvars" value="' + this.getFlashVars() + '" />',
				'</object>'].join("");
};

// Private: getFlashVars builds the parameter string that will be passed
// to flash in the flashvars param.
SWFUpload.prototype.getFlashVars = function () {
	// Build a string from the post param object
	var paramString = this.buildParamString();

	// Build the parameter string
	return ["movieName=", encodeURIComponent(this.movieName),
			"&amp;uploadURL=", encodeURIComponent(this.settings.upload_url),
			"&amp;useQueryString=", encodeURIComponent(this.settings.use_query_string),
			"&amp;requeueOnError=", encodeURIComponent(this.settings.requeue_on_error),
			"&amp;params=", encodeURIComponent(paramString),
			"&amp;filePostName=", encodeURIComponent(this.settings.file_post_name),
			"&amp;fileTypes=", encodeURIComponent(this.settings.file_types),
			"&amp;fileTypesDescription=", encodeURIComponent(this.settings.file_types_description),
			"&amp;fileSizeLimit=", encodeURIComponent(this.settings.file_size_limit),
			"&amp;fileUploadLimit=", encodeURIComponent(this.settings.file_upload_limit),
			"&amp;fileQueueLimit=", encodeURIComponent(this.settings.file_queue_limit),
			"&amp;debugEnabled=", encodeURIComponent(this.settings.debug_enabled),
			"&amp;buttonImageURL=", encodeURIComponent(this.settings.button_image_url),
			"&amp;buttonWidth=", encodeURIComponent(this.settings.button_width),
			"&amp;buttonHeight=", encodeURIComponent(this.settings.button_height),
			"&amp;buttonText=", encodeURIComponent(this.settings.button_text),
			"&amp;buttonTextTopPadding=", encodeURIComponent(this.settings.button_text_top_padding),
			"&amp;buttonTextLeftPadding=", encodeURIComponent(this.settings.button_text_left_padding),
			"&amp;buttonTextStyle=", encodeURIComponent(this.settings.button_text_style),
			"&amp;buttonAction=", encodeURIComponent(this.settings.button_action),
			"&amp;buttonDisabled=", encodeURIComponent(this.settings.button_disabled)
		].join("");
};

// Public: getMovieElement retrieves the DOM reference to the Flash element added by SWFUpload
// The element is cached after the first lookup
SWFUpload.prototype.getMovieElement = function () {
	if (this.movieElement == undefined) {
		this.movieElement = document.getElementById(this.movieName);
	}

	if (this.movieElement === null) {
		throw "Could not find Flash element";
	}
	
	return this.movieElement;
};

// Private: buildParamString takes the name/value pairs in the post_params setting object
// and joins them up in to a string formatted "name=value&amp;name=value"
SWFUpload.prototype.buildParamString = function () {
	var postParams = this.settings.post_params; 
	var paramStringPairs = [];

	if (typeof(postParams) === "object") {
		for (var name in postParams) {
			if (postParams.hasOwnProperty(name)) {
				paramStringPairs.push(encodeURIComponent(name.toString()) + "=" + encodeURIComponent(postParams[name].toString()));
			}
		}
	}

	return paramStringPairs.join("&amp;");
};

// Public: Used to remove a SWFUpload instance from the page. This method strives to remove
// all references to the SWF, and other objects so memory is properly freed.
// Returns true if everything was destroyed. Returns a false if a failure occurs leaving SWFUpload in an inconsistant state.
SWFUpload.prototype.destroy = function () {
	try {
		// Make sure Flash is done before we try to remove it
		this.stopUpload();
		
		// Remove the SWFUpload DOM nodes
		var movieElement = null;
		try {
			movieElement = this.getMovieElement();
		} catch (ex) {
		}
		
		if (movieElement != undefined && movieElement.parentNode != undefined && typeof movieElement.parentNode.removeChild === "function") {
			var container = movieElement.parentNode;
			if (container != undefined) {
				container.removeChild(movieElement);
				if (container.parentNode != undefined && typeof container.parentNode.removeChild === "function") {
					container.parentNode.removeChild(container);
				}
			}
		}
		
		// Destroy references
		SWFUpload.instances[this.movieName] = null;
		delete SWFUpload.instances[this.movieName];

		delete this.movieElement;
		delete this.settings;
		delete this.customSettings;
		delete this.eventQueue;
		delete this.movieName;
		
		delete window[this.movieName];
		
		return true;
	} catch (ex1) {
		return false;
	}
};

// Public: displayDebugInfo prints out settings and configuration
// information about this SWFUpload instance.
// This function (and any references to it) can be deleted when placing
// SWFUpload in production.
SWFUpload.prototype.displayDebugInfo = function () {
	this.debug(
		[
			"---SWFUpload Instance Info---\n",
			"Version: ", SWFUpload.version, "\n",
			"Movie Name: ", this.movieName, "\n",
			"Settings:\n",
			"\t", "upload_url:               ", this.settings.upload_url, "\n",
			"\t", "flash_url:                ", this.settings.flash_url, "\n",
			"\t", "use_query_string:         ", this.settings.use_query_string.toString(), "\n",
			"\t", "file_post_name:           ", this.settings.file_post_name, "\n",
			"\t", "post_params:              ", this.settings.post_params.toString(), "\n",
			"\t", "file_types:               ", this.settings.file_types, "\n",
			"\t", "file_types_description:   ", this.settings.file_types_description, "\n",
			"\t", "file_size_limit:          ", this.settings.file_size_limit, "\n",
			"\t", "file_upload_limit:        ", this.settings.file_upload_limit, "\n",
			"\t", "file_queue_limit:         ", this.settings.file_queue_limit, "\n",
			"\t", "debug:                    ", this.settings.debug.toString(), "\n",

			"\t", "prevent_swf_caching:      ", this.settings.prevent_swf_caching.toString(), "\n",

			"\t", "button_placeholder_id:    ", this.settings.button_placeholder_id.toString(), "\n",
			"\t", "button_image_url:         ", this.settings.button_image_url.toString(), "\n",
			"\t", "button_width:             ", this.settings.button_width.toString(), "\n",
			"\t", "button_height:            ", this.settings.button_height.toString(), "\n",
			"\t", "button_text:              ", this.settings.button_text.toString(), "\n",
			"\t", "button_text_style:        ", this.settings.button_text_style.toString(), "\n",
			"\t", "button_text_top_padding:  ", this.settings.button_text_top_padding.toString(), "\n",
			"\t", "button_text_left_padding: ", this.settings.button_text_left_padding.toString(), "\n",
			"\t", "button_action:            ", this.settings.button_action.toString(), "\n",
			"\t", "button_disabled:          ", this.settings.button_disabled.toString(), "\n",

			"\t", "custom_settings:          ", this.settings.custom_settings.toString(), "\n",
			"Event Handlers:\n",
			"\t", "swfupload_loaded_handler assigned:  ", (typeof this.settings.swfupload_loaded_handler === "function").toString(), "\n",
			"\t", "file_dialog_start_handler assigned: ", (typeof this.settings.file_dialog_start_handler === "function").toString(), "\n",
			"\t", "file_queued_handler assigned:       ", (typeof this.settings.file_queued_handler === "function").toString(), "\n",
			"\t", "file_queue_error_handler assigned:  ", (typeof this.settings.file_queue_error_handler === "function").toString(), "\n",
			"\t", "upload_start_handler assigned:      ", (typeof this.settings.upload_start_handler === "function").toString(), "\n",
			"\t", "upload_progress_handler assigned:   ", (typeof this.settings.upload_progress_handler === "function").toString(), "\n",
			"\t", "upload_error_handler assigned:      ", (typeof this.settings.upload_error_handler === "function").toString(), "\n",
			"\t", "upload_success_handler assigned:    ", (typeof this.settings.upload_success_handler === "function").toString(), "\n",
			"\t", "upload_complete_handler assigned:   ", (typeof this.settings.upload_complete_handler === "function").toString(), "\n",
			"\t", "debug_handler assigned:             ", (typeof this.settings.debug_handler === "function").toString(), "\n"
		].join("")
	);
};

/* Note: addSetting and getSetting are no longer used by SWFUpload but are included
	the maintain v2 API compatibility
*/
// Public: (Deprecated) addSetting adds a setting value. If the value given is undefined or null then the default_value is used.
SWFUpload.prototype.addSetting = function (name, value, default_value) {
    if (value == undefined) {
        return (this.settings[name] = default_value);
    } else {
        return (this.settings[name] = value);
	}
};

// Public: (Deprecated) getSetting gets a setting. Returns an empty string if the setting was not found.
SWFUpload.prototype.getSetting = function (name) {
    if (this.settings[name] != undefined) {
        return this.settings[name];
	}

    return "";
};



// Private: callFlash handles function calls made to the Flash element.
// Calls are made with a setTimeout for some functions to work around
// bugs in the ExternalInterface library.
SWFUpload.prototype.callFlash = function (functionName, argumentArray) {
	argumentArray = argumentArray || [];
	
	var movieElement = this.getMovieElement();
	var returnValue;

	if (typeof movieElement[functionName] === "function") {
		// We have to go through all this if/else stuff because the Flash functions don't have apply() and only accept the exact number of arguments.
		if (argumentArray.length === 0) {
			returnValue = movieElement[functionName]();
		} else if (argumentArray.length === 1) {
			returnValue = movieElement[functionName](argumentArray[0]);
		} else if (argumentArray.length === 2) {
			returnValue = movieElement[functionName](argumentArray[0], argumentArray[1]);
		} else if (argumentArray.length === 3) {
			returnValue = movieElement[functionName](argumentArray[0], argumentArray[1], argumentArray[2]);
		} else {
			throw "Too many arguments";
		}
		
		// Unescape file post param values
		if (returnValue != undefined && typeof returnValue.post === "object") {
			returnValue = this.unescapeFilePostParams(returnValue);
		}
		
		return returnValue;
	} else {
		throw "Invalid function name: " + functionName;
	}
};


/* *****************************
	-- Flash control methods --
	Your UI should use these
	to operate SWFUpload
   ***************************** */

// Public: selectFile causes a File Selection Dialog window to appear.  This
// dialog only allows 1 file to be selected. WARNING: this function does not work in Flash Player 10
SWFUpload.prototype.selectFile = function () {
	this.callFlash("SelectFile");
};

// Public: selectFiles causes a File Selection Dialog window to appear/ This
// dialog allows the user to select any number of files
// Flash Bug Warning: Flash limits the number of selectable files based on the combined length of the file names.
// If the selection name length is too long the dialog will fail in an unpredictable manner.  There is no work-around
// for this bug.  WARNING: this function does not work in Flash Player 10
SWFUpload.prototype.selectFiles = function () {
	this.callFlash("SelectFiles");
};


// Public: startUpload starts uploading the first file in the queue unless
// the optional parameter 'fileID' specifies the ID 
SWFUpload.prototype.startUpload = function (fileID) {
	this.callFlash("StartUpload", [fileID]);
};

/* Cancels a the file upload.  You must specify a file_id */
// Public: cancelUpload cancels any queued file.  The fileID parameter
// must be specified.
SWFUpload.prototype.cancelUpload = function (fileID) {
	this.callFlash("CancelUpload", [fileID]);
};

// Public: stopUpload stops the current upload and requeues the file at the beginning of the queue.
// If nothing is currently uploading then nothing happens.
SWFUpload.prototype.stopUpload = function () {
	this.callFlash("StopUpload");
};

/* ************************
 * Settings methods
 *   These methods change the SWFUpload settings.
 *   SWFUpload settings should not be changed directly on the settings object
 *   since many of the settings need to be passed to Flash in order to take
 *   effect.
 * *********************** */

// Public: getStats gets the file statistics object.
SWFUpload.prototype.getStats = function () {
	return this.callFlash("GetStats");
};

// Public: setStats changes the SWFUpload statistics.  You shouldn't need to 
// change the statistics but you can.  Changing the statistics does not
// affect SWFUpload accept for the successful_uploads count which is used
// by the upload_limit setting to determine how many files the user may upload.
SWFUpload.prototype.setStats = function (statsObject) {
	this.callFlash("SetStats", [statsObject]);
};

// Public: getFile retrieves a File object by ID or Index.  If the file is
// not found then 'null' is returned.
SWFUpload.prototype.getFile = function (fileID) {
	if (typeof(fileID) === "number") {
		return this.callFlash("GetFileByIndex", [fileID]);
	} else {
		return this.callFlash("GetFile", [fileID]);
	}
};

// Public: addFileParam sets a name/value pair that will be posted with the
// file specified by the Files ID.  If the name already exists then the
// exiting value will be overwritten.
SWFUpload.prototype.addFileParam = function (fileID, name, value) {
	return this.callFlash("AddFileParam", [fileID, name, value]);
};

// Public: removeFileParam removes a previously set (by addFileParam) name/value
// pair from the specified file.
SWFUpload.prototype.removeFileParam = function (fileID, name) {
	this.callFlash("RemoveFileParam", [fileID, name]);
};

// Public: setUploadUrl changes the upload_url setting.
SWFUpload.prototype.setUploadURL = function (url) {
	this.settings.upload_url = url.toString();
	this.callFlash("SetUploadURL", [url]);
};

// Public: setPostParams changes the post_params setting
SWFUpload.prototype.setPostParams = function (paramsObject) {
	this.settings.post_params = paramsObject;
	this.callFlash("SetPostParams", [paramsObject]);
};

// Public: addPostParam adds post name/value pair.  Each name can have only one value.
SWFUpload.prototype.addPostParam = function (name, value) {
	this.settings.post_params[name] = value;
	this.callFlash("SetPostParams", [this.settings.post_params]);
};

// Public: removePostParam deletes post name/value pair.
SWFUpload.prototype.removePostParam = function (name) {
	delete this.settings.post_params[name];
	this.callFlash("SetPostParams", [this.settings.post_params]);
};

// Public: setFileTypes changes the file_types setting and the file_types_description setting
SWFUpload.prototype.setFileTypes = function (types, description) {
	this.settings.file_types = types;
	this.settings.file_types_description = description;
	this.callFlash("SetFileTypes", [types, description]);
};

// Public: setFileSizeLimit changes the file_size_limit setting
SWFUpload.prototype.setFileSizeLimit = function (fileSizeLimit) {
	this.settings.file_size_limit = fileSizeLimit;
	this.callFlash("SetFileSizeLimit", [fileSizeLimit]);
};

// Public: setFileUploadLimit changes the file_upload_limit setting
SWFUpload.prototype.setFileUploadLimit = function (fileUploadLimit) {
	this.settings.file_upload_limit = fileUploadLimit;
	this.callFlash("SetFileUploadLimit", [fileUploadLimit]);
};

// Public: setFileQueueLimit changes the file_queue_limit setting
SWFUpload.prototype.setFileQueueLimit = function (fileQueueLimit) {
	this.settings.file_queue_limit = fileQueueLimit;
	this.callFlash("SetFileQueueLimit", [fileQueueLimit]);
};

// Public: setFilePostName changes the file_post_name setting
SWFUpload.prototype.setFilePostName = function (filePostName) {
	this.settings.file_post_name = filePostName;
	this.callFlash("SetFilePostName", [filePostName]);
};

// Public: setUseQueryString changes the use_query_string setting
SWFUpload.prototype.setUseQueryString = function (useQueryString) {
	this.settings.use_query_string = useQueryString;
	this.callFlash("SetUseQueryString", [useQueryString]);
};

// Public: setRequeueOnError changes the requeue_on_error setting
SWFUpload.prototype.setRequeueOnError = function (requeueOnError) {
	this.settings.requeue_on_error = requeueOnError;
	this.callFlash("SetRequeueOnError", [requeueOnError]);
};

// Public: setDebugEnabled changes the debug_enabled setting
SWFUpload.prototype.setDebugEnabled = function (debugEnabled) {
	this.settings.debug_enabled = debugEnabled;
	this.callFlash("SetDebugEnabled", [debugEnabled]);
};

// Public: setButtonImageURL loads a button image sprite
SWFUpload.prototype.setButtonImageURL = function (buttonImageURL) {
	if (buttonImageURL == undefined) {
		buttonImageURL = "";
	}
	
	this.settings.button_image_url = buttonImageURL;
	this.callFlash("SetButtonImageURL", [buttonImageURL]);
};

// Public: setButtonDimensions resizes the Flash Movie and button
SWFUpload.prototype.setButtonDimensions = function (width, height) {
	this.settings.button_width = width;
	this.settings.button_height = height;
	
	var movie = this.getMovieElement();
	if (movie != undefined) {
		movie.style.width = width + "px";
		movie.style.height = height + "px";
	}
	
	this.callFlash("SetButtonDimensions", [width, height]);
};
// Public: setButtonText Changes the text overlaid on the button
SWFUpload.prototype.setButtonText = function (html) {
	this.settings.button_text = html;
	this.callFlash("SetButtonText", [html]);
};
// Public: setButtonTextPadding changes the top and left padding of the text overlay
SWFUpload.prototype.setButtonTextPadding = function (left, top) {
	this.settings.button_text_top_padding = top;
	this.settings.button_text_left_padding = left;
	this.callFlash("SetButtonTextPadding", [left, top]);
};

// Public: setButtonTextStyle changes the CSS used to style the HTML/Text overlaid on the button
SWFUpload.prototype.setButtonTextStyle = function (css) {
	this.settings.button_text_style = css;
	this.callFlash("SetButtonTextStyle", [css]);
};
// Public: setButtonDisabled disables/enables the button
SWFUpload.prototype.setButtonDisabled = function (isDisabled) {
	this.settings.button_disabled = isDisabled;
	this.callFlash("SetButtonDisabled", [isDisabled]);
};
// Public: setButtonAction sets the action that occurs when the button is clicked
SWFUpload.prototype.setButtonAction = function (buttonAction) {
	this.settings.button_action = buttonAction;
	this.callFlash("SetButtonAction", [buttonAction]);
};

/* *******************************
	Flash Event Interfaces
	These functions are used by Flash to trigger the various
	events.
	
	All these functions a Private.
	
	Because the ExternalInterface library is buggy the event calls
	are added to a queue and the queue then executed by a setTimeout.
	This ensures that events are executed in a determinate order and that
	the ExternalInterface bugs are avoided.
******************************* */

SWFUpload.prototype.queueEvent = function (handlerName, argumentArray) {
	// Warning: Don't call this.debug inside here or you'll create an infinite loop
	
	if (argumentArray == undefined) {
		argumentArray = [];
	} else if (!(argumentArray instanceof Array)) {
		argumentArray = [argumentArray];
	}
	
	var self = this;
	if (typeof this.settings[handlerName] === "function") {
		// Queue the event
		this.eventQueue.push(function () {
			this.settings[handlerName].apply(this, argumentArray);
		});
		
		// Execute the next queued event
		setTimeout(function () {
			self.executeNextEvent();
		}, 0);
		
	} else if (this.settings[handlerName] !== null) {
		throw "Event handler " + handlerName + " is unknown or is not a function";
	}
};

// Private: Causes the next event in the queue to be executed.  Since events are queued using a setTimeout
// we must queue them in order to garentee that they are executed in order.
SWFUpload.prototype.executeNextEvent = function () {
	// Warning: Don't call this.debug inside here or you'll create an infinite loop

	var  f = this.eventQueue ? this.eventQueue.shift() : null;
	if (typeof(f) === "function") {
		f.apply(this);
	}
};

// Private: unescapeFileParams is part of a workaround for a flash bug where objects passed through ExternalInterface cannot have
// properties that contain characters that are not valid for JavaScript identifiers. To work around this
// the Flash Component escapes the parameter names and we must unescape again before passing them along.
SWFUpload.prototype.unescapeFilePostParams = function (file) {
	var reg = /[$]([0-9a-f]{4})/i;
	var unescapedPost = {};
	var uk;

	if (file != undefined) {
		for (var k in file.post) {
			if (file.post.hasOwnProperty(k)) {
				uk = k;
				var match;
				while ((match = reg.exec(uk)) !== null) {
					uk = uk.replace(match[0], String.fromCharCode(parseInt("0x" + match[1], 16)));
				}
				unescapedPost[uk] = file.post[k];
			}
		}

		file.post = unescapedPost;
	}

	return file;
};

SWFUpload.prototype.flashReady = function () {
	// Check that the movie element is loaded correctly with its ExternalInterface methods defined
	var movieElement = this.getMovieElement();
	if (typeof movieElement.StartUpload !== "function") {
		throw "ExternalInterface methods failed to initialize.";
	}

	// Fix IE Flash/Form bug
	if (window[this.movieName] == undefined) {
		window[this.movieName] = movieElement;
	}
	
	this.queueEvent("swfupload_loaded_handler");
};


/* This is a chance to do something before the browse window opens */
SWFUpload.prototype.fileDialogStart = function () {
	this.queueEvent("file_dialog_start_handler");
};


/* Called when a file is successfully added to the queue. */
SWFUpload.prototype.fileQueued = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("file_queued_handler", file);
};


/* Handle errors that occur when an attempt to queue a file fails. */
SWFUpload.prototype.fileQueueError = function (file, errorCode, message) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("file_queue_error_handler", [file, errorCode, message]);
};

/* Called after the file dialog has closed and the selected files have been queued.
	You could call startUpload here if you want the queued files to begin uploading immediately. */
SWFUpload.prototype.fileDialogComplete = function (numFilesSelected, numFilesQueued) {
	this.queueEvent("file_dialog_complete_handler", [numFilesSelected, numFilesQueued]);
};

SWFUpload.prototype.uploadStart = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("return_upload_start_handler", file);
};

SWFUpload.prototype.returnUploadStart = function (file) {
	var returnValue;
	if (typeof this.settings.upload_start_handler === "function") {
		file = this.unescapeFilePostParams(file);
		returnValue = this.settings.upload_start_handler.call(this, file);
	} else if (this.settings.upload_start_handler != undefined) {
		throw "upload_start_handler must be a function";
	}

	// Convert undefined to true so if nothing is returned from the upload_start_handler it is
	// interpretted as 'true'.
	if (returnValue === undefined) {
		returnValue = true;
	}
	
	returnValue = !!returnValue;
	
	this.callFlash("ReturnUploadStart", [returnValue]);
};



SWFUpload.prototype.uploadProgress = function (file, bytesComplete, bytesTotal) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_progress_handler", [file, bytesComplete, bytesTotal]);
};

SWFUpload.prototype.uploadError = function (file, errorCode, message) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_error_handler", [file, errorCode, message]);
};

SWFUpload.prototype.uploadSuccess = function (file, serverData) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_success_handler", [file, serverData]);
};

SWFUpload.prototype.uploadComplete = function (file) {
	file = this.unescapeFilePostParams(file);
	this.queueEvent("upload_complete_handler", file);
};

/* Called by SWFUpload JavaScript and Flash functions when debug is enabled. By default it writes messages to the
   internal debug console.  You can override this event and have messages written where you want. */
SWFUpload.prototype.debug = function (message) {
	n2i.log(message);
};
/*
 * Copyright (C) 2004 Baron Schwartz <baron at sequent dot org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 */

Date.parseFunctions = {count:0};
Date.parseRegexes = [];
Date.formatFunctions = {count:0};

Date.prototype.dateFormat = function(format) {
    if (Date.formatFunctions[format] == null) {
        Date.createNewFormat(format);
    }
    var func = Date.formatFunctions[format];
    return this[func]();
}

Date.createNewFormat = function(format) {
    var funcName = "format" + Date.formatFunctions.count++;
    Date.formatFunctions[format] = funcName;
    var code = "Date.prototype." + funcName + " = function(){return ";
    var special = false;
    var ch = '';
    for (var i = 0; i < format.length; ++i) {
        ch = format.charAt(i);
        if (!special && ch == "\\") {
            special = true;
        }
        else if (special) {
            special = false;
            code += "'" + String.escape(ch) + "' + ";
        }
        else {
            code += Date.getFormatCode(ch);
        }
    }
    eval(code.substring(0, code.length - 3) + ";}");
}

Date.getFormatCode = function(character) {
    switch (character) {
    case "d":
        return "String.leftPad(this.getDate(), 2, '0') + ";
    case "D":
        return "Date.dayNames[this.getDay()].substring(0, 3) + ";
    case "j":
        return "this.getDate() + ";
    case "l":
        return "Date.dayNames[this.getDay()] + ";
    case "S":
        return "this.getSuffix() + ";
    case "w":
        return "this.getDay() + ";
    case "z":
        return "this.getDayOfYear() + ";
    case "W":
        return "this.getWeekOfYear() + ";
    case "F":
        return "Date.monthNames[this.getMonth()] + ";
    case "m":
        return "String.leftPad(this.getMonth() + 1, 2, '0') + ";
    case "M":
        return "Date.monthNames[this.getMonth()].substring(0, 3) + ";
    case "n":
        return "(this.getMonth() + 1) + ";
    case "t":
        return "this.getDaysInMonth() + ";
    case "L":
        return "(this.isLeapYear() ? 1 : 0) + ";
    case "Y":
        return "this.getFullYear() + ";
    case "y":
        return "('' + this.getFullYear()).substring(2, 4) + ";
    case "a":
        return "(this.getHours() < 12 ? 'am' : 'pm') + ";
    case "A":
        return "(this.getHours() < 12 ? 'AM' : 'PM') + ";
    case "g":
        return "((this.getHours() %12) ? this.getHours() % 12 : 12) + ";
    case "G":
        return "this.getHours() + ";
    case "h":
        return "String.leftPad((this.getHours() %12) ? this.getHours() % 12 : 12, 2, '0') + ";
    case "H":
        return "String.leftPad(this.getHours(), 2, '0') + ";
    case "i":
        return "String.leftPad(this.getMinutes(), 2, '0') + ";
    case "s":
        return "String.leftPad(this.getSeconds(), 2, '0') + ";
    case "O":
        return "this.getGMTOffset() + ";
    case "T":
        return "this.getTimezone() + ";
    case "Z":
        return "(this.getTimezoneOffset() * -60) + ";
    default:
        return "'" + String.escape(character) + "' + ";
    }
}

Date.parseDate = function(input, format) {
    if (Date.parseFunctions[format] == null) {
        Date.createParser(format);
    }
    var func = Date.parseFunctions[format];
    return Date[func](input);
}

Date.createParser = function(format) {
    var funcName = "parse" + Date.parseFunctions.count++;
    var regexNum = Date.parseRegexes.length;
    var currentGroup = 1;
    Date.parseFunctions[format] = funcName;

    var code = "Date." + funcName + " = function(input){\n"
        + "var y = -1, m = -1, d = -1, h = -1, i = -1, s = -1;\n"
        + "var d = new Date();\n"
        + "y = d.getFullYear();\n"
        + "m = d.getMonth();\n"
        + "d = d.getDate();\n"
        + "var results = input.match(Date.parseRegexes[" + regexNum + "]);\n"
        + "if (results && results.length > 0) {"
    var regex = "";

    var special = false;
    var ch = '';
    for (var i = 0; i < format.length; ++i) {
        ch = format.charAt(i);
        if (!special && ch == "\\") {
            special = true;
        }
        else if (special) {
            special = false;
            regex += String.escape(ch);
        }
        else {
            obj = Date.formatCodeToRegex(ch, currentGroup);
            currentGroup += obj.g;
            regex += obj.s;
            if (obj.g && obj.c) {
                code += obj.c;
            }
        }
    }

    code += "if (y > 0 && m >= 0 && d > 0 && h >= 0 && i >= 0 && s >= 0)\n"
        + "{return new Date(y, m, d, h, i, s);}\n"
        + "else if (y > 0 && m >= 0 && d > 0 && h >= 0 && i >= 0)\n"
        + "{return new Date(y, m, d, h, i);}\n"
        + "else if (y > 0 && m >= 0 && d > 0 && h >= 0)\n"
        + "{return new Date(y, m, d, h);}\n"
        + "else if (y > 0 && m >= 0 && d > 0)\n"
        + "{return new Date(y, m, d);}\n"
        + "else if (y > 0 && m >= 0)\n"
        + "{return new Date(y, m);}\n"
        + "else if (y > 0)\n"
        + "{return new Date(y);}\n"
        + "}return null;}";

    Date.parseRegexes[regexNum] = new RegExp("^" + regex + "$");
    eval(code);
}

Date.formatCodeToRegex = function(character, currentGroup) {
    switch (character) {
    case "D":
        return {g:0,
        c:null,
        s:"(?:Sun|Mon|Tue|Wed|Thu|Fri|Sat)"};
    case "j":
    case "d":
        return {g:1,
            c:"d = parseInt(results[" + currentGroup + "], 10);\n",
            s:"(\\d{1,2})"};
    case "l":
        return {g:0,
            c:null,
            s:"(?:" + Date.dayNames.join("|") + ")"};
    case "S":
        return {g:0,
            c:null,
            s:"(?:st|nd|rd|th)"};
    case "w":
        return {g:0,
            c:null,
            s:"\\d"};
    case "z":
        return {g:0,
            c:null,
            s:"(?:\\d{1,3})"};
    case "W":
        return {g:0,
            c:null,
            s:"(?:\\d{2})"};
    case "F":
        return {g:1,
            c:"m = parseInt(Date.monthNumbers[results[" + currentGroup + "].substring(0, 3)], 10);\n",
            s:"(" + Date.monthNames.join("|") + ")"};
    case "M":
        return {g:1,
            c:"m = parseInt(Date.monthNumbers[results[" + currentGroup + "]], 10);\n",
            s:"(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"};
    case "n":
    case "m":
        return {g:1,
            c:"m = parseInt(results[" + currentGroup + "], 10) - 1;\n",
            s:"(\\d{1,2})"};
    case "t":
        return {g:0,
            c:null,
            s:"\\d{1,2}"};
    case "L":
        return {g:0,
            c:null,
            s:"(?:1|0)"};
    case "Y":
        return {g:1,
            c:"y = parseInt(results[" + currentGroup + "], 10);\n",
            s:"(\\d{4})"};
    case "y":
        return {g:1,
            c:"var ty = parseInt(results[" + currentGroup + "], 10);\n"
                + "y = ty > Date.y2kYear ? 1900 + ty : 2000 + ty;\n",
            s:"(\\d{1,2})"};
    case "a":
        return {g:1,
            c:"if (results[" + currentGroup + "] == 'am') {\n"
                + "if (h == 12) { h = 0; }\n"
                + "} else { if (h < 12) { h += 12; }}",
            s:"(am|pm)"};
    case "A":
        return {g:1,
            c:"if (results[" + currentGroup + "] == 'AM') {\n"
                + "if (h == 12) { h = 0; }\n"
                + "} else { if (h < 12) { h += 12; }}",
            s:"(AM|PM)"};
    case "g":
    case "G":
    case "h":
    case "H":
        return {g:1,
            c:"h = parseInt(results[" + currentGroup + "], 10);\n",
            s:"(\\d{1,2})"};
    case "i":
        return {g:1,
            c:"i = parseInt(results[" + currentGroup + "], 10);\n",
            s:"(\\d{2})"};
    case "s":
        return {g:1,
            c:"s = parseInt(results[" + currentGroup + "], 10);\n",
            s:"(\\d{2})"};
    case "O":
        return {g:0,
            c:null,
            s:"[+-]\\d{4}"};
    case "T":
        return {g:0,
            c:null,
            s:"[A-Z]{3}"};
    case "Z":
        return {g:0,
            c:null,
            s:"[+-]\\d{1,5}"};
    default:
        return {g:0,
            c:null,
            s:String.escape(character)};
    }
}

Date.prototype.getTimezone = function() {
    return this.toString().replace(
        /^.*? ([A-Z]{3}) [0-9]{4}.*$/, "$1").replace(
        /^.*?\(([A-Z])[a-z]+ ([A-Z])[a-z]+ ([A-Z])[a-z]+\)$/, "$1$2$3");
}

Date.prototype.getGMTOffset = function() {
    return (this.getTimezoneOffset() > 0 ? "-" : "+")
        + String.leftPad(Math.floor(this.getTimezoneOffset() / 60), 2, "0")
        + String.leftPad(this.getTimezoneOffset() % 60, 2, "0");
}

Date.prototype.getDayOfYear = function() {
    var num = 0;
    Date.daysInMonth[1] = this.isLeapYear() ? 29 : 28;
    for (var i = 0; i < this.getMonth(); ++i) {
        num += Date.daysInMonth[i];
    }
    return num + this.getDate() - 1;
}

Date.prototype.getWeekOfYear = function() {
    // Skip to Thursday of this week
    var now = this.getDayOfYear() + (5 - this.getDay());
    // Find the first Thursday of the year
    var jan1 = new Date(this.getFullYear(), 0, 1);
    var then = (5 - jan1.getDay());
    return String.leftPad(((now - then) / 7) + 1, 2, "0");
}

Date.prototype.isLeapYear = function() {
    var year = this.getFullYear();
    return ((year & 3) == 0 && (year % 100 || (year % 400 == 0 && year)));
}

Date.prototype.getFirstDayOfMonth = function() {
    var day = (this.getDay() - (this.getDate() - 1)) % 7;
    return (day < 0) ? (day + 7) : day;
}

Date.prototype.getLastDayOfMonth = function() {
    var day = (this.getDay() + (Date.daysInMonth[this.getMonth()] - this.getDate())) % 7;
    return (day < 0) ? (day + 7) : day;
}

Date.prototype.getDaysInMonth = function() {
    Date.daysInMonth[1] = this.isLeapYear() ? 29 : 28;
    return Date.daysInMonth[this.getMonth()];
}

Date.prototype.getSuffix = function() {
    switch (this.getDate()) {
        case 1:
        case 21:
        case 31:
            return "st";
        case 2:
        case 22:
            return "nd";
        case 3:
        case 23:
            return "rd";
        default:
            return "th";
    }
}

String.escape = function(string) {
    return string.replace(/('|\\)/g, "\\$1");
}

String.leftPad = function (val, size, ch) {
    var result = new String(val);
    if (ch == null) {
        ch = " ";
    }
    while (result.length < size) {
        result = ch + result;
    }
    return result;
}

Date.daysInMonth = [31,28,31,30,31,30,31,31,30,31,30,31];
Date.monthNames =
   ["January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"];
Date.dayNames =
   ["Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"];
Date.y2kYear = 50;
Date.monthNumbers = {
    Jan:0,
    Feb:1,
    Mar:2,
    Apr:3,
    May:4,
    Jun:5,
    Jul:6,
    Aug:7,
    Sep:8,
    Oct:9,
    Nov:10,
    Dec:11};
Date.patterns = {
    ISO8601LongPattern:"Y-m-d H:i:s",
    ISO8601ShortPattern:"Y-m-d",
    ShortDatePattern: "n/j/Y",
    LongDatePattern: "l, F d, Y",
    FullDateTimePattern: "l, F d, Y g:i:s A",
    MonthDayPattern: "F d",
    ShortTimePattern: "g:i A",
    LongTimePattern: "g:i:s A",
    SortableDateTimePattern: "Y-m-d\\TH:i:s",
    UniversalSortableDateTimePattern: "Y-m-d H:i:sO",
    YearMonthPattern: "F, Y"};
/*
    json2.js
    2008-01-17

    Public Domain

    No warranty expressed or implied. Use at your own risk.

    See http://www.JSON.org/js.html

    This file creates a global JSON object containing two methods:

        JSON.stringify(value, whitelist)
            value       any JavaScript value, usually an object or array.

            whitelist   an optional array prameter that determines how object
                        values are stringified.

            This method produces a JSON text from a JavaScript value.
            There are three possible ways to stringify an object, depending
            on the optional whitelist parameter.

            If an object has a toJSON method, then the toJSON() method will be
            called. The value returned from the toJSON method will be
            stringified.

            Otherwise, if the optional whitelist parameter is an array, then
            the elements of the array will be used to select members of the
            object for stringification.

            Otherwise, if there is no whitelist parameter, then all of the
            members of the object will be stringified.

            Values that do not have JSON representaions, such as undefined or
            functions, will not be serialized. Such values in objects will be
            dropped; in arrays will be replaced with null.
            JSON.stringify(undefined) returns undefined. Dates will be
            stringified as quoted ISO dates.

            Example:

            var text = JSON.stringify(['e', {pluribus: 'unum'}]);
            // text is '["e",{"pluribus":"unum"}]'

        JSON.parse(text, filter)
            This method parses a JSON text to produce an object or
            array. It can throw a SyntaxError exception.

            The optional filter parameter is a function that can filter and
            transform the results. It receives each of the keys and values, and
            its return value is used instead of the original value. If it
            returns what it received, then structure is not modified. If it
            returns undefined then the member is deleted.

            Example:

            // Parse the text. If a key contains the string 'date' then
            // convert the value to a date.

            myData = JSON.parse(text, function (key, value) {
                return key.indexOf('date') >= 0 ? new Date(value) : value;
            });

    This is a reference implementation. You are free to copy, modify, or
    redistribute.

    Use your own copy. It is extremely unwise to load third party
    code into your pages.
*/

/*jslint evil: true */

/*global JSON */

/*members "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
    charCodeAt, floor, getUTCDate, getUTCFullYear, getUTCHours,
    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join, length,
    parse, propertyIsEnumerable, prototype, push, replace, stringify, test,
    toJSON, toString
*/

if (!this.JSON) {

    JSON = function () {

        function f(n) {    // Format integers to have at least two digits.
            return n < 10 ? '0' + n : n;
        }

        Date.prototype.toJSON = function () {

// Eventually, this method will be based on the date.toISOString method.

            return this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z';
        };


        var m = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        };

        function stringify(value, whitelist) {
            var a,          // The array holding the partial texts.
                i,          // The loop counter.
                k,          // The member key.
                l,          // Length.
                r = /["\\\x00-\x1f\x7f-\x9f]/g,
                v;          // The member value.

            switch (typeof value) {
            case 'string':

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe sequences.

                return r.test(value) ?
                    '"' + value.replace(r, function (a) {
                        var c = m[a];
                        if (c) {
                            return c;
                        }
                        c = a.charCodeAt();
                        return '\\u00' + Math.floor(c / 16).toString(16) +
                                                   (c % 16).toString(16);
                    }) + '"' :
                    '"' + value + '"';

            case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':
                return String(value);

            case 'object':

// Due to a specification blunder in ECMAScript,
// typeof null is 'object', so watch out for that case.

                if (!value) {
                    return 'null';
                }

// If the object has a toJSON method, call it, and stringify the result.

                if (typeof value.toJSON === 'function') {
                    return stringify(value.toJSON());
                }
                a = [];
                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length'))) {

// The object is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                    l = value.length;
                    for (i = 0; i < l; i += 1) {
                        a.push(stringify(value[i], whitelist) || 'null');
                    }

// Join all of the elements together and wrap them in brackets.

                    return '[' + a.join(',') + ']';
                }
                if (whitelist) {

// If a whitelist (array of keys) is provided, use it to select the components
// of the object.

                    l = whitelist.length;
                    for (i = 0; i < l; i += 1) {
                        k = whitelist[i];
                        if (typeof k === 'string') {
                            v = stringify(value[k], whitelist);
                            if (v) {
                                a.push(stringify(k) + ':' + v);
                            }
                        }
                    }
                } else {

// Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (typeof k === 'string') {
                            v = stringify(value[k], whitelist);
                            if (v) {
                                a.push(stringify(k) + ':' + v);
                            }
                        }
                    }
                }

// Join all of the member texts together and wrap them in braces.

                return '{' + a.join(',') + '}';
            }
        }

        return {
            stringify: stringify,
            parse: function (text, filter) {
                var j;

                function walk(k, v) {
                    var i, n;
                    if (v && typeof v === 'object') {
                        for (i in v) {
                            if (Object.prototype.hasOwnProperty.apply(v, [i])) {
                                n = walk(i, v[i]);
                                if (n !== undefined) {
                                    v[i] = n;
                                }
                            }
                        }
                    }
                    return filter(k, v);
                }


// Parsing happens in three stages. In the first stage, we run the text against
// regular expressions that look for non-JSON patterns. We are especially
// concerned with '()' and 'new' because they can cause invocation, and '='
// because it can cause mutation. But just to be safe, we want to reject all
// unexpected forms.

// We split the first stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace all backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

                if (/^[\],:{}\s]*$/.test(text.replace(/\\./g, '@').
replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the second stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                    j = eval('(' + text + ')');

// In the optional third stage, we recursively walk the new structure, passing
// each name/value pair to a filter function for possible transformation.

                    return typeof filter === 'function' ? walk('', j) : j;
                }

// If the text is not JSON parseable, then a SyntaxError is thrown.

                throw new SyntaxError('parseJSON');
            }
        };
    }();
}


var in2igui = {
	locales : {
		'da/DK':{decimal:',',thousands:'.'},
		'en/US':{decimal:'.',thousands:','}
	},
	locale : {decimal:',',thousands:'.'},
	setLocale : function(code) {
		if (this.locales[code]) {
			this.locale = this.locales[code];
		}
	}
};

/**
  The base class of the In2iGui framework
  @constructor
 */
function In2iGui() {
	/** {boolean} Is true when the DOM is loaded */
	this.domLoaded = false;
	/** @private */
	this.overflows = null;
	/** @private */
	this.delegates = [];
	/** @private */
	this.objects = $H();
	/** @private */
	this.layoutWidgets = [];
	/** @private */
	this.state = 'default';
	this.addBehavior();
}

window.ui = In2iGui;

/** @private */
In2iGui.latestObjectIndex = 0;
/** @private */
In2iGui.latestIndex = 500;
/** @private */
In2iGui.latestPanelIndex = 1000;
/** @private */
In2iGui.latestAlertIndex = 1500;
/** @private */
In2iGui.latestTopIndex = 2000;
/** @private */
In2iGui.toolTips = {};

/** Gets the one instance of In2iGui */
In2iGui.get = function(nameOrWidget) {
	if (!In2iGui.instance) {
		In2iGui.instance = new In2iGui();
	}
	if (nameOrWidget) {
		if (nameOrWidget.element) {
			return nameOrWidget;
		}
		return In2iGui.instance.objects.get(nameOrWidget);
	} else {
		return In2iGui.instance;
	}
};

document.observe('dom:loaded', function () {
	In2iGui.get().ignite();
});

In2iGui.prototype = {
	/** @private */
	ignite : function() {
		if (window.dwr) {
			if (dwr && dwr.engine && dwr.engine.setErrorHandler) {
				dwr.engine.setErrorHandler(function(msg,e) {
					n2i.log(msg);
					n2i.log(e);
					In2iGui.get().alert({title:'An unexpected error occurred!',text:msg,emotion:'gasp'});
				});
			}
		}
		this.domLoaded = true;
		In2iGui.domReady = true;
		this.resize();
		In2iGui.callSuperDelegates(this,'interfaceIsReady');

		this.reLayout();
		Event.observe(window,'resize',this.reLayout.bind(this));
	},
	/** @private */
	addBehavior : function() {
		Event.observe(window,'resize',this.resize.bind(this));
	},
	/** Adds a global delegate
	 * @deprecated
	*/
	listen : function(delegate) {
		this.delegates.push(delegate);
	},
	listen : function(delegate) {
		this.delegates.push(delegate);
	},
	getTopPad : function(element) {
		var all,top;
		all = parseInt(n2i.getStyle(element,'padding'),10);
		top = parseInt(n2i.getStyle(element,'padding-top'),10);
		if (all) {return all;}
		if (top) {return top;}
		return 0;
	},
	getBottomPad : function(element) {
		var all,bottom;
		all = parseInt(n2i.getStyle(element,'padding'),10);
		bottom = parseInt(n2i.getStyle(element,'padding-bottom'),10);
		if (all) {return all;}
		if (bottom) {return bottom;}
		return 0;
	},
	/** @private */
	resize : function() {
		if (!this.overflows) {return;}
		var height = n2i.getInnerHeight();
		this.overflows.each(function(overflow) {
			if (n2i.browser.webkit || n2i.browser.gecko) {
				overflow.element.style.display='none';
				overflow.element.style.width = overflow.element.parentNode.clientWidth+'px';
				overflow.element.style.display='';
			}
			overflow.element.style.height = height+overflow.diff+'px';
		});
	},
	registerOverflow : function(id,diff) {
		if (!this.overflows) {this.overflows=[];}
		var overflow = $(id);
		this.overflows.push({element:overflow,diff:diff});
	},
	/** @private */
	alert : function(options) {
		if (!this.alertBox) {
			this.alertBox = In2iGui.Alert.create(options);
			this.alertBoxButton = In2iGui.Button.create({name:'in2iGuiAlertBoxButton',text : 'OK'});
			this.alertBoxButton.listen(this);
			this.alertBox.addButton(this.alertBoxButton);
		} else {
			this.alertBox.update(options);
		}
		this.alertBoxCallBack = options.onOK;
		this.alertBoxButton.setText(options.button ? options.button : 'OK');
		this.alertBox.show();
	},
	/** @private */
	$click$in2iGuiAlertBoxButton : function() {
		In2iGui.get().alertBox.hide();
		if (this.alertBoxCallBack) {
			this.alertBoxCallBack();
			this.alertBoxCallBack = null;
		}
	},
	confirm : function(options) {
		if (!options.name) {
			options.name = 'in2iguiConfirm';
		}
		var alert = In2iGui.get(options.name);
		var ok;
		if (!alert) {
			alert = In2iGui.Alert.create(options);
			var cancel = In2iGui.Button.create({name:name+'_cancel',text : options.cancel || 'Cancel',highlighted:options.highlighted==='cancel'});
			cancel.listen({$click:function(){
				alert.hide();
				if (options.onCancel) {
					options.onCancel();
				}
				In2iGui.callDelegates(alert,'cancel');
			}});
			alert.addButton(cancel);
		
			ok = In2iGui.Button.create({name:name+'_ok',text : options.ok || 'OK',highlighted:options.highlighted==='ok'});
			alert.addButton(ok);
		} else {
			alert.update(options);
			ok = In2iGui.get(name+'_ok');
			ok.setText(options.ok || 'OK');
			ok.setHighlighted(options.highlighted=='ok');
			ok.clearDelegates();
			In2iGui.get(name+'_cancel').setText(options.ok || 'Cancel');
			In2iGui.get(name+'_cancel').setHighlighted(options.highlighted=='cancel');
			if (options.cancel) {In2iGui.get(name+'_cancel').setText(options.cancel);}
		}
		ok.listen({$click:function(){
			alert.hide();
			if (options.onOK) {
				options.onOK();
			}
			In2iGui.callDelegates(alert,'ok');
		}});
		alert.show();
	},
	reLayout : function() {
		for (var i = this.layoutWidgets.length - 1; i >= 0; i--){
			this.layoutWidgets[i]['$$layout']();
		};
	},
	getDescendants : function(widgetOrElement) {
		var desc = [],e = widgetOrElement.getElement ? widgetOrElement.getElement() : widgetOrElement;
		if (e) {
			var d = e.descendants();
			var o = this.objects.values();
			for (var i=0; i < d.length; i++) {
				for (var j=0; j < o.length; j++) {
					if (d[i]==o[j].element) {
						desc.push(o[j]);
					}
				};
				
			};
		}
		return desc;
	},
	/** Gets all ancestors of a widget
		@param {Widget} A widget
		@returns {Array} An array of all ancestors
	*/
	getAncestors : function(widget) {
		var desc = [];
		var e = widget.element;
		if (e) {
			var a = e.ancestors();
			var o = this.objects.values();
			for (var i=0; i < a.length; i++) {
				for (var j=0; j < o.length; j++) {
					if (o[j].element==a[i]) {
						desc.push(o[j]);
					}
					
				};
			};
		}
		return desc;
	},
	getAncestor : function(widget,cls) {
		var a = this.getAncestors(widget);
		for (var i=0; i < a.length; i++) {
			if (a[i].getElement().hasClassName(cls)) {
				return a[i];
			}
		};
		return null;
	}
};

In2iGui.destroyDescendants = function(element) {
	var desc = In2iGui.get().getDescendants(element);
	var objects = In2iGui.get().objects;
	for (var i=0; i < desc.length; i++) {
		var obj  = objects.unset(desc[i].name);
		if (!obj) {
			n2i.log('not found: '+desc[i].name);
		}
	};
}

In2iGui.changeState = function(state) {
	if (In2iGui.get().state===state) {return;}
	var objects = In2iGui.get().objects.values();
	objects.each(function(obj) {
		if (obj.options && obj.options.state) {
			if (obj.options.state==state) {obj.show();}
			else {obj.hide();}
		}
	});
	In2iGui.get().state==state;
}

///////////////////////////////// Indexes /////////////////////////////

In2iGui.nextIndex = function() {
	In2iGui.latestIndex++;
	return 	In2iGui.latestIndex;
};

In2iGui.nextPanelIndex = function() {
	In2iGui.latestPanelIndex++;
	return 	In2iGui.latestPanelIndex;
};

In2iGui.nextAlertIndex = function() {
	In2iGui.latestAlertIndex++;
	return 	In2iGui.latestAlertIndex;
};

In2iGui.nextTopIndex = function() {
	In2iGui.latestTopIndex++;
	return 	In2iGui.latestTopIndex;
};

///////////////////////////////// Curtain /////////////////////////////

In2iGui.showCurtain = function(widget,zIndex) {
	if (!widget.curtain) {
		widget.curtain = new Element('div',{'class':'in2igui_curtain'}).setStyle({'z-index':'none'});
		widget.curtain.onclick = function() {
			if (widget.curtainWasClicked) {
				widget.curtainWasClicked();
			}
		};
		var body = $$('.in2igui_body')[0];
		if (!body) {
			body=document.body;
		}
		body.appendChild(widget.curtain);
	}
	widget.curtain.style.height=n2i.getDocumentHeight()+'px';
	widget.curtain.style.zIndex=zIndex;
	n2i.setOpacity(widget.curtain,0);
	widget.curtain.style.display='block';
	n2i.ani(widget.curtain,'opacity',0.7,1000,{ease:n2i.ease.slowFastSlow});
};

In2iGui.hideCurtain = function(widget) {
	if (widget.curtain) {
		n2i.ani(widget.curtain,'opacity',0,200,{hideOnComplete:true});
	}
};

//////////////////////////////// Message //////////////////////////////

In2iGui.alert = function(o) {
	In2iGui.get().alert(o);
};

In2iGui.showMessage = function(options) {
	if (typeof(options)=='string') {
		// TODO: Backwards compatibility
		options={text:options};
	}
	if (!In2iGui.message) {
		In2iGui.message = new Element('div',{'class':'in2igui_message'}).update('<div><div></div></div>');
		if (!n2i.browser.msie) {
			In2iGui.message.setStyle({opacity:0});
		}
		document.body.appendChild(In2iGui.message);
	}
	In2iGui.message.select('div')[1].update(options.text);
	In2iGui.message.setStyle({'display':'block',zIndex:In2iGui.nextTopIndex()});
	In2iGui.message.setStyle({marginLeft:(In2iGui.message.getWidth()/-2)+'px',marginTop:n2i.getScrollTop()+'px'});
	if (!n2i.browser.msie) {
		n2i.ani(In2iGui.message,'opacity',1,300);
	}
	window.clearTimeout(In2iGui.messageTimer);
	if (options.duration) {
		In2iGui.messageTimer = window.setTimeout(In2iGui.hideMessage,options.duration);
	}
};

In2iGui.hideMessage = function() {
	if (In2iGui.message) {
		if (!n2i.browser.msie) {
			n2i.ani(In2iGui.message,'opacity',0,300,{hideOnComplete:true});
		} else {
			In2iGui.message.setStyle({display:'none'});
		}
	}
};

In2iGui.showToolTip = function(options) {
	var key = options.key || 'common';
	var t = In2iGui.toolTips[key];
	if (!t) {
		t = new Element('div',{'class':'in2igui_tooltip'}).update('<div><div></div></div>').setStyle({display:'none'});
		document.body.appendChild(t);
		In2iGui.toolTips[key] = t;
	}
	t.onclick = function() {In2iGui.hideToolTip(options);};
	var n = $(options.element);
	var pos = n.cumulativeOffset();
	t.select('div')[1].update(options.text);
	if (t.style.display=='none' && !n2i.browser.msie) {t.setStyle({opacity:0});}
	t.setStyle({'display':'block',zIndex:In2iGui.nextTopIndex()});
	t.setStyle({left:(pos.left-t.getWidth()+4)+'px',top:(pos.top+2-(t.getHeight()/2)+(n.getHeight()/2))+'px'});
	if (!n2i.browser.msie) {
		n2i.ani(t,'opacity',1,300);
	}
};

In2iGui.hideToolTip = function(options) {
	var key = options ? options.key || 'common' : 'common';
	var t = In2iGui.toolTips[key];
	if (t) {
		if (!n2i.browser.msie) {
			n2i.ani(t,'opacity',0,300,{hideOnComplete:true});
		} else {
			t.setStyle({display:'none'});
		}
	}
};

/////////////////////////////// Utilities /////////////////////////////

In2iGui.isWithin = function(e,element) {
	Event.extend(e);
	var offset = element.cumulativeOffset();
	var dims = element.getDimensions();
	return e.pointerX()>offset.left && e.pointerX()<offset.left+dims.width && e.pointerY()>offset.top && e.pointerY()<offset.top+dims.height;
};

In2iGui.getIconUrl = function(icon,size) {
	return In2iGui.context+'/In2iGui/icons/'+icon+size+'.png';
};

In2iGui.createIcon = function(icon,size) {
	return new Element('span',{'class':'in2igui_icon in2igui_icon_'+size}).setStyle({'backgroundImage':'url('+In2iGui.getIconUrl(icon,size)+')'});
};

In2iGui.onDomReady = In2iGui.onReady = function(func) {
	if (In2iGui.domReady) {return func();}
	if (n2i.browser.gecko && document.baseURI.endsWith('xml')) {
		window.setTimeout(func,1000);
		return;
	}
	document.observe('dom:loaded', func);
};

In2iGui.wrapInField = function(e) {
	var w = new Element('div',{'class':'in2igui_field'}).update(
		'<span class="in2igui_field_top"><span><span></span></span></span>'+
		'<span class="in2igui_field_middle"><span class="in2igui_field_middle"><span class="in2igui_field_content"></span></span></span>'+
		'<span class="in2igui_field_bottom"><span><span></span></span></span>'
	);
	w.select('span.in2igui_field_content')[0].insert(e);
	return w;
};

In2iGui.addFocusClass = function(o) {
	var ce = o.classElement || o.element, c = o['class'];
	o.element.observe('focus',function() {
		ce.addClassName(c);
	}).observe('blur',function() {
		ce.removeClassName(c);
	});
};


/////////////////////////////// Validation /////////////////////////////

In2iGui.NumberValidator = function(options) {
	n2i.override({allowNull:false,min:0,max:10},options)
	this.min = options.min;
	this.max = options.max;
	this.allowNull = options.allowNull;
	this.middle = Math.max(Math.min(this.max,0),this.min);
}

In2iGui.NumberValidator.prototype = {
	validate : function(value) {
		if (n2i.isBlank(value) && this.allowNull) {
			return {valid:true,value:null};
		}
		var number = parseFloat(value);
		if (isNaN(number)) {
			return {valid:false,value:this.middle};
		} else if (number<this.min) {
			return {valid:false,value:this.min};
		} else if (number>this.max) {
			return {valid:false,value:this.max};
		}
		return {valid:true,value:number};
	}
}

/////////////////////////////// Animation /////////////////////////////

In2iGui.fadeIn = function(node,time) {
	if ($(node).getStyle('display')=='none') {
		node.setStyle({opacity:0,display:''});
	}
	n2i.ani(node,'opacity',1,time);
};

In2iGui.fadeOut = function(node,time) {
	n2i.ani(node,'opacity',0,time,{hideOnComplete:true});
};

//////////////////////////// Positioning /////////////////////////////

In2iGui.positionAtElement = function(element,target,options) {
	options = options || {};
	element = $(element);
	target = $(target);
	var origDisplay = element.getStyle('display');
	if (origDisplay=='none') {
		element.setStyle({'visibility':'hidden','display':'block'});
	}
	var pos = target.cumulativeOffset(),left = pos.left,top = pos.top;
	var vert=options.vertical || null;
	if (options.horizontal && options.horizontal=='right') {
		left = left+target.getWidth()-element.getWidth();
	}
	if (vert=='topOutside') {
		top = top-element.getHeight();
	} else if (vert=='bottomOutside') {
		top = top+target.getHeight();
	}
	left+=(options.left || 0);
	top+=(options.top || 0);
	element.setStyle({'left':left+'px','top':top+'px'});
	if (origDisplay=='none') {
		element.setStyle({'visibility':'visible','display':'none'});
	}
};


//////////////////////////////// Drag drop //////////////////////////////

In2iGui.getDragProxy = function() {
	if (!In2iGui.dragProxy) {
		In2iGui.dragProxy = new Element('div',{'class':'in2igui_dragproxy'}).setStyle({'display':'none'});
		document.body.appendChild(In2iGui.dragProxy);
	}
	return In2iGui.dragProxy;
};

In2iGui.startDrag = function(e,element,options) {
	e = e || window.event;
	var info = element.dragDropInfo;
	In2iGui.dropTypes = In2iGui.findDropTypes(info);
	if (!In2iGui.dropTypes) return;
	var proxy = In2iGui.getDragProxy();
	Event.observe(document.body,'mousemove',In2iGui.dragListener);
	Event.observe(document.body,'mouseup',In2iGui.dragEndListener);
	In2iGui.dragInfo = info;
	if (info.icon) {
		proxy.style.backgroundImage = 'url('+In2iGui.getIconUrl(info.icon,1)+')';
	}
	In2iGui.startDragPos = {top:Event.pointerY(e),left:Event.pointerX(e)};
	proxy.innerHTML = '<span>'+info.title+'</span>' || '###';
	In2iGui.dragging = true;
	document.body.onselectstart = function () { return false; };
};

In2iGui.findDropTypes = function(drag) {
	var gui = In2iGui.get();
	var drops = null;
	for (var i=0; i < gui.delegates.length; i++) {
		if (gui.delegates[i].dragDrop) {
			for (var j=0; j < gui.delegates[i].dragDrop.length; j++) {
				var rule = gui.delegates[i].dragDrop[j];
				if (rule.drag==drag.kind) {
					if (drops==null) drops={};
					drops[rule.drop] = {};
				}
			};
		}
	}
	return drops;
};

In2iGui.dragListener = function(e) {
	In2iGui.dragProxy.style.left = (Event.pointerX(e)+10)+'px';
	In2iGui.dragProxy.style.top = Event.pointerY(e)+'px';
	In2iGui.dragProxy.style.display='block';
	var target = In2iGui.findDropTarget(Event.element(e));
	if (target && In2iGui.dropTypes[target.dragDropInfo['kind']]) {
		if (In2iGui.latestDropTarget) {
			In2iGui.latestDropTarget.removeClassName('in2igui_drop');
		}
		target.addClassName('in2igui_drop');
		In2iGui.latestDropTarget = target;
	} else if (In2iGui.latestDropTarget) {
		In2iGui.latestDropTarget.removeClassName('in2igui_drop');
		In2iGui.latestDropTarget = null;
	}
	return false;
};

In2iGui.findDropTarget = function(node) {
	while (node) {
		if (node.dragDropInfo) {
			return node;
		}
		node = node.parentNode;
	}
	return null;
};

In2iGui.dragEndListener = function(event) {
	Event.stopObserving(document.body,'mousemove',In2iGui.dragListener);
	Event.stopObserving(document.body,'mouseup',In2iGui.dragEndListener);
	In2iGui.dragging = false;
	if (In2iGui.latestDropTarget) {
		In2iGui.latestDropTarget.removeClassName('in2igui_drop');
		In2iGui.callDelegatesDrop(In2iGui.dragInfo,In2iGui.latestDropTarget.dragDropInfo);
		In2iGui.dragProxy.style.display='none';
	} else {
		n2i.ani(In2iGui.dragProxy,'left',(In2iGui.startDragPos.left+10)+'px',200,{ease:n2i.ease.fastSlow});
		n2i.ani(In2iGui.dragProxy,'top',(In2iGui.startDragPos.top-5)+'px',200,{ease:n2i.ease.fastSlow,hideOnComplete:true});
	}
	In2iGui.latestDropTarget=null;
	document.body.onselectstart=null;
};

In2iGui.dropOverListener = function(event) {
	if (In2iGui.dragging) {
		//this.style.backgroundColor='#3875D7';
	}
};

In2iGui.dropOutListener = function(event) {
	if (In2iGui.dragging) {
		//this.style.backgroundColor='';
	}
};

//////////////////// Delegating ////////////////////

In2iGui.extend = function(obj,options) {
	if (!obj.name) {
		In2iGui.latestObjectIndex++;
		obj.name = 'unnamed'+In2iGui.latestObjectIndex;
	}
	if (options!==undefined) {
		if (obj.options) {
			obj.options = n2i.override(obj.options,options);
		}
		obj.element = $(options.element);
		obj.name = options.name;
	}
	var ctrl = In2iGui.get();
	ctrl.objects.set(obj.name,obj);
	obj.delegates = [];
	obj.addDelegate = obj.listen = function(delegate) {
		n2i.addToArray(this.delegates,delegate);
	}
	obj.removeDelegate = function(delegate) {
		n2i.removeFromArray(this.delegates,delegate);
	}
	obj.clearDelegates = function() {
		this.delegates = [];
	}
	obj.fire = function(method,value,event) {
		In2iGui.callDelegates(this,method,value,event);
	}
	obj.fireProperty = function(key,value) {
		In2iGui.firePropertyChange(this,key,value);
	}
	if (!obj.getElement) {
		obj.getElement = function() {
			return this.element;
		}
	}
	if (!obj.valueForProperty) {
		obj.valueForProperty = function(p) {return this[p]};
	}
	if (obj['$$layout']) {
		ctrl.layoutWidgets.push(obj);
	}
};

In2iGui.callDelegatesDrop = function(dragged,dropped) {
	var gui = In2iGui.get();
	var result = null;
	for (var i=0; i < gui.delegates.length; i++) {
		if (gui.delegates[i]['$drop$'+dragged.kind+'$'+dropped.kind]) {
			gui.delegates[i]['$drop$'+dragged.kind+'$'+dropped.kind](dragged,dropped);
		}
	}
};

In2iGui.callAncestors = function(obj,method,value,event) {
	if (typeof(value)=='undefined') value=obj;
	var d = In2iGui.get().getAncestors(obj);
	for (var i=0; i < d.length; i++) {
		if (d[i][method]) {
			d[i][method](value,event);
		}
	};
};

In2iGui.callDescendants = function(obj,method,value,event) {
	if (typeof(value)=='undefined') {
		value=obj;
	}
	var d = In2iGui.get().getDescendants(obj);
	d.each(function(child) {
		if (child[method]) {
			thisResult = child[method](value,event);
		}
	});
};

In2iGui.callVisible = function(widget) {
	In2iGui.callDescendants(widget,'$visibilityChanged');
}

In2iGui.addDelegate = In2iGui.observe = In2iGui.listen = function(d) {
	In2iGui.get().listen(d);
}

In2iGui.callDelegates = function(obj,method,value,event) {
	if (typeof(value)=='undefined') {
		value=obj;
	}
	var result = null;
	if (obj.delegates) {
		for (var i=0; i < obj.delegates.length; i++) {
			var delegate = obj.delegates[i];
			var thisResult = null;
			if (obj.name && delegate['$'+method+'$'+obj.name]) {
				thisResult = delegate['$'+method+'$'+obj.name](value,event);
			} else if (delegate['$'+method]) {
				thisResult = delegate['$'+method](value,event);
			}
			if (result==null && thisResult!=null && typeof(thisResult)!='undefined') {
				result = thisResult;
			}
		};
	}
	var superResult = In2iGui.callSuperDelegates(obj,method,value,event);
	if (result==null && superResult!=null) {
		result = superResult;
	}
	return result;
};

In2iGui.callSuperDelegates = function(obj,method,value,event) {
	if (typeof(value)=='undefined') value=obj;
	var gui = In2iGui.get();
	var result = null;
	for (var i=0; i < gui.delegates.length; i++) {
		var delegate = gui.delegates[i];
		var thisResult = null;
		if (obj.name && delegate['$'+method+'$'+obj.name]) {
			thisResult = delegate['$'+method+'$'+obj.name](value,event);
		} else if (delegate['$'+method]) {
			thisResult = delegate['$'+method](value,event);
		}
		if (result==null && thisResult!=null && typeof(thisResult)!='undefined') {
			result = thisResult;
		}
	};
	return result;
};

In2iGui.resolveImageUrl = function(widget,img,width,height) {
	for (var i=0; i < widget.delegates.length; i++) {
		if (widget.delegates[i].$resolveImageUrl) {
			return widget.delegates[i].$resolveImageUrl(img,width,height);
		}
	};
	var gui = In2iGui.get();
	for (var i=0; i < gui.delegates.length; i++) {
		var delegate = gui.delegates[i];
		if (delegate.$resolveImageUrl) {
			return delegate.$resolveImageUrl(img,width,height);
		}
	}
	return null;
};

////////////////////////////// Bindings ///////////////////////////

In2iGui.firePropertyChange = function(obj,name,value) {
	In2iGui.callDelegates(obj,'propertyChanged',{property:name,value:value});
};

In2iGui.bind = function(expression,delegate) {
	if (expression.charAt(0)=='@') {
		var pair = expression.substring(1).split('.');
		var obj = In2iGui.get(pair[0]);
		if (!obj) {
			n2i.log('Unable to bind to '+expression);
			return;
		}
		var p = pair.slice(1).join('.');
		obj.listen({
			$propertyChanged : function(prop) {
				if (prop.property==p) {
					delegate(prop.value);
				}
			}
		});
		return obj.valueForProperty(p);
	}
	return expression;
};

//////////////////////////////// Data /////////////////////////////

In2iGui.dwrUpdate = function() {
	var func = arguments[0];
	var delegate = {
  		callback:function(data) { In2iGui.handleDwrUpdate(data) }
	}
	var num = arguments.length;
	if (num==1) {
		func(delegate);
	} else if (num==2) {
		func(arguments[1],delegate);
	} else {
		alert('Too many parameters');
	}
};

In2iGui.handleDwrUpdate = function(data) {
	var gui = In2iGui.get();
	for (var i=0; i < data.length; i++) {
		if (gui.objects.get(data[i].name)) {
			gui.objects.get(data[i].name).updateFromObject(data[i]);
		}
	};
};

In2iGui.update = function(url,delegate) {
	var dlgt = {
		onSuccess:function(t) {In2iGui.handleUpdate(t,delegate)}
	}
	$get(url,dlgt);
};

In2iGui.handleUpdate = function(t,delegate) {
	var gui = In2iGui.get();
	var doc = t.responseXML.firstChild;
	var children = doc.childNodes;
	for (var i=0; i < children.length; i++) {
		if (children[i].nodeType==1) {
			var name = children[i].getAttribute('name');
			if (name && name!='' && gui.objects.get(name)) {
				gui.objects.get(name).updateFromNode(children[i]);
			}
		}
	};
	delegate.onSuccess();
};

/** @private */
In2iGui.jsonResponse = function(t,key) {
	if (!t.responseXML || !t.responseXML.documentElement) {
		var str = t.responseText.replace(/^\s+|\s+$/g, '');
		if (str.length>0) {
			var json = t.responseText.evalJSON(true);
		} else {
			json = '';
		}
		In2iGui.callDelegates(json,'success$'+key)
	} else {
		In2iGui.callDelegates(t,'success$'+key)
	}
};

/** @deprecated */
In2iGui.json = function(data,url,delegateOrKey) {
	var options = {method:'post',parameters:{},onException:function(e) {throw e}};
	if (typeof(delegateOrKey)=='string') {
		options.onSuccess=function(t) {In2iGui.jsonResponse(t,delegateOrKey)};
	} else {
		delegate = delegateOrKey;
	}
	for (key in data) {
		options.parameters[key]=Object.toJSON(data[key])
	}
	new Ajax.Request(url,options);
};

In2iGui.jsonRequest = function(o) {
	var options = {method:'post',parameters:{},onException:function(e) {throw e}};
	if (typeof(o.event)=='string') {
		options.onSuccess=function(t) {In2iGui.jsonResponse(t,o.event)};
	} else {
		delegate = delegateOrKey;
	}
	for (key in o.parameters) {
		options.parameters[key]=Object.toJSON(o.parameters[key])
	}
	new Ajax.Request(o.url,options)
};

In2iGui.request = function(options) {
	options = n2i.override({method:'post',parameters:{}},options);
	if (options.json) {
		for (key in options.json) {
			options.parameters[key]=Object.toJSON(options.json[key])
		}
	}
	var onSuccess = options.onSuccess;
	options.onSuccess=function(t) {
		if (typeof(onSuccess)=='string') {
			In2iGui.jsonResponse(t,onSuccess);
		} else if (t.responseXML && t.responseXML.documentElement && t.responseXML.documentElement.nodeName!='parsererror' && options.onXML) {
			options.onXML(t.responseXML);
		} else if (options.onJSON) {
			var str = t.responseText.replace(/^\s+|\s+$/g, '');
			if (str.length>0) {
				var json = t.responseText.evalJSON(true);
			} else {
				var json = null;
			}
			options.onJSON(json);
		} else if (typeof(onSuccess)=='function') {
			onSuccess(t);
		}
	};
	var onFailure = options.onFailure;
	options.onFailure = function(t) {
		if (typeof(onFailure)=='string') {
			In2iGui.callDelegates(t,'failure$'+onFailure)
		}
	}
	options.onException = function(t,e) {n2i.log(e)};
	new Ajax.Request(options.url,options);
};

In2iGui.parseItems = function(doc) {
	var root = doc.documentElement;
	var out = [];
	In2iGui.parseSubItems(root,out);
	return out;
};

In2iGui.parseSubItems = function(parent,array) {
	var children = parent.childNodes;
	for (var i=0; i < children.length; i++) {
		var node = children[i];
		if (node.nodeType==1 && node.nodeName=='item') {
			var sub = [];
			In2iGui.parseSubItems(node,sub);
			array.push({
				title:node.getAttribute('title'),
				value:node.getAttribute('value'),
				icon:node.getAttribute('icon'),
				kind:node.getAttribute('kind'),
				badge:node.getAttribute('badge'),
				children:sub
			});
		}
	};
}

////////////////////////////////// Source ///////////////////////////

/** A data source
 * @constructor
 */
In2iGui.Source = function(options) {
	this.options = n2i.override({url:null,dwr:null,parameters:[]},options);
	this.name = options.name;
	this.data = null;
	this.parameters = this.options.parameters;
	In2iGui.extend(this);
	if (options.delegate) {
		this.listen(options.delegate);
	}
	this.busy=false;
	In2iGui.onDomReady(this.init.bind(this));
};

In2iGui.Source.prototype = {
	/** @private */
	init : function() {
		var self = this;
		this.parameters.each(function(parm) {
			parm.value = In2iGui.bind(parm.value,function(value) {
				self.changeParameter(parm.key,value);
			});
		})
		this.refresh();
	},
	/** Refreshes the data source */
	refresh : function() {
		if (this.delegates.length==0) return;
		if (this.busy) {
			this.pendingRefresh = true;
			return;
		}
		this.pendingRefresh = false;
		var self = this;
		if (this.options.url) {
			var url = new n2i.URL(this.options.url);
			this.parameters.each(function(p) {
				url.addParameter(p.key,p.value);
			});
			this.busy=true;
			In2iGui.callDelegates(this,'sourceIsBusy');
			new Ajax.Request(url.toString(), {onSuccess: function(t) {self.parse(t)},onException:function(t,e) {n2i.log(e)}});
		} else if (this.options.dwr) {
			var pair = this.options.dwr.split('.');
			var facade = eval(pair[0]);
			var method = pair[1];
			var args = facade[method].argumentNames();
			for (var i=0; i < args.length; i++) {
				if (this.parameters[i]) {
					args[i]=this.parameters[i].value===undefined ? null : this.parameters[i].value;
				}
			};
			args[args.length-1]=function(r) {self.parseDWR(r)};
			this.busy=true;
			In2iGui.callDelegates(this,'sourceIsBusy');
			facade[method].apply(facade,args);
		}
	},
	/** @private */
	end : function() {
		In2iGui.callDelegates(this,'sourceIsNotBusy');
		this.busy=false;
		if (this.pendingRefresh) {
			this.refresh();
		}
	},
	/** @private */
	parse : function(t) {
		if (t.responseXML) {
			this.parseXML(t.responseXML);
		}
		this.end();
	},
	/** @private */
	parseXML : function(doc) {
		if (doc.documentElement.tagName=='items') {
			this.data = In2iGui.parseItems(doc);
			this.fire('itemsLoaded',this.data);
		} else if (doc.documentElement.tagName=='list') {
			this.fire('listLoaded',doc);
		} else if (doc.documentElement.tagName=='articles') {
			this.fire('articlesLoaded',doc);
		}
	},
	/** @private */
	parseDWR : function(data) {
		this.data = data;
		this.fire('objectsLoaded',data);
		this.end();
	},
	addParameter : function(parm) {
		this.parameters.push(parm);
	},
	changeParameter : function(key,value) {
		this.parameters.each(function(p) {
			if (p.key==key) p.value=value;
		})
		var self = this;
		window.clearTimeout(this.paramDelay);
		this.paramDelay = window.setTimeout(function() {
			this.refresh();
		}.bind(this),100)
	}
}


//////////////////////////// Prototype extensions ////////////////////////////////

Element.addMethods({
	setClassName : function(element,name,set) {
		if (set) {
			element.addClassName(name);
		} else {
			element.removeClassName(name);
		}
		return element;
	}
});

/* EOF */
/**
 * @constructor
 */
In2iGui.Window = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	this.close = this.element.select('.in2igui_window_close')[0];
	this.titlebar = this.element.select('.in2igui_window_titlebar')[0];
	this.title = this.titlebar.select('.in2igui_window_title')[0];
	this.content = this.element.select('.in2igui_window_body')[0];
	this.visible = false;
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Window.create = function(options) {
	options = n2i.override({title:'Window',close:true},options);
	var element = options.element = new Element('div',{'class':'in2igui_window'+(options.variant ? ' in2igui_window_'+options.variant : '')});
	var html = (options.close ? '<div class="in2igui_window_close"></div>' : '')+
		'<div class="in2igui_window_titlebar"><div><div>';
		if (options.icon) {
			html+='<span class="in2igui_window_icon" style="background-image: url('+In2iGui.getIconUrl(options.icon,1)+')"></span>';
		}
	html+='<span class="in2igui_window_title">'+options.title+'</span></div></div></div>'+
		'<div class="in2igui_window_content"><div class="in2igui_window_content"><div class="in2igui_window_body" style="'+
		(options.width ? 'width:'+options.width+'px;':'')+
		(options.padding ? 'padding:'+options.padding+'px;':'')+
		'">'+
		'</div></div></div>'+
		'<div class="in2igui_window_bottom"><div class="in2igui_window_bottom"><div class="in2igui_window_bottom"></div></div></div>';
	element.update(html);
	document.body.appendChild(element);
	return new In2iGui.Window(options);
}

In2iGui.Window.prototype = {
	addBehavior : function() {
		var self = this;
		if (this.close) {
			this.close.observe('click',function(e) {
				this.hide();
				this.fire('userClosedWindow');
			}.bind(this)
			).observe('mousedown',function(e) {e.stop();});
		}
		this.titlebar.onmousedown = function(e) {self.startDrag(e);return false;};
		this.element.observe('mousedown',function() {
			self.element.style.zIndex=In2iGui.nextPanelIndex();
		})
	},
	setTitle : function(title) {
		this.title.update(title);
	},
	show : function() {
		if (this.visible) return;
		this.element.setStyle({
			zIndex : In2iGui.nextPanelIndex(), visibility : 'hidden', display : 'block', top: (n2i.getScrollTop()+40)+'px'
		})
		var width = this.element.clientWidth;
		this.element.setStyle({
			width : width+'px' , visibility : 'visible'
		});
		if (!n2i.browser.msie) {
			n2i.ani(this.element,'opacity',1,0);
		}
		this.visible = true;
		In2iGui.callDescendants(this,'parentShown');
		In2iGui.callVisible(this);
	},
	toggle : function() {
		(this.visible ? this.hide() : this.show() );
	},
	hide : function() {
		if (!this.visible) return;
		if (!n2i.browser.msie) {
			n2i.ani(this.element,'opacity',0,200,{hideOnComplete:true});
		} else {
			this.element.setStyle({display:'none'});
		}
		this.visible = false;
	},
	add : function(widgetOrNode) {
		if (widgetOrNode.getElement) {
			this.content.insert(widgetOrNode.getElement());
		} else {
			this.content.insert(widgetOrNode);
		}
	},
	setVariant : function(variant) {
		this.element.removeClassName('in2igui_window_dark');
		this.element.removeClassName('in2igui_window_light');
		if (variant=='dark' || variant=='light') {
			this.element.addClassName('in2igui_window_'+variant);
		}
	},

////////////////////////////// Dragging ////////////////////////////////

	startDrag : function(e) {
		var event = Event.extend(e || window.event);
		this.element.style.zIndex=In2iGui.nextPanelIndex();
		var pos = this.element.cumulativeOffset();
		this.dragState = {left:event.pointerX()-pos.left,top:event.pointerY()-pos.top};
		this.latestPosition = {left: this.dragState.left, top:this.dragState.top};
		this.latestTime = new Date().getMilliseconds();
		var self = this;
		this.moveListener = function(e) {self.drag(e)};
		this.upListener = function(e) {self.endDrag(e)};
		Event.observe(document,'mousemove',this.moveListener);
		Event.observe(document,'mouseup',this.upListener);
		Event.observe(document,'mousedown',this.upListener);
		event.stop();
		document.body.onselectstart = function () { return false; };
		return false;
	},
	calc : function(top,left) {
		// TODO: No need to do this all the time
		this.a = this.latestPosition.left-left;
		this.b = this.latestPosition.top-top;
		this.dist = Math.sqrt(Math.pow((this.a),2),Math.pow((this.b),2));
		this.latestTime = new Date().getMilliseconds();
		this.latestPosition = {'top':top,'left':left};
	},
	drag : function(e) {
		var event = Event.extend(e);
		this.element.style.right = 'auto';
		var top = (event.pointerY()-this.dragState.top);
		var left = (event.pointerX()-this.dragState.left);
		this.element.style.top = Math.max(top,0)+'px';
		this.element.style.left = Math.max(left,0)+'px';
		//this.calc(top,left);
		return false;
	},
	endDrag : function(e) {
		Event.stopObserving(document,'mousemove',this.moveListener);
		Event.stopObserving(document,'mouseup',this.upListener);
		Event.stopObserving(document,'mousedown',this.upListener);
		document.body.onselectstart = null;
	}
}

/* EOF *//**
 * @class
 * This is a formula
 */
In2iGui.Formula = function(options) {
	this.options = options;
	In2iGui.extend(this,options);
	this.addBehavior();
}

/** @static Creates a new formula */
In2iGui.Formula.create = function(o) {
	o = o || {};
	var atts = {'class':'in2igui_formula'};
	if (o.action) {
		atts.action=o.action;
	}
	if (o.method) {
		atts.method=o.method;
	}
	o.element = new Element('form',atts);
	return new In2iGui.Formula(o);
}

In2iGui.Formula.prototype = {
	/** @private */
	addBehavior : function() {
		this.element.onsubmit=function() {return false;};
	},
	submit : function() {
		this.fire('submit');
	},
	/** Returns a map of all values of descendants */
	getValues : function() {
		var data = {};
		var d = In2iGui.get().getDescendants(this);
		for (var i=0; i < d.length; i++) {
			if (d[i].options && d[i].options.key && d[i].getValue) {
				data[d[i].options.key] = d[i].getValue();
			} else if (d[i].name && d[i].getValue) {
				data[d[i].name] = d[i].getValue();
			}
		};
		return data;
	},
	/** Sets the values of the descendants */
	setValues : function(values) {
		var d = In2iGui.get().getDescendants(this);
		for (var i=0; i < d.length; i++) {
			if (d[i].options && d[i].options.key) {
				var key = d[i].options.key;
				if (key && values[key]!=undefined) {
					d[i].setValue(values[key]);
				}
			}
		}
	},
	/** Sets focus in the first found child */
	focus : function() {
		var d = In2iGui.get().getDescendants(this);
		for (var i=0; i < d.length; i++) {
			if (d[i].focus) {
				d[i].focus();
				return;
			}
		}
	},
	/** Resets all descendants */
	reset : function() {
		var d = In2iGui.get().getDescendants(this);
		for (var i=0; i < d.length; i++) {
			if (d[i].reset) d[i].reset();
		}
	},
	/** Adds a widget to the form */
	add : function(widget) {
		this.element.insert(widget.getElement());
	},
	/** Creates a new form group and adds it to the form
	 * @returns {'In2iGui.Formula.Group'} group
	 */
	createGroup : function(options) {
		var g = In2iGui.Formula.Group.create(options);
		this.add(g);
		return g;
	},
	/** Builds and adds a new group according to a recipe
	 * @returns {'In2iGui.Formula.Group'} group
	 */
	buildGroup : function(options,recipe) {
		var g = this.createGroup(options);
		recipe.each(function(item) {
			if (In2iGui.Formula[item.type]) {
				var w = In2iGui.Formula[item.type].create(item.options);
				g.add(w);
			}
		});
		return g;
	},
	/** @private */
	childValueChanged : function(value) {
		this.fire('valuesChanged',this.getValues());
	}
}

///////////////////////// Group //////////////////////////


/**
 * A form group
 * @constructor
 */
In2iGui.Formula.Group = function(options) {
	this.name = options.name;
	this.element = $(options.element);
	this.body = this.element.select('tbody')[0];
	this.options = n2i.override({above:true},options);
	In2iGui.extend(this);
}

/** Creates a new form group */
In2iGui.Formula.Group.create = function(options) {
	options = n2i.override({above:true},options);
	var element = options.element = new Element('table',
		{'class':'in2igui_formula_group'}
	);
	if (options.above) {
		element.addClassName('in2igui_formula_group_above');
	}
	element.insert(new Element('tbody'));
	return new In2iGui.Formula.Group(options);
}

In2iGui.Formula.Group.prototype = {
	add : function(widget) {
		var tr = new Element('tr');
		this.body.insert(tr);
		if (widget.getLabel) {
			var label = widget.getLabel();
			if (label) {
				var th = new Element('th');
				th.insert(new Element('label').insert(label));
				tr.insert(th);
			}
		}
		var td = new Element('td');
		td.insert(new Element('div',{'class':'in2igui_formula_item'}).insert(widget.getElement()));
		if (this.options.above) {
			tr = new Element('tr');
			this.body.insert(tr);
		}
		tr.insert(td);
	},
	createButtons : function(options) {
		var tr = new Element('tr');
		this.body.insert(tr);
		var td = new Element('td',{colspan:this.options.above?1:2});
		tr.insert(td);
		var b = In2iGui.Buttons.create(options);
		td.insert(b.getElement());
		return b;
	}
}

///////////////////////// Text /////////////////////////

/**
 * A text fields
 * @constructor
 */
In2iGui.Formula.Text = function(options) {
	this.options = n2i.override({label:null,key:null,lines:1,live:false},options);
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
	this.input = this.element.select('.in2igui_formula_text')[0];
	this.placeholder = this.element.select('.in2igui_field_placeholder')[0];
	this.value = this.input.value;
	if (this.placeholder) {
		var self = this;
		In2iGui.onDomReady(function() {
			window.setTimeout(function() {
				self.value = self.input.value;
				self.updateClass();
			},500);
		});
	}
	this.addBehavior();
}

In2iGui.Formula.Text.create = function(options) {
	options = n2i.override({lines:1},options);
	var node,input;
	if (options.lines>1) {
		input = new Element('textarea',
			{'class':'in2igui_formula_text','rows':options.lines}
		);
		node = new Element('span',{'class':'in2igui_formula_text_multiline'}).insert(input);
	} else {
		input = new Element('input',{'class':'in2igui_formula_text'});
		node = new Element('span',{'class':'in2igui_formula_text_singleline'}).insert(input);
	}
	if (options.value!==undefined) {
		input.value=options.value;
	}
	options.element = In2iGui.wrapInField(node);
	return new In2iGui.Formula.Text(options);
}

In2iGui.Formula.Text.prototype = {
	/** @private */
	addBehavior : function() {
		In2iGui.addFocusClass({element:this.input,classElement:this.element,'class':'in2igui_field_focused'});
		this.input.observe('keyup',this.onKeyUp.bind(this));
		var p = this.element.select('em')[0];
		if (p) {
			this.updateClass();
			p.observe('mousedown',function(){window.setTimeout(function() {this.input.focus();this.input.select();}.bind(this))}.bind(this));
			p.observe('mouseup',function(){this.input.focus();this.input.select();}.bind(this));
		}
	},
	updateClass : function() {
		this.element.setClassName('in2igui_field_dirty',this.value.length>0);
	},
	/** @private */
	onKeyUp : function(e) {
		if (this.options.lines<2 && e.keyCode===Event.KEY_RETURN) {
			this.fire('submit');
			var form = In2iGui.get().getAncestor(this,'in2igui_formula');
			if (form) {form.submit();}
			return;
		}
		if (this.input.value==this.value) {return;}
		this.value=this.input.value;
		this.updateClass();
		In2iGui.callAncestors(this,'childValueChanged',this.input.value);
		this.fire('valueChanged',this.input.value);
	},
	updateFromNode : function(node) {
		if (node.firstChild) {
			this.setValue(node.firstChild.nodeValue);
		} else {
			this.setValue(null);
		}
	},
	updateFromObject : function(data) {
		this.setValue(data.value);
	},
	focus : function() {
		try {
			this.input.focus();
		} catch (e) {}
	},
	select : function() {
		try {
			this.input.focus();
			this.input.select();
		} catch (e) {}
	},
	reset : function() {
		this.setValue('');
	},
	setValue : function(value) {
		if (value===undefined || value===null) {
			value='';
		}
		this.value = value;
		this.input.value = value;
	},
	getValue : function() {
		return this.input.value;
	},
	getLabel : function() {
		return this.options.label;
	},
	isEmpty : function() {
		return this.input.value=='';
	},
	isBlank : function() {
		return this.input.value.strip()=='';
	},
	setError : function(error) {
		var isError = error ? true : false;
		this.element.setClassName('in2igui_field_error',isError);
		if (typeof(error) == 'string') {
			In2iGui.showToolTip({text:error,element:this.element,key:this.name});
		}
		if (!isError) {
			In2iGui.hideToolTip({key:this.name});
		}
	}
}

/////////////////////////// Date time /////////////////////////

/**
 * A date and time field
 * @constructor
 */
In2iGui.Formula.DateTime = function(o) {
	this.inputFormats = ['d-m-Y','d/m-Y','d/m/Y','d-m-Y H:i:s','d/m-Y H:i:s','d/m/Y H:i:s','d-m-Y H:i','d/m-Y H:i','d/m/Y H:i','d-m-Y H','d/m-Y H','d/m/Y H','d-m','d/m','d','Y','m-d-Y','m-d','m/d'];
	this.outputFormat = 'd-m-Y H:i:s';
	this.name = o.name;
	this.element = $(o.element);
	this.input = this.element.select('input')[0];
	this.options = n2i.override({returnType:null,label:null,allowNull:true,value:null},o);
	this.value = this.options.value;
	In2iGui.extend(this);
	this.addBehavior();
	this.updateUI();
}

In2iGui.Formula.DateTime.create = function(options) {
	var input = new Element('input',{'class':'in2igui_formula_text'});
	var node = new Element('span',{'class':'in2igui_formula_text_singleline'}).insert(input);
	options.element = In2iGui.wrapInField(node);
	return new In2iGui.Formula.DateTime(options);
}

In2iGui.Formula.DateTime.prototype = {
	addBehavior : function() {
		In2iGui.addFocusClass({element:this.input,classElement:this.element,'class':'in2igui_field_focused'});
		this.input.observe('blur',this.check.bind(this));
	},
	updateFromNode : function(node) {
		if (node.firstChild) {
			this.setValue(node.firstChild.nodeValue);
		} else {
			this.setValue(null);
		}
	},
	updateFromObject : function(data) {
		this.setValue(data.value);
	},
	focus : function() {
		try {this.input.focus();} catch (ignore) {}
	},
	reset : function() {
		this.setValue('');
	},
	setValue : function(value) {
		if (!value) {
			this.value = null;
		} else if (value.constructor==Date) {
			this.value = value;
		} else {
			this.value = new Date();
			this.value.setTime(parseInt(value)*1000);
		}
		this.updateUI();
	},
	check : function() {
		var str = this.input.value;
		var parsed = null;
		for (var i=0; i < this.inputFormats.length && parsed==null; i++) {
			parsed = Date.parseDate(str,this.inputFormats[i]);
		};
		if (this.options.allowNull || parsed!=null) {
			this.value = parsed;
		}
		this.updateUI();
	},
	getValue : function() {
		if (this.value!=null && this.options.returnType=='seconds') {
			return Math.round(this.value.getTime()/1000);
		}
		return this.value;
	},
	getElement : function() {
		return this.element;
	},
	getLabel : function() {
		return this.options.label;
	},
	updateUI : function() {
		if (this.value) {
			this.input.value = this.value.dateFormat(this.outputFormat);
		} else {
			this.input.value = ''
		}
	}
}

/////////////////////////// Number /////////////////////////

/**
 * A date and time field
 * @constructor
 */
In2iGui.Formula.Number = function(o) {
	this.options = n2i.override({min:0,max:10000,value:null,decimals:0,allowNull:false},o);	
	this.name = o.name;
	var e = this.element = $(o.element);
	this.input = e.select('input')[0];
	this.up = e.select('.in2igui_number_up')[0];
	this.down = e.select('.in2igui_number_down')[0];
	this.value = this.options.value;
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Formula.Number.create = function(o) {
	var e = o.element = new Element('span',{'class':'in2igui_number'});
	e.update('<span><span><input type="text" value="'+(o.value!==undefined ? o.value : '0')+'"/><a class="in2igui_number_up"></a><a class="in2igui_number_down"></a></span></span>');
	return new In2iGui.Formula.Number(o);
}

In2iGui.Formula.Number.prototype = {
	addBehavior : function() {
		var e = this.element;
		this.input.observe('focus',function() {e.addClassName('in2igui_number_focused')});
		this.input.observe('blur',this.blurEvent.bind(this));
		this.input.observe('keyup',this.keyEvent.bind(this));
		//this.input.observe('keypress',this.keyEvent.bind(this));
		this.up.observe('mousedown',this.upEvent.bind(this));
		this.down.observe('mousedown',this.downEvent.bind(this));
	},
	blurEvent : function() {
		this.element.removeClassName('in2igui_number_focused');
		this.input.value = this.value;
	},
	keyEvent : function(e) {
		if (e.keyCode==Event.KEY_UP) {
			e.stop();
			this.upEvent();
		} else if (e.keyCode==Event.KEY_DOWN) {
			this.downEvent();
		} else {
			var parsed = parseInt(this.input.value,10);
			if (!isNaN(parsed)) {
				this.setLocalValue(parsed);
			}
		}
	},
	downEvent : function() {
		if (this.value===null) {
			this.setValue(this.options.min);
		} else {
			this.setValue(this.value-1);
		}
	},
	upEvent : function() {
		this.setValue(this.value+1);
	},
	getValue : function() {
		return this.value;
	},
	getLabel : function() {
		return this.options.label;
	},
	setValue : function(value) {
		this.setLocalValue(value);
		this.input.value = this.value;
	},
	setLocalValue : function(value) {
		this.value = Math.min(Math.max(value,this.options.min),this.options.max);
		In2iGui.callAncestors(this,'childValueChanged',this.value);
		this.fire('valueChanged',this.value);
	}
}

////////////////////////// DropDown ///////////////////////////

/**
 * A drop down selector
 * @constructor
 */
In2iGui.Formula.DropDown = function(o) {
	this.options = n2i.override({label:null,placeholder:null,url:null,source:null},o);
	this.name = o.name;
	var e = this.element = $(o.element);
	this.inner = e.select('strong')[0];
	this.items = o.items || [];
	this.index = -1;
	this.value = this.options.value || null;
	this.dirty = true;
	In2iGui.extend(this);
	this.addBehavior();
	this.updateIndex();
	this.updateUI();
	if (this.options.url) {
		this.options.source = new In2iGui.Source({url:this.options.url,delegate:this});
	} else if (this.options.source) {
		this.options.source.addDelegate(this);	
	}
}

In2iGui.Formula.DropDown.create = function(o) {
	o = o || {};
	o.element = new Element('a',{'class':'in2igui_dropdown',href:'#'}).update(
		'<span><span><strong></strong></span></span>'
	);
	return new In2iGui.Formula.DropDown(o);
}

In2iGui.Formula.DropDown.prototype = {
	addBehavior : function() {
		In2iGui.addFocusClass({element:this.element,'class':'in2igui_dropdown_focused'});
		this.element.observe('click',this.clicked.bind(this));
		this.element.observe('blur',this.hideSelector.bind(this));
	},
	updateIndex : function() {
		this.index=-1;
		this.items.each(function(item,i) {
			if (item.value==this.value) this.index=i;
		}.bind(this));
	},
	updateUI : function() {
		var selected = this.items[this.index];
		if (selected) {
			this.inner.update(selected.label || selected.title);
		} else if (this.options.placeholder) {
			this.inner.update(new Element('em').update(this.options.placeholder.escapeHTML()));
		} else {
			this.inner.update();
		}
		if (!this.selector) return;
		this.selector.select('a').each(function(a,i) {
			if (this.index==i) {
				a.addClassName('in2igui_selected');
			}
			else a.className='';
		}.bind(this));
	},
	clicked : function(e) {
		e.stop();
		this.buildSelector();
		var el = this.element,s=this.selector;
		el.focus();
		if (!this.items) return;
		var docHeight = n2i.getDocumentHeight();
		if (docHeight<200) {
			var left = this.element.cumulativeOffset().left;
			this.selector.setStyle({'left':left+'px',top:'5px'});
		} else {
			n2i.place({
				target:{element:this.element,vertical:1,horizontal:0},
				source:{element:this.selector,vertical:0,horizontal:0}
			});
		}
		s.setStyle({visibility:'hidden',display:'block',width:''});
		var height = Math.min(docHeight-s.cumulativeOffset().top-5,200);
		var width = Math.max(el.getWidth()-5,100,s.getWidth());
		var space = n2i.getDocumentWidth()-el.cumulativeOffset().left-20;
		width = Math.min(width,space);
		s.setStyle({visibility:'visible',width:width+'px',zIndex:In2iGui.nextTopIndex(),maxHeight:height+'px'});
	},
	getValue : function(value) {
		return this.value;
	},
	setValue : function(value) {
		this.value = value;
		this.updateIndex();
		this.updateUI();
	},
	reset : function() {
		this.setValue(null);
	},
	getLabel : function() {
		return this.options.label;
	},
	refresh : function() {
		if (this.options.source) {
			this.options.source.refresh();
		}
	},
	addItem : function(item) {
		this.items.push(item);
		this.dirty = true;
		this.updateIndex();
		this.updateUI();
	},
	setItems : function(items) {
		this.items = items;
		this.dirty = true;
		this.index = -1;
		this.updateIndex();
		this.updateUI();
	},
	$itemsLoaded : function(items) {
		this.setItems(items);
	},
	hideSelector : function() {
		if (!this.selector) return;
		this.selector.hide();
	},
	buildSelector : function() {
		if (!this.dirty || !this.items) return;
		if (!this.selector) {
			this.selector = new Element('div',{'class':'in2igui_dropdown_selector'});
			document.body.appendChild(this.selector);
		} else {
			this.selector.update();
		}
		var self = this;
		this.items.each(function(item,i) {
			var e = new Element('a',{href:'#'}).update(item.label || item.title).observe('mousedown',function(e) {
				e.stop();
				self.itemClicked(item,i);
			})
			if (i==self.index) e.addClassName('in2igui_selected');
			self.selector.insert(e);
		});
		this.dirty = false;
	},
	itemClicked : function(item,index) {
		this.index = index;
		var changed = this.value!=this.items[index].value;
		this.value = this.items[index].value;
		this.updateUI();
		this.hideSelector();
		if (changed) {
			In2iGui.callAncestors(this,'childValueChanged',this.value);
			this.fire('valueChanged',this.value);
		}
	}
}


//////////////////////////// Radio buttons ////////////////////////////

/**
 * @constructor
 */
In2iGui.Formula.Radiobuttons = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.name = options.name;
	this.radios = [];
	this.value = options.value;
	this.defaultValue = this.value;
	In2iGui.extend(this);
}

In2iGui.Formula.Radiobuttons.prototype = {
	click : function() {
		this.value = !this.value;
		this.updateUI();
	},
	/** @private */
	updateUI : function() {
		for (var i=0; i < this.radios.length; i++) {
			var radio = this.radios[i];
			$(radio.id).setClassName('in2igui_selected',radio.value==this.value);
		};
	},
	setValue : function(value) {
		this.value = value;
		this.updateUI();
	},
	getValue : function() {
		return this.value;
	},
	reset : function() {
		this.setValue(this.defaultValue);
	},
	registerRadiobutton : function(radio) {
		this.radios.push(radio);
		var element = $(radio.id);
		var self = this;
		element.onclick = function() {
			self.setValue(radio.value);
		}
	}
}


///////////////////////////// Checkbox /////////////////////////////////

/**
 * A check box
 * @constructor
 */
In2iGui.Formula.Checkbox = function(o) {
	this.element = $(o.element);
	this.control = this.element.select('span')[0];
	this.options = o;
	this.name = o.name;
	this.value = o.value==='true' || o.value===true;
	In2iGui.extend(this);
	this.addBehavior();
}

/**
 * Creates a new checkbox
 */
In2iGui.Formula.Checkbox.create = function(o) {
	var e = o.element = new Element('a',{'class':'in2igui_checkbox',href:'#'});
	if (o.value) {
		e.addClassName('in2igui_checkbox_selected');
	}
	e.update('<span><span></span></span>');
	return new In2iGui.Formula.Checkbox(o);
}

In2iGui.Formula.Checkbox.prototype = {
	/** @private */
	addBehavior : function() {
		In2iGui.addFocusClass({element:this.element,'class':'in2igui_checkbox_focused'});
		this.element.observe('click',this.click.bind(this));
	},
	/** @private */
	click : function(e) {
		e.stop();
		this.element.focus();
		this.value = !this.value;
		this.updateUI();
		In2iGui.callAncestors(this,'childValueChanged',this.value);
		this.fire('valueChanged',this.value);
	},
	/** @private */
	updateUI : function() {
		this.element.setClassName('in2igui_checkbox_selected',this.value);
	},
	/** Sets the value
	 * @param {Boolean} value Whether the checkbox is checked
	 */
	setValue : function(value) {
		this.value = value;
		this.updateUI();
	},
	/** Gets the value
	 * @return {Boolean} Whether the checkbox is checked
	 */
	getValue : function() {
		return this.value;
	},
	/** Resets the checkbox */
	reset : function() {
		this.setValue(false);
	},
	/** Gets the label
	 * @return {String} The checkbox label
	 */
	getLabel : function() {
		return this.options.label;
	}
}

/////////////////////////// Checkboxes ////////////////////////////////

/**
 * Multiple checkboxes
 * @constructor
 */
In2iGui.Formula.Checkboxes = function(o) {
	this.options = o;
	this.element = $(o.element);
	this.name = o.name;
	this.items = o.items || [];
	this.sources = [];
	this.subItems = [];
	this.values = o.values || o.value || []; // values is deprecated
	n2i.log(this.values);
	In2iGui.extend(this);
	this.addBehavior();
	this.updateUI();
	if (o.url) {
		new In2iGui.Source({url:o.url,delegate:this});
	}
}

In2iGui.Formula.Checkboxes.create = function(o) {
	o.element = new Element('div',{'class':o.vertical ? 'in2igui_checkboxes in2igui_checkboxes_vertical' : 'in2igui_checkboxes'});
	if (o.items) {
		o.items.each(function(item) {
			var node = new Element('a',{'class':'in2igui_checkbox',href:'javascript:void(0);'}).update(
				'<span><span></span></span>'+item.title
			);
			In2iGui.addFocusClass({element:node,'class':'in2igui_checkbox_focused'});
			o.element.insert(node);
		});
	}
	return new In2iGui.Formula.Checkboxes(o);
}

In2iGui.Formula.Checkboxes.prototype = {
	addBehavior : function() {
		this.element.select('a.in2igui_checkbox').each(function(check,i) {
			check.observe('click',function(e) {
				e.stop();
				this.flipValue(this.items[i].value);
			}.bind(this))
		}.bind(this));
	},
	getValue : function() {
		return this.values;
	},
	/** @deprecated */
	getValues : function() {
		return this.values;
	},
	checkValues : function() {
		var newValues = [];
		for (var i=0; i < this.values.length; i++) {
			var value = this.values[i];
			var found = false;
			for (var j=0; j < this.items.length; j++) {
				found = found || this.items[j].value===value;
			}
			for (var j=0; j < this.subItems.length; j++) {
				found = found || this.subItems[j].hasValue(value);
			};
			if (found) {
				newValues.push(value);
			}
		};
		this.values=newValues;
	},
	setValue : function(values) {
		this.values=values;
		this.checkValues();
		this.updateUI();
	},
	/** @deprecated */
	setValues : function(values) {
		this.setValue(values);
	},
	flipValue : function(value) {
		n2i.flipInArray(this.values,value);
		this.checkValues();
		this.updateUI();
		this.fire('valueChanged',this.values);
		In2iGui.callAncestors(this,'childValueChanged',this.values);
	},
	updateUI : function() {
		for (var i=0; i < this.subItems.length; i++) {
			this.subItems[i].updateUI();
		};
		var nodes = this.element.select('a.in2igui_checkbox');
		this.items.each(function(item,i) {
			nodes[i].setClassName('in2igui_checkbox_selected',this.values.indexOf(item.value)!==-1);
		}.bind(this));
	},
	refresh : function() {
		for (var i=0; i < this.subItems.length; i++) {
			this.subItems[i].refresh();
		};
	},
	reset : function() {
		this.setValues([]);
	},
	registerSource : function(source) {
		source.parent = this;
		this.sources.push(source);
	},
	registerItems : function(items) {
		items.parent = this;
		this.subItems.push(items);
	},
	getLabel : function() {
		return this.options.label;
	},
	$itemsLoaded : function(items) {
		items.each(function(item) {
			var node = new Element('a',{'class':'in2igui_checkbox',href:'javascript:void(0);'}).update(
				'<span><span></span></span>'+item.title
			);
			node.observe('click',function(e) {
				e.stop();
				this.flipValue(item.value);
			}.bind(this))
			In2iGui.addFocusClass({element:node,'class':'in2igui_checkbox_focused'});
			this.element.insert(node);
			this.items.push(item);
		}.bind(this));
		this.checkValues();
		this.updateUI();
	}
}

/////////////////////// Checkbox items ///////////////////

/**
 * Check box items
 * @constructor
 */
In2iGui.Formula.Checkboxes.Items = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	this.parent = null;
	this.options = options;
	this.checkboxes = [];
	In2iGui.extend(this);
	if (this.options.source) {
		this.options.source.addDelegate(this);
	}
}

In2iGui.Formula.Checkboxes.Items.prototype = {
	refresh : function() {
		if (this.options.source) {
			this.options.source.refresh();
		}
	},
	$itemsLoaded : function(items) {
		this.checkboxes = [];
		this.element.update();
		var self = this;
		items.each(function(item) {
			var node = new Element('a',{'class':'in2igui_checkbox',href:'#'}).update(
				'<span><span></span></span>'+item.title
			).observe('click',function(e) {e.stop();node.focus();self.itemWasClicked(item)});
			In2iGui.addFocusClass({element:node,'class':'in2igui_checkbox_focused'});
			self.element.insert(node);
			self.checkboxes.push({title:item.title,element:node,value:item.value});
		})
		this.parent.checkValues();
		this.updateUI();
	},
	itemWasClicked : function(item) {
		this.parent.flipValue(item.value);
	},
	updateUI : function() {
		for (var i=0; i < this.checkboxes.length; i++) {
			var item = this.checkboxes[i];
			item.element.setClassName('in2igui_checkbox_selected',this.parent.values.indexOf(item.value)!=-1);
		};
	},
	hasValue : function(value) {
		for (var i=0; i < this.checkboxes.length; i++) {
			if (this.checkboxes[i].value==value) {
				return true;
			}
		};
		return false;
	}
}

///////////////////////// Tokens //////////////////////////////

/**
 * A tokens component
 * @constructor
 */
In2iGui.Formula.Tokens = function(o) {
	this.options = n2i.override({label:null,key:null},o);
	this.element = $(o.element);
	this.name = o.name;
	this.value = [''];
	In2iGui.extend(this);
	this.updateUI();
}

In2iGui.Formula.Tokens.create = function(o) {
	o = o || {};
	o.element = new Element('div').addClassName('in2igui_tokens');
	return new In2iGui.Formula.Tokens(o);
}

In2iGui.Formula.Tokens.prototype = {
	setValue : function(objects) {
		this.value = objects;
		this.value.push('');
		this.updateUI();
	},
	reset : function() {
		this.value = [''];
		this.updateUI();
	},
	getValue : function() {
		var out = [];
		this.value.each(function(value) {
			value = value.strip();
			if (value.length>0) out.push(value);
		})
		return out;
	},
	getLabel : function() {
		return this.options.label;
	},
	updateUI : function() {
		this.element.update();
		this.value.each(function(value,i) {
			var input = new Element('input').addClassName('in2igui_tokens_token');
			if (this.options.width) {
				input.setStyle({width:this.options.width+'px'});
			}
			input.value = value;
			input.in2iguiIndex = i;
			this.element.insert(input);
			input.observe('keyup',function() {this.inputChanged(input,i)}.bind(this));
		}.bind(this));
	},
	/** @private */
	inputChanged : function(input,index) {
		if (index==this.value.length-1 && input.value!=this.value[index]) {
			this.addField();
		}
		this.value[index] = input.value;
	},
	/** @private */
	addField : function() {
		var input = new Element('input').addClassName('in2igui_tokens_token');
		if (this.options.width) {
			input.setStyle({width:this.options.width+'px'});
		}
		var i = this.value.length;
		this.value.push('');
		this.element.insert(input);
		var self = this;
		input.observe('keyup',function() {self.inputChanged(input,i)});
	}
}

/////////////////////////// Style length /////////////////////////

/**
 * A date and time field
 * @constructor
 */
In2iGui.Formula.StyleLength = function(o) {
	this.options = n2i.override({value:null,min:0,max:1000,units:['px','pt','em','%'],defaultUnit:'px',allowNull:false},o);	
	this.name = o.name;
	var e = this.element = $(o.element);
	this.input = e.select('input')[0];
	var as = e.select('a');
	this.up = as[0];
	this.down = as[1];
	this.value = this.parseValue(this.options.value);
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Formula.StyleLength.prototype = {
	/** @private */
	addBehavior : function() {
		var e = this.element;
		this.input.observe('focus',function() {e.addClassName('in2igui_number_focused')});
		this.input.observe('blur',this.blurEvent.bind(this));
		this.input.observe('keyup',this.keyEvent.bind(this));
		this.up.observe('mousedown',this.upEvent.bind(this));
		this.down.observe('mousedown',this.downEvent.bind(this));
	},
	/** @private */
	parseValue : function(value) {
		var num = parseFloat(value,10);
		if (isNaN(num)) {
			return null;
		}
		var parsed = {number: num, unit:this.options.defaultUnit};
		for (var i=0; i < this.options.units.length; i++) {
			var unit = this.options.units[i];
			if (value.indexOf(unit)!=-1) {
				parsed.unit = unit;
				break;
			}
		};
		parsed.number = Math.max(this.options.min,Math.min(this.options.max,parsed.number));
		return parsed;
	},
	/** @private */
	blurEvent : function() {
		this.element.removeClassName('in2igui_number_focused');
		this.updateInput();
	},
	/** @private */
	keyEvent : function(e) {
		if (e.keyCode==Event.KEY_UP) {
			e.stop();
			this.upEvent();
		} else if (e.keyCode==Event.KEY_DOWN) {
			this.downEvent();
		} else {
			this.checkAndSetValue(this.parseValue(this.input.value));
		}
	},
	/** @private */
	updateInput : function() {
		this.input.value = this.getValue();
	},
	/** @private */
	checkAndSetValue : function(value) {
		var old = this.value;
		var changed = false;
		if (old===null && value===null) {
			// nothing
		} else if (old!=null && value!=null && old.number===value.number && old.unit===value.unit) {
			// nothing
		} else {
			changed = true;
		}
		this.value = value;
		if (changed) {
			this.fire('valueChanged',this.getValue());
		}
	},
	/** @private */
	downEvent : function() {
		if (this.value) {
			this.checkAndSetValue({number:Math.max(this.options.min,this.value.number-1),unit:this.value.unit});
		} else {
			this.checkAndSetValue({number:this.options.min,unit:this.options.defaultUnit});
		}
		this.updateInput();
	},
	/** @private */
	upEvent : function() {
		if (this.value) {
			this.checkAndSetValue({number:Math.min(this.options.max,this.value.number+1),unit:this.value.unit});
		} else {
			this.checkAndSetValue({number:this.options.min+1,unit:this.options.defaultUnit});
		}
		this.updateInput();
	},
	
	// Public
	
	getValue : function() {
		return this.value ? this.value.number+this.value.unit : '';
	},
	setValue : function(value) {
		this.value = this.parseValue(value);
		this.updateInput();
	}
}

/////////////////////////// Style length /////////////////////////

/**
 * A component for geo-location
 * @constructor
 */
In2iGui.Formula.Location = function(options) {
	this.options = n2i.override({value:null},options);
	this.name = options.name;
	this.element = $(options.element);
	this.chooser = this.element.select('a')[0];
	this.latField = new In2iGui.TextField({element:this.element.select('input')[0],validator:new In2iGui.NumberValidator({min:-90,max:90,allowNull:true})});
	this.latField.listen(this);
	this.lngField = new In2iGui.TextField({element:this.element.select('input')[1],validator:new In2iGui.NumberValidator({min:-180,max:180,allowNull:true})});
	this.lngField.listen(this);
	this.value = this.options.value;
	In2iGui.extend(this);
	this.setValue(this.value);
	this.addBehavior();
}

In2iGui.Formula.Location.create = function(options) {
	options = options || {};
	var e = options.element = new Element('div',{'class':'in2igui_location'});
	var b = new Element('span');
	b.update('<span class="in2igui_location_latitude"><span><input/></span></span><span class="in2igui_location_longitude"><span><input/></span></span>');
	e.insert(In2iGui.wrapInField(b));
	e.insert('<a class="in2igui_location_picker" href="javascript:void(0);"></a>');
	return new In2iGui.Formula.Location(options);
}

In2iGui.Formula.Location.prototype = {
	/** @private */
	addBehavior : function() {
		this.chooser.observe('click',this.showPicker.bind(this));
		In2iGui.addFocusClass({element:this.latField.element,classElement:this.element,'class':'in2igui_field_focused'});
		In2iGui.addFocusClass({element:this.lngField.element,classElement:this.element,'class':'in2igui_field_focused'});
	},
	getLabel : function() {
		return this.options.label;
	},
	reset : function() {
		this.setValue();
	},
	getValue : function() {
		return this.value;
	},
	setValue : function(loc) {
		if (loc) {
			this.latField.setValue(loc.latitude);
			this.lngField.setValue(loc.longitude);
			this.value = loc;
		} else {
			this.latField.setValue();
			this.lngField.setValue();
			this.value = null;
		}
		this.updatePicker();
		n2i.log(this.value);
	},
	updatePicker : function() {
		if (this.picker) {
			this.picker.setLocation(this.value);
		}
	},
	/** @private */
	showPicker : function() {
		if (!this.picker) {
			this.picker = new In2iGui.LocationPicker();
			this.picker.listen(this);
		}
		this.picker.show({node:this.chooser,location:this.value});
	},
	$locationChanged : function(loc) {
		this.setValue(loc);
	},
	$valueChanged : function() {
		var lat = this.latField.getValue();
		var lng = this.lngField.getValue();
		if (lat===null || lng===null) {
			this.value = null;
		} else {
			this.value = {latitude:lat,longitude:lng};
		}
		this.updatePicker();
	}
}

/* EOF *//**
 * @constructor
 * @param {Object} options The options : {url:null,source:null}
 */
In2iGui.List = function(options) {
	this.options = n2i.override({url:null,source:null},options);
	this.element = $(options.element);
	this.name = options.name;
	if (this.options.source) {
		this.options.source.addDelegate(this);
	}
	this.url = options.url;
	this.table = this.element.select('table')[0];
	this.head = this.element.select('thead')[0];
	this.body = this.element.select('tbody')[0];
	this.columns = [];
	this.rows = [];
	this.selected = [];
	this.navigation = this.element.select('.in2igui_list_navigation')[0];
	this.count = this.navigation.select('.in2igui_list_count')[0];
	this.windowPage = this.navigation.select('.window_page')[0];
	this.windowPageBody = this.navigation.select('.window_page_body')[0];
	this.parameters = {};
	this.sortKey = null;
	this.sortDirection = null;
	
	this.window = {size:null,page:0,total:0};
	if (options.windowSize!='') {
		this.window.size = parseInt(options.windowSize);
	}
	In2iGui.extend(this);
	if (this.url) this.refresh();
}

/**
 * Creates a new list widget
 * @param {Object} options The options
 */
In2iGui.List.create = function(options) {
	options = n2i.override({},options);
	var e = options.element = new Element('div',{'class':'in2igui_list'});
	e.update('<div class="in2igui_list_navigation"><div class="in2igui_list_selection window_page"><div><div class="window_page_body"></div></div></div><span class="in2igui_list_count"></span></div><div class="in2igui_list_body"'+(options.maxHeight>0 ? ' style="max-height: '+options.maxHeight+'px;"' : '')+'><table cellspacing="0" cellpadding="0"><thead><tr></tr></thead><tbody></tbody></table></div>');
	return new In2iGui.List(options);
}

In2iGui.List.prototype = {
	/** Hides the list */
	hide : function() {
		this.element.hide();
	},
	/** Shows the list */
	show : function() {
		this.element.show();
	},
	/** @private */
	registerColumn : function(column) {
		this.columns.push(column);
	},
	/** Gets an array of selections
	 * @returns {Array} The selected rows
	 */
	getSelection : function() {
		var items = [];
		for (var i=0; i < this.selected.length; i++) {
			items.push(this.rows[this.selected[i]]);
		};
		return items;
	},
	/** Gets the first selection or null
	 * @returns {Object} The first selected row
	 */
	getFirstSelection : function() {
		var items = this.getSelection();
		if (items.length>0) return items[0];
		else return null;
	},
	/** Add a parameter 
	 * @param {String} key The key
	 * @param {String} value The value
	 */
	setParameter : function(key,value) {
		this.parameters[key]=value;
	},
	/** @private */
	loadData : function(url) {
		this.setUrl(url);
	},
	/**
	 * Sets the lists data source and refreshes it if it is new
	 * @param {In2iGui.Source} source The source
	 */
	setSource : function(source) {
		if (this.options.source!=source) {
			if (this.options.source) {
				this.options.source.removeDelegate(this);
			}
			source.addDelegate(this);
			this.options.source = source;
			source.refresh();
		}
	},
	/**
	 * Set an url to load data from, and load the data
	 * @param {String} url The url
	 */
	setUrl : function(url) {
		if (this.options.source) {
			this.options.source.removeDelegate(this);
			this.options.source=null;
		}
		this.url = url;
		this.selected = [];
		this.sortKey = null;
		this.sortDirection = null;
		this.resetState();
		this.refresh();
	},
	/** Clears the data of the list */
	clear : function() {
		this.selected = [];
		this.columns = [];
		this.rows = [];
		this.navigation.style.display='none';
		this.body.update();
		this.head.update();
		if (this.options.source) {
			this.options.source.removeDelegate(this);
		}
		this.options.source = null;
		this.url = null;
	},
	/** Resets the window state of the navigator */
	resetState : function() {
		this.window = {size:null,page:0,total:0};
		In2iGui.firePropertyChange(this,'window',this.window);
		In2iGui.firePropertyChange(this,'window.page',this.window.page);
	},
	/** @private */
	valueForProperty : function(p) {
		if (p=='window.page') return this.window.page;
		if (p=='window.page') return this.window.page;
		else if (p=='sort.key') return this.sortKey;
		else if (p=='sort.direction') return (this.sortDirection || 'ascending');
		else return this[p];
	},
	/**
	 * @private
	 */
	refresh : function() {
		if (this.options.source) {
			this.options.source.refresh();
			return;
		}
		if (!this.url) return;
		var url = this.url;
		if (typeof(this.window.page)=='number') {
			url+=url.indexOf('?')==-1 ? '?' : '&';
			url+='windowPage='+this.window.page;
		}
		if (this.window.size) {
			url+=url.indexOf('?')==-1 ? '?' : '&';
			url+='windowSize='+this.window.size;
		}
		if (this.sortKey) {
			url+=url.indexOf('?')==-1 ? '?' : '&';
			url+='sort='+this.sortKey;
		}
		if (this.sortDirection) {
			url+=url.indexOf('?')==-1 ? '?' : '&';
			url+='direction='+this.sortDirection;
		}
		for (key in this.parameters) {
			url+=url.indexOf('?')==-1 ? '?' : '&';
			url+=key+'='+this.parameters[key];
		}
		In2iGui.request({
			url:url,
			onJSON : this.$objectsLoaded.bind(this),
			onXML : this.$listLoaded.bind(this)
		});
	},
	/** @private */
	sort : function(index) {
		var key = this.columns[index].key;
		if (key==this.sortKey) {
			this.sortDirection = this.sortDirection=='ascending' ? 'descending' : 'ascending';
			In2iGui.firePropertyChange(this,'sort.direction',this.sortDirection);
		} else {
			In2iGui.firePropertyChange(this,'sort.key',key);
		}
		this.sortKey = key;
	},

	/** @private */
	$listLoaded : function(doc) {
		this.selected = [];
		this.parseWindow(doc);
		this.buildNavigation();
		this.body.update();
		this.head.update();
		this.rows = [];
		this.columns = [];
		var headTr = new Element('tr');
		var sort = doc.getElementsByTagName('sort');
		this.sortKey=null;
		this.sortDirection=null;
		if (sort.length>0) {
			this.sortKey=sort[0].getAttribute('key');
			this.sortDirection=sort[0].getAttribute('direction');
		}
		var headers = doc.getElementsByTagName('header');
		for (var i=0; i < headers.length; i++) {
			var className = '';
			var th = new Element('th');
			var width = headers[i].getAttribute('width');
			var key = headers[i].getAttribute('key');
			var sortable = headers[i].getAttribute('sortable')=='true';
			if (width && width!='') {
				th.style.width=width+'%';
			}
			if (sortable) {
				var self = this;
				th.in2iguiIndex = i;
				th.onclick=function() {self.sort(this.in2iguiIndex)};
				className+='sortable';
			}
			if (key==this.sortKey) {
				className+=' sort_'+this.sortDirection;
			}
			th.className=className;
			var span = new Element('span');
			th.appendChild(span);
			span.appendChild(document.createTextNode(headers[i].getAttribute('title')));
			headTr.appendChild(th);
			this.columns.push({'key':key,'sortable':sortable,'width':width});
		};
		this.head.appendChild(headTr);
		var frag = document.createDocumentFragment();
		var rows = doc.getElementsByTagName('row');
		for (var i=0; i < rows.length; i++) {
			var cells = rows[i].getElementsByTagName('cell');
			var row = new Element('tr');
			var icon = rows[i].getAttribute('icon');
			var title = rows[i].getAttribute('title');
			for (var j=0; j < cells.length; j++) {
				var td = new Element('td');
				this.parseCell(cells[j],td);
				row.insert(td);
				if (!title) title = cells[j].innerText || cells[j].textContent;
				if (!icon && cells[j].getAttribute('icon')) icon = cells[j].getAttribute('icon');
			};
			var info = {id:rows[i].getAttribute('id'),kind:rows[i].getAttribute('kind'),icon:icon,title:title,index:i};
			row.dragDropInfo = info;
			this.addRowBehavior(row,i);
			frag.appendChild(row);
			//this.body.insert(row);
			this.rows.push(info);
		};
		this.body.appendChild(frag);
		this.fire('selectionReset');
	},
	
	/** @private */
	$objectsLoaded : function(data) {
		if (data.constructor == Array) {
			this.setObjects(data);
		} else {
			this.setData(data);
		}
		this.fire('selectionReset');
	},
	/** @private */
	$sourceIsBusy : function() {
		//this.element.addClassName('in2igui_list_busy');
	},
	/** @private */
	$sourceIsNotBusy : function() {
		//this.element.removeClassName('in2igui_list_busy');
	},
	
	/** @private */
	filter : function(str) {
		var len = 20;
		var regex = new RegExp("[\\w]{"+len+",}","g");
		var match = regex.exec(str);
		if (match) {
			for (var i=0; i < match.length; i++) {
				var rep = '';
				for (var j=0; j < match[i].length; j++) {
					rep+=match[i][j];
					if ((j+1)%len==0) rep+='\u200B';
				};
				str = str.replace(match[i],rep);
			};
		}
		return str;
	},
	
	/** @private */
	parseCell : function(node,cell) {
		var icon = node.getAttribute('icon');
		if (icon!=null && !icon.blank()) {
			cell.insert(In2iGui.createIcon(icon,1));
		}
		for (var i=0; i < node.childNodes.length; i++) {
			var child = node.childNodes[i];
			if (n2i.dom.isDefinedText(child)) {
				n2i.dom.addText(cell,child.nodeValue);
			} else if (n2i.dom.isElement(child,'break')) {
				cell.insert(new Element('br'));
			} else if (n2i.dom.isElement(child,'line')) {
				var line = new Element('p',{'class':'in2igui_list_line'}).insert(n2i.dom.getNodeText(child));
				if (child.getAttribute('dimmed')=='true') {
					line.addClassName('in2igui_list_dimmed')
				}
				cell.insert(line);
			} else if (n2i.dom.isElement(child,'object')) {
				var obj = new Element('div',{'class':'object'});
				if (child.getAttribute('icon')) {
					obj.insert(In2iGui.createIcon(child.getAttribute('icon'),1));
				}
				if (child.firstChild && child.firstChild.nodeType==n2i.TEXT_NODE && child.firstChild.nodeValue.length>0) {
					obj.appendChild(document.createTextNode(child.firstChild.nodeValue));
				}
				cell.insert(obj);
			}
		};
	},
	/** @private */
	parseWindow : function(doc) {
		var wins = doc.getElementsByTagName('window');
		if (wins.length>0) {
			var win = wins[0];
			this.window.total = parseInt(win.getAttribute('total'));
			this.window.size = parseInt(win.getAttribute('size'));
			this.window.page = parseInt(win.getAttribute('page'));
		} else {
			this.window.total = 0;
			this.window.size = 0;
			this.window.page = 0;
		}
	},
	/** @private */
	buildNavigation : function() {
		var self = this;
		var pages = this.window.size>0 ? Math.ceil(this.window.total/this.window.size) : 0;
		if (pages<2) {
			this.navigation.style.display='none';
			return;
		}
		this.navigation.style.display='block';
		var from = ((this.window.page)*this.window.size+1);
		this.count.update(from+'-'+Math.min((this.window.page+1)*this.window.size,this.window.total)+' / '+this.window.total);
		var pageBody = this.windowPageBody;
		pageBody.update();
		if (pages<2) {
			this.windowPage.style.display='none';	
		} else {
			var indices = $R(0, pages-1);
			indices = this.buildPages(pages,this.window.page);
			indices.each(function(i){
				if (i==='') {
					pageBody.insert('<span>·</span>');
				} else {
					var a = document.createElement('a');
					a.appendChild(document.createTextNode(i+1));
					a.onclick = function() {
						self.windowPageWasClicked(this,i);
						return false;
					}
					if (i==self.window.page) {
						a.className='selected';
					}
					pageBody.appendChild(a);
				}
			});
			this.windowPage.style.display='block';
		}
	},
	buildPages : function(count,selected) {
		var pages = [];
		var x = false;
		for (var i=0;i<count;i++) {
			if (i<1 || i>count-2 || Math.abs(selected-i)<5) {
				pages.push(i);
				x=false;
			} else {
				if (!x) {pages.push('')};
				x=true;
			}
		}
		return pages;
	},
	/** @private */
	setData : function(data) {
		this.selected = [];
		var win = data.window || {};
		this.window.total = win.total || 0;
		this.window.size = win.size || 0;
		this.window.page = win.page || 0;
		this.buildNavigation();
		this.buildHeaders(data.headers);
		this.buildRows(data.rows);
	},
	/** @private */
	buildHeaders : function(headers) {
		this.head.update();
		this.columns = [];
		var tr = new Element('tr');
		this.head.insert(tr);
		headers.each(function(h,i) {
			var th = new Element('th');
			if (h.width) {
				th.setStyle({width:h.width+'%'});
			}
			if (h.sortable) {
				th.observe('click',function() {this.sort(i)}.bind(this));
				th.addClassName('sortable');
			}
			th.insert(new Element('span').update(h.title));
			tr.insert(th);
			this.columns.push(h);
		}.bind(this));
	},
	/** @private */
	buildRows : function(rows) {
		var self = this;
		this.body.update();
		this.rows = [];
		if (!rows) return;
		rows.each(function(r,i) {
			var tr = new Element('tr');
			var icon = r.icon;
			var title = r.title;
			r.cells.each(function(c) {
				var td = new Element('td');
				if (c.icon) {
					td.insert(In2iGui.createIcon(c.icon,1));
					icon = icon || c.icon;
				}
				if (c.text) {
					td.appendChild(document.createTextNode(c.text))
					title = title || c.text;
				}
				tr.insert(td);
			})
			self.body.insert(tr);
			// TODO: Memory leak!
			var info = {id:r.id,kind:r.kind,icon:icon,title:title,index:i};
			tr.dragDropInfo = info;
			self.rows.push({id:r.id,kind:r.kind,icon:icon,title:title,index:i,data:r.data});
			this.addRowBehavior(tr,i);
		}.bind(this));
	},
	/** @private */
	setObjects : function(objects) {
		this.selected = [];
		this.body.update();
		this.rows = [];
		for (var i=0; i < objects.length; i++) {
			var row = new Element('tr');
			var obj = objects[i];
			var title = null;
			for (var j=0; j < this.columns.length; j++) {
				var cell = new Element('td');
				if (this.builder) {
					cell.update(this.builder.buildColumn(this.columns[j],obj));
				} else {
					var value = obj[this.columns[j].key] || '';
					if (value.constructor == Array) {
						for (var k=0; k < value.length; k++) {
							if (value[k].constructor == Object) {
								cell.insert(this.createObject(value[k]));
							} else {
								cell.insert(new Element('div').update(value));
							}
						};
					} else if (value.constructor == Object) {
						cell.insert(this.createObject(value[j]));
					} else {
						cell.insert(value);
						title = title==null ? value : title;
					}
				}
				row.insert(cell);
			};
			var info = {id:obj.id,kind:obj.kind,title:title};
			row.dragDropInfo = info;
			this.body.insert(row);
			this.addRowBehavior(row,i);
			this.rows.push(obj);
		};
	},
	/** @private */
	createObject : function(object) {
		var node = new Element('div',{'class':'object'});
		if (object.icon) {
			node.insert(In2iGui.createIcon(object.icon,1));
			//node.insert(new Element('span',{'class':'in2igui_icon in2igui_icon_1'}).setStyle({'backgroundImage':'url("'+In2iGui.getIconUrl(object.icon,1)+'")'}));
		}
		return node.insert(object.text || object.name || '');
	},
	/** @private */
	addRowBehavior : function(row,index) {
		var self = this;
		row.onmousedown = function(e) {
			self.rowDown(index);
			In2iGui.startDrag(e,row);
			return false;
		}
		row.ondblclick = function() {
			self.rowDoubleClick(index);
			return false;
		}
	},
	/** @private */
	changeSelection : function(indexes) {
		var rows = this.body.getElementsByTagName('tr');
		for (var i=0;i<this.selected.length;i++) {
			rows[this.selected[i]].removeClassName('selected');
		}
		for (var i=0;i<indexes.length;i++) {
			rows[indexes[i]].addClassName('selected');
		}
		this.selected = indexes;
		this.fire('selectionChanged',this.rows[indexes[0]]);
	},
	/** @private */
	rowDown : function(index) {
		this.changeSelection([index]);
	},
	/** @private */
	rowDoubleClick : function(index) {
		In2iGui.callDelegates(this,'listRowsWasOpened');
		In2iGui.callDelegates(this,'listRowWasOpened',this.getFirstSelection());
		In2iGui.callDelegates(this,'onRowOpen',this.getFirstSelection());
	},
	/** @private */
	windowPageWasClicked : function(tag,index) {
		this.window.page = index;
		In2iGui.firePropertyChange(this,'window',this.window);
		In2iGui.firePropertyChange(this,'window.page',this.window.page);
	}
};

/* EOF *//**
 * @constructor
 */
In2iGui.Tabs = function(o) {
	o = o || {};
	this.name = o.name;
	this.element = $(o.element);
	this.activeTab = -1;
	this.bar = this.element.select('.in2igui_tabs_bar ul')[0];
	this.tabs = this.bar.select('li');
	this.contents = this.element.select('.in2igui_tabs_tab');
	this.addBehavior();
	In2iGui.extend(this);
}

In2iGui.Tabs.create = function(options) {
	options = options || {};
	var e = options.element = new Element('div',{'class':'in2igui_tabs'});
	var cls = 'in2igui_tabs_bar';
	if (options.small) {
		cls+=' in2igui_tabs_bar_small';
	}
	if (options.centered) {
		cls+=' in2igui_tabs_bar_centered';
	}
	var bar = new Element('div',{'class' : cls});
	e.insert(bar);
	var ul = new Element('ul');
	bar.insert(ul);
	return new In2iGui.Tabs(options);
}

In2iGui.Tabs.prototype = {
	/** @private */
	addBehavior : function() {
		this.tabs.each(this.addTabBehavior.bind(this));
	},
	/** @private */
	addTabBehavior : function(tab,index) {	
		tab.observe('click',function() {
			this.tabWasClicked(index);
		}.bind(this))
	},
	/** @private */
	registerTab : function(obj) {
		obj.parent = this;
		this.tabs.push(obj);
	},
	/** @private */
	tabWasClicked : function(index) {
		this.activeTab = index;
		this.updateGUI();
	},
	/** @private */
	updateGUI : function() {
		for (var i=0; i < this.tabs.length; i++) {
			this.tabs[i].setClassName('in2igui_tabs_selected',i==this.activeTab);
			this.contents[i].setStyle({display : (i==this.activeTab ? 'block' : 'none')});
		};
	},
	createTab : function(options) {
		options = options || {};
		var tab = new Element('li').update('<a><span><span>'+options.title+'</span></span></a>');
		this.bar.insert(tab);
		this.addTabBehavior(tab,this.tabs.length);
		this.tabs.push(tab);
		var e = options.element = new Element('div',{'class':'in2igui_tabs_tab'});
		if (options.padding>0) {
			e.setStyle({'padding':options.padding+'px'});
		}
		this.contents.push(e);
		this.element.insert(e);
		if (this.activeTab==-1) {
			this.activeTab=0;
			tab.addClassName('in2igui_tabs_selected');
		} else {
			e.setStyle({display:'none'});			
		}
		return new In2iGui.Tab(options);
	}
};

/**
 * @constructor
 */
In2iGui.Tab = function(o) {
	this.name = o.name;
	this.element = $(o.element);
}

In2iGui.Tab.prototype = {
	add : function(widget) {
		this.element.insert(widget.element);
	}
}

/* EOF *//**
 * @constructor
 */
In2iGui.ObjectList = function(o) {
	this.options = n2i.override({key:null},o);
	this.name = o.name;
	this.element = $(o.element);
	this.body = this.element.select('tbody')[0];
	this.template = [];
	this.objects = [];
	In2iGui.extend(this);
}

In2iGui.ObjectList.create = function(o) {
	o=o || {};
	var e = o.element = new Element('table',{'class':'in2igui_objectlist',cellpadding:'0',cellspacing:'0'});
	if (o.template) {
		var head = '<thead><tr>';
		o.template.each(function(item) {
			head+='<th>'+(item.label || '')+'</th>';
		});
		head+='</tr></thead>';
		e.insert(head);
	}
	e.insert(new Element('tbody'));
	var list = new In2iGui.ObjectList(o);
	if (o.template) {
		o.template.each(function(item) {
			list.registerTemplateItem(new In2iGui.ObjectList.Text(item.key));
		});
	}
	return list;
}

In2iGui.ObjectList.prototype = {
	ignite : function() {
		this.addObject({});
	},
	addObject : function(data,addToEnd) {
		if (this.objects.length==0 || addToEnd) {
			var obj = new In2iGui.ObjectList.Object(this.objects.length,data,this);
			this.objects.push(obj);
			this.body.appendChild(obj.getElement());
		} else {
			var last = this.objects.pop();
			var obj = new In2iGui.ObjectList.Object(last.index,data,this);
			last.index++;
			this.objects.push(obj);
			this.objects.push(last);
			this.body.insertBefore(obj.getElement(),last.getElement())
		}
	},
	reset : function() {
		for (var i=0; i < this.objects.length; i++) {
			var element = this.objects[i].getElement();
			element.parentNode.removeChild(element);
		};
		this.objects = [];
		this.addObject({});
	},
	addObjects : function(data) {
		for (var i=0; i < data.length; i++) {
			this.addObject(data[i]);
		};
	},
	setObjects : function(data) {
		this.reset();
		this.addObjects(data);
	},
	getObjects : function(data) {
		var list = [];
		for (var i=0; i < this.objects.length-1; i++) {
			list.push(this.objects[i].getData());
		};
		return list;
	},
	getValue : function() {
		return this.getObjects();
	},
	setValue : function(data) {
		this.setObjects(data);
	},
	registerTemplateItem : function(item) {
		this.template.push(item);
	},
	objectDidChange : function(obj) {
		if (obj.index>=this.objects.length-1) {
			this.addObject({},true);
		}
	},
	getLabel : function() {
		return this.options.label;
	}
}

/********************** Object ********************/

/** @constructor */
In2iGui.ObjectList.Object = function(index,data,list) {
	this.data = data;
	this.index = index;
	this.list = list;
	this.fields = [];
}

In2iGui.ObjectList.Object.prototype = {
	getElement : function() {
		if (!this.element) {
			var self = this;
			this.element = document.createElement('tr');
			for (var i=0; i < this.list.template.length; i++) {
				var template = this.list.template[i];
				var field = template.clone();
				field.object = this;
				this.fields.push(field);
				var cell = document.createElement('td');
				if (i==0) cell.className='first';
				cell.appendChild(field.getElement());
				field.setValue(this.data[template.key]);
				this.element.appendChild(cell);
			};
		}
		return this.element;
	},
	valueDidChange : function() {
		this.list.objectDidChange(this);
	},
	getData : function() {
		var data = this.data;
		for (var i=0; i < this.fields.length; i++) {
			data[this.fields[i].key] = this.fields[i].getValue();
		};
		return data;
	}
}

/*************************** Text **************************/

In2iGui.ObjectList.Text = function(key) {
	this.key = key;
	this.value = null;
}

In2iGui.ObjectList.Text.prototype = {
	clone : function() {
		return new In2iGui.ObjectList.Text(this.key);
	},
	getElement : function() {
		var input = new Element('input',{'class':'in2igui_formula_text'});
		var field = In2iGui.wrapInField(input);
		this.wrapper = new In2iGui.TextField({element:input});
		this.wrapper.addDelegate(this);
		return field;
	},
	$valueChanged : function(value) {
		this.value = value;
		this.object.valueDidChange();
	},
	getValue : function() {
		return this.value;
	},
	setValue : function(value) {
		this.value = value;
		this.wrapper.setValue(value);
	}
}

/*************************** Select **************************/

In2iGui.ObjectList.Select = function(key) {
	this.key = key;
	this.value = null;
	this.options = [];
}

In2iGui.ObjectList.Select.prototype = {
	clone : function() {
		var copy = new In2iGui.ObjectList.Select(this.key);
		copy.options = this.options;
		return copy;
	},
	getElement : function() {
		this.select = new Element('select');
		for (var i=0; i < this.options.length; i++) {
			this.select.options[this.select.options.length] = new Option(this.options[i].label,this.options[i].value);
		};
		var self = this;
		this.select.onchange = function() {
			self.object.valueDidChange();
		}
		return this.select;
	},
	getValue : function() {
		return this.select.value;
	},
	setValue : function(value) {
		this.select.value = value;
	},
	addOption : function(value,label) {
		this.options.push({value:value,label:label});
	}
}

/* EOF *//**
 * An alert
 * @constructor
 */
In2iGui.Alert = function(options) {
	this.options = n2i.override({modal:false},options);
	this.element = $(options.element);
	this.name = options.name;
	this.body = this.element.select('.in2igui_alert_body')[0];
	this.content = this.element.select('.in2igui_alert_content')[0];
	this.emotion = this.options.emotion;
	this.title = this.element.select('h1').first();
	In2iGui.extend(this);
}

/**
 * Creates a new instance of an alert
 * <br/><strong>options:</strong> { name: «String», title: «String», text: «String», emotion: «'smile' | 'gasp'», modal: «Boolean»}
 * @static
 */
In2iGui.Alert.create = function(options) {
	options = n2i.override({title:'',text:'',emotion:null,title:null},options);
	
	var element = options.element = new Element('div',{'class':'in2igui_alert'});
	var body = new Element('div',{'class':'in2igui_alert_body'});
	element.insert(body);
	var content = new Element('div',{'class':'in2igui_alert_content'});
	body.insert(content);
	document.body.appendChild(element);
	var obj = new In2iGui.Alert(options);
	if (options.emotion) {
		obj.setEmotion(options.emotion);
	}
	if (options.title) {
		obj.setTitle(options.title);
	}
	if (options.text) {
		obj.setText(options.text);
	}
	
	return obj;
}

In2iGui.Alert.prototype = {
	/** Shows the alert */
	show : function() {
		var zIndex = In2iGui.nextAlertIndex();
		if (this.options.modal) {
			In2iGui.showCurtain(this,zIndex);
			zIndex++;
		}
		this.element.style.zIndex=zIndex;
		this.element.style.display='block';
		this.element.style.top=(n2i.getScrollTop()+100)+'px';
		n2i.animate(this.element,'opacity',1,200);
		n2i.animate(this.element,'margin-top','40px',600,{ease:n2i.ease.elastic});
	},
	/** Hides the alert */
	hide : function() {
		n2i.animate(this.element,'opacity',0,200,{hideOnComplete:true});
		n2i.animate(this.element,'margin-top','0px',200);
		In2iGui.hideCurtain(this);
	},
	/** Sets the alert title */
	setTitle : function(/**String*/ text) {
		if (!this.title) {
			this.title = new Element('h1');
			this.content.appendChild(this.title);
		}
		this.title.innerHTML = text;
	},
	/** Sets the alert text */
	setText : function(/**String*/ text) {
		if (!this.text) {
			this.text = new Element('p');
			this.content.appendChild(this.text);
		}
		this.text.innerHTML = text || '';
	},
	/** Sets the alert emotion */
	setEmotion : function(/**String*/ emotion) {
		if (this.emotion) {
			this.body.removeClassName(this.emotion);
		}
		this.emotion = emotion;
		this.body.addClassName(emotion);
	},
	/** Updates multiple properties
	 * @param {Object} options {title: «String», text: «String», emotion: «'smile' | 'gasp'»}
	 */
	update : function(options) {
		options = options || {};
		this.setTitle(options.title || null);
		this.setText(options.text || null);
		this.setEmotion(options.emotion || null);
	},
	/** Adds an In2iGui.Button to the alert */
	addButton : function(button) {
		if (!this.buttons) {
			this.buttons = In2iGui.Buttons.create({align:'right'});
			this.body.appendChild(this.buttons.element);
		}
		this.buttons.add(button);
	}
}

/* EOF *//**
 * @constructor
 * A button
 */
In2iGui.Button = function(options) {
	this.options = options;
	this.name = options.name;
	this.element = $(options.element);
	this.enabled = !this.element.hasClassName('in2igui_button_disabled');
	In2iGui.extend(this);
	this.addBehavior();
}

/**
 * Creates a new button
 */
In2iGui.Button.create = function(o) {
	var o = n2i.override({text:'',highlighted:false,enabled:true},o);
	var className = 'in2igui_button'+(o.highlighted ? ' in2igui_button_highlighted' : '');
	if (o.small && o.rounded) {
		className+=' in2igui_button_small_rounded';
	}
	if (!o.enabled) {
		className+=' in2igui_button_disabled';
	}
	var element = o.element = new Element('a',{'class':className,href:'#'});
	var element2 = new Element('span');
	element.appendChild(element2);
	var element3 = new Element('span');
	element2.appendChild(element3);
	if (o.icon) {
		var icon = new Element('em',{'class':'in2igui_button_icon'}).setStyle({'backgroundImage':'url('+In2iGui.getIconUrl(o.icon,1)+')'});
		if (!o.text || o.text.length==0) {
			icon.addClassName('in2igui_button_icon_notext');
		}
		element3.insert(icon);
	}
	if (o.text && o.text.length>0) {
		element3.insert(o.text);
	}
	if (o.title && o.title.length>0) {
		element3.insert(o.title);
	}
	return new In2iGui.Button(o);
}

In2iGui.Button.prototype = {
	/** @private */
	addBehavior : function() {
		var self = this;
		this.element.onclick = function() {
			self.clicked();
			return false;
		}
	},
	/** @private */
	clicked : function() {
		if (this.enabled) {
			this.fire('click');
			if (this.options.submit) {
				var form = In2iGui.get().getAncestor(this,'in2igui_formula');
				if (form) {form.submit();}
			}
		} else {
			this.element.blur();
		}
	},
	/** Enables or disables the button */
	setEnabled : function(enabled) {
		this.enabled = enabled;
		this.updateUI();
	},
	/** Sets whether the button is highlighted */
	setHighlighted : function(highlighted) {
		this.element.setClassName('in2igui_button_highlighted',highlighted);
	},
	/** @private */
	updateUI : function() {
		this.element.setClassName('in2igui_button_disabled',!this.enabled);
	},
	/** Sets the button text */
	setText : function(text) {
		this.element.getElementsByTagName('span')[1].innerHTML = text;
	}
}

////////////////////////////////// Buttons /////////////////////////////

/** @constructor */
In2iGui.Buttons = function(o) {
	this.name = o.name;
	this.element = $(o.element);
	this.body = this.element.select('.in2igui_buttons_body')[0];
	In2iGui.extend(this);
}

In2iGui.Buttons.create = function(o) {
	o = n2i.override({top:0},o);
	var e = o.element = new Element('div',{'class':'in2igui_buttons'});
	if (o.align=='right') {
		e.addClassName('in2igui_buttons_right');
	}
	if (o.align=='center') {
		e.addClassName('in2igui_buttons_center');
	}
	if (o.top>0) e.setStyle({paddingTop:o.top+'px'});
	e.insert(new Element('div',{'class':'in2igui_buttons_body'}));
	return new In2iGui.Buttons(o);
}

In2iGui.Buttons.prototype = {
	add : function(widget) {
		this.body.insert(widget.element);
		return this;
	}
}

/* EOF *//**
 * @constructor
 * @param {Object} options The options : {value:null}
 */
In2iGui.Selection = function(options) {
	this.options = n2i.override({value:null},options);
	this.element = $(options.element);
	this.name = options.name;
	this.items = [];
	this.subItems = [];
	this.selection=null;
	if (this.options.value!=null) {
		this.selection = {value:this.options.value};
	}
	In2iGui.extend(this);
}

/**
 * Creates a new selection widget
 * @param {Object} options The options : {width:0}
 */
In2iGui.Selection.create = function(options) {
	options = n2i.override({width:0},options);
	var e = options.element = new Element('div',{'class':'in2igui_selection'});
	if (options.width>0) e.setStyle({width:options.width+'px'});
	return new In2iGui.Selection(options);
}

In2iGui.Selection.prototype = {
	/** Get the selected item
	 * @returns {Object} The selected item, null if no selection */
	getValue : function() {
		return this.selection;
	},
	/** Set the selected item
	 * @param {Object} value The selected item */
	setValue : function(value) {
		var item = this.getSelectionWithValue(value);
		if (item===null) {
			this.selection = null;
		} else {
			this.selection = item;
			this.kind=item.kind;
		}
		this.updateUI();
		this.fireChange();
	},
	/** @private */
	getSelectionWithValue : function(value) {
		for (var i=0; i < this.items.length; i++) {
			if (this.items[i].value==value) {
				return this.items[i];
			}
		};
		for (var i=0; i < this.subItems.length; i++) {
			var items = this.subItems[i].items;
			for (var j=0; j < items.length; j++) {
				if (items[j].value==value) {
					return items[j];
				}
			};
		};
		return null;
	},
	/** Set the value to null */
	reset : function() {
		this.setValue(null);
	},
	/** @private */
	updateUI : function() {
		this.items.each(function(item) {
			item.element.setClassName('in2igui_selected',this.isSelection(item));
		}.bind(this));
		this.subItems.each(function(sub) {
			sub.updateUI();
		});
	},
	/** @private */
	changeSelection : function(item) {
		this.subItems.each(function(sub) {
			sub.selectionChanged(this.selection,item);
		});
		this.selection = item;
		this.updateUI();
		this.fireChange();
	},
	/** @private */
	fireChange : function() {
		this.fire('selectionChanged',this.selection);
		this.fireProperty('value',this.selection ? this.selection.value : null);
		this.fireProperty('kind',this.selection ? this.selection.kind : null);
	},
	/** @private */
	registerItems : function(items) {
		items.parent = this;
		this.subItems.push(items);
	},
	/** @private */
	registerItem : function(id,title,icon,badge,value,kind) {
		var element = $(id);
		var item = {id:id,title:title,icon:icon,badge:badge,element:element,value:value,kind:kind};
		this.items.push(item);
		this.addItemBehavior(element,item);
		this.selection = this.getSelectionWithValue(this.options.value);
	},
	/** @private */
	addItemBehavior : function(node,item) {
		node.observe('click',function() {
			this.itemWasClicked(item);
		}.bind(this));
		node.observe('dblclick',function() {
			this.itemWasDoubleClicked(item);
		}.bind(this));
		node.dragDropInfo = item;
	},
	/** Untested!! */
	setObjects : function(items) {
		this.items = [];
		items.each(function(item) {
			this.items.push(item);
			var node = new Element('div',{'class':'in2igui_selection_item'});
			item.element = node;
			this.element.insert(node);
			var inner = new Element('span',{'class':'in2igui_selection_label'}).update(item.title);
			if (item.icon) {
				node.insert(In2iGui.createIcon(item.icon,1));
			}
			node.insert(inner);
			node.observe('click',function() {
				this.itemWasClicked(item);
			}.bind(this));
			node.observe('dblclick',function(e) {
				this.itemWasDoubleClicked(item);
				e.stop();
			}.bind(this));
		}.bind(this));
	},
	/** @private */
	isSelection : function(item) {
		if (this.selection===null) {
			return false;
		}
		var selected = item.value==this.selection.value;
		if (this.selection.kind) {
			selected = selected && item.kind==this.selection.kind;
		}
		return selected;
	},
	
	/** @private */
	itemWasClicked : function(item) {
		this.changeSelection(item);
	},
	/** @private */
	itemWasDoubleClicked : function(item) {
		this.fire('selectionWasOpened',item);
	}
}

/////////////////////////// Items ///////////////////////////

/**
 * @constructor
 * A group of items loaded from a source
 * @param {Object} options The options : {element,name,source}
 */
In2iGui.Selection.Items = function(options) {
	this.options = n2i.override({source:null},options);
	this.element = $(options.element);
	this.name = options.name;
	this.parent = null;
	this.items = [];
	In2iGui.extend(this);
	if (this.options.source) {
		this.options.source.addDelegate(this);
	}
}

In2iGui.Selection.Items.prototype = {
	/**
	 * Refresh the underlying source
	 */
	refresh : function() {
		if (this.options.source) {
			this.options.source.refresh();
		}
	},
	/** @private */
	$objectsLoaded : function(objects) {
		this.$itemsLoaded(objects);
	},
	/** @private */
	$itemsLoaded : function(items) {
		this.items = [];
		this.element.update();
		this.buildLevel(this.element,items,0);
		this.parent.updateUI();
	},
	/** @private */
	buildLevel : function(parent,items,inc) {
		if (!items) return;
		var hierarchical = this.isHierarchy(items);
		var open = inc==0;
		var level = new Element('div',{'class':'in2igui_selection_level'}).setStyle({display:open ? 'block' : 'none'});
		parent.insert(level);
		items.each(function(item) {
			var hasChildren = item.children && item.children.length>0;
			var left = inc*16+6;
			if (!hierarchical && inc>0 || hierarchical && !hasChildren) left+=13;
			var node = new Element('div',{'class':'in2igui_selection_item'}).setStyle({paddingLeft:left+'px'})
			if (item.badge) {
				node.insert(new Element('strong',{'class':'in2igui_selection_badge'}).update(item.badge));
			}
			if (hierarchical && hasChildren) {
				var self = this;
				node.insert(new Element('span',{'class':'in2igui_disclosure'}).observe('click',function(e) {
					e.stop();
					self.toggle(this);
				}));
			}
			var inner = new Element('span',{'class':'in2igui_selection_label'});
			if (item.icon) {
				node.insert(new Element('span',{'class':'in2igui_icon_1'}).setStyle({'backgroundImage' : 'url('+In2iGui.getIconUrl(item.icon,1)+')'}));
			}
			node.insert(inner.insert(item.title));
			node.observe('click',function(e) {
				this.parent.itemWasClicked(item);
			}.bind(this));
			node.observe('dblclick',function(e) {
				this.parent.itemWasDoubleClicked(item);
			}.bind(this));
			level.insert(node);
			var info = {title:item.title,icon:item.icon,badge:item.badge,kind:item.kind,element:node,value:item.value};
			node.dragDropInfo = info;
			this.items.push(info);
			this.buildLevel(level,item.children,inc+1);
		}.bind(this));
	},
	/** @private */
	toggle : function(node) {
		if (node.hasClassName('in2igui_disclosure_open')) {
			node.parentNode.next().hide();
			node.removeClassName('in2igui_disclosure_open');
		} else {
			node.parentNode.next().show();
			node.addClassName('in2igui_disclosure_open');
		}
	},
	/** @private */
	isHierarchy : function(items) {
		if (!items) {return false};
		for (var i=0; i < items.length; i++) {
			if (items[i]!==null && items[i].children && items[i].children.length>0) {
				return true;
			}
		};
		return false;
	},
	/** Get the selection of this items group
	 * @returns {Object} The selected item or null */
	getValue : function() {
		if (this.parent.selection==null) {
			return null;
		}
		for (var i=0; i < this.items.length; i++) {
			if (this.items[i].value == this.parent.selection.value) {
				return this.items[i];
			}
		};
		return null;
	},
	/** @private */
	updateUI : function() {
		this.items.each(function(item) {
			item.element.setClassName('in2igui_selected',this.parent.isSelection(item));
		}.bind(this));
	},
	/** @private */
	selectionChanged : function(oldSelection,newSelection) {
		for (var i=0; i < this.items.length; i++) {
			var value = this.items[i].value;
			if (value == newSelection.value) {
				this.fireProperty('value',newSelection.value);
				return;
			}
		};
		this.fireProperty('value',null);
	}
}
/* EOF */
/** @constructor */
In2iGui.Toolbar = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.Toolbar.create = function(options) {
	options = options || {};
	options.element = new Element('div',{'class':'in2igui_toolbar'});
	if (options.labels==false) {
		options.element.addClassName('in2igui_toolbar_nolabels');
	}
	return new In2iGui.Toolbar(options);
}

In2iGui.Toolbar.prototype = {
	add : function(widget) {
		this.element.appendChild(widget.getElement());
	},
	addDivider : function() {
		this.element.appendChild(new Element('span',{'class':'in2igui_divider'}));
	}
}



/////////////////////// Revealing toolbar ////////////////////////

/** @constructor */
In2iGui.RevealingToolbar = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.RevealingToolbar.create = function(options) {
	options = options || {};
	options.element = new Element('div',{'class':'in2igui_revealing_toolbar'}).setStyle({'display':'none'});
	document.body.appendChild(options.element);
	var rev = new In2iGui.RevealingToolbar(options);
	var toolbar = In2iGui.Toolbar.create();
	rev.setToolbar(toolbar);
	return rev;
}

In2iGui.RevealingToolbar.prototype = {
	setToolbar : function(widget) {
		this.toolbar = widget;
		this.element.appendChild(widget.getElement());
	},
	getToolbar : function() {
		return this.toolbar;
	},
	show : function(instantly) {
		this.element.style.display='';
		n2i.ani(this.element,'height','58px',instantly ? 0 : 600,{ease:n2i.ease.slowFastSlow});
	},
	hide : function() {
		n2i.ani(this.element,'height','0px',500,{ease:n2i.ease.slowFastSlow,hideOnComplete:true});
	}
}



/////////////////////// Icon ///////////////////

/** @constructor */
In2iGui.Toolbar.Icon = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	this.enabled = !this.element.hasClassName('in2igui_toolbar_icon_disabled');
	this.icon = this.element.select('.in2igui_icon')[0];
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Toolbar.Icon.create = function(options) {
	var element = options.element = new Element('a',{'class':'in2igui_toolbar_icon'});
	var icon = new Element('span',{'class':'in2igui_icon'}).setStyle({'backgroundImage':'url('+In2iGui.getIconUrl(options.icon,2)+')'});
	var inner = new Element('span',{'class':'in2igui_toolbar_inner_icon'});
	var innerest = new Element('span',{'class':'in2igui_toolbar_inner_icon'});
	element.insert(inner);
	inner.insert(innerest);
	var title = new Element('strong');
	title.innerHTML=options.title;
	if (options.overlay) {
		var overlay = new Element('span',{'class':'in2igui_icon_overlay'}).setStyle({'backgroundImage':'url('+In2iGui.getIconUrl('overlay/'+options.overlay,2)+')'});
		icon.insert(overlay);
	}
	innerest.insert(icon);
	innerest.insert(title);
	return new In2iGui.Toolbar.Icon(options);
}

In2iGui.Toolbar.Icon.prototype = {
	/** @private */
	addBehavior : function() {
		var self = this;
		this.element.onclick = function() {
			self.wasClicked();
		}
	},
	/** Sets wether the icon should be enabled */
	setEnabled : function(enabled) {
		this.enabled = enabled;
		this.element.setClassName('in2igui_toolbar_icon_disabled',!this.enabled);
	},
	/** Sets wether the icon should be selected */
	setSelected : function(selected) {
		this.element.setClassName('in2igui_toolbar_icon_selected',selected);
	},
	/** @private */
	wasClicked : function() {
		if (this.enabled) {
			In2iGui.callDelegates(this,'toolbarIconWasClicked');
			In2iGui.callDelegates(this,'click');
		}
	}
}


/////////////////////// Search field ///////////////////////

/** @constructor */
In2iGui.Toolbar.SearchField = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.name = options.name;
	this.field = this.element.select('input')[0];
	this.value = this.field.value;
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Toolbar.SearchField.create = function(options) {
	options = options || {};
	var e = options.element = new Element('div',{'class': options.adaptive ? 'in2igui_toolbar_search in2igui_toolbar_search_adaptive' : 'in2igui_toolbar_search'});
	e.update('<div class="in2igui_searchfield"><strong class="in2igui_searchfield_button"></strong><div><div><input type="text"/></div></div></div>'+
		'<span>'+options.title+'</span>');
	return new In2iGui.Toolbar.SearchField(options);
}

In2iGui.Toolbar.SearchField.prototype = {
	getValue : function() {
		return this.field.value;
	},
	addBehavior : function() {
		var self = this;
		this.field.onkeyup = function() {
			self.fieldChanged();
		}
		if (!this.options.adaptive) {
			this.field.onfocus = function() {
				n2i.ani(this,'width','120px',500,{ease:n2i.ease.slowFastSlow});
			}
			this.field.onblur = function() {
				n2i.ani(this,'width','80px',500,{ease:n2i.ease.slowFastSlow});
			}
		}
	},
	fieldChanged : function() {
		if (this.field.value!=this.value) {
			this.value=this.field.value;
			In2iGui.callDelegates(this,'valueChanged');
			In2iGui.firePropertyChange(this,'value',this.value);
		}
	}
}


//////////////////////// Badge ///////////////////////

/** @constructor */
In2iGui.Toolbar.Badge = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	this.label = this.element.select('strong')[0];
	this.text = this.element.select('span')[0];
	In2iGui.extend(this);
}

In2iGui.Toolbar.Badge.prototype = {
	setLabel : function(str) {
		this.label.update(str);
	},
	setText : function(str) {
		this.text.update(str);
	}
}

/* EOF *//**
	Used to choose an image
	@constructor
*/
In2iGui.ImagePicker = function(o) {
	this.name = o.name;
	this.options = o || {};
	this.element = $(o.element);
	this.images = [];
	this.object = null;
	this.thumbnailsLoaded = false;
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.ImagePicker.prototype = {
	/** @private */
	addBehavior : function() {
		this.element.onclick = this.showPicker.bind(this);
	},
	setObject : function(obj) {
		this.object = obj;
		this.updateUI();
	},
	getObject : function() {
		return this.object;
	},
	reset : function() {
		this.object = null;
		this.updateUI();
	},
	/** @private */
	updateUI : function() {
		if (this.object==null) {
			this.element.style.backgroundImage='';
		} else {
			var url = In2iGui.resolveImageUrl(this,this.object,48,48);
			this.element.style.backgroundImage='url('+url+')';
		}
	},
	/** @private */
	showPicker : function() {
		if (!this.picker) {
			var self = this;
			this.picker = In2iGui.BoundPanel.create();
			this.content = new Element('div',{'class':'in2igui_imagepicker_thumbs'});
			var buttons = In2iGui.Buttons.create({align:'right'});
			var close = In2iGui.Button.create({text:'Luk',highlighted:true});
			close.listen({
				$click:function() {self.hidePicker()}
			});
			var remove = In2iGui.Button.create({text:'Fjern'});
			remove.listen({
				$click:function() {self.setObject(null);self.hidePicker()}
			});
			buttons.add(remove).add(close);
			this.picker.add(this.content);
			this.picker.add(buttons);
		}
		this.picker.position(this.element);
		this.picker.show();
		if (!this.thumbnailsLoaded) {
			this.updateImages();
			this.thumbnailsLoaded = true;
		}
	},
	/** @private */
	hidePicker : function() {
		this.picker.hide();
	},
	/** @private */
	updateImages : function() {
		var self = this;
		var delegate = {
			onSuccess:function(t) {
				self.parse(t.responseXML);
			}
		};
		new Ajax.Request(this.options.source,delegate);
	},
	/** @private */
	parse : function(doc) {
		this.content.update();
		var images = doc.getElementsByTagName('image');
		var self = this;
		for (var i=0; i < images.length; i++) {
			var id = images[i].getAttribute('id');
			var img = {id:images[i].getAttribute('id')};
			var url = In2iGui.resolveImageUrl(this,img,48,48);
			var thumb = new Element('div',{'class':'in2igui_imagepicker_thumbnail'}).setStyle({'backgroundImage':'url('+url+')'});
			thumb.in2iguiObject = {'id':id};
			thumb.onclick = function() {
				self.setObject(this.in2iguiObject);
				self.hidePicker();
			}
			this.content.appendChild(thumb);
		};
	}
}

/* EOF *//**
 * A bound panel is a panel that is shown at a certain place
 * @constructor
 * @param {Object} options { element: «Node | id», name: «String» }
 */
In2iGui.BoundPanel = function(options) {
	this.element = $(options.element);
	this.name = options.name;
	this.visible = false;
	this.content=this.element.select('.content')[0];
	this.arrow=this.element.select('.arrow')[0];
	In2iGui.extend(this);
}

/**
 * Creates a new bound panel
 * <br/><strong>options:</strong> { name: «String», top: «pixels», left: «pixels», padding: «pixels», width: «pixels» }
 * @param {Object} options The options
 */
In2iGui.BoundPanel.create = function(options) {
	var options = n2i.override({name:null, top:0, left:0, width:null, padding: null}, options);
	var element = options.element = new Element('div',
		{'class':'in2igui_boundpanel'}).setStyle({'display':'none','zIndex':In2iGui.nextPanelIndex(),'top':options.top+'px','left':options.left+'px'});
	
	var html = 
		'<div class="arrow"></div>'+
		'<div class="top"><div><div></div></div></div>'+
		'<div class="body"><div class="body"><div class="body"><div class="content" style="';
	if (options.width) {
		html+='width:'+options.width+'px;';
	}
	if (options.padding) {
		html+='padding:'+options.padding+'px;';
	}
	html+='"></div></div></div></div>'+
		'<div class="bottom"><div><div></div></div></div>';
	element.innerHTML=html;
	document.body.appendChild(element);
	return new In2iGui.BoundPanel(options);
}

/********************************* Public methods ***********************************/

In2iGui.BoundPanel.prototype = {
	/** Shows the panel */
	show : function() {
		if (!this.visible) {
			if (!n2i.browser.msie) {
				n2i.setOpacity(this.element,0);
			}
			if (this.relativePosition=='left') {
				this.element.style.marginLeft='30px';
			} else {
				this.element.style.marginLeft='-30px';
			}
			this.element.setStyle({
				visibility : 'hidden', display : 'block'
			})
			var width = this.element.clientWidth;
			this.element.setStyle({
				width : width+'px' , visibility : 'visible'
			});
			this.element.style.marginTop='0px';
			this.element.style.display='block';
			if (!n2i.browser.msie) {
				n2i.animate(this.element,'opacity',1,400,{ease:n2i.ease.fastSlow});
			}
			n2i.animate(this.element,'margin-left','0px',800,{ease:n2i.ease.bounce});
		}
		this.element.style.zIndex = In2iGui.nextPanelIndex();
		this.visible=true;
	},
	/** Hides the panel */
	hide : function() {
		if (n2i.browser.msie) {
			this.element.style.display='none';
		} else {
			n2i.animate(this.element,'opacity',0,300,{ease:n2i.ease.slowFast,hideOnComplete:true});
		}
		this.visible=false;
	},
	/**
	 * Adds a widget or element to the panel
	 * @param {Node | Widget} child The object to add
	 */
	add : function(child) {
		if (child.getElement) {
			this.content.insert(child.getElement());
		} else {
			this.content.insert(child);
		}
	},
	/**
	 * Adds som vertical space to the panel
	 * @param {pixels} height The height of the space in pixels
	 */
	addSpace : function(height) {
		this.add(new Element('div').setStyle({fontSize:'0px',height:height+'px'}));
	},
	/** @private */
	getDimensions : function() {
		if (this.element.style.display=='none') {
			this.element.style.visibility='hidden';
			this.element.style.display='block';
			var width = this.element.getWidth();
			var height = this.element.getHeight();
			this.element.style.display='none';
			this.element.style.visibility='';
		} else {
			var width = this.element.getWidth();
			var height = this.element.getHeight();
		}
		return {width:width,height:height};
	},
	/** Position the panel at a node
	 * @param {Node} node The node the panel should be positioned at 
	 */
	position : function(node) {
		node = $(node);
		var offset = node.cumulativeOffset();
		var scrollOffset = node.cumulativeScrollOffset();
		var dims = this.getDimensions();
		var winWidth = n2i.getInnerWidth();
		var nodeLeft = offset.left-scrollOffset.left+n2i.getScrollLeft();
		var nodeWidth = node.getWidth();
		var nodeHeight = node.getHeight();
		var nodeTop = offset.top-scrollOffset.top+n2i.getScrollTop();
		if ((nodeLeft+nodeWidth/2)/winWidth<.5) {
			this.relativePosition='left';
			var left = nodeLeft+nodeWidth+10;
			this.arrow.className='arrow arrow_left';
			var arrowLeft=-14;
		} else {
			this.relativePosition='right';
			var left = nodeLeft-dims.width-10;
			this.arrow.className='arrow arrow_right';
			var arrowLeft=dims.width-4;
		}
		var top = Math.max(0,nodeTop+(nodeHeight-dims.height)/2);
		this.arrow.style.marginTop = (dims.height-32)/2+Math.min(0,nodeTop+(nodeHeight-dims.height)/2)+'px';
		this.arrow.style.marginLeft = arrowLeft+'px';
		if (this.visible) {
			n2i.animate(this.element,'top',top+'px',500,{ease:n2i.ease.fastSlow});
			n2i.animate(this.element,'left',left+'px',500,{ease:n2i.ease.fastSlow});
		} else {
			this.element.style.top=top+'px';
			this.element.style.left=left+'px';
		}
	}
}

/* EOF *//**
 * @constructor
 */
In2iGui.RichText = function(options) {
	this.name = options.name;
	var e = this.element = $(options.element);
	this.options = n2i.override({debug:false,value:'',autoHideToolbar:true,style:'font-family: sans-serif;'},options);
	this.textarea = new Element('textarea');
	e.insert(this.textarea);
	this.editor = WysiHat.Editor.attach(this.textarea);
	this.editor.setAttribute('frameborder','0');
	/* @private */
	this.toolbar = e.select('.in2igui_richtext_toolbar')[0];
	this.toolbarContent = e.select('.in2igui_richtext_toolbar_content')[0];
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
	var input = $(options.input);
	input.style.display='none';
	options.value = input.value;
	var obj = In2iGui.RichText.create(options);
	input.parentNode.insertBefore(obj.element,input);
	obj.ignite();
}

In2iGui.RichText.create = function(options) {
	options = options || {};
	options.element = new Element('div',{'class':'in2igui_richtext'}).update(
		'<div class="in2igui_richtext_toolbar"><div class="in2igui_richtext_inner_toolbar"><div class="in2igui_richtext_toolbar_content"></div></div></div>'
	);
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
				this.toolbarContent.insert(new Element('div',{'class':'in2igui_richtext_divider'}));
			} else {
				var div = new Element('div').addClassName('action action_'+actions[i].key);
				div.title=actions[i].key;
				div.in2iguiRichTextAction = actions[i]
				div.onclick = div.ondblclick = function(e) {return self.actionWasClicked(this.in2iguiRichTextAction,e);}
				var img = new Element('img');
				img.src=In2iGui.context+'/In2iGui/gfx/trans.png';
				if (actions[i].icon) {
					div.setStyle({'backgroundImage':'url('+In2iGui.getIconUrl(actions[i].icon,1)+')'});
				}
				div.insert(img);
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
			$(this.options.input).value=this.value;
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
			picker.addDelegate(this);
			panel.add(picker);
			panel.show();
			this.colorPicker = panel;
		}
		this.colorPicker.show();
	},
	colorWasHovered : function(color) {
		//this.document.execCommand('forecolor',false,color);
	},
	colorWasSelected : function(color) {
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

/* EOF *//**
 @constructor
 */
In2iGui.ImageViewer = function(options) {
	this.options = n2i.override({
		maxWidth:800,maxHeight:600,perimeter:100,sizeSnap:100,
		margin:0,
		ease:n2i.ease.slowFastSlow,
		easeEnd:n2i.ease.bounce,
		easeAuto:n2i.ease.slowFastSlow,
		easeReturn:n2i.ease.cubicInOut,transition:400,transitionEnd:1000,transitionReturn:300
		},options);
	this.element = $(options.element);
	this.box = this.options.box;
	this.viewer = this.element.select('.in2igui_imageviewer_viewer')[0];
	this.innerViewer = this.element.select('.in2igui_imageviewer_inner_viewer')[0];
	this.status = this.element.select('.in2igui_imageviewer_status')[0];
	this.previousControl = this.element.select('.in2igui_imageviewer_previous')[0];
	this.controller = this.element.select('.in2igui_imageviewer_controller')[0];
	this.nextControl = this.element.select('.in2igui_imageviewer_next')[0];
	this.playControl = this.element.select('.in2igui_imageviewer_play')[0];
	this.closeControl = this.element.select('.in2igui_imageviewer_close')[0];
	this.text = this.element.select('.in2igui_imageviewer_text')[0];
	this.dirty = false;
	this.width = 600;
	this.height = 460;
	this.index = 0;
	this.playing=false;
	this.name = options.name;
	this.images = [];
	this.box.listen(this);
	this.addBehavior();
	In2iGui.extend(this);
}

In2iGui.ImageViewer.create = function(options) {
	options = options || {};
	var element = options.element = new Element('div',{'class':'in2igui_imageviewer'});
	element.update('<div class="in2igui_imageviewer_viewer"><div class="in2igui_imageviewer_inner_viewer"></div></div>'+
	'<div class="in2igui_imageviewer_text"></div>'+
	'<div class="in2igui_imageviewer_status"></div>'+
	'<div class="in2igui_imageviewer_controller"><div><div>'+
	'<a class="in2igui_imageviewer_previous"></a>'+
	'<a class="in2igui_imageviewer_play"></a>'+
	'<a class="in2igui_imageviewer_next"></a>'+
	'<a class="in2igui_imageviewer_close"></a>'+
	'</div></div></div>');
	var box = In2iGui.Box.create({absolute:true,modal:true,closable:true});
	box.add(element);
	box.addToDocument();
	options.box=box;
	return new In2iGui.ImageViewer(options);
}

In2iGui.ImageViewer.prototype = {
	/** @private */
	addBehavior : function() {
		var self = this;
		this.nextControl.onclick = function() {
			self.next(true);
		}
		this.previousControl.onclick = function() {
			self.previous(true);
		}
		this.playControl.onclick = function() {
			self.playOrPause();
		}
		this.closeControl.onclick = this.hide.bind(this);
		this.viewer.observe('click',this.zoom.bind(this));
		this.timer = function() {
			self.next(false);
		}
		this.keyListener = function(e) {
			if (n2i.isRightKey(e)) {
				self.next(true);
			} else if (n2i.isLeftKey(e)) {
				self.previous(true);
			} else if (n2i.isEscapeKey(e)) {
				self.hide();
			} else if (n2i.isReturnKey(e)) {
				self.playOrPause();
			}
		},
		this.viewer.observe('mousemove',this.mouseMoveEvent.bind(this));
		this.controller.observe('mouseenter',function() {
			self.overController = true;
		});
		this.controller.observe('mouseleave',function() {
			self.overController = false;
		});
		this.viewer.observe('mouseout',function(e) {
			if (!In2iGui.isWithin(e,this.viewer)) {
				self.hideController();
			}
		}.bind(this));
	},
	/** @private */
	mouseMoveEvent : function() {
		window.clearTimeout(this.ctrlHider);
		if (this.shouldShowController()) {
			this.ctrlHider = window.setTimeout(this.hideController.bind(this),2000);
			if (n2i.browser.msie) {
				this.controller.show();
			} else {
				In2iGui.fadeIn(this.controller,200);
			}
		}
	},
	/** @private */
	hideController : function() {
		if (!this.overController) {
			if (n2i.browser.msie) {
				this.controller.hide();
			} else {
				In2iGui.fadeOut(this.controller,500);
			}
		}
	},
	/** @private */
	zoom : function(e) {
		var img = this.images[this.index];
		if (img.width<=this.width && img.height<=this.height) {
			return; // Don't zoom if small
		}
		if (!this.zoomer) {
			this.zoomer = new Element('div',{'class':'in2igui_imageviewer_zoomer'}).setStyle({width:this.viewer.clientWidth+'px',height:this.viewer.clientHeight+'px'});
			this.element.insert({top:this.zoomer});
			this.zoomer.observe('mousemove',this.zoomMove.bind(this));
			this.zoomer.observe('click',function() {
				this.hide();
			});
		}
		this.pause();
		var size = this.getLargestSize({width:2000,height:2000},img);
		var url = In2iGui.resolveImageUrl(this,img,size.width,size.height);
		this.zoomer.update('<div style="width:'+size.width+'px;height:'+size.height+'px;"><img src="'+url+'"/></div>').show();
		this.zoomInfo = {width:size.width,height:size.height};
		this.zoomMove(e);
	},
	zoomMove : function(e) {
		if (!this.zoomInfo) {
			return;
		}
		var offset = this.zoomer.cumulativeOffset();
		var x = (e.pointerX()-offset.left)/this.zoomer.clientWidth*(this.zoomInfo.width-this.zoomer.clientWidth);
		var y = (e.pointerY()-offset.top)/this.zoomer.clientHeight*(this.zoomInfo.height-this.zoomer.clientHeight);
		this.zoomer.scrollLeft = x;
		this.zoomer.scrollTop = y;
	},
	/** @private */
	getLargestSize : function(canvas,image) {
		if (image.width<=canvas.width && image.height<=canvas.height) {
			return {width:image.width,height:image.height};
		} else if (canvas.width/canvas.height>image.width/image.height) {
			return {width:Math.round(canvas.height/image.height*image.width),height:canvas.height};
		} else if (canvas.width/canvas.height<image.width/image.height) {
			return {width:canvas.width,height:Math.round(canvas.width/image.width*image.height)};
		} else {
			return {width:canvas.width,height:canvas.height};
		}
	},
	/** @private */
	calculateSize : function() {
		var snap = this.options.sizeSnap;
		var newWidth = n2i.getInnerWidth()-this.options.perimeter;
		newWidth = Math.floor(newWidth/snap)*snap;
		newWidth = Math.min(newWidth,this.options.maxWidth);
		var newHeight = n2i.getInnerHeight()-this.options.perimeter;
		newHeight = Math.floor(newHeight/snap)*snap;
		newHeight = Math.min(newHeight,this.options.maxHeight);
		var maxWidth = 0;
		var maxHeight = 0;
		for (var i=0; i < this.images.length; i++) {
			var dims = this.getLargestSize({width:newWidth,height:newHeight},this.images[i]);
			maxWidth = Math.max(maxWidth,dims.width);
			maxHeight = Math.max(maxHeight,dims.height);
		};
		newHeight = Math.floor(Math.min(newHeight,maxHeight));
		newWidth = Math.floor(Math.min(newWidth,maxWidth));
		
		if (newWidth!=this.width || newHeight!=this.height) {
			this.width = newWidth;
			this.height = newHeight;
			this.dirty = true;
		}
	},
	adjustSize : function() {
		
	},
	showById: function(id) {
		for (var i=0; i < this.images.length; i++) {
			if (this.images[i].id==id) {
				this.show(i);
				break;
			}
		};
	},
	show: function(index) {
		this.index = index || 0;
		this.calculateSize();
		this.updateUI();
		var margin = this.options.margin;
		this.element.setStyle({width:(this.width+margin)+'px',height:(this.height+margin*2-1)+'px'});
		this.viewer.setStyle({width:(this.width+margin)+'px',height:(this.height-1)+'px'});
		this.innerViewer.setStyle({width:((this.width+margin)*this.images.length)+'px',height:(this.height-1)+'px'});
		this.controller.setStyle({marginLeft:((this.width-180)/2+margin*0.5)+'px',display:'none'});
		this.box.show();
		this.goToImage(false,0,false);
		Event.observe(document,'keydown',this.keyListener);
	},
	hide: function(index) {
		this.pause();
		this.box.hide();
		Event.stopObserving(document,'keydown',this.keyListener);
	},
	/** @private */
	$boxCurtainWasClicked : function() {
		this.hide();
	},
	/** @private */
	$boxWasClosed : function() {
		this.hide();
	},
	/** @private */
	updateUI : function() {
		if (this.dirty) {
			this.innerViewer.innerHTML='';
			for (var i=0; i < this.images.length; i++) {
				var element = new Element('div',{'class':'in2igui_imageviewer_image'}).setStyle({'width':(this.width+this.options.margin)+'px','height':(this.height-1)+'px'});
				this.innerViewer.appendChild(element);
			};
			if (this.shouldShowController()) {
				this.controller.show();
			} else {
				this.controller.hide();
			}
			this.dirty = false;
			this.preload();
		}
	},
	/** @private */
	shouldShowController : function() {
		return this.images.length>1;
	},
	/** @private */
	goToImage : function(animate,num,user) {	
		if (animate) {
			if (num>1) {
				n2i.ani(this.viewer,'scrollLeft',this.index*(this.width+this.options.margin),Math.min(num*this.options.transitionReturn,2000),{ease:this.options.easeReturn});				
			} else {
				var end = this.index==0 || this.index==this.images.length-1;
				var ease = (end ? this.options.easeEnd : this.options.ease);
				if (!user) {
					ease = this.options.easeAuto;
				}
				n2i.ani(this.viewer,'scrollLeft',this.index*(this.width+this.options.margin),(end ? this.options.transitionEnd : this.options.transition),{ease:ease});
			}
		} else {
			this.viewer.scrollLeft=this.index*(this.width+this.options.margin);
		}
		var text = this.images[this.index].text;
		if (text) {
			this.text.update(text).show();			
		} else {
			this.text.update().hide();
		}
	},
	clearImages : function() {
		this.images = [];
		this.dirty = true;
	},
	addImages : function(images) {
		for (var i=0; i < images.length; i++) {
			this.addImage(images[i]);
		};
	},
	addImage : function(img) {
		this.images.push(img);
		this.dirty = true;
	},
	play : function() {
		if (!this.interval) {
			this.interval = window.setInterval(this.timer,6000);
		}
		this.next(false);
		this.playing=true;
		this.playControl.className='in2igui_imageviewer_pause';
	},
	pause : function() {
		window.clearInterval(this.interval);
		this.interval = null;
		this.playControl.className='in2igui_imageviewer_play';
		this.playing = false;
	},
	playOrPause : function() {
		if (this.playing) {
			this.pause();
		} else {
			this.play();
		}
	},
	resetPlay : function() {
		if (this.playing) {
			window.clearInterval(this.interval);
			this.interval = window.setInterval(this.timer,6000);
		}
	},
	previous : function(user) {
		var num = 1;
		this.index--;
		if (this.index<0) {
			this.index=this.images.length-1;
			num = this.images.length-1;
		}
		this.goToImage(true,num,user);
		this.resetPlay();
	},
	next : function(user) {
		var num = 1;
		this.index++;
		if (this.index==this.images.length) {
			this.index=0;
			num = this.images.length-1;
		}
		this.goToImage(true,num,user);
		this.resetPlay();
	},
	/** @private */
	preload : function() {
		var guiLoader = new n2i.Preloader();
		guiLoader.addImages(In2iGui.context+'In2iGui/gfx/imageviewer_controls.png');
		var self = this;
		guiLoader.setDelegate({allImagesDidLoad:function() {self.preloadImages()}});
		guiLoader.load();
	},
	/** @private */
	preloadImages : function() {
		var loader = new n2i.Preloader();
		loader.setDelegate(this);
		for (var i=0; i < this.images.length; i++) {
			var url = In2iGui.resolveImageUrl(this,this.images[i],this.width,this.height);
			if (url!==null) {
				loader.addImages(url);
			}
		};
		this.status.innerHTML = '0%';
		this.status.style.display='';
		loader.load(this.index);
	},
	/** @private */
	allImagesDidLoad : function() {
		this.status.style.display='none';
	},
	/** @private */
	imageDidLoad : function(loaded,total,index) {
		this.status.innerHTML = Math.round(loaded/total*100)+'%';
		var url = In2iGui.resolveImageUrl(this,this.images[index],this.width,this.height);
		url = url.replace(/&amp;/g,'&');
		this.innerViewer.childNodes[index].style.backgroundImage="url('"+url+"')";
		Element.setClassName(this.innerViewer.childNodes[index],'in2igui_imageviewer_image_abort',false);
		Element.setClassName(this.innerViewer.childNodes[index],'in2igui_imageviewer_image_error',false);
	},
	/** @private */
	imageDidGiveError : function(loaded,total,index) {
		Element.setClassName(this.innerViewer.childNodes[index],'in2igui_imageviewer_image_error',true);
	},
	/** @private */
	imageDidAbort : function(loaded,total,index) {
		Element.setClassName(this.innerViewer.childNodes[index],'in2igui_imageviewer_image_abort',true);
	}
}

/* EOF *//** @constructor */
In2iGui.Picker = function(o) {
	o = this.options = n2i.override({itemWidth:100,itemHeight:150,itemsVisible:null,shadow:true,valueProperty:'value'},o);
	this.element = $(o.element);
	this.name = o.name;
	this.container = this.element.select('.in2igui_picker_container')[0];
	this.content = this.element.select('.in2igui_picker_content')[0];
	this.title = this.element.select('in2igui_picker_title')[0];
	this.objects = [];
	this.selected = null;
	this.value = null;
	this.addBehavior();
	In2iGui.extend(this);
}

In2iGui.Picker.create = function(o) {
	o = n2i.override({shadow:true},o);
	var element = new Element('div',{'class':'in2igui_picker'});
	element.update('<div class="in2igui_picker_top"><div><div></div></div></div>'+
	'<div class="in2igui_picker_middle"><div class="in2igui_picker_middle">'+
	(o.title ? '<div class="in2igui_picker_title">'+o.title+'</div>' : '')+
	'<div class="in2igui_picker_container"><div class="in2igui_picker_content"></div></div>'+
	'</div></div>'+
	'<div class="in2igui_picker_bottom"><div><div></div></div></div>');
	if (o.shadow==true) {
		element.addClassName('in2igui_picker_shadow')
	}
	o.element = element;
	return new In2iGui.Picker(o);
}

In2iGui.Picker.prototype = {
	addBehavior : function() {
		var self = this;
		this.content.observe('mousedown',function(e) {
			self.startDrag(e);
			return false;
		});
	},
	setObjects : function(objects) {
		this.selected = null;
		this.objects = objects || [];
		this.updateUI();
	},
	setValue : function(value) {
		this.value = value;
		this.updateSelection();
	},
	getValue : function() {
		return this.value;
	},
	reset : function() {
		this.value = null;
		this.updateSelection();
	},
	updateUI : function() {
		var self = this;
		this.content.update();
		this.container.scrollLeft=0;
		if (this.options.itemsVisible) {
			var width = this.options.itemsVisible*(this.options.itemWidth+14);
		} else {
			var width = this.container.clientWidth;
		}
		this.container.setStyle({width:width+'px',height:(this.options.itemHeight+10)+'px'});
		this.content.style.width=(this.objects.length*(this.options.itemWidth+14))+'px';
		this.content.style.height=(this.options.itemHeight+10)+'px';
		this.objects.each(function(object,i) {
			var item = new Element('div',{'class':'in2igui_picker_item',title:object.title});
			if (self.value!=null && object[self.options.valueProperty]==self.value) item.addClassName('in2igui_picker_item_selected');
			item.update(
				'<div class="in2igui_picker_item_middle"><div class="in2igui_picker_item_middle">'+
				'<div style="width:'+self.options.itemWidth+'px;height:'+self.options.itemHeight+'px;background-image:url(\''+object.image+'\')"></div>'+
				'</div></div>'+
				'<div class="in2igui_picker_item_bottom"><div><div></div></div></div>'
			);
			item.observe('mouseup',function() {self.selectionChanged(object[self.options.valueProperty])});
			self.content.insert(item);			
		});
	},
	updateSelection : function() {
		var children = this.content.childNodes;
		for (var i=0; i < children.length; i++) {
			Element.setClassName(children[i],'in2igui_picker_item_selected',this.value!=null && this.objects[i][this.options.valueProperty]==this.value);
		};
	},
	selectionChanged : function(value) {
		if (this.dragging) return;
		if (this.value==value) return;
		this.value = value;
		this.updateSelection();
		this.fire('selectionChanged',value);
	},
	
	// Dragging
	startDrag : function(e) {
		e.stop();
		var self = this;
		this.dragX = Event.pointerX(e);
		this.dragScroll = this.container.scrollLeft;
		In2iGui.Picker.mousemove = function(e) {self.drag(e);return false;}
		In2iGui.Picker.mouseup = In2iGui.Picker.mousedown = function(e) {self.endDrag(e);return false;}
		window.document.observe('mousemove',In2iGui.Picker.mousemove);
		window.document.observe('mouseup',In2iGui.Picker.mouseup);
		window.document.observe('mousedown',In2iGui.Picker.mouseup);
	},
	drag : function(e) {
		this.dragging = true;
		Event.stop(e);
		this.container.scrollLeft=this.dragX-e.pointerX()+this.dragScroll;
	},
	endDrag : function(e) {
		this.dragging = false;
		Event.stop(e);
		window.document.stopObserving('mousemove',In2iGui.Picker.mousemove);
		window.document.stopObserving('mouseup',In2iGui.Picker.mouseup);
		window.document.stopObserving('mousedown',In2iGui.Picker.mouseup);
		var size = (this.options.itemWidth+14);
		n2i.ani(this.container,'scrollLeft',Math.round(this.container.scrollLeft/size)*size,500,{ease:n2i.ease.bounceOut});
	},
	$visibilityChanged : function() {
		this.container.setStyle({display:'none'});
		if (this.options.itemsVisible) {
			var width = this.options.itemsVisible*(this.options.itemWidth+14);
		} else {
			var width = this.container.parentNode.clientWidth-12;
		}
		width = Math.max(width,0);
		this.container.setStyle({width:width+'px',display:'block'});
	}
}

/* EOF *//**
 * @constructor
 */
In2iGui.Editor = function() {
	this.name = 'In2iGuiEditor';
	this.parts = [];
	this.rows = [];
	this.partControllers = [];
	this.activePart = null;
	this.active = false;
	this.dragProxy = null;
	In2iGui.extend(this);
}

In2iGui.Editor.get = function() {
	if (!In2iGui.Editor.instance) {
		In2iGui.Editor.instance = new In2iGui.Editor();
	}
	return In2iGui.Editor.instance;
}

In2iGui.Editor.prototype = {
	ignite : function() {
		this.reload();
	},
	addPartController : function(key,title,controller) {
		this.partControllers.push({key:key,'title':title,'controller':controller});
	},
	getPartController : function(key) {
		var ctrl = null;
		this.partControllers.each(function(item) {
			if (item.key==key) ctrl=item;
		});
		return ctrl;
	},
	reload : function() {
		if (this.partControls) {
			this.partControls.hide();
		}
		var self = this;
		this.parts = [];
		var rows = $$('.row');
		rows.each(function(row,i) {
			var columns = row.select('.column');
			self.reloadColumns(columns,i);
			columns.each(function(column,j) {
				var parts = column.select('.part');
				self.reloadParts(parts,i,j);
			});
		});
		var parts = $$('.part');
		
		this.parts.each(function(part) {
			var i = parts.indexOf(part.element);
			if (i!=-1) delete(parts[i]);
		});
		this.reloadParts(parts,-1,-1);
	},
	partExists : function(element) {
		
	},
	reloadColumns : function(columns,rowIndex) {
		var self = this;
		columns.each(function(column,columnIndex) {
			column.observe('mouseover',function() {
				self.hoverColumn(column);
			});
			column.observe('mouseout',function() {
				self.blurColumn();
			});
			column.observe('contextmenu',function(e) {
				self.contextColumn(column,rowIndex,columnIndex,e);
			});
		});
	},
	reloadParts : function(parts,row,column) {
		var self = this;
		parts.each(function(element,partIndex) {
			if (!element) return;
			var match = element.className.match(/part_([\w]+)/i);
			if (match && match[1]) {
				var handler = self.getPartController(match[1]);
				if (handler) {
					var part = new handler.controller(element,row,column,partIndex);
					part.type=match[1];
					element.observe('click',function() {
						self.editPart(part);
					});
					element.observe('mouseover',function(e) {
						self.hoverPart(part);
					});
					element.observe('mouseout',function(e) {
						self.blurPart(e);
					});
					element.observe('mousedown',function(e) {
						self.startPartDrag(e);
					});
					self.parts.push(part);
				}
			}
		});
	},
	activate : function() {
		this.active = true;
	},
	deactivate : function() {
		this.active = false;
		if (this.activePart) {
			this.activePart.deactivate();
		}
		if (this.partControls) this.partControls.hide();
	},
	
	
	///////////////////////// Columns ////////////////////////
	
	hoverColumn : function(column) {
		this.hoveredColumn = column;
		if (!this.active || this.activePart) return;
		column.addClassName('in2igui_editor_column_hover');
	},
	
	blurColumn : function() {
		if (!this.active || !this.hoveredColumn) return;
		this.hoveredColumn.removeClassName('in2igui_editor_column_hover');
	},
	
	contextColumn : function(column,rowIndex,columnIndex,e) {
		if (!this.active || this.activePart) return;
		if (!this.columnMenu) {
			var menu = In2iGui.Menu.create({name:'In2iGuiEditorColumnMenu'});
			menu.addItem({title:'Rediger kolonne',value:'editColumn'});
			menu.addItem({title:'Slet kolonne',value:'removeColumn'});
			menu.addItem({title:'Tilføj kolonne',value:'addColumn'});
			menu.addDivider();
			this.partControllers.each(function(item) {
				menu.addItem({title:item.title,value:item.key});
			});
			this.columnMenu = menu;
			menu.addDelegate(this);
		}
		this.hoveredRow=rowIndex;
		this.hoveredColumnIndex=columnIndex;
		this.columnMenu.showAtPointer(e);
	},
	$itemWasClicked$In2iGuiEditorColumnMenu : function(value) {
		if (value=='removeColumn') {
			this.fire('removeColumn',{'row':this.hoveredRow,'column':this.hoveredColumnIndex});
		} else if (value=='editColumn') {
			this.editColumn(this.hoveredRow,this.hoveredColumnIndex);
		} else if (value=='addColumn') {
			this.fire('addColumn',{'row':this.hoveredRow,'position':this.hoveredColumnIndex+1});
		} else {
			this.fire('addPart',{'row':this.hoveredRow,'column':this.hoveredColumnIndex,'position':0,type:value});
		}
	},
	
	///////////////////// Column editor //////////////////////
	
	editColumn : function(rowIndex,columnIndex) {
		this.closeColumn();
		var c = this.activeColumn = $$('.row')[rowIndex].select('.column')[columnIndex];
		c.addClassName('in2igui_editor_column_edit');
		this.showColumnWindow();
		this.columnEditorForm.setValues({width:c.getStyle('width'),paddingLeft:c.getStyle('padding-left')});
	},
	closeColumn : function() {
		if (this.activeColumn) {
			this.activeColumn.removeClassName('in2igui_editor_column_edit');
		}
	},
	showColumnWindow : function() {
		if (!this.columnEditor) {
			var w = this.columnEditor = In2iGui.Window.create({name:'columnEditor',title:'Rediger kolonne',width:200});
			var f = this.columnEditorForm = In2iGui.Formula.create();
			var g = f.createGroup();
			var width = In2iGui.Formula.Text.create({label:'Bredde',key:'width'});
			width.addDelegate({$valueChanged:function(v) {this.changeColumnWidth(v)}.bind(this)})
			g.add(width);
			var marginLeft = In2iGui.Formula.Text.create({label:'Venstremargen',key:'left'});
			marginLeft.addDelegate({$valueChanged:function(v) {this.changeColumnLeftMargin(v)}.bind(this)})
			g.add(marginLeft);
			var marginRight = In2iGui.Formula.Text.create({label:'Højremargen',key:'right'});
			marginRight.addDelegate({$valueChanged:this.changeColumnRightMargin.bind(this)})
			g.add(marginRight);
			w.add(f);
			w.addDelegate(this);
		}
		this.columnEditor.show();
	},
	$userClosedWindow$columnEditor : function() {
		this.closeColumn();
		var values = this.columnEditorForm.getValues();
		values.row=this.hoveredRow;
		values.column=this.hoveredColumnIndex;
		this.fire('updateColumn',values);
	},
	changeColumnWidth : function(width) {
		this.activeColumn.setStyle({'width':width});
	},
	changeColumnLeftMargin : function(margin) {
		this.activeColumn.setStyle({'paddingLeft':margin});
	},
	changeColumnRightMargin : function(margin) {
		this.activeColumn.setStyle({'paddingRight':margin});
	},
	///////////////////////// Parts //////////////////////////
	
	hoverPart : function(part,event) {
		if (!this.active || this.activePart) return;
		this.hoveredPart = part;
		part.element.addClassName('in2igui_editor_part_hover');
		var self = this;
		this.partControlTimer = window.setTimeout(function() {self.showPartControls()},200);
	},
	blurPart : function(e) {
		window.clearTimeout(this.partControlTimer);
		if (!this.active) return;
		if (this.partControls && !In2iGui.isWithin(e,this.partControls.element)) {
			this.hidePartControls();
			this.hoveredPart.element.removeClassName('in2igui_editor_part_hover');
		}
		if (!this.partControls && this.hoveredPart) {
			this.hoveredPart.element.removeClassName('in2igui_editor_part_hover');			
		}
	},
	showPartEditControls : function() {
		if (!this.partEditControls) {
			this.partEditControls = In2iGui.Overlay.create({name:'In2iGuiEditorPartEditActions'});
			this.partEditControls.addIcon('save','common/save');
			this.partEditControls.addIcon('cancel','common/close');
			this.partEditControls.addDelegate(this);
		}
		this.partEditControls.showAtElement(this.activePart.element,{'horizontal':'right','vertical':'topOutside'});
	},
	showPartControls : function() {
		if (!this.partControls) {
			this.partControls = In2iGui.Overlay.create({name:'In2iGuiEditorPartActions'});
			this.partControls.addIcon('edit','common/edit');
			this.partControls.addIcon('new','common/new');
			this.partControls.addIcon('delete','common/delete');
			var self = this;
			this.partControls.getElement().observe('mouseout',function(e) {
				self.blurControls(e);
			});
			this.partControls.getElement().observe('mouseover',function(e) {
				self.hoverControls(e);
			});
			this.partControls.addDelegate(this);
		}
		if (this.hoveredPart.column==-1) {
			this.partControls.hideIcons(['new','delete']);
		} else {
			this.partControls.showIcons(['new','delete']);
		}
		this.partControls.showAtElement(this.hoveredPart.element,{'horizontal':'right'});
	},
	hoverControls : function(e) {
		this.hoveredPart.element.addClassName('in2igui_editor_part_hover');
	},
	blurControls : function(e) {
		this.hoveredPart.element.removeClassName('in2igui_editor_part_hover');
		if (!In2iGui.isWithin(e,this.hoveredPart.element)) {
			this.hidePartControls();
		}
	},
	$iconWasClicked$In2iGuiEditorPartActions : function(key,event) {
		if (key=='delete') {
			this.deletePart(this.hoveredPart);
		} else if (key=='new') {
			this.newPart(event);
		} else if (key=='edit') {
			this.editPart(this.hoveredPart);
		}
	},
	$iconWasClicked$In2iGuiEditorPartEditActions : function(key,event) {
		if (key=='cancel') {
			this.cancelPart(this.activePart);
		} else if (key=='save') {
			this.savePart(this.activePart);
		}
	},
	hidePartControls : function() {
		if (this.partControls) {
			this.partControls.hide();
		}
	},
	hidePartEditControls : function() {
		if (this.partEditControls) {
			this.partEditControls.hide();
		}
	},
	editPart : function(part) {
		if (!this.active || this.activePart) return;
		if (this.activePart) this.activePart.deactivate();
		if (this.hoveredPart) {
			this.hoveredPart.element.removeClassName('in2igui_editor_part_hover');
		}
		this.activePart = part;
		this.showPartEditControls();
		part.element.addClassName('in2igui_editor_part_active');
		part.activate();
		window.clearTimeout(this.partControlTimer);
		this.hidePartControls();
		this.blurColumn();
		this.showPartEditor();
	},
	showPartEditor : function() {
		 // TODO: Disabled!
		/*if (!this.partEditor) {
			var w = this.partEditor = In2iGui.Window.create({padding:5,title:'Afstande',close:false,variant:'dark',width: 200});
			var f = this.partEditorForm = In2iGui.Formula.create();
			f.buildGroup({above:false},[
				{type:'Text',options:{label:'Top',key:'top'}},
				{type:'Text',options:{label:'Bottom',key:'bottom'}},
				{type:'Text',options:{label:'Left',key:'left'}},
				{type:'Text',options:{label:'Right',key:'right'}}
			]);
			w.add(f);
			f.addDelegate({valuesChanged:this.updatePartProperties.bind(this)});
		}
		var e = this.activePart.element;
		this.partEditorForm.setValues({
			top: e.getStyle('marginTop'),
			bottom: e.getStyle('marginBottom'),
			left: e.getStyle('marginLeft'),
			right: e.getStyle('marginRight')
		});
		this.partEditor.show();*/
	},
	updatePartProperties : function(values) {
		this.activePart.element.setStyle({
			marginTop:values.top,
			marginBottom:values.bottom,
			marginLeft:values.left,
			marginRight:values.right
		});
		this.activePart.properties = values;
		n2i.log(values);
	},
	hidePartEditor : function() {
		if (this.partEditor) this.partEditor.hide();
	},
	cancelPart : function(part) {
		part.cancel();
		this.hidePartEditor();
	},
	savePart : function(part) {
		part.save();
		this.hidePartEditor();
	},
	getEditorForElement : function(element) {
		for (var i=0; i < this.parts.length; i++) {
			if (this.parts[i].element==element) {
				return this.parts[i];
			}
		};
		return null;
	},
	partDidDeacivate : function(part) {
		part.element.removeClassName('in2igui_editor_part_active');
		this.activePart = null;
		this.hidePartEditControls();
	},
	partChanged : function(part) {
		In2iGui.callDelegates(part,'partChanged');
	},
	deletePart : function(part) {
		In2iGui.callDelegates(part,'deletePart');
		this.partControls.hide();
	},
	newPart : function(e) {
		if (!this.newPartMenu) {
			var menu = In2iGui.Menu.create({name:'In2iGuiEditorNewPartMenu'});
			this.partControllers.each(function(item) {
				menu.addItem({title:item.title,value:item.key});
			});
			menu.addDelegate(this);
			this.newPartMenu=menu;
		}
		this.newPartMenu.showAtPointer(e);
	},
	itemWasClicked$In2iGuiEditorNewPartMenu : function(value) {
		var info = {row:this.hoveredPart.row,column:this.hoveredPart.column,position:this.hoveredPart.position+1,type:value};
		In2iGui.callDelegates(this,'addPart',info);
	},
	/**** Dragging ****/
	startPartDrag : function(e) {
		return true;
		if (!this.active || this.activePart) return true;
		if (!this.dragProxy) {
			this.dragProxy = new Element('div').addClassName('in2igui_editor_dragproxy part part_header');
			document.body.appendChild(this.dragProxy);
		}
		var element = this.hoveredPart.element;
		this.dragProxy.setStyle({'width':element.getWidth()+'px'});
		this.dragProxy.innerHTML = element.innerHTML;
		In2iGui.Editor.startDrag(e,this.dragProxy);
		return;
		Element.observe(document.body,'mouseup',function() {
			self.endPartDrag();
		})
	},
	dragPart : function() {
		
	},
	endPartDrag : function() {
		In2iGui.get();
	}
}



In2iGui.Editor.startDrag = function(e,element) {
	In2iGui.Editor.dragElement = element;
	Element.observe(document.body,'mousemove',In2iGui.Editor.dragListener);
	Element.observe(document.body,'mouseup',In2iGui.Editor.dragEndListener);
	In2iGui.Editor.startDragPos = {top:e.pointerY(),left:e.pointerX()};
	e.stop();
	return false;
}

In2iGui.Editor.dragListener = function(e) {
	var element = In2iGui.Editor.dragElement;
	element.style.left = e.pointerX()+'px';
	element.style.top = e.pointerY()+'px';
	element.style.display='block';
	return false;
}

In2iGui.Editor.dragEndListener = function(event) {
	Event.stopObserving(document.body,'mousemove',In2iGui.Editor.dragListener);
	Event.stopObserving(document.body,'mouseup',In2iGui.Editor.dragEndListener);
	In2iGui.Editor.dragElement.style.display='none';
	In2iGui.Editor.dragElement=null;
}

In2iGui.Editor.getPartId = function(element) {
	var m = element.id.match(/part\-([\d]+)/i);
	if (m && m.length>0) return m[1];
}

////////////////////////////////// Header editor ////////////////////////////////

/**
 * @constructor
 */
In2iGui.Editor.Header = function(element,row,column,position) {
	this.element = $(element);
	this.row = row;
	this.column = column;
	this.position = position;
	this.id = In2iGui.Editor.getPartId(this.element);
	this.header = this.element.firstDescendant();
	this.field = null;
}

In2iGui.Editor.Header.prototype = {
	activate : function() {
		this.value = this.header.innerHTML;
		this.field = new Element('textarea').addClassName('in2igui_editor_header');
		this.field.value = this.value;
		this.header.style.visibility='hidden';
		this.updateFieldStyle();
		this.element.insertBefore(this.field,this.header);
		this.field.focus();
		this.field.select();
		this.field.observe('keydown',function(e) {
			if (e.keyCode==Event.KEY_RETURN) {
				this.save();
			}
		}.bind(this));
	},
	save : function() {
		var value = this.field.value;
		this.header.innerHTML = value;
		this.deactivate();
		if (value!=this.value) {
			this.value = value;
			In2iGui.Editor.get().partChanged(this);
		}
	},
	cancel : function() {
		this.deactivate();
	},
	deactivate : function() {
		this.header.style.visibility='';
		this.element.removeChild(this.field);
		In2iGui.Editor.get().partDidDeacivate(this);
	},
	updateFieldStyle : function() {
		this.field.setStyle({width:this.header.getWidth()+'px',height:this.header.getHeight()+'px'});
		n2i.copyStyle(this.header,this.field,['fontSize','marginTop','fontWeight','fontFamily','textAlign','color','fontStyle']);
	},
	getValue : function() {
		return this.value;
	}
}

////////////////////////////////// Html editor ////////////////////////////////

/**
 * @constructor
 */
In2iGui.Editor.Html = function(element,row,column,position) {
	this.element = $(element);
	this.row = row;
	this.column = column;
	this.position = position;
	this.id = In2iGui.Editor.getPartId(this.element);
	this.field = null;
}

In2iGui.Editor.Html.prototype = {
	activate : function() {
		this.value = this.element.innerHTML;
		//if (Prototype.Browser.IE) return;
		var height = this.element.getHeight();
		this.element.update('');
		var style = this.buildStyle();
		this.editor = In2iGui.RichText.create({autoHideToolbar:false,style:style});
		this.editor.setHeight(height);
		this.element.appendChild(this.editor.getElement());
		this.editor.addDelegate(this);
		this.editor.ignite();
		this.editor.setValue(this.value);
		this.editor.focus();
	},
	buildStyle : function() {
		return {
			'textAlign':n2i.getStyle(this.element,'text-align')
			,'fontFamily':n2i.getStyle(this.element,'font-family')
			,'fontSize':n2i.getStyle(this.element,'font-size')
			,'fontWeight':n2i.getStyle(this.element,'font-weight')
			,'color':n2i.getStyle(this.element,'color')
		}
	},
	cancel : function() {
		this.deactivate();
		this.element.innerHTML = this.value;
	},
	save : function() {
		this.deactivate();
		var value = this.editor.value;
		if (value!=this.value) {
			this.value = value;
			In2iGui.Editor.get().partChanged(this);
		}
		this.element.innerHTML = this.value;
	},
	deactivate : function() {
		if (this.editor) {
			this.editor.deactivate();
			this.element.innerHTML = this.value;
		}
		In2iGui.Editor.get().partDidDeacivate(this);
	},
	richTextDidChange : function() {
		//this.deactivate();
	},
	getValue : function() {
		return this.value;
	}
}

/* EOF *//**
 * @constructor
 */
In2iGui.Menu = function(options) {
	this.options = n2i.override({autoHide:false,parentElement:null},options);
	this.element = $(options.element);
	this.name = options.name;
	this.value = null;
	this.subMenus = [];
	this.visible = false;
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Menu.create = function(options) {
	options = options || {};
	options.element = new Element('div').addClassName('in2igui_menu');
	var obj = new In2iGui.Menu(options);
	document.body.appendChild(options.element);
	return obj;
}

In2iGui.Menu.prototype = {
	addBehavior : function() {
		var self = this;
		this.hider = function() {
			self.hide();
		}
		if (this.options.autoHide) {
			var x = function(e) {
				if (!In2iGui.isWithin(e,self.element) && (!self.options.parentElement || !In2iGui.isWithin(e,self.options.parentElement))) {
					if (!self.isSubMenuVisible()) {
						self.hide();
					}
				}
			};
			this.element.observe('mouseout',x);
			if (this.options.parentElement) {
				this.options.parentElement.observe('mouseout',x);
			}
		}
	},
	addDivider : function() {
		this.element.insert(new Element('div').addClassName('in2igui_menu_divider'));
	},
	addItem : function(item) {
		var self = this;
		var element = new Element('div').addClassName('in2igui_menu_item').update(item.title);
		element.observe('click',function(e) {
			self.itemWasClicked(item.value);
			Event.stop(e);
		});
		if (item.children) {
			var sub = In2iGui.Menu.create(null,{autoHide:true,parentElement:element});
			sub.addItems(item.children);
			element.observe('mouseover',function(e) {
				sub.showAtElement(element,e,'horizontal');
			});
			//sub.addDelegate({itemWasClicked:function(value) {self.itemWasClicked(value)}});
			self.subMenus.push(sub);
			element.addClassName('in2igui_menu_item_children');
			//element.observe('mouseleave',function() {
			//	sub.hide();
			//});
		}
		this.element.insert(element);
	},
	addItems : function(items) {
		for (var i=0; i < items.length; i++) {
			if (items[i]==null) {
				this.addDivider();
			} else {
				this.addItem(items[i]);
			}
		};
	},
	getValue : function() {
		return this.value;
	},
	itemWasClicked : function(value) {
		this.value = value;
		In2iGui.callDelegates(this,'itemWasClicked',value);
		In2iGui.callDelegates(this,'select',value);
		this.hide();
	},
	showAtPointer : function(event) {
		if (event) {
			Event.stop(event);
			//if (event.type!='click') this.ignoreNextClick=true;
		}
		this.showAtPoint({'top':event.pointerY(),'left':event.pointerX()});
	},
	showAtElement : function(element,event,position) {
		if (event) {
			Event.stop(event);
		}
		element = $(element);
		var point = element.cumulativeOffset();
		if (position=='horizontal') {
			point.left += element.getWidth();
		} else if (position=='vertical') {
			point.top += element.getHeight();
		}
		this.showAtPoint(point);
	},
	showAtPoint : function(pos) {
		var innerWidth = n2i.getInnerWidth();
		var innerHeight = n2i.getInnerHeight();
		var scrollTop = n2i.getScrollTop();
		var scrollLeft = n2i.getScrollLeft();
		if (!this.visible) {
			this.element.setStyle({'display':'block','visibility':'hidden',opacity:0});
		}
		var width = this.element.getWidth();
		var height = this.element.getHeight();
		var left = Math.min(pos.left,innerWidth-width-20+scrollLeft);
		var top = Math.max(0,Math.min(pos.top,innerHeight-height-20+scrollTop));
		this.element.setStyle({'top':top+'px','left':left+'px','visibility':'visible',zIndex:In2iGui.nextTopIndex(),'width':(width-2)+'px'});
		if (!this.visible) {
			n2i.ani(this.element,'opacity',1,200);
			this.addHider();
			this.visible = true;
		}
	},
	hide : function() {
		if (!this.visible) return;
		var self = this;
		n2i.ani(this.element,'opacity',0,200,{onComplete:function() {
			self.element.setStyle({'display':'none'});
		}})
		this.removeHider();
		for (var i=0; i < this.subMenus.length; i++) {
			this.subMenus[i].hide();
		};
		this.visible = false;
	},
	isSubMenuVisible : function() {
		for (var i=0; i < this.subMenus.length; i++) {
			if (this.subMenus[i].visible) return true;
		};
		return false;
	},
	addHider : function() {
		Element.observe(document.body,'click',this.hider);
	},
	removeHider : function() {
		Event.stopObserving(document.body,'click',this.hider);
	}
}



/* EOF *//**
 * @constructor
 */
In2iGui.Overlay = function(options) {
	this.element = $(options.element);
	this.content = this.element.select('div.in2igui_inner_overlay')[1];
	this.name = options.name;
	this.icons = new Hash();
	this.visible = false;
	In2iGui.extend(this);
	this.addBehavior();
}

/**
 * Creates a new overlay
 */
In2iGui.Overlay.create = function(options) {
	options = options || {};
	var e = options.element = new Element('div').addClassName('in2igui_overlay').setStyle({'display':'none'});
	e.update('<div class="in2igui_inner_overlay"><div class="in2igui_inner_overlay"></div></div>');
	document.body.appendChild(e);
	return new In2iGui.Overlay(options);
}

In2iGui.Overlay.prototype = {
	/** @private */
	addBehavior : function() {
		var self = this;
		this.hider = function(e) {
			if (self.boundElement) {
				if (In2iGui.isWithin(e,self.boundElement) || In2iGui.isWithin(e,self.element)) return;
				// TODO: should be unreg'ed but it fails
				//self.boundElement.stopObserving(self.hider);
				self.boundElement.removeClassName('in2igui_overlay_bound');
				self.boundElement = null;
				self.hide();
			}
		}
		this.element.observe('mouseout',this.hider);
	},
	addIcon : function(key,icon) {
		var self = this;
		var element = new Element('div').addClassName('in2igui_overlay_icon');
		element.setStyle({'backgroundImage':'url('+In2iGui.getIconUrl(icon,2)+')'});
		element.observe('click',function(e) {
			self.iconWasClicked(key,e);
		});
		this.icons.set(key,element);
		this.content.insert(element);
	},
	hideIcons : function(keys) {
		var self = this;
		keys.each(function(key) {
			self.icons.get(key).hide();
		});
	},
	showIcons : function(keys) {
		var self = this;
		keys.each(function(key) {
			self.icons.get(key).show();
		});
	},
	iconWasClicked : function(key,e) {
		In2iGui.callDelegates(this,'iconWasClicked',key,e);
	},
	showAtElement : function(element,options) {
		options = options || {};
		In2iGui.positionAtElement(this.element,element,options);
		if (this.visible) return;
		if (n2i.browser.msie) {
			this.element.setStyle({'display':'block'});
		} else {
			this.element.setStyle({'display':'block','opacity':0});
			n2i.ani(this.element,'opacity',1,150);
		}
		this.visible = true;
		if (options.autoHide) {
			this.boundElement = element;
			element.observe('mouseout',this.hider);
			element.addClassName('in2igui_overlay_bound');
		}
		return;
	},
	hide : function() {
		this.element.setStyle({'display':'none'});
		this.visible = false;
	}
}

/* EOF */
/**
 * @class
 * @constructor
 *
 * Options
 * {url:'',parameters:{}}
 *
 * Events:
 * uploadDidCompleteQueue
 * uploadDidStartQueue
 * uploadDidComplete(file)
 */
In2iGui.Upload = function(options) {
	this.options = n2i.override({url:'',parameters:{},maxItems:50,maxSize:"20480",types:"*.*",useFlash:true,fieldName:'file',chooseButton:'Choose files...'},options);
	this.element = $(options.element);
	this.itemContainer = this.element.select('.in2igui_upload_items')[0];
	this.status = this.element.select('.in2igui_upload_status')[0];
	this.placeholder = this.element.select('.in2igui_upload_placeholder')[0];
	this.name = options.name;
	this.items = [];
	this.busy = false;
	this.loaded = false;
	this.useFlash = this.options.useFlash;
	if (this.options.useFlash) {
		this.useFlash = swfobject.hasFlashPlayerVersion("10");
	}
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.Upload.nameIndex = 0;

/** Creates a new upload widget */
In2iGui.Upload.create = function(options) {
	options = options || {};
	options.element = new Element('div',{'class':'in2igui_upload'});
	options.element.update(
		'<div class="in2igui_upload_items"></div>'+
		'<div class="in2igui_upload_status"></div>'+
		(options.placeholder ? '<div class="in2igui_upload_placeholder"><span class="in2igui_upload_icon"></span>'+
			(options.placeholder.title ? '<h2>'+options.placeholder.title+'</h2>' : '')+
			(options.placeholder.text ? '<p>'+options.placeholder.text+'</p>' : '')+
		'</div>' : '')
	);
	return new In2iGui.Upload(options);
}

In2iGui.Upload.prototype = {
	/** @private */
	addBehavior : function() {
		if (!this.useFlash) {
			this.createIframeVersion();
			return;
		}
		if (In2iGui.get().domLoaded) {
			this.createFlashVersion();			
		} else {
			In2iGui.onDomReady(this.createFlashVersion.bind(this));
		}
	},
	
	/////////////////////////// Iframe //////////////////////////
	
	/** @private */
	createIframeVersion : function() {
		In2iGui.Upload.nameIndex++;
		var frameName = 'in2igui_upload_'+In2iGui.Upload.nameIndex;
		
		var atts = {'action':this.options.url || '', 'method':'post', 'enctype':'multipart/form-data','encoding':'multipart/form-data','target':frameName};
		var form = this.form = new Element('form',atts);
		if (this.options.parameters) {
			$H(this.options.parameters).each(function(pair) {
				var hidden = new Element('input',{'type':'hidden','name':pair.key});
				hidden.setValue(pair.value);
				form.insert(hidden);
			});
		}
		var iframe = this.iframe = new Element('iframe',{name:frameName,id:frameName,src:In2iGui.context+'/In2iGui/html/blank.html',style:'display:none'});
		this.element.insert(iframe);
		this.fileInput = new Element('input',{'type':'file','class':'file','name':this.options.fieldName});
		this.fileInput.observe('change',this.iframeSubmit.bind(this));
		form.insert(this.fileInput);
		var buttonContainer = new Element('span',{'class':'in2igui_upload_button'});
		var span = new Element('span',{'class':'in2igui_upload_button_input'});
		span.insert(form);
		buttonContainer.insert(span);
		if (this.options.widget) {
			In2iGui.onDomReady(function() {
				var w = In2iGui.get(this.options.widget);
				w.element.wrap(buttonContainer);
			}.bind(this));
		} else {
			buttonContainer.insert('<a href="javascript:void(0);" class="in2igui_button"><span><span>'+this.options.chooseButton+'</span></span></a>');
			this.element.insert(buttonContainer);
		}
		iframe.observe('load',function() {this.iframeUploadComplete()}.bind(this));
	},
	/** @private */
	iframeUploadComplete : function() {
		n2i.log('iframeUploadComplete uploading: '+this.uploading+' ('+this.name+')');
		if (!this.uploading) return;
		this.uploading = false;
		this.form.reset();
		var doc = n2i.getFrameDocument(this.iframe);
		var last = this.items.last();
		if (doc.body.innerHTML.indexOf('SUCCESS')!=-1) {
			if (last) {
				last.update({progress:1,filestatus:'Færdig'});
			}
		} else if (last) {
			last.setError('Upload af filen fejlede!');
		}
		this.fire('uploadDidCompleteQueue');
		this.iframe.src=In2iGui.context+'/In2iGui/html/blank.html';
		this.endIframeProgress();
	},
	/** @private */
	iframeSubmit : function() {
		n2i.log('iframeSubmit');
		this.startIframeProgress();
		this.uploading = true;
		// IE: set value of parms again since they disappear
		if (n2i.browser.msie) {
			var p = this.options.parameters;
			$H(this.options.parameters).each(function(pair) {
				this.form[pair.key].value=pair.value;
			}.bind(this));
		}
		this.form.submit();
		this.fire('uploadDidSubmit');
		var fileName = this.fileInput.value.split('\\').pop();
		this.addItem({name:fileName,filestatus:'I gang'}).setWaiting();
	},
	/** @private */
	startIframeProgress : function() {
		this.form.style.display='none';
	},
	/** @private */
	endIframeProgress : function() {
		n2i.log('endIframeProgress');
		this.form.style.display='block';
		this.form.reset();
	},
	/** @public */
	clear : function() {
		for (var i=0; i < this.items.length; i++) {
			if (this.items[i]) {
				this.items[i].destroy();
			}
		};
		this.items = [];
		this.itemContainer.hide();
		this.status.update();
		if (this.placeholder) {
			this.placeholder.show();
		}
	},
	
	/////////////////////////// Flash //////////////////////////
	
	createFlashVersion : function() {
		var loc = new String(document.location);
		var url = loc.slice(0,loc.lastIndexOf('/')+1);
		url += this.options.url;
		var javaSession = n2i.cookie.get('JSESSIONID');
		if (javaSession) {
			url+=';jsessionid='+javaSession;
		}
		var phpSession = n2i.cookie.get('PHPSESSID');
		if (phpSession) {
			url+='?PHPSESSID='+phpSession;
		}
		var buttonContainer = new Element('span',{'class':'in2igui_upload_button'});
		var placeholder = new Element('span',{'class':'in2igui_upload_button_object'});
		buttonContainer.insert(placeholder);
		if (this.options.widget) {
			var w = In2iGui.get(this.options.widget);
			w.element.wrap(buttonContainer);
		} else {
			buttonContainer.insert('<a href="javascript:void(0);" class="in2igui_button"><span><span>'+this.options.chooseButton+'</span></span></a>');
			this.element.insert(buttonContainer);
		}
		
		var self = this;
		this.loader = new SWFUpload({
			upload_url : url,
			flash_url : In2iGui.context+"/In2iGui/lib/swfupload/swfupload.swf",
			file_size_limit : this.options.maxSize,
			file_queue_limit : this.options.maxItems,
			file_post_name : this.options.fieldName,
			file_upload_limit : this.options.maxItems,
			file_types : this.options.types,
			debug : true,
			post_params : this.options.parameters,
			button_placeholder_id : 'x',
			button_placeholder : placeholder,
			button_width : '100%',
			button_height : 30,

			swfupload_loaded_handler : this.flashLoaded.bind(this),
			file_queued_handler : self.fileQueued.bind(self),
			file_queue_error_handler : this.fileQueueError.bind(this),
			file_dialog_complete_handler : this.fileDialogComplete.bind(this),
			upload_start_handler : this.uploadStart.bind(this),
			upload_progress_handler : this.uploadProgress.bind(this),
			upload_error_handler : this.uploadError.bind(this),
			upload_success_handler : this.uploadSuccess.bind(this),
			upload_complete_handler : this.uploadComplete.bind(this)
		});
	},
	startNextUpload : function() {
		this.loader.startUpload();
	},
	
	//////////////////// Events //////////////
	
	flashLoaded : function() {
		n2i.log('flash loaded');
		this.loaded = true;
	},
	addError : function(file,error) {
		var item = this.addItem(file);
		item.setError(error);
	},
	fileQueued : function(file) {
		this.addItem(file);
	},
	fileQueueError : function(file, error, message) {
		if (file!==null) {
			this.addError(file,error);
		} else {
			In2iGui.showMessage({text:In2iGui.Upload.errors[error],duration:4000});
		}
	},
	fileDialogComplete : function() {
		n2i.log('fileDialogComplete');
		this.startNextUpload();
	},
	uploadStart : function() {
		this.status.setStyle({display:'block'});
		n2i.log('uploadStart');
		if (!this.busy) {
			this.fire('uploadDidStartQueue');
		}
		this.busy = true;
	},
	uploadProgress : function(file,complete,total) {
		this.updateStatus();
		this.items[file.index].updateProgress(complete,total);
	},
	uploadError : function(file, error, message) {
		n2i.log('uploadError file:'+file+', error:'+error+', message:'+message);
		if (file) {
			this.items[file.index].update(file);
		}
	},
	uploadSuccess : function(file,data) {
		n2i.log('uploadSuccess file:'+file+', data:'+data);
		this.items[file.index].updateProgress(file.size,file.size);
	},
	uploadComplete : function(file) {
		this.items[file.index].update(file);
		this.startNextUpload();
		var self = this;
		this.fire('uploadDidComplete',file);
		if (this.loader.getStats().files_queued==0) {
			this.fire('uploadDidCompleteQueue');
		}
		this.updateStatus();
		this.busy = false;
	},
	
	//////////// Items ////////////
	
	addItem : function(file) {
		var index = file.index;
		if (index===undefined) {
			index = this.items.length;
			file.index = index;
		}
		var item = new In2iGui.Upload.Item(file);
		this.items[index] = item;
		this.itemContainer.insert(item.element);
		this.itemContainer.setStyle({display:'block'});
		if (this.placeholder) {
			this.placeholder.hide();
		}
		return item;
	},
	
	updateStatus : function() {
		var s = this.loader.getStats();
		this.status.update('Status: '+Math.round(s.successful_uploads/this.items.length*100)+'%');
		n2i.log(s);
	}
}

In2iGui.Upload.Item = function(file) {
	this.element = new Element('div').addClassName('in2igui_upload_item');
	if (file.index % 2 == 1) {
		this.element.addClassName('in2igui_upload_item_alt')
	}
	this.content = new Element('div').addClassName('in2igui_upload_item_content');
	this.icon = In2iGui.createIcon('file/generic',2);
	this.element.insert(this.icon);
	this.element.insert(this.content);
	this.info = new Element('strong');
	this.status = new Element('em');
	this.progress = In2iGui.ProgressBar.create({small:true});
	this.content.insert(this.progress.getElement());
	this.content.insert(this.info);
	this.content.insert(this.status);
	this.update(file);
}

In2iGui.Upload.Item.prototype = {
	update : function(file) {
		this.status.update(In2iGui.Upload.status[file.filestatus] || file.filestatus);
		if (file.name) {
			this.info.update(file.name);
		}
		if (file.progress!==undefined) {
			this.setProgress(file.progress);
		}
		if (file.filestatus==SWFUpload.FILE_STATUS.ERROR) {
			this.element.addClassName('in2igui_upload_item_error');
			this.progress.hide();
		}
	},
	setError : function(error) {
		this.status.update(In2iGui.Upload.errors[error] || error);
		this.element.addClassName('in2igui_upload_item_error');
		this.progress.hide();
	},
	updateProgress : function(complete,total) {
		this.progress.setValue(complete/total);
		return this;
	},
	setProgress : function(value) {
		this.progress.setValue(value);
		return this;
	},
	setWaiting : function(value) {
		this.progress.setWaiting();
		return this;
	},
	hide : function() {
		this.element.hide();
	},
	destroy : function() {
		this.element.remove();
	}
}

if (window.SWFUpload) {
(function(){
	var e = In2iGui.Upload.errors = {};
	e[SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED]			= 'Der er valgt for mange filer';
	e[SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT]		= 'Filen er for stor';
	e[SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE]					= 'Filen er tom';
	e[SWFUpload.QUEUE_ERROR.INVALID_FILETYPE]				= 'Filens type er ikke understøttet';
	e[SWFUpload.UPLOAD_ERROR.HTTP_ERROR]					= 'Der skete en netværksfejl';
	e[SWFUpload.UPLOAD_ERROR.MISSING_UPLOAD_URL]			= 'Upload-adressen findes ikke';
	e[SWFUpload.UPLOAD_ERROR.IO_ERROR]						= 'Der skete en IO-fejl';
	e[SWFUpload.UPLOAD_ERROR.SECURITY_ERROR]				= 'Der skete en sikkerhedsfejl';
	e[SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED]			= 'Upload-størrelsen er overskredet';
	e[SWFUpload.UPLOAD_ERROR.UPLOAD_FAILED]					= 'Upload af filen fejlede';
	e[SWFUpload.UPLOAD_ERROR.SPECIFIED_FILE_ID_NOT_FOUND]	= 'Filens id kunne ikke findes';
	e[SWFUpload.UPLOAD_ERROR.FILE_VALIDATION_FAILED]		= 'Validering af filen fejlede';
	e[SWFUpload.UPLOAD_ERROR.FILE_CANCELLED]				= 'Filen blev afbrudt';
	e[SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED]				= 'Upload af filen blev stoppet';
	var s = In2iGui.Upload.status = {};
	s[SWFUpload.FILE_STATUS.QUEUED] 		= 'I kø';
	s[SWFUpload.FILE_STATUS.IN_PROGRESS] 	= 'I gang';
	s[SWFUpload.FILE_STATUS.ERROR] 			= 'Filen gav fejl';
	s[SWFUpload.FILE_STATUS.COMPLETE] 		= 'Færdig';
	s[SWFUpload.FILE_STATUS.CANCELLED] 		= 'Afbrudt';
}())
}
/* EOF *//** A progress bar is a widget that shows progress from 0% to 100%
	@constructor
*/
In2iGui.ProgressBar = function(o) {
	this.element = $(o.element);
	this.name = o.name;
	/** @private */
	this.WAITING = o.small ? 'in2igui_progressbar_small_waiting' : 'in2igui_progressbar_waiting';
	/** @private */
	this.COMPLETE = o.small ? 'in2igui_progressbar_small_complete' : 'in2igui_progressbar_complete';
	/** @private */
	this.options = o || {};
	/** @private */
	this.indicator = this.element.firstDescendant();
	In2iGui.extend(this);
}

/** Creates a new progress bar:
	@param o {Object} Options : {small:false}
*/
In2iGui.ProgressBar.create = function(o) {
	o = o || {};
	var e = o.element = new Element('div',{'class':'in2igui_progressbar'}).insert(new Element('div'));
	if (o.small) e.addClassName('in2igui_progressbar_small');
	return new In2iGui.ProgressBar(o);
}
	
In2iGui.ProgressBar.prototype = {
	/** Set the progress value
	@param value {Number} A number between 0 and 1
	*/
	setValue : function(value) {
		var el = this.element;
		if (this.waiting) el.removeClassName(this.WAITING);
		el.setClassName(this.COMPLETE,value==1);
		n2i.ani(this.indicator,'width',(value*100)+'%',200);
	},
	/** Mark progress as waiting */
	setWaiting : function() {
		this.waiting = true;
		this.indicator.setStyle({width:0});
		this.element.addClassName(this.WAITING);
	},
	/** Reset the progress bar */
	reset : function() {
		var el = this.element;
		if (this.waiting) el.removeClassName(this.WAITING);
		el.removeClassName(this.COMPLETE);
		this.indicator.style.width='0%';
	},
	/** Hide the progress bar */
	hide : function() {
		this.element.style.display = 'none';
	},
	/** Show the progress bar */
	show : function() {
		this.element.style.display = 'block';
	}
}

/* EOF *//** @constructor */
In2iGui.Gallery = function(options) {
	this.options = options || {};
	this.name = options.name;
	this.element = $(options.element);
	this.objects = [];
	this.nodes = [];
	this.selected = [];
	this.width = 100;
	this.height = 100;
	In2iGui.extend(this);
	if (this.options.source) {
		this.options.source.listen(this);
	}
}

In2iGui.Gallery.create = function(options) {
	options = options || {};
	options.element = new Element('div',{'class':'in2igui_gallery'});
	return new In2iGui.Gallery(options);
}

In2iGui.Gallery.prototype = {
	setObjects : function(objects) {
		this.objects = objects;
		this.render();
	},
	getObjects : function() {
		return this.objects;
	},
	/** @private */
	$objectsLoaded : function(objects) {
		this.setObjects(objects);
	},
	/** @private */
	render : function() {
		this.nodes = [];
		this.element.update();
		var self = this;
		this.objects.each(function(object,i) {
			var url = self.resolveImageUrl(object);
			url = url.replace(/&amp;/,'&');
			if (object.height<object.width) {
				var top = (self.height-(self.height*object.height/object.width))/2;
			} else {
				var top = 0;
			}
			var img = new Element('img',{src:url}).setStyle({margin:top+'px auto 0px'});
			var item = new Element('div',{'class':'in2igui_gallery_item'}).setStyle({'width':self.width+'px','height':self.height+'px'}).insert(img);
			item.observe('click',function() {
				self.itemClicked(i);
			});
			item.dragDropInfo = {kind:'image',icon:'common/image',id:object.id,title:object.name};
			item.onmousedown=function(e) {
				In2iGui.startDrag(e,item);
				return false;
			};
			item.observe('dblclick',function() {
				self.itemDoubleClicked(i);
			});
			self.element.insert(item);
			self.nodes.push(item);
		});
	},
	/** @private */
	updateUI : function() {
		var s = this.selected;
		this.nodes.each(function(node,i) {
			node.setClassName('in2igui_gallery_item_selected',n2i.inArray(s,i));
		});
	},
	/** @private */
	resolveImageUrl : function(img) {
		return In2iGui.resolveImageUrl(this,img,this.width,this.height);
		for (var i=0; i < this.delegates.length; i++) {
			if (this.delegates[i]['$resolveImageUrl']) {
				return this.delegates[i]['$resolveImageUrl'](img,this.width,this.height);
			}
		};
		return '';
	},
	/** @private */
	itemClicked : function(index) {
		this.selected = [index];
		this.fire('selectionChanged');
		this.updateUI();
	},
	getFirstSelection : function() {
		if (this.selected.length>0) {
			return this.objects[this.selected[0]];
		}
		return null;
	},
	/** @private */
	itemDoubleClicked : function(index) {
		this.fire('itemOpened',this.objects[index]);
	}
}

/* EOF *//**
 * @constructor
 */
In2iGui.Calendar = function(o) {
	this.name = o.name;
	this.options = n2i.override({startHour:7,endHour:24},o);
	this.element = $(o.element);
	this.head = $(this.element.getElementsByTagName('thead')[0]);
	this.body = $(this.element.getElementsByTagName('tbody')[0]);
	this.date = new Date();
	In2iGui.extend(this);
	this.buildUI();
	this.updateUI();
}

In2iGui.Calendar.prototype = {
	getFirstDay : function() {
		var date = new Date(this.date.getTime());
		date.setDate(date.getDate()-date.getDay()+1);
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		return date;
	},
	getLastDay : function() {
		var date = new Date(this.date.getTime());
		date.setDate(date.getDate()-date.getDay()+7);
		date.setHours(23);
		date.setMinutes(59);
		date.setSeconds(59);
		return date;
	},
	clearEvents : function() {
		this.events = [];
		var nodes = this.element.select('.in2igui_calendar_event');
		nodes.each(function(node) {
			node.remove();
		});
		this.hideEventViewer();
	},
	setEvents : function(events) {
		this.setBusy(false);
		this.clearEvents();
		this.events = events;
		var self = this;
		var pixels = (this.options.endHour-this.options.startHour)*40;
		this.events.each(function(event) {
			var day = self.body.select('.day')[event.startTime.getDay()-1];
			var node = new Element('div',{'class':'in2igui_calendar_event'});
			var top = ((event.startTime.getHours()*60+event.startTime.getMinutes())/60-self.options.startHour)*40-1;
			var height = (event.endTime.getTime()-event.startTime.getTime())/1000/60/60*40+1;
			var height = Math.min(pixels-top,height);
			node.setStyle({'marginTop':top+'px','height':height+'px'});
			var content = new Element('div');
			content.insert(new Element('p',{'class':'in2igui_calendar_event_time'}).update(event.startTime.dateFormat('H:i')));
			content.insert(new Element('p',{'class':'in2igui_calendar_event_text'}).update(event.text));
			if (event.location) {
				content.insert(new Element('p',{'class':'in2igui_calendar_event_location'}).update(event.location));
			}
			day.insert(node.insert(content));
			node.observe('click',function() {
				self.eventWasClicked(event,this);
			});
		});
	},
	eventWasClicked : function(event,node) {
		this.showEvent(event,node);
	},
	setBusy : function(busy) {
		if (busy) {
			this.element.addClassName('in2igui_calendar_busy');
		} else {
			this.element.removeClassName('in2igui_calendar_busy');
		}
	},
	updateUI : function() {
		var first = this.getFirstDay();
		var x = this.head.select('.time')[0];
		x.update('<div>Uge '+this.date.getWeekOfYear()+' '+this.date.getFullYear()+'</div>');
		
		var days = this.head.select('.day');
		for (var i=0; i < days.length; i++) {
			var date = new Date(first.getTime());
			date.setDate(date.getDate()+i);
			days[i].update(date.dateFormat('l \\d. d M'))
		};
	},
	buildUI : function() {
		var bar = this.element.select('.in2igui_calendar_bar')[0];
		this.toolbar = In2iGui.Toolbar.create({labels:false});
		bar.insert(this.toolbar.getElement());
		var previous = In2iGui.Button.create({name:'in2iguiCalendarPrevious',text:'',icon:'monochrome/previous'});
		previous.addDelegate(this);
		this.toolbar.add(previous);
		var today = In2iGui.Button.create({name:'in2iguiCalendarToday',text:'Idag'});
		today.addDelegate(this);
		this.toolbar.add(today);
		var next = In2iGui.Button.create({name:'in2iguiCalendarNext',text:'',icon:'monochrome/next'});
		next.addDelegate(this);
		this.toolbar.add(next);
		this.datePickerButton = In2iGui.Button.create({name:'in2iguiCalendarDatePicker',text:'Vælg dato...'});
		this.datePickerButton.addDelegate(this);
		this.toolbar.add(this.datePickerButton);
		
		var time = this.body.select('.time')[0];
		for (var i=this.options.startHour; i <= this.options.endHour; i++) {
			var node = new Element('div').update('<span><em>'+i+':00</em></span>');
			if (i==this.options.startHour) {
				node.addClassName('first');
			}
			if (i==this.options.endHour) {
				node.addClassName('last');
			}
			time.insert(node);
		};
	},
	$click$in2iguiCalendarPrevious : function() {
		var date = new Date(this.date.getTime());
		date.setDate(this.date.getDate()-7);
		this.setDate(date);
	},
	$click$in2iguiCalendarNext : function() {
		var date = new Date(this.date.getTime());
		date.setDate(this.date.getDate()+7);
		this.setDate(date);
	},
	$click$in2iguiCalendarToday : function() {
		this.setDate(new Date());
	},
	setDate: function(date) {
		this.date = new Date(date.getTime());
		this.updateUI();
		this.refresh();
		if (this.datePicker) {
			this.datePicker.setValue(this.date);
		}
	},
	$click$in2iguiCalendarDatePicker : function() {
		this.showDatePicker();
	},
	refresh : function() {
		this.clearEvents();
		this.setBusy(true);
		var info = {'startTime':this.getFirstDay(),'endTime':this.getLastDay()};
		In2iGui.callDelegates(this,'calendarSpanChanged',info);
	},
	
	////////////////////////////////// Date picker ///////////////////////////
	showDatePicker : function() {
		if (!this.datePickerPanel) {
			this.datePickerPanel = In2iGui.BoundPanel.create();
			this.datePicker = In2iGui.DatePicker.create({name:'in2iguiCalendarDatePicker',value:this.date});
			this.datePicker.addDelegate(this);
			this.datePickerPanel.add(this.datePicker);
			this.datePickerPanel.addSpace(5);
			var button = In2iGui.Button.create({name:'in2iguiCalendarDatePickerClose',text:'Luk'});
			button.addDelegate(this);
			this.datePickerPanel.add(button);
		}
		this.datePickerPanel.position(this.datePickerButton.getElement());
		this.datePickerPanel.show();
	},
	$click$in2iguiCalendarDatePickerClose : function() {
		this.datePickerPanel.hide();
	},
	$dateChanged$in2iguiCalendarDatePicker : function(date) {
		this.setDate(date);
	},
	
	//////////////////////////////// Event viewer //////////////////////////////
	
	showEvent : function(event,node) {
		if (!this.eventViewerPanel) {
			this.eventViewerPanel = In2iGui.BoundPanel.create({width:270,padding: 3});
			this.eventInfo = In2iGui.InfoView.create(null,{height:240,clickObjects:true});
			this.eventViewerPanel.add(this.eventInfo);
			this.eventViewerPanel.addSpace(5);
			var button = In2iGui.Button.create({name:'in2iguiCalendarEventClose',text:'Luk'});
			button.addDelegate(this);
			this.eventViewerPanel.add(button);
		}
		this.eventInfo.clear();
		this.eventInfo.setBusy(true);
		this.eventViewerPanel.position(node);
		this.eventViewerPanel.show();
		In2iGui.callDelegates(this,'requestEventInfo',event);
		return;
	},
	updateEventInfo : function(event,data) {
		this.eventInfo.setBusy(false);
		this.eventInfo.update(data);
	},
	click$in2iguiCalendarEventClose : function() {
		this.hideEventViewer();
	},
	hideEventViewer : function() {
		if (this.eventViewerPanel) {
			this.eventViewerPanel.hide();
		}
	}
}


////////////////////////// Date picker ////////////////////////

/** @constructor */
In2iGui.DatePicker = function(options) {
	this.name = options.name;
	this.element = $(options.element);
	this.options = {};
	n2i.override(this.options,options);
	this.cells = [];
	this.title = this.element.select('strong')[0];
	this.today = new Date();
	this.value = this.options.value ? new Date(this.options.value.getTime()) : new Date();
	this.viewDate = new Date(this.value.getTime());
	this.viewDate.setDate(1);
	In2iGui.extend(this);
	this.addBehavior();
	this.updateUI();
}

In2iGui.DatePicker.create = function(options) {
	var element = options.element = new Element('div',{'class':'in2igui_datepicker'});
	element.insert('<div class="in2igui_datepicker_header"><a class="in2igui_datepicker_next"></a><a class="in2igui_datepicker_previous"></a><strong></strong></div>');
	var table = new Element('table');
	var head = new Element('tr');
	table.insert(new Element('thead').insert(head));
	for (var i=0;i<7;i++) {
		head.insert(new Element('th').update(Date.dayNames[i].substring(0,3)));
	}
	var body = new Element('tbody');
	table.insert(body);
	for (var i=0;i<6;i++) {
		var row = new Element('tr');
		for (var j=0;j<7;j++) {
			var cell = new Element('td');
			row.insert(cell);
		}
		body.insert(row);
	}
	element.insert(table);
	return new In2iGui.DatePicker(options);
}

In2iGui.DatePicker.prototype = {
	addBehavior : function() {
		var self = this;
		this.cells = this.element.select('td');
		this.cells.each(function(cell,index) {
			cell.observe('mousedown',function() {self.selectCell(index)});
		})
		this.element.select('.in2igui_datepicker_next')[0].observe('mousedown',function() {self.next()});
		this.element.select('.in2igui_datepicker_previous')[0].observe('mousedown',function() {self.previous()});
	},
	setValue : function(date) {
		this.value = new Date(date.getTime());
		this.viewDate = new Date(date.getTime());
		this.viewDate.setDate(1);
		this.updateUI();
	},
	updateUI : function() {
		this.title.update(this.viewDate.dateFormat('F Y'));
		var isSelectedYear =  this.value.getFullYear()==this.viewDate.getFullYear();
		var month = this.viewDate.getMonth();
		for (var i=0; i < this.cells.length; i++) {
			var date = this.indexToDate(i);
			var cell = this.cells[i];
			if (date.getMonth()<month) {
				cell.className = 'in2igui_datepicker_dimmed';
			} else if (date.getMonth()>month) {
				cell.className = 'in2igui_datepicker_dimmed';
			} else {
				cell.className = '';
			}
			if (date.getDate()==this.value.getDate() && date.getMonth()==this.value.getMonth() && isSelectedYear) {
				cell.addClassName('in2igui_datepicker_selected');
			}
			if (date.getDate()==this.today.getDate() && date.getMonth()==this.today.getMonth() && date.getFullYear()==this.today.getFullYear()) {
				cell.addClassName('in2igui_datepicker_today');
			}
			cell.update(date.getDate());
		};
	},
	getPreviousMonth : function() {
		var previous = new Date(this.viewDate.getTime());
		previous.setMonth(previous.getMonth()-1);
		return previous;
	},
	getNextMonth : function() {
		var previous = new Date(this.viewDate.getTime());
		previous.setMonth(previous.getMonth()+1);
		return previous;
	},

	////////////////// Events ///////////////

	previous : function() {
		this.viewDate = this.getPreviousMonth();
		this.updateUI();
	},
	next : function() {
		this.viewDate = this.getNextMonth();
		this.updateUI();
	},
	selectCell : function(index) {
		this.value = this.indexToDate(index);
		this.viewDate = new Date(this.value.getTime());
		this.viewDate.setDate(1);
		this.updateUI();
		In2iGui.callDelegates(this,'dateChanged',this.value);
	},
	indexToDate : function(index) {
		var first = this.viewDate.getDay();
		var days = this.viewDate.getDaysInMonth();
		var previousDays = this.getPreviousMonth().getDaysInMonth();
		if (index<first) {
			var date = this.getPreviousMonth();
			date.setDate(previousDays-first+index+1);
			return date;
		} else if (index>first+days-1) {
			var date = this.getPreviousMonth();
			date.setDate(index-first-days+1);
			return date;
			cell.update(i-first-days+1);
		} else {
			var date = new Date(this.viewDate.getTime());
			date.setDate(index+1-first);
			return date;
		}
	}
}

Date.monthNames =
   ["Januar",
    "Februar",
    "Marts",
    "April",
    "Maj",
    "Juni",
    "Juli",
    "August",
    "September",
    "Oktober",
    "November",
    "December"];
Date.dayNames =
   ["Søndag",
    "Mandag",
    "Tirsdag",
    "Onsdag",
    "Torsdag",
    "Fredag",
    "Lørdag"];

/* EOF *//**
 * @constructor
 * @param {Object} options { element «Node | id», name: «String» }
 */
In2iGui.Layout = function(options) {
	this.name = options.name;
	this.options = options || {};
	this.element = $(options.element);
	In2iGui.extend(this);
}

In2iGui.Layout.prototype = {
	$$layout : function() {
		if (!n2i.browser.msie7 && !n2i.browser.msie8) {
			return;
		}
		if (this.diff===undefined) {
			var top = this.element.select('thead td')[0].firstDescendant().clientHeight;
			var foot = this.element.select('tfoot td')[0];
			var bottom = 0;
			if (foot) {
				bottom = foot.firstDescendant().clientHeight;
			}
			top+=this.element.cumulativeOffset().top;
			this.diff = bottom+top;
			if (this.element.parentNode!==document.body) {
				this.diff+=15;
			} else {
			}
		}
		var cell = this.element.select('tbody tr td')[0];
		cell.style.height=(n2i.getViewPortHeight()-this.diff)+'px';
	}
};


/**
 * @constructor
 * @param {Object} options { element «Node | id», name: «String» }
 */
In2iGui.Columns = function(options) {
	this.name = options.name;
	this.options = options || {};
	this.element = $(options.element);
	this.body = this.element.select('tr')[0];
	In2iGui.extend(this);
}

/**
 * Creates a new Columns opject
 */
In2iGui.Columns.create = function(options) {
	options = options || {};
	options.element = new Element('table',{'class':'in2igui_columns'}).insert(new Element('tbody').insert(new Element('tr')));
	return new In2iGui.Columns(options);
}

In2iGui.Columns.prototype = {
	addToColumn : function(index,widget) {
		var c = this.ensureColumn(index);
		c.insert(widget.getElement());
	},
	setColumnStyle : function(index,style) {
		var c = this.ensureColumn(index);
		c.setStyle(style);
	},
	setColumnWidth : function(index,width) {
		var c = this.ensureColumn(index);
		c.setStyle({width:width+'px'});
	},
	/** @private */
	ensureColumn : function(index) {
		var children = this.body.childElements();
		for (var i=children.length-1;i<index;i++) {
			this.body.insert(new Element('td',{'class':'in2igui_columns_column'}));
		}
		return this.body.childElements()[index];
	}
}

/* EOF *//**
 * A dock
 * @param {Object} The options
 * @constructor
 */
In2iGui.Dock = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.iframe = this.element.select('iframe')[0];
	this.name = options.name;
	In2iGui.extend(this);
	this.diff = -69;
	if (this.options.tabs) {
		this.diff-=15;
	}
}

In2iGui.Dock.prototype = {
	/** Change the url of the iframe
	 * @param {String} url The url to change the iframe to
	 */
	setUrl : function(url) {
		n2i.getFrameDocument(this.iframe).location.href=url;
	},
	/** @private */
	$$layout : function() {
		var height = n2i.getInnerHeight();
		this.iframe.style.height=(height+this.diff)+'px';
	}
}

/* EOF *//**
 * @constructor
 * @param {Object} options The options : {modal:false}
 */
In2iGui.Box = function(options) {
	this.options = n2i.override({},options);
	this.name = options.name;
	this.element = $(options.element);
	this.body = this.element.select('.in2igui_box_body')[0];
	this.close = this.element.select('.in2igui_box_close')[0];
	if (this.close) {
		this.close.observe('click',function(e) {
			e.stop();
			this.hide();
			this.fire('boxWasClosed');
		}.bind(this));
	}
	In2iGui.extend(this);
};

/**
 * Creates a new box widget
 * @param {Object} options The options : {width:0,padding:0,absolute:false,closable:false}
 */
In2iGui.Box.create = function(options) {
	options = n2i.override({},options);
	var e = options.element = new Element('div',{'class':'in2igui_box'});
	if (options.width) {
		e.setStyle({width:options.width+'px'});
	}
	if (options.absolute) {
		e.addClassName('in2igui_box_absolute');
	}
	e.update(
		(options.closable ? '<a class="in2igui_box_close" href="#"></a>' : '')+
		'<div class="in2igui_box_top"><div><div></div></div></div>'+
		'<div class="in2igui_box_middle"><div class="in2igui_box_middle">'+
		(options.title ? '<div class="in2igui_box_header"><strong class="in2igui_box_title">'+options.title+'</strong></div>' : '')+
		'<div class="in2igui_box_body"'+(options.padding ? ' style="padding: '+options.padding+'px;"' : '')+'></div>'+
		'</div></div>'+
		'<div class="in2igui_box_bottom"><div><div></div></div></div>');
	return new In2iGui.Box(options);
};

In2iGui.Box.prototype = {
	/**
	 * Adds the box to the end of the body
	 */
	addToDocument : function() {
		document.body.appendChild(this.element);
	},
	/**
	 * Adds a child widget or node
	 */
	add : function(widget) {
		if (widget.getElement) {
			this.body.insert(widget.getElement());
		} else {
			this.body.insert(widget);
		}
	},
	/**
	 * Shows the box
	 */
	show : function() {
		var e = this.element;
		if (this.options.modal) {
			var index = In2iGui.nextPanelIndex();
			e.style.zIndex=index+1;
			In2iGui.showCurtain(this,index);
		}
		if (this.options.absolute) {
			e.setStyle({display:'block',visibility:'hidden'});
			var w = e.getWidth();
			var top = (n2i.getInnerHeight()-e.getHeight())/2+n2i.getScrollTop();
			e.setStyle({'marginLeft':(w/-2)+'px',top:top+'px'});
			e.setStyle({display:'block',visibility:'visible'});
		} else {
			e.setStyle({display:'block'});
		}
		In2iGui.callVisible(this);
	},
	/**
	 * Hides the box
	 */
	hide : function() {
		In2iGui.hideCurtain(this);
		this.element.setStyle({display:'none'});
	},
	/** @private */
	curtainWasClicked : function() {
		this.fire('boxCurtainWasClicked');
	}
};/**
 * @constructor
 * A wizard with a number of steps
 */
In2iGui.Wizard = function(o) {
	/** @private */
	this.options = o || {};
	/** @private */
	this.element = $(o.element);
	/** @private */
	this.name = o.name;
	/** @private */
	this.container = this.element.select('.in2igui_wizard_steps')[0];
	/** @private */
	this.steps = this.element.select('.in2igui_wizard_step');
	/** @private */
	this.anchors = this.element.select('ul.in2igui_wizard a');
	/** @private */
	this.selected = 0;
	In2iGui.extend(this);
	this.addBehavior();
}
	
In2iGui.Wizard.prototype = {
	/** @private */
	addBehavior : function() {
		this.anchors.each(function(node,i) {
			node.observe('mousedown',function(e) {e.stop();this.goToStep(i)}.bind(this));
			node.observe('click',function(e) {e.stop();});
		}.bind(this));
	},
	/** Goes to the step with the index */
	goToStep : function(index) {
		var c = this.container;
		c.setStyle({height:c.getHeight()+'px'})
		this.anchors[this.selected].removeClassName('in2igui_selected');
		this.steps[this.selected].hide();
		this.anchors[index].addClassName('in2igui_selected');
		this.steps[index].show();
		this.selected=index;
		n2i.ani(c,'height',this.steps[index].getHeight()+'px',500,{ease:n2i.ease.slowFastSlow,onComplete:function() {
			c.setStyle({height:''});
		}});
		In2iGui.callVisible(this);
	},
	/** Goes to the next step */
	next : function() {
		if (this.selected<this.steps.length-1) {
			this.goToStep(this.selected+1);
		}
	},
	/** Goes to the previous step */
	previous : function() {
		if (this.selected>0) {
			this.goToStep(this.selected-1);
		}
	}
}

/* EOF *//**
 * A list of articles
 * @constructor
 * @param {Object} options { element: «Node | id», name: «String», source: «In2iGui.Source» }
 */
In2iGui.Articles = function(options) {
	this.options = options;
	this.name = options.name;
	this.element = $(options.element);
	if (options.source) {
		options.source.listen(this);
	}
}

In2iGui.Articles.prototype = {
	/** @private */
	$articlesLoaded : function(doc) {
		this.element.update();
		var a = doc.getElementsByTagName('article');
		for (var i=0; i < a.length; i++) {
			var e = new Element('div',{'class':'in2igui_article'});
			var c = a[i].childNodes;
			for (var j=0; j < c.length; j++) {
				if (n2i.dom.isElement(c[j],'title')) {
					e.insert(new Element('h2').update(n2i.dom.getNodeText(c[j])));
				} else if (n2i.dom.isElement(c[j],'paragraph')) {
					var p = new Element('p').update(n2i.dom.getNodeText(c[j]));
					if (c[j].getAttribute('dimmed')==='true') {
						p.addClassName('in2igui_dimmed');
					}
					e.insert(p);
				}
			};
			this.element.insert(e);
		};
	},
	/** @private */
	$sourceIsBusy : function() {
		this.element.update('<div class="in2igui_articles_loading">Loading...</div>');
	},
	/** @private */
	$sourceIsNotBusy : function() {
		this.element.removeClassName('in2igui_list_busy');
	}
}

/* EOF *//** @constructor */
In2iGui.TextField = function(options) {
	this.options = n2i.override({placeholderElement:null,validator:null},options);
	var e = this.element = $(options.element);
	this.element.setAttribute('autocomplete','off');
	this.value = this.validate(this.element.value);
	this.isPassword = this.element.type=='password';
	this.name = options.name;
	In2iGui.extend(this);
	this.addBehavior();
	if (this.options.placeholderElement && this.value!='') {
		In2iGui.fadeOut(this.options.placeholderElement,0);
	}
	this.checkPlaceholder();
	try { // IE hack
		if (e==document.activeElement) {
			this.focused();
		}
	} catch (e) {}
}

In2iGui.TextField.prototype = {
	addBehavior : function() {
		var e = this.element;
		e.observe('keyup',this.keyDidStrike.bind(this));
		var p = this.options.placeholderElement;
		e.observe('blur',this.onBlur.bind(this));
		if (p) {
			e.observe('focus',this.focused.bind(this));
			e.observe('blur',this.checkPlaceholder.bind(this));
			if (p) {
				p.setStyle({cursor:'text'});
				p.observe('mousedown',this.focus.bind(this)).observe('click',this.focus.bind(this));
			}
		}
	},
	focused : function() {
		var e = this.element,p = this.options.placeholderElement;
		if (p && e.value=='') {
			In2iGui.fadeOut(p,0);
		}
	},
	/** @private */
	validate : function(value) {
		var validator = this.options.validator, result;
		if (validator) {
			result = validator.validate(value);
			this.element.setClassName('in2igui_invalid',!result.valid);
			return result.value;
		}
		return value;
	},
	checkPlaceholder : function() {
		if (this.options.placeholderElement && this.value=='') {
			In2iGui.fadeIn(this.options.placeholderElement,200);
		}
		if (this.isPassword && !n2i.browser.msie) {
			this.element.type='password';
		}
	},
	/** @private */
	keyDidStrike : function() {
		if (this.value!==this.element.value) {
			var newValue = this.validate(this.element.value);
			var changed = newValue!==this.value;
			this.value = newValue;
			if (changed) {
				this.fire('valueChanged',this.value);
			}
		}
	},
	/** @private */
	onBlur : function() {
		this.element.removeClassName('in2igui_invalid');
		this.element.value = this.value || '';
	},
	getValue : function() {
		return this.value;
	},
	setValue : function(value) {
		if (value===undefined || value===null) value='';
		this.element.value = value;
		this.value = this.validate(value);
	},
	isEmpty : function() {
		return this.value=='';
	},
	isBlank : function() {
		return this.value.strip()=='';
	},
	focus : function() {
		this.element.focus();
	},
	setError : function(error) {
		var isError = error ? true : false;
		this.element.setClassName('in2igui_field_error',isError);
		if (typeof(error) == 'string') {
			In2iGui.showToolTip({text:error,element:this.element,key:this.name});
		}
		if (!isError) {
			In2iGui.hideToolTip({key:this.name});
		}
	}
};

/* EOF *//** @constructor */
In2iGui.InfoView = function(options) {
	this.options = n2i.override({clickObjects:false},options);
	this.element = $(options.element);
	this.body = this.element.select('tbody')[0];
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.InfoView.create = function(options) {
	options = options || {};
	var element = options.element = new Element('div',{'class':'in2igui_infoview'});
	if (options.height) {
		element.setStyle({height:options.height+'px','overflow':'auto','overflowX':'hidden'});
	}
	if (options.margin) {
		element.setStyle({margin:options.margin+'px'});
	}
	element.update('<table><tbody></tbody></table>');
	return new In2iGui.InfoView(options);
}

In2iGui.InfoView.prototype = {
	addHeader : function(text) {
		var row = new Element('tr');
		row.insert(new Element('th',{'class' : 'in2igui_infoview_header','colspan':'2'}).insert(text));
		this.body.insert(row);
	},
	addProperty : function(label,text) {
		var row = new Element('tr');
		row.insert(new Element('th').insert(label));
		row.insert(new Element('td').insert(text));
		this.body.insert(row);
	},
	addObjects : function(label,objects) {
		if (!objects || objects.length==0) return;
		var row = new Element('tr');
		row.insert(new Element('th').insert(label));
		var cell = new Element('td');
		var click = this.options.clickObjects;
		objects.each(function(obj) {
			var node = new Element('div').insert(obj.title);
			if (click) {
				node.addClassName('in2igui_infoview_click')
				node.observe('click',function() {
					In2iGui.callDelegates(this,'objectWasClicked',obj);
				});
			}
			cell.insert(node);
		});
		row.insert(cell);
		this.body.insert(row);
	},
	setBusy : function(busy) {
		if (busy) {
			this.element.addClassName('in2igui_infoview_busy');
		} else {
			this.element.removeClassName('in2igui_infoview_busy');
		}
	},
	clear : function() {
		this.body.update();
	},
	update : function(data) {
		this.clear();
		for (var i=0; i < data.length; i++) {
			switch (data[i].type) {
				case 'header': this.addHeader(data[i].value); break;
				case 'property': this.addProperty(data[i].label,data[i].value); break;
				case 'objects': this.addObjects(data[i].label,data[i].value); break;
			}
		};
	}
}

/* EOF *//**
 * Overflow with scroll bars
 * @param {Object} The options
 * @constructor
 */
In2iGui.Overflow = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.Overflow.create = function(options) {
	options = options || {};
	var e = options.element = new Element('div',{'class':'in2igui_overflow'});
	if (options.height) {
		e.setStyle({height:options.height+'px'});
	}
	return new In2iGui.Overflow(options);
}

In2iGui.Overflow.prototype = {
	calculate : function() {
		var top,bottom,parent,viewport;
		viewport = n2i.getInnerHeight();
		parent = $(this.element.parentNode);
		top = this.element.cumulativeOffset().top;
		bottom = parent.cumulativeOffset().top+parent.getDimensions().height;
		this.element.nextSiblings().each(function(node) {
			bottom-=node.clientHeight;
		})
		this.diff=-1*(top+(viewport-bottom));
	},
	add : function(widgetOrNode) {
		if (widgetOrNode.getElement) {
			this.element.insert(widgetOrNode.getElement());
		} else {
			this.element.insert(widgetOrNode);
		}
		return this;
	},
	$$layout : function() {
		if (!this.options.dynamic) {
			if (this.options.vertical) {
				var height = n2i.getInnerHeight();
				this.element.style.height = Math.max(0,height-this.options.vertical)+'px';
			}
			return;
		}
		if (this.diff===undefined) {
			this.calculate();
		}
		var height = n2i.getInnerHeight();
		this.element.style.height = Math.max(0,height+this.diff)+'px';
	}
}

/* EOF *//** @constructor */
In2iGui.SearchField = function(options) {
	this.options = n2i.override({expandedWidth:null},options);
	this.element = $(options.element);
	this.name = options.name;
	this.field = this.element.select('input')[0];
	this.value = this.field.value;
	this.adaptive = this.element.hasClassName('in2igui_searchfield_adaptive');
	In2iGui.onDomReady(function() {this.initialWidth=parseInt(this.element.getStyle('width'))}.bind(this));
	In2iGui.extend(this);
	this.addBehavior();
	this.updateClass()
}

In2iGui.SearchField.create = function(options) {
	options = options || {};
	
	var e = options.element = new Element('span',{'class': options.adaptive ? 'in2igui_searchfield in2igui_searchfield_adaptive' : 'in2igui_searchfield'});
	e.update('<em class="in2igui_searchfield_placeholder"></em><a href="javascript:void(0);" class="in2igui_searchfield_reset"></a><span><span><input type="text"/></span></span>');
	return new In2iGui.SearchField(options);
}

In2iGui.SearchField.prototype = {
	/** @private */
	addBehavior : function() {
		var self = this;
		this.field.observe('keyup',this.onKeyUp.bind(this));
		var reset = this.element.select('a')[0];
		reset.tabIndex=-1;
		var focus = function() {self.field.focus();self.field.select()};
		this.element.observe('mousedown',focus).observe('mouseup',focus);
		reset.observe('mousedown',function(e) {e.stop();self.reset();focus()});
		this.element.select('em')[0].observe('mousedown',focus);
		this.field.observe('focus',function() {
			self.focused=true;
			self.updateClass();
		});
		this.field.observe('blur',function() {
			self.focused=false;
			self.updateClass();
		});
		if (this.options.expandedWidth>0) {
			this.field.onfocus = function() {
				n2i.ani(self.element,'width',self.options.expandedWidth+'px',500,{ease:n2i.ease.slowFastSlow});
			}
			this.field.onblur = function() {
				n2i.ani(self.element,'width',self.initialWidth+'px',500,{ease:n2i.ease.slowFastSlow,delay:100});
			}
		}
	},
	onKeyUp : function(e) {
		this.fieldChanged();
		if (e.keyCode===Event.KEY_RETURN) {
			this.fire('submit');
		}
	},
	setValue : function(value) {
		this.field.value=value===undefined || value===null ? '' : value;
		this.fieldChanged();
	},
	getValue : function() {
		return this.field.value;
	},
	isEmpty : function() {
		return this.field.value=='';
	},
	isBlank : function() {
		return this.field.value.strip()=='';
	},
	reset : function() {
		this.field.value='';
		this.fieldChanged();
	},
	/** @private */
	updateClass : function() {
		var className = 'in2igui_searchfield';
		if (this.adaptive) {
			className+=' in2igui_searchfield_adaptive';
		}
		if (this.focused && this.value!='') {
			className+=' in2igui_searchfield_focus_dirty';
		} else if (this.focused) {
			className+=' in2igui_searchfield_focus';
		} else if (this.value!='') {
			className+=' in2igui_searchfield_dirty';
		}
		this.element.className=className;
	},
	/** @private */
	fieldChanged : function() {
		if (this.field.value!=this.value) {
			this.value=this.field.value;
			this.updateClass();
			this.fire('valueChanged',this.value);
			In2iGui.firePropertyChange(this,'value',this.value);
		}
	}
}

/* EOF *//**
 * Simple container
 * @param {Object} The options
 * @constructor
 */
In2iGui.Fragment = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.Fragment.prototype = {
	show : function() {
		this.element.style.display='block';
	},
	hide : function() {
		this.element.style.display='none';
	}
}

/* EOF *//**
	Used to get a geografical location
	@constructor
*/
In2iGui.LocationPicker = function(options) {
	options = options || {};
	this.name = options.name;
	this.options = options.options || {};
	this.element = $(options.element);
	this.defaultCenter = new google.maps.LatLng(57.0465, 9.9185);
	In2iGui.extend(this);
	this.addBehavior();
}

In2iGui.LocationPicker.prototype = {
	/** @private */
	addBehavior : function() {
	},
	show : function(options) {
		if (!this.panel) {
			var panel = this.panel = In2iGui.BoundPanel.create({width:300});
			var mapContainer = new Element('div').setStyle({width:'300px',height:'300px'});
			panel.add(mapContainer);
			var buttons = In2iGui.Buttons.create({align:'right',top:5});
			var button = In2iGui.Button.create({text:'Luk'});
			button.listen({$click:function() {panel.hide()}});
			panel.add(buttons.add(button));
			panel.element.setStyle({left:'-10000px',top:'-10000px',display:''});
			var latLng = this.buildLatLng();
		    var mapOptions = {
		      zoom: 15,
		      mapTypeId: google.maps.MapTypeId.TERRAIN
		    }
		    this.map = new google.maps.Map(mapContainer, mapOptions);
			google.maps.event.addListener(this.map, 'click', function(obj) {
				var loc = {latitude:obj.latLng.lat(),longitude:obj.latLng.lng()};
    			this.setLocation(loc);
				this.fire('locationChanged',loc);
  			}.bind(this));
			panel.element.setStyle({display:'none'});
		}
		this.setLocation(options.location);
		if (options.node) {
			this.panel.position(options.node);
		}
		this.panel.show();
	},
	setLocation : function(loc) {
		if (!loc && this.marker) {
			this.marker.setMap(null);
			this.map.setCenter(this.defaultCenter);
			return;
		}
		loc = this.buildLatLng(loc);
		if (!this.marker) {
		    this.marker = new google.maps.Marker({
		        position: loc, 
		        map: this.map
		    });
		} else {
    		this.marker.setPosition(loc);
			this.marker.setMap(this.map);
		}
		this.map.setCenter(loc);
	},
	/** @private */
	buildLatLng : function(loc) {
		if (!loc) {
			loc = {latitude:57.0465, longitude:9.9185};
		}
		return new google.maps.LatLng(loc.latitude, loc.longitude);
	}
}

/* EOF *//**
 * @constructor
 * @param {Object} options The options
 */
In2iGui.Bar = function(options) {
	this.options = n2i.override({},options);
	this.name = options.name;
	this.element = $(options.element);
	In2iGui.extend(this);
};

/**
 * @constructor
 * @param {Object} options The options
 */
In2iGui.Bar.Button = function(options) {
	this.options = n2i.override({},options);
	this.name = options.name;
	this.element = $(options.element);
	this.element.observe('click',this.onClick.bind(this));
	In2iGui.extend(this);
};

In2iGui.Bar.Button.prototype = {
	onClick : function() {
		this.fire('click');
	}
}/**
 * A dock
 * @param {Object} The options
 * @constructor
 */
In2iGui.IFrame = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.name = options.name;
	In2iGui.extend(this);
}

In2iGui.IFrame.prototype = {
	/** Change the url of the iframe
	 * @param {String} url The url to change the iframe to
	 */
	setUrl : function(url) {
		n2i.getFrameDocument(this.element).location.href=url;
	},
	getDocument : function() {
		return n2i.getFrameDocument(this.element);
	},
	getWindow : function() {
		return n2i.getFrameWindow(this.element);
	},
	reload : function() {
		this.getWindow().location.reload();
	}
}

/* EOF */
In2iGui.VideoPlayer = function(options) {
	this.options = options;
	this.element = $(options.element);
	this.placeholder = this.element.select('div')[0];
	this.name = options.name;
	this.state = {duration:0,time:0,loaded:0};
	this.handlers = [In2iGui.VideoPlayer.HTML5,In2iGui.VideoPlayer.QuickTime,In2iGui.VideoPlayer.Embedded];
	this.handler = null;
	In2iGui.extend(this);
	if (this.options.video) {
		if (this.placeholder) {
			this.placeholder.observe('click',function() {
				this.setVideo(this.options.video);
			}.bind(this))
		} else {
			In2iGui.onReady(function() {
				this.setVideo(this.options.video);
			}.bind(this));			
		}
	}
}

In2iGui.VideoPlayer.prototype = {
	setVideo : function(video) {
		this.handler = this.getHandler(video);
		this.element.update(this.handler.element);
		if (this.handler.showController()) {
			this.buildController();
		}
	},
	getHandler : function(video) {
		for (var i=0; i < this.handlers.length; i++) {
			var handler = this.handlers[i];
			if (handler.isSupported(video)) {
				return new handler(video,this);
			}
		};
	},
	buildController : function() {
		var e = new Element('div',{'class':'in2igui_videoplayer_controller'});
		this.playButton = new Element('a',{href:'javascript:void(0);','class':'in2igui_videoplayer_playpause'}).insert('wait!');
		this.playButton.observe('click',this.playPause.bind(this));
		this.status = new Element('span',{'class':'in2igui_videoplayer_status'});
		e.insert(this.playButton).insert(this.status);
		this.element.insert(e);
	},
	onCanPlay : function() {
		this.playButton.update('Play');
	},
	onLoad : function() {
		this.state.loaded = this.state.duration;
		this.updateStatus();
	},
	onDurationChange : function(duration) {
		this.state.duration = duration;
		this.updateStatus();
	},
	onTimeChange : function(time) {
		this.state.time = time;
		this.updateStatus();
	},
	onLoadProgressChange : function(progress) {
		this.state.loaded = progress;
		this.updateStatus();
	},
	playPause : function() {
		if (this.handler.isPlaying()) {
			this.pause();
		} else {
			this.play();
		}
	},
	play : function() {
		this.handler.play();
	},
	pause : function() {
		this.handler.pause();
	},
	updateStatus : function() {
		this.status.innerHTML = this.state.time+' / '+this.state.duration+' / '+this.state.loaded;
	}
}

///////// HTML5 //////////

In2iGui.VideoPlayer.HTML5 = function(video,player) {
	var e = this.element = new Element('video',{width:video.width,height:video.height,src:video.src});
	e.observe('load',player.onLoad.bind(player));
	e.observe('canplay',player.onCanPlay.bind(player));
	e.observe('durationchange',function(x) {
		player.onDurationChange(e.duration);
	});
	e.observe('timeupdate',function() {
		player.onTimeChange(this.element.currentTime);
	}.bind(this));
}

In2iGui.VideoPlayer.HTML5.isSupported = function(video) {
	if (n2i.browser.webkitVersion>528 && (video.type==='video/quicktime' || video.type==='video/mp4')) {
		return true;
	}
	return false;
}

In2iGui.VideoPlayer.HTML5.prototype = {
	showController : function() {
		return true;
	},
	pause : function() {
		this.element.pause();
	},
	play : function() {
		this.element.play();
	},
	getTime : function() {
		return this.element.currentTime;
	},
	isPlaying : function() {
		return !this.element.paused;
	}
}

///////// QuickTime //////////

In2iGui.VideoPlayer.QuickTime = function(video,player) {
	this.player = player;
	var e = this.element = new Element('object',{width:video.width,height:video.height,data:video.src,type:'video/quicktime'});
	e.update('<param value="false" name="controller"/>'
		+'<param value="true" name="enablejavascript"/>'
		+'<param value="undefined" name="posterframe"/>'
		+'<param value="false" name="showlogo"/>'
		+'<param value="false" name="autostart"/>'
		+'<param value="true" name="cache"/>'
		+'<param value="white" name="bgcolor"/>'
		+'<param value="false" name="aggressivecleanup"/>'
		+'<param value="true" name="saveembedtags"/>'
		+'<param value="true" name="postdomevents"/>');
		
	e.observe('qt_canplay',player.onCanPlay.bind(player));
	e.observe('qt_load',player.onLoad.bind(player));
	e.observe('qt_progress',function() {
		player.onLoadProgressChange(e.GetMaxTimeLoaded()/3000);
	});
	e.observe('qt_durationchange',function(x) {
		player.onDurationChange(e.GetDuration()/3000);
	});
	e.observe('qt_timechanged',function() {
		player.onTimeChange(e.GetTime());
	})
}

In2iGui.VideoPlayer.QuickTime.isSupported = function(video) {
	return video.html==undefined;
}

In2iGui.VideoPlayer.QuickTime.prototype = {
	showController : function() {
		return true;
	},
	pause : function() {
		window.clearInterval(this.observer);
		this.element.Stop();
	},
	play : function() {
		this.element.Play();
		this.observer = window.setInterval(this.observeVideo.bind(this),100);
	},
	observeVideo : function() {
		this.player.onTimeChange(this.element.GetTime()/3000);
	},
	getTime : function() {
		return this.element.GetTime();
	},
	isPlaying : function() {
		return this.element.GetRate()!==0;
	}
}

///////// Embedded //////////

In2iGui.VideoPlayer.Embedded = function(video,player) {
	var e = this.element = new Element('div',{width:video.width,height:video.height});
	e.update(video.html);
}

In2iGui.VideoPlayer.Embedded.isSupported = function(video) {
	return video.html!==undefined;
}

In2iGui.VideoPlayer.Embedded.prototype = {
	showController : function() {
		return false;
	},
	pause : function() {
		
	},
	play : function() {
		
	},
	getTime : function() {
		
	},
	isPlaying : function() {
		
	}
}

/* EOF */