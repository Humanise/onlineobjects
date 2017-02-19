oo.Masonry = function(options) {
	this.options = options;
	this.element = hui.get(options.element);
	this.name = options.name;

	this.height = 200;
	this.items = [];
	this.latestWidth = 0;

	hui.ui.extend(this);
	this._attach();
}

oo.Masonry.prototype = {
	_attach : function() {
		var links = hui.get.byTag(this.element,'a');
		for (var i = 0; i < links.length; i++) {
			var data = hui.string.fromJSON(links[i].getAttribute('data'));
			data.href = links[i].href;
			this.items.push(data)
		}
		this.element.innerHTML = '';
		this._rebuild();
		hui.ui.listen({
			$$afterResize : this._rebuild.bind(this)
		})
		hui.listen(window,'scroll',this._reveal.bind(this));
		hui.on(this.element,'tap',this._click.bind(this));
	},
	_rebuild : function() {
		var fullWidth = this.element.clientWidth;
		if (Math.abs(this.latestWidth-fullWidth)<100) {
			return;
		}
    this.height = hui.between(100,Math.round(fullWidth / 3),200)
    hui.log(this.height);
		this.latestWidth = fullWidth;
		var rows = [];
		var row = [];
		rows.push(row);
		var pixels = 0;
		for (var i = 0,l = this.items.length; i < l; i++) {
			var item = this.items[i];
			item.revealed = false;
			item.index = i;
			var width = item.width/item.height * this.height;
			pixels+=width;
			var info = {width:width,item:item,percent:percent};
			if (pixels/fullWidth>1.2) {
				pixels = width;
				row = [];
				rows.push(row);
			}
			info.place = pixels;
			item.row = rows.length;
			row.push(info);
		}
    // If the last row has one and the second last has more than 2... move one
    if (rows.length > 1) {
      if (rows[rows.length-1].length == 1) {
        if (rows[rows.length-2].length > 2) {
          var popped = rows[rows.length-2].pop();
          rows[rows.length-1].unshift(popped);
        }
      }
    }

		for (var i = 0; i < rows.length; i++) {
			var row = rows[i];
			var total = 0;
			for (var j = 0; j < row.length; j++) {
				total+=row[j].width;
			}
			var adjustment = fullWidth/total;
			var pos = 0;
      var rowHeight = Number.MAX_VALUE;

      for (var j = 0; j < row.length; j++) {
        var last = j == row.length - 1;
        var info = row[j], item = info.item;
        var percent = info.width / fullWidth * 100;
        percent = Math.round(adjustment * percent);
        pos+=percent;
        if (last) {
          percent += 100 - pos;
        }
        info.percent = percent;
        rowHeight = Math.min(rowHeight, (percent/100 * fullWidth) * item.height/item.width );
      }
      rowHeight = Math.floor(rowHeight);

			for (var j = 0; j < row.length; j++) {
				var last = j == row.length - 1;
				var info = row[j], item = info.item;
				var percent = info.percent;
				var cls = last ? 'oo_masonry_item oo_masonry_item_last' : 'oo_masonry_item';
				if (item.element) {
					item.element.style.width = percent+'%';
					item.element.style.height = rowHeight+'px';
					item.element.className = cls;
          item.element.style.backgroundImage = 'linear-gradient(' + item.colors + ')';
				} else {
					item.element = hui.build('div',{
						'class' : cls,
						style : {
							width : percent+'%',
							height : rowHeight+'px',
              backgroundImage: 'linear-gradient(' + item.colors + ')'
						},
						'data' : item.index,
						parent : this.element
					});
				}
			}
		}
		this._reveal();
	},
	_getUrl : function(item,info) {
		var x = window.devicePixelRatio==2 ? 2 : 1;
    var url = oo.baseContext+'/service/image/id'+item.id+'width'+(info.width*x)+'height'+(info.height * x);
    if (item.rotation) {
      url+='rotation' + item.rotation+'.0';
    }
		return url+'.jpg';
	},
	_reveal : function() {
		var min = hui.window.getScrollTop();
		var max = min + hui.window.getViewHeight();
		for (var i = 0,l = this.items.length; i < l; i++) {
			var item = this.items[i],
				element = item.element;
			if (item.revealed) {
				continue;
			}
			var top = hui.position.getTop(element),
				bottom = top + this.height;
			if (top > max || bottom < min) {
				continue;
			}
      var height = Math.ceil(element.clientHeight/30) * 30;
			var width = Math.round(item.width/item.height * height);
      var url = this._getUrl(item,{width:width,height:height});
      this._load(item.element,url);
			item.revealed = true;
		}
	},
  _load : function(node,url) {
    var img = new Image()
    img.onload = function() {
      node.style.backgroundImage = 'url(' + url + ')';
    }
    img.src = url;
  },
	_click : function(e) {
		e = hui.event(e);
		var item = e.findByClass('oo_masonry_item');
		if (item) {
			var index = parseInt(item.getAttribute('data'),10);
			if (hui.window.getViewWidth()<400) {
				document.location = this.items[index].href;
			} else {
				this._toggle(index);
			}
		}
	},
	_toggle : function(index) {
		var dur = 400;
		if (this._toggled!==undefined) {
			var tog = this.items[this._toggled];
			hui.animate({node:tog.disclosed,css:{height:'0px'},duration:200,ease:hui.ease.fastSlow})
			hui.animate({node:tog.element,css:{marginBottom:'0px'},duration:200,ease:hui.ease.fastSlow,$complete : function() {
				tog.disclosed.style.display = 'none';
				var same = this._toggled === index;
				this._toggled = undefined;
				if (!same) {
					this._toggle(index);
				} else {
					this._reveal();
				}
			}.bind(this)})
			return;
		}
		this._toggled = index;
		var item = this.items[index],
			element = item.element,
    top = hui.position.getTop(element) + element.clientHeight - hui.position.getTop(this.element) + 2;
		if (!item.disclosed) {
			item.disclosed = hui.build('div',{
				'class' : 'oo_masonry_disclosed',
				style : {top : top + 'px'},
				parent: this.element
			})
		} else {
			item.disclosed.style.display='block';
		}
		var height = Math.round(this.latestWidth*.6);
		this._updateDiclosed(item,height);
		hui.animate({
			node : item.disclosed,
			css : {height: (height-1) + 'px'},
			duration : dur,
			ease : hui.ease.backOut
		})
		hui.animate({
			node : element,
			css : {'margin-bottom':height + 'px'},
			duration : dur,
			ease : hui.ease.backOut,
			$complete : function() {
				hui.window.scrollTo({
					element : item.disclosed,
					duration : 300
				});
			}
		})
	},
	_updateDiclosed : function(item,height) {
		item.disclosed.innerHTML = '<div class="oo_masonry_disclosed_image" style="background-image: linear-gradient(' + item.colors + ')"></div>' +
		'<div class="oo_masonry_disclosed_info">' +
			'<h1 class="oo_masonry_disclosed_title">' + hui.string.escape(item.title) + '</h1>' +
			'<p class="oo_masonry_disclosed_actions">'+
				//'<a href="javascript://">Full screen</a>'+
				'<a href="' + item.href + '">Info...</a>'+
			'</p>'+
		'</div>';
    var url = this._getUrl(item,{width:this.latestWidth,height:600});
    var node = hui.find('.oo_masonry_disclosed_image',item.disclosed);
    this._load(node,url);
	}
};